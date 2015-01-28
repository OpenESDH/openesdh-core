package dk.openesdh.repo.webscripts.cases;

import dk.openesdh.repo.services.audit.AuditSearchService;
import dk.openesdh.repo.services.NodeInfoService;
import dk.openesdh.repo.services.cases.CaseService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.JSONArray;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import org.springframework.extensions.surf.util.I18NUtil;


import java.io.IOException;

/**
 * Created by flemming on 19/09/14.
 */
public class CaseHistory extends AbstractWebScript {

    private AuditSearchService auditSearchService;

    public void setAuditSearchService(AuditSearchService auditSearchService) {
        this.auditSearchService = auditSearchService;
    }


    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        NodeRef caseNodeRef = new NodeRef(req.getParameter("nodeRef"));

        JSONArray result = auditSearchService.getAuditLogByCaseNodeRef(caseNodeRef, new Long(1000));

        JSONObject json = new JSONObject();
        JSONObject json1 = new JSONObject();
        try {
            json.put("success", true);
            json1.append("test", json);
            json1.append("test", json);

            result.writeJSONString(res.getWriter());
//            result.write(res.getWriter());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


}
