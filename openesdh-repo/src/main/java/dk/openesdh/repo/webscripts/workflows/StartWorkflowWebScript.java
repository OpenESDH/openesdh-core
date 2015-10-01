package dk.openesdh.repo.webscripts.workflows;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

import com.github.dynamicextensionsalfresco.webscripts.annotations.Attribute;
import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;

import dk.openesdh.repo.model.WorkflowInfo;
import dk.openesdh.repo.services.workflow.CaseWorkflowService;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

@Component
@WebScript(description = "Starts workflow relevant to the passed data", families = { "Case Workflow Tools" })
public class StartWorkflowWebScript {

    @Autowired
    private CaseWorkflowService workflowService;

    @Attribute
    protected WorkflowInfo getWorkflowInfo(WebScriptRequest req) throws IOException {
        return (WorkflowInfo) WebScriptUtils.readJson(WorkflowInfo.class, req);
    }

    @Uri(value = "/api/openesdh/workflow/start", method = HttpMethod.POST, defaultFormat = "json")
    public void startWorkflow(@Attribute WorkflowInfo workflowInfo, WebScriptResponse res)
            throws IOException {
        WorkflowPath wfPath = workflowService.startWorkflow(workflowInfo);
        res.setContentEncoding(WebScriptUtils.CONTENT_ENCODING_UTF_8);
        Map<String, String> result = new HashMap<String, String>();
        result.put("workflowPathId", wfPath.getId());
        WebScriptUtils.writeJson(result, res);
    }

}
