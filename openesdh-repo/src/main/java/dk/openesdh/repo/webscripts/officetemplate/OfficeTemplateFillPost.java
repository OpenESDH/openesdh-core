package dk.openesdh.repo.webscripts.officetemplate;

import dk.openesdh.repo.services.officetemplate.OfficeTemplate;
import dk.openesdh.repo.services.officetemplate.OfficeTemplateService;
import dk.openesdh.repo.webscripts.AbstractRESTWebscript;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class OfficeTemplateFillPost extends AbstractRESTWebscript {
    private static Logger LOGGER = Logger.getLogger(OfficeTemplateFillPost.class);
    private OfficeTemplateService officeTemplateService;

    public void setOfficeTemplateService(OfficeTemplateService officeTemplateService) {
        this.officeTemplateService = officeTemplateService;
    }

    @Override
    protected void post(NodeRef nodeRef, WebScriptRequest req, WebScriptResponse res) throws IOException, JSONException {
        if (nodeRef != null) {
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

    /**
     * Fetch an individual property's value.
     * @param infoJson
     * @param propertyName
     * @return
     */
    private Serializable getPropertyValue(String propertyName, org.json.JSONObject infoJson) {
        try {
            org.json.JSONObject propertyObj = infoJson.getJSONObject(propertyName);
            try {
                Object displayValue = propertyObj.get("displayValue");
                return (Serializable) displayValue;
            } catch (JSONException e) {
                try {
                    return (Serializable) propertyObj.get("value");
                } catch (JSONException e1) {
                    return null;
                }
            }
        } catch (JSONException e) {
            return null;
        }
    }
}
