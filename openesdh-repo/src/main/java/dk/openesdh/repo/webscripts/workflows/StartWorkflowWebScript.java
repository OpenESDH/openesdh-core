package dk.openesdh.repo.webscripts.workflows;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.stereotype.Component;

import com.github.dynamicextensionsalfresco.webscripts.annotations.Attribute;
import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import com.github.dynamicextensionsalfresco.webscripts.resolutions.Resolution;

import dk.openesdh.repo.model.WorkflowInfo;
import dk.openesdh.repo.services.workflow.CaseWorkflowService;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

@Component
@WebScript(description = "Starts workflow relevant to the passed data", families = { "Case Workflow Tools" })
public class StartWorkflowWebScript {

    @Autowired
    @Qualifier(CaseWorkflowService.NAME)
    private CaseWorkflowService workflowService;

    @Attribute
    protected WorkflowInfo getWorkflowInfo(WebScriptRequest req) throws IOException {
        return (WorkflowInfo) WebScriptUtils.readJson(WorkflowInfo.class, req);
    }

    @Uri(value = "/api/openesdh/workflow/start", method = HttpMethod.POST, defaultFormat = "json")
    public Resolution startWorkflow(@Attribute WorkflowInfo workflowInfo) {
        WorkflowPath wfPath = workflowService.startWorkflow(workflowInfo);
        Map<String, String> result = new HashMap<String, String>();
        result.put("workflowPathId", wfPath.getId());
        return WebScriptUtils.jsonResolution(result);
    }

}
