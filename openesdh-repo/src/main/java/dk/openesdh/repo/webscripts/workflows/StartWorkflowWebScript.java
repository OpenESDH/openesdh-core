package dk.openesdh.repo.webscripts.workflows;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import dk.openesdh.repo.model.WorkflowInfo;
import dk.openesdh.repo.services.workflow.CaseWorkflowService;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

public class StartWorkflowWebScript extends AbstractWebScript {

    private CaseWorkflowService workflowService;

    public void setWorkflowService(CaseWorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        WorkflowInfo wfInfo = (WorkflowInfo) WebScriptUtils.readJson(WorkflowInfo.class, req);
        WorkflowPath wfPath = workflowService.startWorkflow(wfInfo);
        res.setContentEncoding(WebScriptUtils.CONTENT_ENCODING_UTF_8);
        Map<String, String> result = new HashMap<String, String>();
        result.put("workflowPathId", wfPath.getId());
        WebScriptUtils.writeJson(result, res);
    }

}
