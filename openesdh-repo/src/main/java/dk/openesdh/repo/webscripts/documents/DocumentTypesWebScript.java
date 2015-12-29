package dk.openesdh.repo.webscripts.documents;

import com.github.dynamicextensionsalfresco.webscripts.annotations.Authentication;
import com.github.dynamicextensionsalfresco.webscripts.annotations.AuthenticationType;
import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.RequestParam;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import com.github.dynamicextensionsalfresco.webscripts.resolutions.Resolution;

import java.io.IOException;
import java.util.stream.Collectors;

import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.stereotype.Component;

import dk.openesdh.repo.model.DocumentType;
import dk.openesdh.repo.services.documents.DocumentTypeService;
import dk.openesdh.repo.services.system.MultiLanguageValue;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

@Component
@WebScript(description = "Manage document types", families = {"Case Document Tools"})
public class DocumentTypesWebScript {

    @Autowired
    private DocumentTypeService documentTypeService;

    @Authentication(AuthenticationType.ADMIN)
    @Uri(value = "/api/openesdh/document/type?nodeRefId=", method = HttpMethod.GET, defaultFormat = "json")
    public Resolution get(@RequestParam(required = true) final NodeRef nodeRefId) throws JSONException {
        DocumentType documentType = documentTypeService.getDocumentType(nodeRefId);
        if (documentType == null) {
            throw new WebScriptException("Document type not found");
        }
        return WebScriptUtils.jsonResolution(createJSONObjectWithMultilanguage(documentType));
    }

    @Uri(value = "/api/openesdh/document/types", method = HttpMethod.GET, defaultFormat = "json")
    public Resolution getAllTypes() throws IOException, JSONException {
        return WebScriptUtils.jsonResolution(
                new JSONArray(documentTypeService.getDocumentTypes().stream()
                        .map(DocumentType::toJSONObject)
                        .collect(Collectors.toList())
                ));
    }

    private JSONObject createJSONObjectWithMultilanguage(DocumentType type) throws JSONException {
        JSONObject json = type.toJSONObject();
        json.put("mlDisplayNames", documentTypeService.getMultiLanguageDisplayNames(type.getNodeRef()).toJSONArray());
        return json;
    }

    @Authentication(AuthenticationType.ADMIN)
    @Uri(value = "/api/openesdh/document/type?nodeRefId=", method = HttpMethod.POST, defaultFormat = "json")
    public Resolution post(
            @RequestParam(required = false) final NodeRef nodeRefId,
            @RequestParam(required = true) final String name,
            @RequestParam(required = true) final String mlDisplayNames
    ) throws IOException, JSONException {
        DocumentType savedDocumentType = createOrUpdateDocumentType(nodeRefId, name, mlDisplayNames);
        return WebScriptUtils.jsonResolution(savedDocumentType.toJSONObject());
    }

    @Authentication(AuthenticationType.ADMIN)
    @Uri(value = "/api/openesdh/document/type?nodeRefId=", method = HttpMethod.DELETE, defaultFormat = "json")
    public Resolution delete(@RequestParam(required = true) final NodeRef nodeRefId) throws IOException, JSONException {
        DocumentType type = new DocumentType();
        type.setNodeRef(nodeRefId);
        documentTypeService.deleteDocumentType(type);
        return WebScriptUtils.jsonResolution("Deleted succesfully");
    }

    private DocumentType createOrUpdateDocumentType(NodeRef nodeRef, String name, String mlDisplayNames) throws JSONException {
        DocumentType type = new DocumentType();
        type.setNodeRef(nodeRef);
        type.setName(name);
        return documentTypeService.createOrUpdateDocumentType(
                type,
                MultiLanguageValue.createFromJSONString(mlDisplayNames));
    }
}
