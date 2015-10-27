package dk.openesdh.repo.webscripts.workflows;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.workflow.WorkflowInstanceQuery;
import org.alfresco.service.namespace.QName;
import org.springframework.stereotype.Component;

import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.UriVariable;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import com.github.dynamicextensionsalfresco.webscripts.resolutions.Resolution;

import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

@Component
@WebScript(description = "Retrieves workflow tasks by caseId", families = "Case Workflow Tools")
public class CaseWorkflowTasksWebScript extends AbstractCaseWorkflowWebScript {

    @Uri(value = "/api/openesdh/case/{caseId}/tasks", method = HttpMethod.GET, defaultFormat = "json")
    public Resolution getCaseWorkflowTasks(@UriVariable(WebScriptUtils.CASE_ID) final String caseId) {
        Map<QName, Object> props = new HashMap<QName, Object>();
        props.put(OpenESDHModel.PROP_OE_CASE_ID, caseId);
        WorkflowInstanceQuery query = new WorkflowInstanceQuery();
        query.setActive(true);
        query.setCustomProps(props);
        return WebScriptUtils.jsonResolution(
                   getWorkflowTasks(
                        workflowService.getWorkflows(query)
        ));
    }
}
