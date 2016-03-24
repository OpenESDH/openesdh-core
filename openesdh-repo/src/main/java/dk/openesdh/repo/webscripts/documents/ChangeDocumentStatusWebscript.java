package dk.openesdh.repo.webscripts.documents;

import java.io.IOException;

import org.alfresco.service.cmr.repository.NodeRef;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.github.dynamicextensionsalfresco.webscripts.annotations.Attribute;
import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.UriVariable;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import com.github.dynamicextensionsalfresco.webscripts.resolutions.Resolution;

import dk.openesdh.repo.model.DocumentStatus;
import dk.openesdh.repo.services.documents.DocumentService;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

@Component
@WebScript(description = "Change document status", families = "Case Document Tools")
public class ChangeDocumentStatusWebscript {

    @Autowired
    @Qualifier(DocumentService.BEAN_ID)
    private DocumentService documentService;

    @Attribute
    protected DocumentStatus getStatus(WebScriptRequest req) throws IOException {
        JSONObject json = WebScriptUtils.readJson(req);
        String status = (String) json.get("status");
        return StringUtils.isEmpty(status)
                ? null
                : DocumentStatus.valueOf(status.toUpperCase());
    }

    @Uri(value = "/api/openesdh/documents/{store_type}/{store_id}/{id}/status", method = HttpMethod.POST, defaultFormat = "json")
    public Resolution execute(
            @UriVariable("store_type") final String storeType,
            @UriVariable("store_id") final String storeId,
            @UriVariable("id") final String id,
            @Attribute(required = true) DocumentStatus documentStatus
    ) throws Exception {
        NodeRef nodeRef = new NodeRef(storeType, storeId, id);
        documentService.changeNodeStatus(nodeRef, documentStatus);
        return WebScriptUtils.respondSuccess("The document status has been changed");
    }

}
