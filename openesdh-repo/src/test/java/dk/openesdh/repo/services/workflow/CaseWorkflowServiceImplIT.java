package dk.openesdh.repo.services.workflow;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.tradeshift.test.remote.Remote;
import com.tradeshift.test.remote.RemoteTestRunner;

import dk.openesdh.repo.helper.CaseDocumentTestHelper;
import dk.openesdh.repo.helper.CaseHelper;
import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.model.WorkflowInfo;
import dk.openesdh.repo.services.cases.CaseService;
import dk.openesdh.repo.services.members.CaseMembersService;

@RunWith(RemoteTestRunner.class)
@Remote(runnerClass = SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:alfresco/application-context.xml")
public class CaseWorkflowServiceImplIT {

    @Autowired
    @Qualifier("NodeService")
    protected NodeService nodeService;

    @Autowired
    @Qualifier("WorkflowService")
    protected WorkflowService workflowService;

    @Autowired
    @Qualifier("PersonService")
    protected PersonService personService;

    @Autowired
    @Qualifier("AuthorityService")
    protected AuthorityService authorityService;

    @Autowired
    @Qualifier("NamespaceService")
    protected NamespaceService namespaceService;

    @Autowired
    @Qualifier("DictionaryService")
    protected DictionaryService dictionaryService;

    @Autowired
    @Qualifier("CaseDocumentTestHelper")
    protected CaseDocumentTestHelper docTestHelper;

    @Autowired
    private CaseService caseService;

    @Autowired
    private CaseMembersService caseMembersService;

    @Autowired
    @Qualifier(CaseWorkflowService.NAME)
    protected CaseWorkflowService service;

    private static final String TEST_FOLDER_NAME = "CaseWorkflowServiceImplIT";
    private static final String TEST_DOCUMENT_NAME = "test_document.txt";
    private static final String ACTIVITY_ADHOC_WORKFLOW_NAME = "activiti$activitiAdhoc";
    private static final String ACTIVITY_PARALLEL_GROUP_REVIEW_WORKFLOW_NAME = "activiti$activitiParallelGroupReview";
    private static final String ACTIVITY_PARALLEL_REVIEW_WORKFLOW_NAME = "activiti$activitiParallelReview";
    private static final String REQUIRED_APPROVE_PERCENT_PROPERTY = "wf_requiredApprovePercent";
    private static final String TEST_GROUP_SHORT_NAME = "assigneesGroup";
    private static final String TEST_GROUP_SHORT_NAME2 = "assigneesGroup2";

    private NodeRef testFolder;
    private NodeRef testDocument;
    private NodeRef personNodeRef;
    private NodeRef caseCreatorsGroupNodeRef;
    private String testGroupAuthorityName;
    private String testGroupAuthorityName2;

    private NodeRef caseNodeRef;

    @Before
    public void setUp() throws Exception {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        personNodeRef = personService.getPerson(AuthenticationUtil.getFullyAuthenticatedUser());
        caseCreatorsGroupNodeRef = authorityService.getAuthorityNodeRef(CaseHelper.CASE_CREATOR_GROUP);

        testFolder = docTestHelper.createFolder(TEST_FOLDER_NAME);
        testDocument = docTestHelper.createDocument(TEST_DOCUMENT_NAME, testFolder);
    }

    @SuppressWarnings("unchecked")
    @After
    public void tearDown() {
        if (testDocument != null) {
            nodeService.deleteNode(testDocument);
        }
        if (testFolder != null) {
            nodeService.deleteNode(testFolder);
        }

        if (caseNodeRef != null) {
            docTestHelper.removeNodesAndDeleteUsersInTransaction(Collections.EMPTY_LIST,
                    Arrays.asList(caseNodeRef), Collections.EMPTY_LIST);
        }

        if (testGroupAuthorityName != null && authorityService.authorityExists(testGroupAuthorityName)) {
            authorityService.deleteAuthority(testGroupAuthorityName);
        }

        if (testGroupAuthorityName2 != null && authorityService.authorityExists(testGroupAuthorityName2)) {
            authorityService.deleteAuthority(testGroupAuthorityName2);
        }

        docTestHelper.removeNodesAndDeleteUsersInTransaction(Collections.EMPTY_LIST, Collections.EMPTY_LIST,
                Arrays.asList(CaseHelper.DEFAULT_USERNAME));
    }

    @Test
    public void shouldStartWorkflowForAssigneePersonThenEndTasks() {

        WorkflowDefinition wfDef = workflowService.getDefinitionByName(ACTIVITY_ADHOC_WORKFLOW_NAME);

        WorkflowInfo wi = new WorkflowInfo();
        wi.setWorkflowType(wfDef.getId());
        wi.setPriority(CaseWorkflowService.WORKFLOW_PRIORITY_MEDIUM);
        wi.getItems().add(testDocument.toString());
        wi.setDueDate(new Date());
        wi.setMessage("Worflow for assignee group person");
        wi.setSendEmailNotifications(false);
        wi.setAssignTo(personNodeRef.toString());
        wi.getProperties().put("oe_caseId", createCase());

        WorkflowPath wfPath = service.startWorkflow(wi);

        List<WorkflowTask> tasks = workflowService.getTasksForWorkflowPath(wfPath.getId());
        Assert.assertEquals("The started workflow should contain a task", 1, tasks.size());
        WorkflowTask wfTask = tasks.get(0);
        workflowService.endTask(wfTask.getId(), null);

        tasks = workflowService.getTasksForWorkflowPath(wfPath.getId());
        workflowService.endTask(tasks.get(0).getId(), null);
    }

    @Test
    public void shouldStartWorkflowForAssigneesListThenEnd() {
        WorkflowDefinition wfDef = workflowService.getDefinitionByName(ACTIVITY_PARALLEL_REVIEW_WORKFLOW_NAME);

        WorkflowInfo wi = new WorkflowInfo();
        wi.setWorkflowType(wfDef.getId());
        wi.setPriority(CaseWorkflowService.WORKFLOW_PRIORITY_MEDIUM);
        wi.getItems().add(testDocument.toString());
        wi.setDueDate(new Date());
        wi.setMessage("Workflow for assignee list");
        wi.setSendEmailNotifications(false);
        wi.getAssignees().add(personNodeRef.toString());
        wi.getProperties().put(REQUIRED_APPROVE_PERCENT_PROPERTY, "100");
        wi.getProperties().put("oe_caseId", createCase());

        WorkflowPath wfPath = service.startWorkflow(wi);
        String wfInstanceId = wfPath.getInstance().getId();

        List<WorkflowTask> tasks = workflowService.getAssignedTasks(AuthenticationUtil.getFullyAuthenticatedUser(),
                WorkflowTaskState.IN_PROGRESS);

        WorkflowTask task = tasks
                .stream()
                .filter(t -> t.getPath().getInstance().getId().equals(wfInstanceId))
                .findFirst()
                .get();

        QName outcomePropName = QName.createQName(NamespaceService.WORKFLOW_MODEL_1_0_URI, "reviewOutcome");
        Map<QName, Serializable> props = new HashMap<QName, Serializable>();
        props.put(outcomePropName, "Approve");
        workflowService.updateTask(task.getId(), props, null, null);
        workflowService.endTask(task.getId(), null);

        String endTaskId = workflowService.getTasksForWorkflowPath(wfPath.getId()).get(0).getId();
        workflowService.endTask(endTaskId, null);

    }

    @Test
    public void shouldStartWorkflowForAssigneeGroupThenEndTasks() {
        WorkflowDefinition wfDef = workflowService
                .getDefinitionByName(ACTIVITY_PARALLEL_GROUP_REVIEW_WORKFLOW_NAME);

        WorkflowInfo wi = new WorkflowInfo();
        wi.setWorkflowType(wfDef.getId());
        wi.setPriority(CaseWorkflowService.WORKFLOW_PRIORITY_MEDIUM);
        wi.getItems().add(testDocument.toString());
        wi.setDueDate(new Date());
        wi.setMessage("Workflow for assignee group");
        wi.setSendEmailNotifications(false);
        wi.setAssignToGroup(caseCreatorsGroupNodeRef.toString());
        wi.getProperties().put("oe_caseId", createCase());

        WorkflowPath wfPath = service.startWorkflow(wi);
        String wfInstanceId = wfPath.getInstance().getId();

        List<WorkflowTask> tasks = workflowService.getAssignedTasks(AuthenticationUtil.getFullyAuthenticatedUser(),
                WorkflowTaskState.IN_PROGRESS);
        
        WorkflowTask task = tasks
                .stream()
                .filter(t -> t.getPath().getInstance().getId().equals(wfInstanceId))
                .findFirst()
                .get();
        QName outcomePropName = QName.createQName(NamespaceService.WORKFLOW_MODEL_1_0_URI, "reviewOutcome");
        Map<QName, Serializable> props = new HashMap<QName, Serializable>();
        props.put(outcomePropName, "Approve");
        workflowService.updateTask(task.getId(), props, null, null);
        workflowService.endTask(task.getId(), null);

        String endTaskId = workflowService.getTasksForWorkflowPath(wfPath.getId()).get(0).getId();
        workflowService.endTask(endTaskId, null);

    }

    @Test
    public void shouldCreateCaseThenAddWorkflowAssigneeAsCaseMember() {
        String caseId = createCase();
        Map<QName, Serializable> params = new HashMap<QName, Serializable>();
        params.put(OpenESDHModel.PROP_OE_CASE_ID, caseId);
        params.put(WorkflowModel.ASSOC_ASSIGNEE, personService.getPerson(CaseHelper.ALICE_BEECHER));
        service.grantCaseAccessToWorkflowAssignees(params);

        assertTrueUsersAmidstCaseReadMembers(CaseHelper.ALICE_BEECHER);
    }

    @Test
    public void shouldCreateCaseThenAddWorkflowAssigneeListAsCaseMembers() {
        String caseId = createCase();

        Map<QName, Serializable> params = new HashMap<QName, Serializable>();
        params.put(OpenESDHModel.PROP_OE_CASE_ID, caseId);
        params.put(
                WorkflowModel.ASSOC_ASSIGNEES,
                (Serializable) Arrays.asList(personService.getPerson(CaseHelper.ALICE_BEECHER),
                        personService.getPerson(CaseHelper.MIKE_JACKSON)));
        service.grantCaseAccessToWorkflowAssignees(params);

        assertTrueUsersAmidstCaseReadMembers(CaseHelper.ALICE_BEECHER, CaseHelper.MIKE_JACKSON);
    }

    @Test
    public void shouldCreateCaseThenCreateGroupWithUsersThenAddWorkflowGroupAssigneeAsCaseMembers() {
        testGroupAuthorityName = authorityService.createAuthority(AuthorityType.GROUP, TEST_GROUP_SHORT_NAME);
        authorityService.addAuthority(testGroupAuthorityName, CaseHelper.ALICE_BEECHER);
        authorityService.addAuthority(testGroupAuthorityName, CaseHelper.MIKE_JACKSON);

        NodeRef groupNodeRef = authorityService.getAuthorityNodeRef(testGroupAuthorityName);

        String caseId = createCase();

        Map<QName, Serializable> params = new HashMap<QName, Serializable>();
        params.put(OpenESDHModel.PROP_OE_CASE_ID, caseId);
        params.put(WorkflowModel.ASSOC_GROUP_ASSIGNEE, groupNodeRef);
        service.grantCaseAccessToWorkflowAssignees(params);

        assertTrueUsersAmidstCaseReadMembers(CaseHelper.ALICE_BEECHER, CaseHelper.MIKE_JACKSON);
    }

    @Test
    public void shouldCreateCaseThenSeveralGroupsWithUsersThenAddWorkflowGroupsAssigneesAsCaseMembers() {
        testGroupAuthorityName = authorityService.createAuthority(AuthorityType.GROUP, TEST_GROUP_SHORT_NAME);
        authorityService.addAuthority(testGroupAuthorityName, CaseHelper.ALICE_BEECHER);
        NodeRef groupNodeRef = authorityService.getAuthorityNodeRef(testGroupAuthorityName);

        testGroupAuthorityName2 = authorityService.createAuthority(AuthorityType.GROUP, TEST_GROUP_SHORT_NAME2);
        authorityService.addAuthority(testGroupAuthorityName2, CaseHelper.MIKE_JACKSON);
        NodeRef groupNodeRef2 = authorityService.getAuthorityNodeRef(testGroupAuthorityName2);

        String caseId = createCase();

        Map<QName, Serializable> params = new HashMap<QName, Serializable>();
        params.put(OpenESDHModel.PROP_OE_CASE_ID, caseId);
        params.put(WorkflowModel.ASSOC_GROUP_ASSIGNEES, (Serializable) Arrays.asList(groupNodeRef, groupNodeRef2));
        service.grantCaseAccessToWorkflowAssignees(params);

        assertTrueUsersAmidstCaseReadMembers(CaseHelper.ALICE_BEECHER, CaseHelper.MIKE_JACKSON);
    }

    @Test
    public void shouldNotAddWorkflowAssigneeToCaseMembersIfSheAlreadyIsWriteMember() {
        String caseId = createCase();
        caseMembersService.addAuthorityToRole(CaseHelper.ALICE_BEECHER, getCaseMemberWriteRole(), caseNodeRef);

        Map<QName, Serializable> params = new HashMap<QName, Serializable>();
        params.put(OpenESDHModel.PROP_OE_CASE_ID, caseId);
        params.put(
                WorkflowModel.ASSOC_ASSIGNEES,
                (Serializable) Arrays.asList(personService.getPerson(CaseHelper.ALICE_BEECHER),
                        personService.getPerson(CaseHelper.MIKE_JACKSON)));
        service.grantCaseAccessToWorkflowAssignees(params);

        assertTrueUsersAmidstCaseReadMembers(CaseHelper.MIKE_JACKSON);
        assertFalseUsersAmidstCaseReadMembers(CaseHelper.ALICE_BEECHER);
    }

    private void assertTrueUsersAmidstCaseReadMembers(String... userNames) {
        Set<String> caseReadMembers = caseMembersService.getMembersByRole(caseNodeRef, true, false).get(
                CaseHelper.CASE_READER_ROLE);
        Assert.assertNotNull("Case members with READ permissions should exist", caseReadMembers);
        for (String userName : userNames) {
            Assert.assertTrue("[" + userName + "] user should be among case members with READ permissions",
                    caseReadMembers.contains(userName));
        }
    }

    private void assertFalseUsersAmidstCaseReadMembers(String... userNames) {
        Set<String> caseReadMembers = caseMembersService.getMembersByRole(caseNodeRef, true, false).get(
                CaseHelper.CASE_READER_ROLE);
        Assert.assertNotNull("Case members with READ permissions should exist", caseReadMembers);
        for (String userName : userNames) {
            Assert.assertFalse("[" + userName + "] user should NOT be among case members with READ permissions",
                    caseReadMembers.contains(userName));
        }
    }

    private String getCaseMemberWriteRole() {
        return caseService.getRoles(caseNodeRef).stream().filter(role -> role.contains(CaseService.WRITER))
                .findAny().get();
    }

    private String createCase() {
        caseNodeRef = docTestHelper.createCaseBehaviourOn("Test case1", testFolder, CaseHelper.DEFAULT_USERNAME);
        return caseService.getCaseId(caseNodeRef);
    }
}

