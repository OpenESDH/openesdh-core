package dk.openesdh.doctemplates.webscripts.officetemplate;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.json.simple.JSONObject;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.UriVariable;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;

import dk.openesdh.doctemplates.services.officetemplate.OfficeTemplateMerged;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

@Component
@WebScript(description = "Fill the specified office template, and send it to receivers", families = {"OpenESDH Office Template"})
public class OfficeTemplateFillToEmailWebScript extends OfficeTemplateFillWebScript {

    @Uri(value = "/api/openesdh/template/{store_type}/{store_id}/{node_id}/case/{caseId}/fillToEmail", method = HttpMethod.POST, defaultFormat = "json")
    public void fillToEmail(
            @UriVariable final String store_type,
            @UriVariable final String store_id,
            @UriVariable final String node_id,
            @UriVariable final String caseId,
            WebScriptRequest req, WebScriptResponse res
    ) throws Exception {
        JSONObject json = WebScriptUtils.readJson(req);
        List<OfficeTemplateMerged> merged = getMergedTemplates(
                new NodeRef(store_type, store_id, node_id),
                caseId,
                json);
        JSONObject fieldData = (JSONObject) json.get("fieldData");
        String subject = (String) fieldData.get("email.subject");
        String message = (String) fieldData.get("email.message");
        officeTemplateService.sendToEmail(caseId, merged, subject, message);
    }
}
