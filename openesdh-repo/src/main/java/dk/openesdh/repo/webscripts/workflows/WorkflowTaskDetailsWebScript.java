package dk.openesdh.repo.webscripts.workflows;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.UriVariable;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import com.github.dynamicextensionsalfresco.webscripts.resolutions.Resolution;

import dk.openesdh.repo.services.workflow.WorkflowTaskService;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

@Component
@WebScript(description = "Retrieves details of the provided workflow task.", families = { "Case Workflow Tools" })
public class WorkflowTaskDetailsWebScript {

    @Autowired
    private WorkflowTaskService workflowTaskService;

    @Uri(value = "/api/openesdh/workflow/task/{taskId}/details", method = HttpMethod.GET, defaultFormat = "json")
    public Resolution getWorkflowTaskDetails(@UriVariable(WebScriptUtils.TASK_ID) final String taskId) {
        Map<String, Object> task = workflowTaskService.getWorkflowTask(taskId);
        return WebScriptUtils.jsonResolution(task);
    }

}
