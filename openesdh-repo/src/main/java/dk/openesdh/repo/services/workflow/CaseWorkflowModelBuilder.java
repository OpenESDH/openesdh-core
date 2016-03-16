package dk.openesdh.repo.services.workflow;

import java.util.Collection;
import java.util.Map;

import org.alfresco.repo.web.scripts.workflow.WorkflowModelBuilder;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.NamespaceService;

public class CaseWorkflowModelBuilder extends WorkflowModelBuilder {

    private WorkflowTaskService workflowTaskService;

    public CaseWorkflowModelBuilder(NamespaceService namespaceService, NodeService nodeService,
            AuthenticationService authenticationService, PersonService personService,
            WorkflowService workflowService, DictionaryService dictionaryService,
            WorkflowTaskService workflowTaskService) {
        super(namespaceService, nodeService, authenticationService, personService, workflowService,
                dictionaryService);
        this.workflowTaskService = workflowTaskService;
    }

    @Override
    public Map<String, Object> buildSimple(WorkflowTask task, Collection<String> propertyFilters) {
        Map<String, Object> result = super.buildSimple(task, propertyFilters);
        workflowTaskService.getCaseIdByTaskId(task.getId())
            .ifPresent(caseId -> result.put(WorkflowTaskService.TASK_CASE_ID, caseId));
        return result;
    }

}
