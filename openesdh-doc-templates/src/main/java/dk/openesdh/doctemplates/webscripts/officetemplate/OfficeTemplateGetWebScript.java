package dk.openesdh.doctemplates.webscripts.officetemplate;

import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.UriVariable;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import com.github.dynamicextensionsalfresco.webscripts.resolutions.Resolution;

import dk.openesdh.doctemplates.services.officetemplate.OfficeTemplateService;
import dk.openesdh.repo.webscripts.AbstractRESTWebscript;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

@Component
@WebScript(description = "Retrieve office templates list or specifictemplate.", families = {"OpenESDH Office Template"})
public class OfficeTemplateGetWebScript extends AbstractRESTWebscript {

    @Autowired
    @Qualifier("OfficeTemplateService")
    private OfficeTemplateService officeTemplateService;

    @Uri(value = "/api/openesdh/officetemplates/{store_type}/{store_id}/{node_id}", method = HttpMethod.GET, defaultFormat = "json")
    public Resolution get(
            @UriVariable final String store_type,
            @UriVariable final String store_id,
            @UriVariable final String node_id) {
        NodeRef nodeRef = new NodeRef(store_type, store_id, node_id);
        return WebScriptUtils.jsonResolution(officeTemplateService.getTemplate(nodeRef));
    }

    @Uri(value = "/api/openesdh/officetemplates", method = HttpMethod.GET, defaultFormat = "json")
    public Resolution getList() {
        return WebScriptUtils.jsonResolution(officeTemplateService.getTemplates());
    }
}
