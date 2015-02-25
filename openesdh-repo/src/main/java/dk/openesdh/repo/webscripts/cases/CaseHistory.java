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
        System.out.println(req.getParameter("nodeRef"));
        NodeRef caseNodeRef = new NodeRef(req.getParameter("nodeRef"));

        JSONArray result = auditSearchService.getAuditLogByCaseNodeRef(caseNodeRef, 1000);

        res.setContentEncoding("UTF-8");
        res.setHeader("Content-Range", "items " + 0 +
                "-" + result.size() + "/" + result.size());
        result.writeJSONString(res.getWriter());
    }


}
