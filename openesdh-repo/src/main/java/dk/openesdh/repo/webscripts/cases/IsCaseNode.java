package dk.openesdh.repo.webscripts.cases;

import dk.openesdh.repo.services.cases.CaseService;

import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;

public class IsCaseNode extends AbstractWebScript {

    private CaseService caseService;

    public void setCaseService(CaseService caseService) {
        this.caseService = caseService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        NodeRef caseNodeRef = new NodeRef(req.getParameter("nodeRef"));
        boolean isCaseNode = caseService.isCaseNode(caseNodeRef);
        if (!isCaseNode) {
            res.setStatus(409);
        } else {
            try {
                JSONObject json = new JSONObject();
                json.put("isCaseNode", isCaseNode);
                json.write(res.getWriter());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
