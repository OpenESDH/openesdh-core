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
@WebScript(description = "Manage document types", families = {"Case Document Tools"})
public class DocumentTypesWebScript extends ClassificatorValuesWebScript {

    @Autowired
    @Qualifier("DocumentTypeService")
    private ClassificatorManagementService documentTypeService;

    @Uri(value = "/api/openesdh/document/types", method = HttpMethod.GET, defaultFormat = "json")
    public Resolution getAllTypes() throws IOException, JSONException {
        return WebScriptUtils.jsonResolution(documentTypeService.getClassifValues());
    }

    @Authentication(AuthenticationType.ADMIN)
    @Uri(value = "/api/openesdh/document/type", method = HttpMethod.POST, defaultFormat = "json")
    public Resolution post(@Attribute("classifValue") ClassifValue documentType) throws IOException, JSONException {
        ClassifValue savedDocumentType = documentTypeService.createOrUpdateClassifValue(documentType);
        return WebScriptUtils.jsonResolution(savedDocumentType.toJSONObject());
    }

    @Authentication(AuthenticationType.ADMIN)
    @Uri(value = "/api/openesdh/document/type?nodeRefId=", method = HttpMethod.DELETE, defaultFormat = "json")
    public Resolution delete(@RequestParam(required = true) final NodeRef nodeRefId) throws IOException, JSONException {
        documentTypeService.deleteClassifValue(nodeRefId);
        return WebScriptUtils.jsonResolution("Deleted succesfully");
    }
}
