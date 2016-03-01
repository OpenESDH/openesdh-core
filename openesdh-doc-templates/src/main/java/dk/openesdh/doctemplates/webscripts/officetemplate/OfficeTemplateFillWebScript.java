package dk.openesdh.doctemplates.webscripts.officetemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.NodeRef;
import org.json.simple.JSONArray;
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

import dk.openesdh.doctemplates.services.officetemplate.OfficeTemplate;
import dk.openesdh.doctemplates.services.officetemplate.OfficeTemplateMerged;
import dk.openesdh.doctemplates.services.officetemplate.OfficeTemplateService;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

@Component
@WebScript(description = "Fill the specified office template, and return a transformed document.", families = {"OpenESDH Office Template"})
public class OfficeTemplateFillWebScript {

    @Autowired
    @Qualifier("OfficeTemplateService")
    private OfficeTemplateService officeTemplateService;

    @Uri(value = "/api/openesdh/template/{store_type}/{store_id}/{node_id}/case/{caseId}/fill", method = HttpMethod.POST, defaultFormat = "json")
    public void fill(
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
        if (merged.size() == 1) {
            writeSingleFileToResponse(res, merged.get(0));
        } else {
            writeMultiFilesToZipResponse(res, merged);
        }
    }

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

    @SuppressWarnings("unchecked")
    private List<OfficeTemplateMerged> getMergedTemplates(NodeRef templateNodeRef, String caseId, JSONObject json) throws Exception {
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

    private void writeSingleFileToResponse(WebScriptResponse res, OfficeTemplateMerged merged) throws IOException {
        res.setContentType(merged.getMimetype());
        res.setContentEncoding(WebScriptUtils.CONTENT_ENCODING_UTF_8);
        res.getOutputStream().write(merged.getContent());
    }

    private void writeMultiFilesToZipResponse(WebScriptResponse res, List<OfficeTemplateMerged> merged) throws Exception {
        res.setContentType(MimetypeMap.MIMETYPE_ZIP);
        res.setContentEncoding(WebScriptUtils.CONTENT_ENCODING_UTF_8);
        res.setHeader("Content-Disposition", "attachment;filename=merged_templates.zip");

        try (ZipOutputStream zos = new ZipOutputStream(res.getOutputStream())) {
            for (OfficeTemplateMerged file : merged) {
                zos.putNextEntry(new ZipEntry(file.getFileName()));
                zos.write(file.getContent());
            }
        }
    }
}
