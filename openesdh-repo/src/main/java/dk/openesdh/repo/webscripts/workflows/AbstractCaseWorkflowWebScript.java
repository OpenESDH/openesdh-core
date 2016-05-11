package dk.openesdh.repo.webscripts.workflows;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.alfresco.repo.web.scripts.workflow.WorkflowModelBuilder;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.namespace.NamespaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import dk.openesdh.repo.services.workflow.CaseWorkflowModelBuilder;
import dk.openesdh.repo.services.workflow.WorkflowTaskService;

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
    @Autowired
    protected WorkflowTaskService workflowTaskService;
    
    protected WorkflowModelBuilder modelBuilder;

    @PostConstruct
    public void init() {
        modelBuilder = new CaseWorkflowModelBuilder(namespaceService, nodeService, authenticationService,
                personService, workflowService, dictionaryService, workflowTaskService);
    }

    protected List<Map<String, Object>> getWorkflowTasks() {
        WorkflowTaskQuery tasksQuery = new WorkflowTaskQuery();
        tasksQuery.setActive(null);
        return getWorkflowTasks(tasksQuery);
    }
    
    protected List<Map<String, Object>> getWorkflowTasks(WorkflowTaskQuery query) {
        return workflowService.queryTasks(query, false)
                .stream()
                .map(task -> modelBuilder.buildSimple(task, null))
                .collect(Collectors.toList());
    }
}
