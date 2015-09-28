package dk.openesdh.repo.webscripts.cases;

import dk.openesdh.repo.model.DocumentType;
import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.NodeInfoService;
import dk.openesdh.repo.services.documents.DocumentService;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PersonService.PersonInfo;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * @author Lanre Abiwon
 */
public class DocumentRecordInfo extends AbstractWebScript {

    private NodeInfoService nodeInfoService;
    private DocumentService documentService;

    public void setNodeInfoService(NodeInfoService nodeInfoService) {
        this.nodeInfoService = nodeInfoService;
    }

    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
        NodeRef documentNodeRef = new NodeRef(templateArgs.get("store_type"), templateArgs.get("store_id"), templateArgs.get("id"));
        NodeInfoService.NodeInfo documentNodeInfo = nodeInfoService.getNodeInfo(documentNodeRef);

        PersonInfo docOwner = documentService.getDocumentOwner(documentNodeRef);
        NodeRef mainDocNodeRef = documentService.getMainDocument(documentNodeRef);
        DocumentType documentType = documentService.getDocumentType(documentNodeRef);

        JSONObject result = new JSONObject();
        try {
            result.put("typeId", documentType.getNodeRef().toString());
            result.put("typeName", documentType.getDisplayName());
            result.put("typeDisplayName", documentType.getDisplayName());
//            result.put("typeChoices", getDocumentTypeChoices());

            result.put("category", documentNodeInfo.properties.get(OpenESDHModel.PROP_DOC_CATEGORY));
            result.put("state", documentNodeInfo.properties.get(OpenESDHModel.PROP_DOC_STATE));
            result.put("status", documentNodeInfo.properties.get(OpenESDHModel.PROP_OE_STATUS));
            result.put("title", documentNodeInfo.properties.get(ContentModel.PROP_TITLE));

            Date created = (Date) documentNodeInfo.properties.get(ContentModel.PROP_CREATED);
            // Returning date in Long format to make it parsable
            result.put("created", created.getTime());

            result.put("owner", docOwner.getFirstName() + " " + docOwner.getLastName());
            result.put("mainDocNodeRef", mainDocNodeRef.toString());
            result.put("statusChoices", documentService.getValidNextStatuses(documentNodeRef));
//            result.put("caseId", documentNodeInfo.properties.get(OpenESDHModel.PROP_OE_CASE_ID));

            result.write(res.getWriter());
        } catch (JSONException jse) {
            throw new WebScriptException("Error when retrieving document details: " + jse.getMessage());
        }
    }
}
