package dk.openesdh.repo.webscripts.cases;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import dk.openesdh.repo.services.cases.CaseService;

public class CaseRoles extends AbstractWebScript {

    protected CaseService caseService;

    public void setCaseService(CaseService caseService) {
        this.caseService = caseService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
        String caseId = templateArgs.get ("caseId");
        NodeRef caseNodeRef = caseService.getCaseById(caseId);
        if(caseNodeRef == null)
            caseNodeRef = new NodeRef(req.getParameter("nodeRef"));
        Set<String> roles = caseService.getAllRoles(caseNodeRef);
        try {
            JSONArray json = buildJSON(roles);
            json.write(res.getWriter());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected Set<String> getRoles(NodeRef caseNodeRef) {
        return caseService.getRoles(caseNodeRef);
    }

    JSONArray buildJSON(Set<String> roles) throws
            JSONException {
        JSONArray result = new JSONArray();
        for (String role : roles) {
            result.put(role);
        }
        return result;
    }


}
