package dk.openesdh.repo.webscripts.cases;

import java.io.IOException;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import dk.openesdh.repo.services.cases.CaseService;
import dk.openesdh.repo.webscripts.notes.Notes;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

public class CaseNotes extends Notes {

    private CaseService caseService;

    public void setCaseService(CaseService caseService) {
        this.caseService = caseService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
        String caseId = templateArgs.get(WebScriptUtils.CASE_ID);
        NodeRef nodeRef = caseService.getCaseById(caseId);

        String method = req.getServiceMatch().getWebScript().getDescription().getMethod();
        try {
            if (method.equals("GET")) {
                get(nodeRef, req, res);
            } else if (method.equals("POST")) {
                post(nodeRef, req, res);
            } else if (method.equals("DELETE")) {
                delete(nodeRef, req, res);
            } else if (method.equals("PUT")) {
                put(nodeRef, req, res);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
