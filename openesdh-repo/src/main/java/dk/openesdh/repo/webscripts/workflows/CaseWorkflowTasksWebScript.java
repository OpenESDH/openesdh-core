package dk.openesdh.repo.webscripts.workflows;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.repo.web.scripts.content.ContentStreamer;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.TempFileProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

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

    @Autowired
    @Qualifier("webscript.content.streamer")
    private ContentStreamer streamer;

    @Uri(value = "/api/openesdh/case/{caseId}/tasks", method = HttpMethod.GET, defaultFormat = WebScriptUtils.JSON)
    public Resolution getCaseWorkflowTasks(@UriVariable(WebScriptUtils.CASE_ID) final String caseId) {
        WorkflowTaskQuery tasksQuery = new WorkflowTaskQuery();
        tasksQuery.setActive(null);
        Map<QName, Object> processCustomProps = new HashMap<QName, Object>();
        processCustomProps.put(OpenESDHModel.PROP_OE_CASE_ID, caseId);
        tasksQuery.setProcessCustomProps(processCustomProps);
        return WebScriptUtils.jsonResolution(getWorkflowTasks(tasksQuery));
    }

    @Uri(value = "/api/openesdh/workflow/instance/{instanceId}/diagram", method = HttpMethod.GET)
    public Resolution getWorkflowInstanceDiagram(@UriVariable("instanceId") String workflowInstanceId)
            throws IOException {
        WorkflowInstance workflowInstance = workflowService.getWorkflowById(workflowInstanceId);
        if (workflowInstance == null) {
            throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND,
                    "Unable to find workflow instance with id: " + workflowInstanceId);
        }
        File file = TempFileProvider.createTempFile("workflow-diagram-", ".png");
        InputStream imageData = workflowService.getWorkflowImage(workflowInstanceId);
        OutputStream os = new FileOutputStream(file);
        FileCopyUtils.copy(imageData, os);
        return (req, res, par) -> {
            streamer.streamContent(req, res, file, null, false, null, null);
        };
    }
}
