package dk.openesdh.repo.services.workflow;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.web.scripts.workflow.WorkflowModelBuilder;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.authorities.UsersService;
import dk.openesdh.repo.services.documents.DocumentService;

@Service
public class WorkflowTaskServiceImpl implements WorkflowTaskService {

    @Autowired
    @Qualifier("WorkflowService")
    private WorkflowService workflowService;
    @Autowired
    private NamespaceService namespaceService;
    @Autowired
    private NodeService nodeService;
    @Autowired
    private PersonService personService;
    @Autowired
    private DictionaryService dictionaryService;
    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private DocumentService documentService;
    @Autowired
    @Qualifier(CaseWorkflowService.NAME)
    private CaseWorkflowService caseWorkflowService;
    @Autowired
    @Qualifier("UsersService")
    private UsersService userService;

    @Override
    public Map<String, Object> getWorkflowTask(String taskId) {
        WorkflowTask task = workflowService.getTaskById(taskId);
        return getTaskDataMap(task);
    }

    @Override
    public Optional<String> getWorkflowCaseId(String workflowOrTaskId) {
        Optional<Serializable> optCaseId = getCaseIdByWorkflowId(workflowOrTaskId);
        return (optCaseId.isPresent() ? optCaseId : getCaseIdByTaskId(workflowOrTaskId))
                .map(Serializable::toString);
    }

    @Override
    public Optional<Serializable> getCaseIdByTaskId(String taskId) {
        return getCaseIdFromWorkflowPath(workflowService.getTaskById(taskId).getPath().getId());
    }

    @Override
    public List<WorkflowTask> getCurrentUserSubordinatesTasks() {
        return userService.getCurrentUserSubordinateNames()
                .stream()
                .flatMap(userName -> workflowService.getAssignedTasks(userName, WorkflowTaskState.IN_PROGRESS, true).stream())
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getTaskDataMap(WorkflowTask task) {
        WorkflowModelBuilder modelBuilder = new WorkflowModelBuilder(namespaceService, nodeService,
                authenticationService, personService, workflowService, dictionaryService);
        Map<String, Object> taskMap = modelBuilder.buildSimple(task, null);

        String pathId = task.getPath().getId();
        getCaseIdFromWorkflowPath(pathId).ifPresent(
                caseId -> taskMap.put(WorkflowTaskService.TASK_CASE_ID, caseId));

        List<NodeRef> contents = workflowService.getPackageContents(task.getId());
        List<Map<String, Object>> packageItems = contents
                .stream()
                .map(nodeRef -> getPackageItem(nodeRef))
                .collect(Collectors.toList());
        taskMap.put(WorkflowTaskService.TASK_PACKAGE_ITEMS, packageItems);

        List<Map<String, Object>> assignees = caseWorkflowService.getWorkflowAssignees(pathId);
        Map<String, Object> workflowInstance = (Map<String, Object>) taskMap.get(WorkflowModelBuilder.TASK_WORKFLOW_INSTANCE);
        workflowInstance.put(WorkflowTaskService.WORKFLOW_ASSIGNEES, assignees);

        return taskMap;
    }

    private Map<String, Object> getPackageItem(NodeRef nodeRef) {
        Map<String, Object> item = new HashMap<>();
        item.put(WorkflowTaskService.NODE_REF, nodeRef.toString());

        Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);

        item.put(WorkflowTaskService.PACKAGE_ITEM_NAME, properties.get(ContentModel.PROP_NAME));
        item.put(WorkflowTaskService.PACKAGE_ITEM_MODIFIED, properties.get(ContentModel.PROP_MODIFIED));
        item.put(WorkflowTaskService.PACKAGE_ITEM_DESCRIPTION, properties.get(ContentModel.PROP_DESCRIPTION));
        String person = properties.get(ContentModel.PROP_CREATOR).toString();
        item.put(WorkflowTaskService.PACKAGE_ITEM_CREATEDBY,
                personService.getPerson(personService.getPerson(person)));

        NodeRef mainDocNodeRef = documentService.getMainDocument(nodeRef);
        if (mainDocNodeRef != null) {
            item.put(WorkflowTaskService.PACKAGE_ITEM_MAIN_DOC_NODE_REF, mainDocNodeRef.toString());
        } else {
            item.put(WorkflowTaskService.PACKAGE_ITEM_DOC_RECORD_NODE_REF,
                    documentService.getDocRecordNodeRef(nodeRef).toString());
        }

        return item;
    }

    private Optional<Serializable> getCaseIdByWorkflowId(String workflowId) {
        return workflowService.getWorkflowPaths(workflowId).stream()
                .findAny()
                .flatMap(path -> getCaseIdFromWorkflowPath(path.getId()));
    }

    private Optional<Serializable> getCaseIdFromWorkflowPath(String pathId) {
        return Optional.ofNullable(workflowService.getPathProperties(pathId).get(OpenESDHModel.PROP_OE_CASE_ID));
    }

}
