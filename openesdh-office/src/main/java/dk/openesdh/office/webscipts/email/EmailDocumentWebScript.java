package dk.openesdh.office.webscipts.email;

import static dk.openesdh.repo.webscripts.utils.WebScriptUtils.jsonResolution;

import org.alfresco.service.cmr.repository.NodeRef;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import com.github.dynamicextensionsalfresco.webscripts.resolutions.Resolution;

import dk.openesdh.office.services.OfficeService;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

@Component
@WebScript(description = "Create Email Document", families = {"Outlook"})
public class EmailDocumentWebScript {

    private final Logger logger = LoggerFactory.getLogger(EmailDocumentWebScript.class);

    @Autowired
    @Qualifier("OfficeService")
    private OfficeService officeService;

    @Uri(value = "/dk-openesdh-case-email", method = HttpMethod.POST, defaultFormat = "json")
    @SuppressWarnings("unchecked")
    public Resolution createEmailDocument(WebScriptRequest req, WebScriptResponse resp) {
        logger.debug("Saving from email");
        JSONObject json = WebScriptUtils.readJson(req);
        logger.debug("json: {}", json);
        String caseId = (String) json.get("caseId");
        String name = (String) json.get("name");
        JSONObject email = (JSONObject) json.get("email");
        String bodyText = (String) email.get("BodyText");
        NodeRef documentFolder = officeService.createEmailDocument(caseId, name, bodyText);
        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("nodeRef", documentFolder.toString());
        return jsonResolution(jsonResponse);
    }
}
