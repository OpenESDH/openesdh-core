package dk.openesdh.repo.webscripts.workflows;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.alfresco.repo.web.scripts.workflow.WorkflowModelBuilder;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.namespace.NamespaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public abstract class AbstractCaseWorkflowWebScript {
    @Autowired
    @Qualifier("WorkflowService")
    protected WorkflowService workflowService;
    @Autowired
    private NamespaceService namespaceService;
    @Autowired
    private NodeService nodeService;
    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private PersonService personService;
    @Autowired
    private DictionaryService dictionaryService;

    protected List<Map<String, Object>> getWorkflowTasks(List<WorkflowInstance> workflows) {
        WorkflowModelBuilder modelBuilder = new WorkflowModelBuilder(namespaceService, nodeService,
                authenticationService, personService, workflowService, dictionaryService);
        return workflows.stream()
            .flatMap(workflow -> getWorkflowTasks(workflow.getId(), modelBuilder).stream())
            .collect(Collectors.toList());
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
