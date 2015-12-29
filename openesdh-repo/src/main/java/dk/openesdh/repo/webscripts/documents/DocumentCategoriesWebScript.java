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

import dk.openesdh.repo.model.DocumentCategory;
import dk.openesdh.repo.services.documents.DocumentCategoryService;
import dk.openesdh.repo.services.system.MultiLanguageValue;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

@Component
@WebScript(description = "Manage document categories", families = {"Case Document Tools"})
public class DocumentCategoriesWebScript {

    @Autowired
    private DocumentCategoryService documentCategoryService;

    @Authentication(AuthenticationType.ADMIN)
    @Uri(value = "/api/openesdh/document/category?nodeRefId=", method = HttpMethod.GET, defaultFormat = "json")
    public Resolution get(@RequestParam(required = true) final NodeRef nodeRefId) throws JSONException {
        DocumentCategory documentCategory = documentCategoryService.getDocumentCategory(nodeRefId);
        if (documentCategory == null) {
            throw new WebScriptException("Document category not found");
        }
        return WebScriptUtils.jsonResolution(createJSONObjectWithMultilanguage(documentCategory));
    }

    @Uri(value = "/api/openesdh/document/categories", method = HttpMethod.GET, defaultFormat = "json")
    public Resolution getAllCategories() throws IOException, JSONException {
        return WebScriptUtils.jsonResolution(
                new JSONArray(documentCategoryService.getDocumentCategories().stream()
                        .map(DocumentCategory::toJSONObject)
                        .collect(Collectors.toList())
                ));
    }

    private JSONObject createJSONObjectWithMultilanguage(DocumentCategory documentCategory) throws JSONException {
        JSONObject json = documentCategory.toJSONObject();
        json.put("mlDisplayNames", documentCategoryService.getMultiLanguageDisplayNames(documentCategory.getNodeRef()).toJSONArray());
        return json;
    }

    @Authentication(AuthenticationType.ADMIN)
    @Uri(value = "/api/openesdh/document/category?nodeRefId=", method = HttpMethod.POST, defaultFormat = "json")
    public Resolution post(
            @RequestParam(required = false) final NodeRef nodeRefId,
            @RequestParam(required = true) final String name,
            @RequestParam(required = true) final String mlDisplayNames
    ) throws IOException, JSONException {
        DocumentCategory savedDocumentCategory = createOrUpdateDocumentCategory(nodeRefId, name, mlDisplayNames);
        return WebScriptUtils.jsonResolution(savedDocumentCategory.toJSONObject());
    }

    @Authentication(AuthenticationType.ADMIN)
    @Uri(value = "/api/openesdh/document/category?nodeRefId=", method = HttpMethod.DELETE, defaultFormat = "json")
    public Resolution delete(@RequestParam(required = true) final NodeRef nodeRefId) throws IOException, JSONException {
        DocumentCategory category = new DocumentCategory();
        category.setNodeRef(nodeRefId);
        documentCategoryService.deleteDocumentCategory(category);
        return WebScriptUtils.jsonResolution("Deleted succesfully");
    }

    private DocumentCategory createOrUpdateDocumentCategory(NodeRef nodeRef, String name, String mlDisplayNames) throws JSONException {
        DocumentCategory category = new DocumentCategory();
        category.setNodeRef(nodeRef);
        category.setName(name);
        return documentCategoryService.createOrUpdateDocumentCategory(
                category,
                MultiLanguageValue.createFromJSONString(mlDisplayNames));
    }
}
