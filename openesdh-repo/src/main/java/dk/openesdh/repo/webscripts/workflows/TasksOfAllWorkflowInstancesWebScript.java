package dk.openesdh.repo.webscripts.workflows;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.alfresco.repo.web.scripts.workflow.WorkflowModelBuilder;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.namespace.NamespaceService;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

public class TasksOfAllWorkflowInstancesWebScript extends AbstractWebScript {

    private WorkflowService workflowService;
    private NamespaceService namespaceService;
    private NodeService nodeService;
    private AuthenticationService authenticationService;
    private PersonService personService;
    private DictionaryService dictionaryService;

    public void setWorkflowService(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    public void setNamespaceService(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        WorkflowModelBuilder modelBuilder = new WorkflowModelBuilder(namespaceService, nodeService,
                authenticationService, personService, workflowService, dictionaryService);

        List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
        
        workflowService.getActiveWorkflows()
            .stream()
            .map(workflow -> getWorkflowTasks(workflow.getId(), modelBuilder))
            .forEach(taskList -> resultList.addAll(taskList));

        Map<String, Object> model = new HashMap<String, Object>();
        model.put(WorkflowModelBuilder.TASK_WORKFLOW_INSTANCE_TASKS, resultList);
        res.setContentEncoding(WebScriptUtils.CONTENT_ENCODING_UTF_8);
        WebScriptUtils.writeJson(model, res);
    }

    protected List<Map<String, Object>> getWorkflowTasks(String instanceId, WorkflowModelBuilder modelBuilder) {
        WorkflowTaskQuery tasksQuery = new WorkflowTaskQuery();
        tasksQuery.setTaskState(WorkflowTaskState.IN_PROGRESS);
        tasksQuery.setActive(null);
        tasksQuery.setProcessId(instanceId);

        return workflowService.queryTasks(tasksQuery, false)
                .stream()
                .map(task -> modelBuilder.buildSimple(task, null))
                .collect(Collectors.toList());
    }

}
