package dk.openesdh.repo.webscripts.workflows;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.UriVariable;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;

import dk.openesdh.repo.services.workflow.WorkflowTaskService;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

@Component
@WebScript(description = "Retrieves details of the provided workflow task.", families = { "Case Workflow Tools" })
public class WorkflowTaskDetailsWebScript {

    @Autowired
    private WorkflowTaskService workflowTaskService;

    @Uri(value = "/api/openesdh/workflow/task/{taskId}/details", method = HttpMethod.GET, defaultFormat = "json")
    public void getWorkflowTaskDetails(@UriVariable(value = "taskId") final String taskId, WebScriptResponse res)
            throws IOException {
        Map<String, Object> task = workflowTaskService.getWorkflowTask(taskId);
        res.setContentEncoding(WebScriptUtils.CONTENT_ENCODING_UTF_8);
        WebScriptUtils.writeJson(task, res);
    }

}
