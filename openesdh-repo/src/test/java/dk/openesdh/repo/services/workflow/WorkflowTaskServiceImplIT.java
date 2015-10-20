package dk.openesdh.repo.services.workflow;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.NamespaceService;
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
import dk.openesdh.repo.model.WorkflowInfo;

@RunWith(RemoteTestRunner.class)
@Remote(runnerClass = SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:alfresco/application-context.xml")
public class WorkflowTaskServiceImplIT {

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
    @Qualifier("AuthenticationService")
    protected AuthenticationService authenticationService;

    @Autowired
    @Qualifier("CaseDocumentTestHelper")
    protected CaseDocumentTestHelper docTestHelper;

    @Autowired
    protected WorkflowTaskService service;

    @Autowired
    @Qualifier("CaseWorkflowService")
    protected CaseWorkflowService caseWorkflowService;

    private NodeRef testFolder;
    private NodeRef testDocument;
    private NodeRef personNodeRef;

    private static final String TEST_FOLDER_NAME = "WorkflowTaskServiceImplIT";
    private static final String TEST_DOCUMENT_NAME = "test_document.txt";
    private static final String ACTIVITY_ADHOC_WORKFLOW_NAME = "activiti$activitiAdhoc";

    @Before
    public void setUp() throws Exception {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        personNodeRef = personService.getPerson(AuthenticationUtil.getFullyAuthenticatedUser());

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

    @SuppressWarnings("unchecked")
    @Test
    public void shouldStartWorkflowWithAttachmentThenRetrieveTaskWithPackageContents() {

        WorkflowDefinition wfDef = workflowService.getDefinitionByName(ACTIVITY_ADHOC_WORKFLOW_NAME);

        WorkflowInfo wi = new WorkflowInfo();
        wi.setWorkflowType(wfDef.getId());
        wi.setPriority(CaseWorkflowService.WORKFLOW_PRIORITY_MEDIUM);
        wi.getItems().add(testDocument.toString());
        wi.setDueDate(new Date());
        wi.setMessage("Worflow to test task info retrieving with package contents");
        wi.setSendEmailNotifications(false);
        wi.setAssignTo(personNodeRef.toString());

        WorkflowPath wfPath = caseWorkflowService.startWorkflow(wi);

        List<WorkflowTask> tasks = workflowService.getTasksForWorkflowPath(wfPath.getId());
        Assert.assertEquals("The started workflow should contain a task", 1, tasks.size());
        WorkflowTask wfTask = tasks.get(0);

        Map<String, Object> taskMap = service.getWorkflowTask(wfTask.getId());
        List<Map<String, Object>> packageItems = (List<Map<String, Object>>) taskMap
                .get(WorkflowTaskService.TASK_PACKAGE_ITEMS);
        Assert.assertEquals("Wrong task package items count", 1, packageItems.size());
        Map<String, Object> item = packageItems.get(0);
        Assert.assertEquals("Wrong task package item retrieved", TEST_DOCUMENT_NAME,
                item.get(WorkflowTaskService.PACKAGE_ITEM_NAME));

        workflowService.endTask(wfTask.getId(), null);

        tasks = workflowService.getTasksForWorkflowPath(wfPath.getId());
        workflowService.endTask(tasks.get(0).getId(), null);
    }

}
