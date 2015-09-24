package dk.openesdh.repo.services.workflow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.web.scripts.workflow.WorkflowModelBuilder;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.NamespaceService;

public class WorkflowTaskServiceImpl implements WorkflowTaskService {
    
    protected WorkflowService workflowService;
    protected NamespaceService namespaceService;
    protected NodeService nodeService;
    protected PersonService personService;
    protected DictionaryService dictionaryService;
    protected AuthenticationService authenticationService;
    protected AuthorityService authorityService;

    public void setWorkflowService(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    public void setNamespaceService(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    @Override
    public Map<String, Object> getWorkflowTask(String taskId) {
        WorkflowTask task = workflowService.getTaskById(taskId);
        return getTaskDataMap(task);
    }

    protected Map<String, Object> getTaskDataMap(WorkflowTask task) {
        WorkflowModelBuilder modelBuilder = new WorkflowModelBuilder(namespaceService, nodeService,
                authenticationService, personService, workflowService, dictionaryService);
        Map<String, Object> taskMap = modelBuilder.buildSimple(task, null);
        List<NodeRef> contents = workflowService.getPackageContents(task.getId());
        List<Map<String, Object>> packageItems = contents
                .stream()
                .map(nodeRef -> getPackageItem(nodeRef))
                .collect(Collectors.toList());
        taskMap.put(WorkflowTaskService.TASK_PACKAGE_ITEMS, packageItems);
        return taskMap;
    }
    
    protected Map<String, Object> getPackageItem(NodeRef nodeRef){
        Map<String, Object> item = new HashMap<String, Object>();
        item.put(WorkflowTaskService.PACKAGE_ITEM_NAME, nodeService.getProperty(nodeRef, ContentModel.PROP_NAME));
        item.put(WorkflowTaskService.PACKAGE_ITEM_MODIFIED,
                nodeService.getProperty(nodeRef, ContentModel.PROP_MODIFIED));
        item.put(WorkflowTaskService.PACKAGE_ITEM_DESCRIPTION,
                nodeService.getProperty(nodeRef, ContentModel.PROP_DESCRIPTION));
        item.put(WorkflowTaskService.NODE_REF, nodeRef.toString());
        return item;
    }

}
