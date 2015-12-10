package dk.openesdh.repo.webscripts.cases;

import dk.openesdh.repo.model.DocumentCategory;
import dk.openesdh.repo.model.DocumentType;
import dk.openesdh.repo.services.NodeInfoService;
import dk.openesdh.repo.services.documents.DocumentService;
import dk.openesdh.repo.services.lock.OELockService;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PersonService.PersonInfo;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang3.StringUtils;
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
    private OELockService oeLockService;

    public void setNodeInfoService(NodeInfoService nodeInfoService) {
        this.nodeInfoService = nodeInfoService;
    }

    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }

    public void setOeLockService(OELockService oeLockService) {
        this.oeLockService = oeLockService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
        NodeRef documentNodeRef = new NodeRef(templateArgs.get("store_type"), templateArgs.get("store_id"), templateArgs.get("id"));
        NodeRef mainDocNodeRef = documentService.getMainDocument(documentNodeRef);

        PersonInfo docOwner = documentService.getDocumentOwner(documentNodeRef);
        NodeInfoService.NodeInfo documentNodeInfo = nodeInfoService.getNodeInfo(documentNodeRef);
        NodeInfoService.NodeInfo mainDocNodeInfo = nodeInfoService.getNodeInfo(mainDocNodeRef);

        DocumentType documentType = documentService.getDocumentType(documentNodeRef);
        DocumentCategory documentCategory = documentService.getDocumentCategory(documentNodeRef);

        JSONObject result = new JSONObject();
        try {
            result.put("typeId", documentType.getNodeRef().toString());
            result.put("typeName", documentType.getName());
            result.put("typeDisplayName", documentType.getDisplayName());

            result.put("categoryId", documentCategory.getNodeRef().toString());
            result.put("categoryName", documentCategory.getName());
            result.put("categoryDisplayName", documentCategory.getDisplayName());
            result.put("owner", docOwner.getFirstName() + " " + docOwner.getLastName());
            result.put("mainDocNodeRef", mainDocNodeRef.toString());
            result.put("description", StringUtils.defaultIfEmpty((String) mainDocNodeInfo.properties.get(ContentModel.PROP_DESCRIPTION), ""));
            result.put("statusChoices", documentService.getValidNextStatuses(documentNodeRef));
            result.put("isLocked", oeLockService.isLocked(documentNodeRef));

            addAllProperties(result, documentNodeInfo.properties);

            res.setContentEncoding(WebScriptUtils.CONTENT_ENCODING_UTF_8);
            result.write(res.getWriter());
        } catch (JSONException jse) {
            throw new WebScriptException("Error when retrieving document details: " + jse.getMessage());
        }
    }

    private void addAllProperties(JSONObject result, Map<QName, Serializable> properties) {
        properties.forEach((name, value) -> {
            try {
                result.put(name.getLocalName(), value instanceof Date ? ((Date) value).getTime() : value);
            } catch (JSONException skipExceptions) {
            }
        });
    }
}
