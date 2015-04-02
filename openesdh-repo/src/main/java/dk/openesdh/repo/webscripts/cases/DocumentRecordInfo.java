package dk.openesdh.repo.webscripts.cases;

import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.NodeInfoService;
import dk.openesdh.repo.services.cases.CaseService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.util.Map;

/**
 * @author Lanre Abiwon
 */
public class DocumentRecordInfo extends AbstractWebScript {

    private NodeInfoService nodeInfoService;

    public void setNodeInfoService(NodeInfoService nodeInfoService) {
        this.nodeInfoService = nodeInfoService;
    }


    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
        String nodeRefStr = templateArgs.get("store_type") +"://" + templateArgs.get("store_id")+"/"+templateArgs.get("id");
        NodeRef documentNodeRef = new NodeRef(nodeRefStr);
        NodeInfoService.NodeInfo documentNodeInfo = nodeInfoService.getNodeInfo(documentNodeRef);

        JSONObject result = new JSONObject();
        try {
            result.put("type", documentNodeInfo.properties.get(OpenESDHModel.PROP_DOC_TYPE));
            result.put("category", documentNodeInfo.properties.get(OpenESDHModel.PROP_DOC_CATEGORY));
            result.put("state", documentNodeInfo.properties.get(OpenESDHModel.PROP_DOC_STATE));
//            result.put("caseId", documentNodeInfo.properties.get(OpenESDHModel.PROP_OE_CASE_ID));

            result.write(res.getWriter());
        }
        catch (JSONException jse){
            throw new WebScriptException("Error when retrieving document details: "+ jse.getMessage());
        }


    }


}
