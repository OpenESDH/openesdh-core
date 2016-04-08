package dk.openesdh.repo.webscripts.documents;

import java.io.IOException;

import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.github.dynamicextensionsalfresco.webscripts.annotations.Attribute;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Authentication;
import com.github.dynamicextensionsalfresco.webscripts.annotations.AuthenticationType;
import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.RequestParam;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import com.github.dynamicextensionsalfresco.webscripts.resolutions.Resolution;

import dk.openesdh.repo.model.ClassifValue;
import dk.openesdh.repo.services.classification.ClassificatorManagementService;
import dk.openesdh.repo.webscripts.classification.ClassificatorValuesWebScript;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

@Component
@WebScript(description = "Manage document categories", families = {"Case Document Tools"})
public class DocumentCategoriesWebScript extends ClassificatorValuesWebScript {

    @Autowired
    @Qualifier("DocumentCategoryService")
    private ClassificatorManagementService documentCategoryService;

    @Uri(value = "/api/openesdh/document/categories", method = HttpMethod.GET, defaultFormat = "json")
    public Resolution getAllCategories() throws IOException, JSONException {
        return WebScriptUtils.jsonResolution(documentCategoryService.getClassifValues());
    }

    @Authentication(AuthenticationType.ADMIN)
    @Uri(value = "/api/openesdh/document/category?nodeRefId=", method = HttpMethod.POST, defaultFormat = "json")
    public Resolution post(@Attribute("classifValue") ClassifValue documentCategory)
            throws IOException, JSONException {
        ClassifValue savedDocumentCategory = documentCategoryService.createOrUpdateClassifValue(documentCategory);
        return WebScriptUtils.jsonResolution(savedDocumentCategory);
    }

    @Authentication(AuthenticationType.ADMIN)
    @Uri(value = "/api/openesdh/document/category?nodeRefId=", method = HttpMethod.DELETE, defaultFormat = "json")
    public Resolution delete(@RequestParam(required = true) final NodeRef nodeRefId) throws IOException, JSONException {
        documentCategoryService.deleteClassifValue(nodeRefId);
        return WebScriptUtils.jsonResolution("Deleted succesfully");
    }
}
