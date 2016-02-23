package dk.openesdh.doctemplates.webscripts.officetemplate;

import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.UriVariable;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;

import dk.openesdh.doctemplates.services.officetemplate.OfficeTemplateService;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

@Component
@WebScript(description = "Fill the specified office template, and return a transformed document.", families = {"OpenESDH Office Template"})
public class OfficeTemplateFillWebScript {

    @Autowired
    @Qualifier("OfficeTemplateService")
    private OfficeTemplateService officeTemplateService;

    @Uri(value = "/api/openesdh/officetemplates/{store_type}/{store_id}/{node_id}/fill", method = HttpMethod.POST, defaultFormat = "json")
    public void post(
            @UriVariable final String store_type,
            @UriVariable final String store_id,
            @UriVariable final String node_id,
            WebScriptRequest req, WebScriptResponse res
    ) throws Exception {
        NodeRef nodeRef = new NodeRef(store_type, store_id, node_id);
        //check if template exists:
        officeTemplateService.getTemplate(nodeRef);

        JSONObject json = WebScriptUtils.readJson(req);
        JSONObject fieldData = (JSONObject) json.get("fieldData");

        @SuppressWarnings("unchecked")
        ContentReader reader = officeTemplateService.renderTemplate(nodeRef, fieldData);

        res.setContentType(reader.getMimetype());
        reader.getContent(res.getOutputStream());
    }
}
