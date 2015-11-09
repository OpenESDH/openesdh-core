package dk.openesdh.repo.webscripts.officetemplate;

import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.RequestParam;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import com.github.dynamicextensionsalfresco.webscripts.resolutions.Resolution;
import dk.openesdh.repo.services.officetemplate.OfficeTemplateService;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;
import fr.opensagres.xdocreport.document.json.JSONObject;
import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.*;
import org.springframework.extensions.webscripts.servlet.FormData;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;

import static dk.openesdh.repo.webscripts.ParamUtils.getNodeRef;

/**
 * @author lanre.
 */

@Component
@WebScript(families = {"OpenESDH Office Template"}, description = "The upload webscript for document templates", defaultFormat = "json")
public class DocTemplate  {
    private static Logger logger = Logger.getLogger(DocTemplate.class);
    private static final long serialVersionUID = 1L;

    @Autowired
    private ServiceRegistry serviceRegistry;
    @Autowired
    private OfficeTemplateService officeTemplateService;

    @Uri(value = "/api/openesdh/officetemplate", method = HttpMethod.POST)
    public Resolution post (@RequestParam(required = false) WebScriptRequest req, WebScriptResponse res) throws IOException {

        System.out.println("For debugging purposes");
        /** Multi-part form data, if provided */
        final FormData formData = (FormData) req.parseContent();
//        final ResourceBundle resourceBundle =  getResources(); //TODO Need to use this to provide localized messages.
        if (formData == null || !formData.getIsMultiPart()) {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Something wrong with the form data. \nPlease contact administrator is problem persists");
        }
        boolean processed = false;
        int n = 1;
        FormData.FormField[] formfields = formData.getFields();
        NodeRef templatefileNodeRef = null;
        HashMap<String, Serializable> formDataMap = new HashMap<>();
        for (FormData.FormField field : formfields) {
            //Put every field into a hashMap. So that we can get what we want arbitrarily target any field we want without having to loop through teh array
            formDataMap.put(field.getName(), field.getValue());
            if (field.getIsFile()) {
                templatefileNodeRef = processUpload(field);
                processed = true;
            } else
                System.out.println("\n" + n + "Field name: " + field.getName() + " => " + field.getValue());
            n++;
        }
        if (!processed) {
            throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR, "Unable to process uploaded template file. Please consult you administrator");
        }
        if (!formDataMap.isEmpty() && templatefileNodeRef != null) {
            String title = formDataMap.get("title").toString();
            String description = formDataMap.get("description").toString();
            if (StringUtils.isNotBlank(title))
                serviceRegistry.getNodeService().setProperty(templatefileNodeRef, ContentModel.PROP_TITLE, title);
            else throw new WebScriptException(Status.STATUS_BAD_REQUEST, "The name of the template is required ");
            if (StringUtils.isNotBlank(description))
                serviceRegistry.getNodeService().setProperty(templatefileNodeRef, ContentModel.PROP_DESCRIPTION, formDataMap.get("description"));
        }
        JSONObject response = new JSONObject();
        response.put("message", "The the template was successfully uploaded.");
        return WebScriptUtils.jsonResolution(response);
    }

    @Uri(value = "/api/openesdh/officetemplate/{store_type}/{store_id}/{id}", method = HttpMethod.DELETE)
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
