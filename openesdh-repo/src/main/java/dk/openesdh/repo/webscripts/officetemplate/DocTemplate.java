package dk.openesdh.repo.webscripts.officetemplate;

import static dk.openesdh.repo.webscripts.ParamUtils.getNodeRef;

import java.io.IOException;
import java.io.InputStream;

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.servlet.FormData;
import org.springframework.extensions.webscripts.servlet.FormData.FormField;
import org.springframework.stereotype.Component;

import com.github.dynamicextensionsalfresco.webscripts.annotations.FileField;
import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.RequestParam;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import com.github.dynamicextensionsalfresco.webscripts.resolutions.Resolution;

import dk.openesdh.repo.services.officetemplate.OfficeTemplateService;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;
import fr.opensagres.xdocreport.document.json.JSONObject;

/**
 * @author lanre.
 */

@Component
@WebScript(families = {"OpenESDH Office Template"}, description = "The CRUD webscripts for  document templates")
public class DocTemplate  {
    private static Logger logger = Logger.getLogger(DocTemplate.class);
    private static final long serialVersionUID = 1L;

    @Autowired
    private ServiceRegistry serviceRegistry;
    @Autowired
    private OfficeTemplateService officeTemplateService;

    @Uri(value = "/api/openesdh/officeDocTemplate", method = HttpMethod.POST, defaultFormat = "json", multipartProcessing = true)
    public Resolution post(@RequestParam(value = "title", required = true) String title,
            @RequestParam(value = "description", required = false) String description,
            @FileField("filedata") FormField fileField) throws IOException {

        NodeRef templatefileNodeRef = processUpload(fileField);
        if (templatefileNodeRef != null) {
            if (StringUtils.isNotBlank(title)) {
                serviceRegistry.getNodeService().setProperty(templatefileNodeRef, ContentModel.PROP_TITLE, title);
            }
            if (StringUtils.isNotBlank(description)) {
                serviceRegistry.getNodeService().setProperty(templatefileNodeRef, ContentModel.PROP_DESCRIPTION,
                        description);
            }
        }
        JSONObject response = new JSONObject();
        response.put("message", "The the template was successfully uploaded.");
        return WebScriptUtils.jsonResolution(response);
    }

    @Uri(value = "/api/openesdh/officeDocTemplate/{store_type}/{store_id}/{id}", method = HttpMethod.DELETE, defaultFormat = "json")
    public Resolution delete(WebScriptRequest req, WebScriptResponse res) throws IOException {
        try {
            this.serviceRegistry.getNodeService().deleteNode(getNodeRef(req));
            JSONObject response = new JSONObject();
            response.put("message", "The template was successfully deleted.");
            return WebScriptUtils.jsonResolution(response);
        } catch (Exception ge) { //Any generic exception
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Issue deleting template: " + ge.getMessage());
        }
    }

    /**
     * Process the file data to create the nodeRef with the file contents in the template storage directory.
     * @param field
     * @return
     */
    protected NodeRef processUpload(FormData.FormField field) {
        NodeRef templateStorageDir = officeTemplateService.getTemplateDirectory();
        NodeRef fileNodeRef = null;
        try {
            String filename = field.getFilename();
            if (filename != null && filename.length() > 0) {
                InputStream is = field.getInputStream();
                FileInfo fileInfo = serviceRegistry.getFileFolderService().create(templateStorageDir, filename, ContentModel.TYPE_CONTENT);
                fileNodeRef = fileInfo.getNodeRef();
                ContentWriter writer = serviceRegistry.getContentService().getWriter(fileNodeRef, ContentModel.PROP_CONTENT, true);
                writer.setMimetype(field.getMimetype());
                writer.setEncoding("UTF-8");
                writer.putContent(is);
            }
            return fileNodeRef;
        } catch (Exception e) {
            throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR, "Error whilst processing uploaded file.\nCause:\n" + e.getMessage());
        }
    }
}
