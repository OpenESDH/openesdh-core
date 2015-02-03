package dk.openesdh.repo.webscripts.cases;

import dk.openesdh.repo.services.cases.CaseService;
import dk.openesdh.repo.services.NodeInfoService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.security.access.AccessDeniedException;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

/**
 * Created by torben on 11/09/14.
 */
public class Journalize extends AbstractWebScript {

    private CaseService caseService;

    public void setCaseService(CaseService caseService) {
        this.caseService = caseService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
        String caseId = templateArgs.get ("caseId");

//        NodeRef caseNodeRef = new NodeRef(req.getParameter("nodeRef"));
        NodeRef caseNodeRef = this.caseService.getCaseById(caseId);
        Boolean unjournalize = Boolean.valueOf(req.getParameter("unjournalize"));

        boolean result = true;
        try {
            if (unjournalize != null && unjournalize) {
                caseService.unJournalize(caseNodeRef);
            } else {
                NodeRef journalKey = new NodeRef(req.getParameter("journalKey"));
                caseService.journalize(caseNodeRef, journalKey);
            }
        } catch (AccessDeniedException e) {
            res.setStatus(409);
            return;
        }

        JSONObject json = new JSONObject();
        try {
            json.put("result", result);
            json.write(res.getWriter());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


}
