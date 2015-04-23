package dk.openesdh.repo.webscripts.workflows;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.web.scripts.workflow.AbstractWorkflowWebscript;
import org.alfresco.repo.web.scripts.workflow.WorkflowModelBuilder;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Webscript implementation to return the latest version of all deployed
 * workflow definitions.
 *
 * @author Lanre
 * @basedOn WorkflowsDefinitionGet by Nick Smith
 */
public class CaseWorkflowsDefinitionGet extends AbstractWorkflowWebscript {

    private String casePrefix;

    public void setCasePrefix(String casePrefix) {
        this.casePrefix = casePrefix;
    }


    @Override
    protected Map<String, Object> buildModel(WorkflowModelBuilder modelBuilder, WebScriptRequest req, Status status, Cache cache) {
        ExcludeFilter excludeFilter = null;
        String excludeParam = req.getParameter(PARAM_EXCLUDE);
        if (excludeParam != null && excludeParam.length() > 0) {
            excludeFilter = new ExcludeFilter(excludeParam);
        }

        // list all workflow's definitions simple representation
        List<WorkflowDefinition> workflowDefinitions = workflowService.getDefinitions();

        ArrayList<Map<String, Object>> results = new ArrayList<Map<String, Object>>();

        for (WorkflowDefinition workflowDefinition : workflowDefinitions) {
            // if present, filter out excluded definitions
            if (excludeFilter == null || !excludeFilter.isMatch(workflowDefinition.getName())) {
                String wrkflowName = workflowDefinition.getName();
                if (wrkflowName.contains(casePrefix))
                    results.add(modelBuilder.buildSimple(workflowDefinition));
            }
        }

        Map<String, Object> model = new HashMap<String, Object>();
        model.put("workflowDefinitions", results);
        System.out.println("Returning case workflows");
        return model;
    }
}