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
    private static final String NODE_ID = "node_id";
    private static final String STORE_ID = "store_id";
    private static final String STORE_TYPE = "store_type";

    private CaseService caseService;

    public void setCaseService(CaseService caseService) {
        this.caseService = caseService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();

        Map<String, String> vars = req.getServiceMatch().getTemplateVars();
        NodeRef nodeRef = null;
        String storeType = vars.get(STORE_TYPE);
        String storeId = vars.get(STORE_ID);
        String nodeId = vars.get(NODE_ID);
        if (storeType != null && storeId != null && nodeId != null)
        {
            nodeRef = new NodeRef(storeType, storeId, nodeId);
        }

        Boolean unjournalize = Boolean.valueOf(req.getParameter("unjournalize"));

        boolean result = true;
        try {
            if (unjournalize != null && unjournalize) {
                caseService.unJournalize(nodeRef);
            } else {
                NodeRef journalKey = new NodeRef(req.getParameter("journalKey"));
                caseService.journalize(nodeRef, journalKey);
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
