package dk.openesdh.doctemplates.webscripts.officetemplate;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import dk.openesdh.doctemplates.services.officetemplate.OfficeTemplate;
import dk.openesdh.doctemplates.services.officetemplate.OfficeTemplateMerged;
import dk.openesdh.doctemplates.services.officetemplate.OfficeTemplateService;

public abstract class OfficeTemplateFillWebScript {

    @Autowired
    @Qualifier("OfficeTemplateService")
    protected OfficeTemplateService officeTemplateService;

    @SuppressWarnings("unchecked")
    protected List<OfficeTemplateMerged> getMergedTemplates(NodeRef templateNodeRef, String caseId, JSONObject json) throws Exception {
        OfficeTemplate template = officeTemplateService.getTemplate(templateNodeRef, true, false);
        JSONObject fieldData = (JSONObject) json.get("fieldData");
        JSONArray receivers = (JSONArray) fieldData.get("receivers");
        List<OfficeTemplateMerged> merged = new ArrayList<>();
        for (Object r : receivers) {
            JSONObject receiver = (JSONObject) r;
            merged.add(officeTemplateService.renderTemplate(
                    template,
                    caseId,
                    new NodeRef((String) receiver.get("nodeRef")),
                    fieldData));
        }
        return merged;
    }
}
