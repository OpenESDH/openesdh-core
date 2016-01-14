package dk.openesdh.repo.webscripts.officetemplate;

import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.UriVariable;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import com.github.dynamicextensionsalfresco.webscripts.resolutions.Resolution;

import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

import dk.openesdh.repo.services.officetemplate.OfficeTemplate;
import dk.openesdh.repo.services.officetemplate.OfficeTemplateService;
import dk.openesdh.repo.webscripts.AbstractRESTWebscript;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

@Component
@WebScript(description = "Retrieve office templates list or specifictemplate.", families = {"OpenESDH Office Template"})
public class OfficeTemplateGetWebScript extends AbstractRESTWebscript {

    @Autowired
    private OfficeTemplateService officeTemplateService;

    @Uri(value = "/api/openesdh/officetemplates/{store_type}/{store_id}/{node_id}", method = HttpMethod.GET, defaultFormat = "json")
    public Resolution get(
            @UriVariable final String store_type,
            @UriVariable final String store_id,
            @UriVariable final String node_id,
            WebScriptRequest req, WebScriptResponse res) {
        NodeRef nodeRef = new NodeRef(store_type, store_id, node_id);
        OfficeTemplate template = null;
        try {
            template = officeTemplateService.getTemplate(nodeRef);
        } catch (Exception e) {
            throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR, "Error retrieving template", e);
        }
        if (template == null) {
            throw new WebScriptException(Status.STATUS_NOT_FOUND, "Template not found");
        }
        return WebScriptUtils.jsonResolution(template);
    }

    @Uri(value = "/api/openesdh/officetemplates", method = HttpMethod.GET, defaultFormat = "json")
    public Resolution getList() {
        return WebScriptUtils.jsonResolution(officeTemplateService.getTemplates());
    }
}
