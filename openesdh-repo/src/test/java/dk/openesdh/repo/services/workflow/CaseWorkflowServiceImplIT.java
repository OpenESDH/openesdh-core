package dk.openesdh.repo.services.workflow;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
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
import dk.openesdh.repo.model.WorkflowInfo;

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
    @Qualifier("CaseWorkflowService")
    protected CaseWorkflowService service;

    private static final String TEST_FOLDER_NAME = "CaseWorkflowServiceImplIT";
    private static final String TEST_DOCUMENT_NAME = "test_document.txt";
    private static final String ACTIVITY_ADHOC_WORKFLOW_NAME = "activiti$activitiAdhoc";
    private static final String ACTIVITY_PARALLEL_GROUP_REVIEW_WORKFLOW_NAME = "activiti$activitiParallelGroupReview";
    private static final String ACTIVITY_PARALLEL_REVIEW_WORKFLOW_NAME = "activiti$activitiParallelReview";
    private static final String REQUIRED_APPROVE_PERCENT_PROPERTY = "wf_requiredApprovePercent";

    private NodeRef testFolder;
    private NodeRef testDocument;
    private NodeRef personNodeRef;
    private NodeRef caseCreatorsGroupNodeRef;

    @Before
    public void setUp() throws Exception {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        personNodeRef = personService.getPerson(AuthenticationUtil.getFullyAuthenticatedUser());
        caseCreatorsGroupNodeRef = authorityService.getAuthorityNodeRef(CaseHelper.CASE_CREATOR_GROUP);

        testFolder = docTestHelper.createFolder(TEST_FOLDER_NAME);
        testDocument = docTestHelper.createDocument(TEST_DOCUMENT_NAME, testFolder);
    }

    @After
    public void tearDown() {
        if (testDocument != null) {
            nodeService.deleteNode(testDocument);
        }
        if (testFolder != null) {
            nodeService.deleteNode(testFolder);
        }
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
}

