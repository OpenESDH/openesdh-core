package dk.openesdh.repo.webscripts.cases;

import static dk.openesdh.repo.model.CaseDocumentJson.CATEGORY_DISPLAY_NAME;
import static dk.openesdh.repo.model.CaseDocumentJson.CATEGORY_ID;
import static dk.openesdh.repo.model.CaseDocumentJson.CATEGORY_NAME;
import static dk.openesdh.repo.model.CaseDocumentJson.DESCRIPTION;
import static dk.openesdh.repo.model.CaseDocumentJson.EDIT_LOCK_STATE;
import static dk.openesdh.repo.model.CaseDocumentJson.EDIT_ONLINE_PATH;
import static dk.openesdh.repo.model.CaseDocumentJson.FILE_MIME_TYPE;
import static dk.openesdh.repo.model.CaseDocumentJson.IS_LOCKED;
import static dk.openesdh.repo.model.CaseDocumentJson.MAIN_DOC_NODE_REF;
import static dk.openesdh.repo.model.CaseDocumentJson.OWNER;
import static dk.openesdh.repo.model.CaseDocumentJson.STATUS_CHOICES;
import static dk.openesdh.repo.model.CaseDocumentJson.TYPE_DISPLAY_NAME;
import static dk.openesdh.repo.model.CaseDocumentJson.TYPE_ID;
import static dk.openesdh.repo.model.CaseDocumentJson.TYPE_NAME;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.domain.node.ContentDataWithId;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PersonService.PersonInfo;
import org.alfresco.service.namespace.NamespaceService;
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
import com.google.common.collect.Sets;

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

        DocumentType documentType = documentService.getDocumentType(documentNodeRef);
        DocumentCategory documentCategory = documentService.getDocumentCategory(documentNodeRef);

        JSONObject result = new JSONObject();
        try {
            result.put(TYPE_ID, documentType.getNodeRef().toString());
            result.put(TYPE_NAME, documentType.getName());
            result.put(TYPE_DISPLAY_NAME, documentType.getDisplayName());

            result.put(CATEGORY_ID, documentCategory.getNodeRef().toString());
            result.put(CATEGORY_NAME, documentCategory.getName());
            result.put(CATEGORY_DISPLAY_NAME, documentCategory.getDisplayName());
            result.put(OWNER, docOwner.getFirstName() + " " + docOwner.getLastName());
            result.put(MAIN_DOC_NODE_REF, mainDocNodeRef.toString());
            result.put(STATUS_CHOICES, documentService.getValidNextStatuses(documentNodeRef));
            result.put(IS_LOCKED, oeLockService.isLocked(mainDocNodeRef));
            result.put(EDIT_LOCK_STATE, documentService.getDocumentEditLockState(mainDocNodeRef));
            result.put(EDIT_ONLINE_PATH, editOnlinePath);

            addMainDocProperties(result, mainDocNodeRef);
            addAllProperties(result, documentNodeInfo.properties);

            return WebScriptUtils.jsonResolution(result);
        } catch (JSONException jse) {
            throw new WebScriptException("Error when retrieving document details: " + jse.getMessage());
        }
    }

    private void addAllProperties(JSONObject result, Map<QName, Serializable> properties) {
        properties.forEach((name, value) -> {
            try {
                result.put(name.getLocalName(), nodeInfoService.formatValue(name, value));
            } catch (JSONException skipExceptions) {
            }
        });
    }

    private static final Set<String> MAIN_DOC_SKIPPED_PROP_NS = Sets.newHashSet(
            NamespaceService.DICTIONARY_MODEL_1_0_URI,
            NamespaceService.SYSTEM_MODEL_1_0_URI,
            NamespaceService.CONTENT_MODEL_1_0_URI
    );

    private void addMainDocProperties(JSONObject result, NodeRef mainDocNodeRef) throws JSONException {
        NodeInfoService.NodeInfo mainDocNodeInfo = nodeInfoService.getNodeInfo(mainDocNodeRef);
        result.put(DESCRIPTION, StringUtils.defaultIfEmpty((String) mainDocNodeInfo.properties.get(ContentModel.PROP_DESCRIPTION), ""));
        result.put(FILE_MIME_TYPE, ((ContentDataWithId) mainDocNodeInfo.properties.get(ContentModel.PROP_CONTENT)).getMimetype());
        Map<QName, Serializable> mainDocProperties = mainDocNodeInfo.properties.entrySet()
                .stream()
                .filter(entry -> {
                    return !MAIN_DOC_SKIPPED_PROP_NS.contains(entry.getKey().getNamespaceURI());
                })
                .filter(entry -> entry.getValue() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        JSONObject mainDocPropsJSON = new JSONObject();
        addAllProperties(mainDocPropsJSON, mainDocProperties);
        if (mainDocPropsJSON.length() > 0) {
            result.put("mainDoc", mainDocPropsJSON);
        }

    }
}
