package dk.openesdh.repo.webscripts.documents;

import dk.openesdh.repo.services.cases.CaseService;
import dk.openesdh.repo.services.documents.DocumentService;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lanre on 04/02/2015.
 */
public class IsCaseDocument extends AbstractWebScript {
    private static Log logger = LogFactory.getLog(DocumentCaseContainers.class);
    private CaseService caseService;
    private DocumentService documentService;

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
        String storeType = templateArgs.get("store_type");
        String storeId = templateArgs.get("store_id");
        String id = templateArgs.get("id");
        NodeRef documentNode = new NodeRef(storeType, storeId, id);

        JSONObject json = new JSONObject();
        try{

            NodeRef caseNodeRef = caseService.getParentCase(documentNode);
            String caseId =  caseService.getCaseId(caseNodeRef);

            if (caseNodeRef != null ){
                json.put("isCaseDoc", true);
                json.put("caseId", caseId);
                json.write(res.getWriter());
            }
            else {
                json.put("isCaseDoc", false);
                json.write(res.getWriter());
            }
        }
        catch (InvalidNodeRefException inre) {
            logger.error("The invalid nodeRef exception: "+ inre.getMessage());
        }
        catch (JSONException jse){
            logger.error("Unable to build teh json model because of the following exception: "+ jse.getMessage());
        }



    }

    public void setCaseService(CaseService caseService) {
        this.caseService = caseService;
    }

    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }

}
