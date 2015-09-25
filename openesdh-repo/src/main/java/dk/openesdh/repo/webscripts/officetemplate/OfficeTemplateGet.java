package dk.openesdh.repo.webscripts.officetemplate;

import dk.openesdh.repo.services.officetemplate.OfficeTemplate;
import dk.openesdh.repo.services.officetemplate.OfficeTemplateService;
import dk.openesdh.repo.webscripts.AbstractRESTWebscript;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;
import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONException;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.util.List;

public class OfficeTemplateGet extends AbstractRESTWebscript {

    private OfficeTemplateService officeTemplateService;

    public void setOfficeTemplateService(OfficeTemplateService officeTemplateService) {
        this.officeTemplateService = officeTemplateService;
    }

    @Override
    protected void get(NodeRef nodeRef, WebScriptRequest req, WebScriptResponse res) throws IOException, JSONException {
        if (nodeRef == null) {
            // List
            List<OfficeTemplate> templates = officeTemplateService.getTemplates();
            res.setContentEncoding(WebScriptUtils.CONTENT_ENCODING_UTF_8);
            WebScriptUtils.writeJson(templates, res);
        } else {
            // Get one
            OfficeTemplate template = null;
            try {
                template = officeTemplateService.getTemplate(nodeRef);
            } catch (Exception e) {
                throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR, "Error retrieving template", e);
            }
            if (template == null) {
                throw new WebScriptException(Status.STATUS_NOT_FOUND, "Template not found");
            }
            res.setContentEncoding(WebScriptUtils.CONTENT_ENCODING_UTF_8);
            WebScriptUtils.writeJson(template, res);
        }
    }
}
