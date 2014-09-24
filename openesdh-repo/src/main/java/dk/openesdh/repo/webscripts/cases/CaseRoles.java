package dk.openesdh.repo.webscripts.cases;

import dk.openesdh.repo.services.CaseService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class CaseRoles extends AbstractWebScript {

    private CaseService caseService;

    public void setCaseService(CaseService caseService) {
        this.caseService = caseService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        NodeRef caseNodeRef = new NodeRef(req.getParameter("nodeRef"));
        Set<String> roles = caseService.getRoles(caseNodeRef);
        try {
            JSONArray json = buildJSON(roles);
            json.write(res.getWriter());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    JSONArray buildJSON(Set<String> roles) throws JSONException {
        JSONArray result = new JSONArray();
        for (String role : roles) {
            result.put(role);
        }
        return result;
    }


}