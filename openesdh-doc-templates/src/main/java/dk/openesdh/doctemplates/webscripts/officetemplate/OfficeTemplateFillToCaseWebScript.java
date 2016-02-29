package dk.openesdh.doctemplates.webscripts.officetemplate;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
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
@WebScript(description = "Fill the specified office template, and save it to case", families = {"OpenESDH Office Template"})
public class OfficeTemplateFillToCaseWebScript extends OfficeTemplateFillWebScript {

    @Uri(value = "/api/openesdh/template/{store_type}/{store_id}/{node_id}/case/{caseId}/fillToCase", method = HttpMethod.POST, defaultFormat = "json")
    public void fillToCase(
            @UriVariable final String store_type,
            @UriVariable final String store_id,
            @UriVariable final String node_id,
            @UriVariable final String caseId,
            WebScriptRequest req, WebScriptResponse res
    ) throws Exception {
        List<OfficeTemplateMerged> merged = getMergedTemplates(
                new NodeRef(store_type, store_id, node_id),
                caseId,
                WebScriptUtils.readJson(req));
        officeTemplateService.saveToCase(caseId, merged);
    }
}
