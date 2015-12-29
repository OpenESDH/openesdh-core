package dk.openesdh.repo.webscripts.cases;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.stereotype.Component;

import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.UriVariable;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import com.github.dynamicextensionsalfresco.webscripts.resolutions.Resolution;

import dk.openesdh.repo.model.DocumentCategory;
import dk.openesdh.repo.model.DocumentType;
import dk.openesdh.repo.services.NodeInfoService;
import dk.openesdh.repo.services.documents.DocumentService;
import dk.openesdh.repo.services.lock.OELockService;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

/**
 * @author Lanre Abiwon
 */
@Component
@WebScript(description = "Retrieve metadata about a document", families = "Case Documents")
public class DocumentRecordInfoWebScript {

    @Autowired
    @Qualifier("NodeInfoService")
    private NodeInfoService nodeInfoService;
    @Autowired
    @Qualifier("DocumentService")
    private DocumentService documentService;
    @Autowired
    @Qualifier("OELockService")
    private OELockService oeLockService;

    @Uri(value = "/api/openesdh/documentInfo/{store_type}/{store_id}/{id}", defaultFormat = "json")
    public Resolution getDocumentRecordInfo(
            @UriVariable("store_type") String storeType,
            @UriVariable("store_id") String storeId, 
            @UriVariable("id") String id) throws IOException {
        NodeRef documentNodeRef = new NodeRef(storeType, storeId, id);
        NodeRef mainDocNodeRef = documentService.getMainDocument(documentNodeRef);

        String editOnlinePath = documentService.getDocumentEditOnlinePath(mainDocNodeRef);

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
            result.put("isLocked", oeLockService.isLocked(mainDocNodeRef));
            result.put("editLockState", documentService.getDocumentEditLockState(mainDocNodeRef));
            result.put("editOnlinePath", editOnlinePath);

            addAllProperties(result, documentNodeInfo.properties);

            return WebScriptUtils.jsonResolution(result);
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

