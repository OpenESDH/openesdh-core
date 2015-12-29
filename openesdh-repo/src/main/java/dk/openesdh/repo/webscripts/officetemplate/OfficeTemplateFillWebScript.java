package dk.openesdh.repo.webscripts.officetemplate;

import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.UriVariable;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

import dk.openesdh.repo.services.officetemplate.OfficeTemplate;
import dk.openesdh.repo.services.officetemplate.OfficeTemplateService;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

@Component
@WebScript(description = "Fill the specified office template, and return a transformed document.", families = {"OpenESDH Office Template"})
public class OfficeTemplateFillWebScript {

    @Autowired
    private OfficeTemplateService officeTemplateService;

    @Uri(value = "/api/openesdh/officetemplates/{store_type}/{store_id}/{node_id}/fill", method = HttpMethod.POST, defaultFormat = "json")
    public void post(
            @UriVariable final String store_type,
            @UriVariable final String store_id,
            @UriVariable final String node_id,
            WebScriptRequest req, WebScriptResponse res
    ) throws IOException, JSONException {
        NodeRef nodeRef = new NodeRef(store_type, store_id, node_id);
        // Check if the template exists
        try {
            OfficeTemplate template = officeTemplateService.getTemplate(nodeRef);
            if (template == null) {
                throw new WebScriptException(Status.STATUS_NOT_FOUND, "Template not found");
            }
        } catch (Exception e) {
            throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR, "Error retrieving template", e);
        }

        try {
            Map<String, Serializable> model = new HashMap<>();
            JSONObject json = WebScriptUtils.readJson(req);
            JSONObject fieldData = (JSONObject) json.get("fieldData");
            if (fieldData != null) {
                // Add the user input to the template model
                fieldData.forEach((k, v) -> {
                    model.put((String) k, (Serializable) v);
                });
            }

            ContentReader reader = officeTemplateService.renderTemplate(nodeRef, model);
            res.setContentType(reader.getMimetype());
            reader.getContent(res.getOutputStream());
        } catch (Exception e) {
            throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR, "Error filling template", e);
        }
    }
}
