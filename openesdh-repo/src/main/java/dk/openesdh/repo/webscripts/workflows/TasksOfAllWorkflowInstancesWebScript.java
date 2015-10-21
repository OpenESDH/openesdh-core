package dk.openesdh.repo.webscripts.workflows;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.web.scripts.workflow.WorkflowModelBuilder;
import org.springframework.stereotype.Component;

import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import com.github.dynamicextensionsalfresco.webscripts.resolutions.Resolution;

import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

@Component
@WebScript(description = "Retrieves all tasks of all workflow instances", families = "Case Workflow Tools")
public class TasksOfAllWorkflowInstancesWebScript extends AbstractCaseWorkflowWebScript {

    @Uri(value = "/api/openesdh/workflow/tasks", method = HttpMethod.GET, defaultFormat = "json")
    public Resolution getAllTasksOfAllWorkflowInstances() {
        List<Map<String, Object>> tasks = getWorkflowTasks(workflowService.getActiveWorkflows());
        Map<String, Object> model = new HashMap<String, Object>();
        model.put(WorkflowModelBuilder.TASK_WORKFLOW_INSTANCE_TASKS, tasks);
        return WebScriptUtils.jsonResolution(model);
    }

}
