package dk.openesdh.repo.webscripts.cases;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.NodeInfoService;
import dk.openesdh.repo.services.cases.CaseService;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

/**
 * Created by torben on 11/09/14.
 */
public class CaseInfoWebScript extends AbstractWebScript {

    private NodeInfoService nodeInfoService;
    private DictionaryService dictionaryService;
    private CaseService caseService;

    public void setNodeInfoService(NodeInfoService nodeInfoService) {
        this.nodeInfoService = nodeInfoService;
    }

    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    public void setCaseService(CaseService caseService) {
        this.caseService = caseService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
        String caseId = templateArgs.get ("caseId");
        NodeRef caseNodeRef;
        
        if(caseId == null)
            caseNodeRef = new NodeRef(req.getParameter("nodeRef"));
        else
            caseNodeRef = caseService.getCaseById(caseId);

        NodeInfoService.NodeInfo nodeInfo = nodeInfoService.getNodeInfo(caseNodeRef);
        List<QName> requiredProps = Arrays.asList(OpenESDHModel.PROP_OE_ID, ContentModel.PROP_TITLE,
                OpenESDHModel.ASSOC_CASE_OWNERS, OpenESDHModel.PROP_OE_STATUS,
                ContentModel.PROP_CREATOR, ContentModel.PROP_CREATED, ContentModel.PROP_MODIFIED,
                ContentModel.PROP_MODIFIER, ContentModel.PROP_DESCRIPTION,
                OpenESDHModel.PROP_OE_JOURNALKEY, OpenESDHModel.PROP_OE_JOURNALFACET, OpenESDHModel.PROP_OE_LOCKED_BY,
                OpenESDHModel.PROP_OE_LOCKED_DATE, OpenESDHModel.PROP_CASE_STARTDATE
        );

        JSONObject json = nodeInfoService.getSelectedProperties(nodeInfo, requiredProps);
        String user = AuthenticationUtil.getFullyAuthenticatedUser();

        res.setContentEncoding(WebScriptUtils.CONTENT_ENCODING_UTF_8);
        try {
            ((JSONObject) json.get("properties")).put("nodeRef", caseNodeRef.toString());
            json.put("allProps", nodeInfoService.buildJSON(nodeInfo));
            json.put("isLocked", caseService.isLocked(caseNodeRef));
            json.put("statusChoices", caseService.getValidNextStatuses(caseNodeRef));
            json.write(res.getWriter());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


}