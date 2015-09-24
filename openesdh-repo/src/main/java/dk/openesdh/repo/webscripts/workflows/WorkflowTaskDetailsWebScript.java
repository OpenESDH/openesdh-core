package dk.openesdh.repo.webscripts.workflows;

import java.io.IOException;
import java.util.Map;

import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.util.StringUtils;

import dk.openesdh.repo.services.workflow.WorkflowTaskService;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

public class WorkflowTaskDetailsWebScript extends AbstractWebScript {

    private WorkflowTaskService workflowTaskService;

    public void setWorkflowTaskService(WorkflowTaskService workflowTaskService) {
        this.workflowTaskService = workflowTaskService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
        String taskId = templateArgs.get(WebScriptUtils.TASK_ID);
        if (StringUtils.isEmpty(taskId)) {
            return;
        }
        Map<String, Object> task = workflowTaskService.getWorkflowTask(taskId);
        res.setContentEncoding(WebScriptUtils.CONTENT_ENCODING_UTF_8);
        WebScriptUtils.writeJson(task, res);
    }

}
