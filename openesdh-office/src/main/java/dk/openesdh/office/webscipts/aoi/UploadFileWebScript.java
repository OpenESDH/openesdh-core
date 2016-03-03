package dk.openesdh.office.webscipts.aoi;

import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.*;
import org.springframework.extensions.webscripts.servlet.FormData;
import org.springframework.extensions.webscripts.servlet.FormData.FormField;
import org.springframework.stereotype.Component;

import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.documents.DocumentCategoryService;
import dk.openesdh.repo.services.documents.DocumentService;
import dk.openesdh.repo.services.documents.DocumentTypeService;

@Component
@WebScript(description = "Upload file to specified folder", families = {"AOI"})
public class UploadFileWebScript {

    private static final Log LOG = LogFactory.getLog(UploadFileWebScript.class);
    @Autowired
    private DocumentService documentService;
    @Autowired
    private DocumentTypeService documentTypeService;
    @Autowired
    private DocumentCategoryService documentCategoryService;

    @Uri(value = "/dk-openesdh-aoi-save", method = HttpMethod.POST, defaultFormat = "json")
    public void saveFile(WebScriptRequest webScriptRequest, WebScriptResponse webScriptResponse) throws IOException {
        LOG.debug("in UploadFile");
        final FormData form = (FormData) webScriptRequest.parseContent();
        if (form == null || !form.getIsMultiPart()) {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "");
        }
        boolean newFolder = false;
        String caseId = null;
        String metadata = null;
        InputStream fileInputStream = null;
        String filename = null;
        String documentName = null;
        String mimetype = null;
        NodeRef docType = null;
        NodeRef docCategory = null;
        for (FormField field : form.getFields()) {
            LOG.debug("field name: " + field.getName());
            switch (field.getName()) {
                case "metadata":
                    metadata = field.getValue();
                    break;
                case "filedata":
                    fileInputStream = field.getInputStream();
                    filename = field.getFilename();
                    mimetype = field.getMimetype();
                    break;
            }
            if (field.getIsFile()) {
                LOG.debug("Filename: " + filename + ", mimetype: " + mimetype);
            }
        }

        ParameterCheck.mandatory("filename", filename);

        NodeRef folderRef;
        try {
            JSONObject md = new JSONObject(metadata);
            newFolder = md.has("newFolder") && md.getBoolean("newFolder");
            caseId = md.getString("caseId");
            folderRef = md.has("nodeRef") ? new NodeRef(md.getString("nodeRef")) : null;
            documentName = md.has("documentName") ? md.getString("documentName") : filename;
            docType = md.has("docType") ? new NodeRef(md.getString("docType")) : getDocumentTypeLetter();
            docCategory = md.has("docCategory") ? new NodeRef(md.getString("docCategory")) : getDocumentCategoryOther();
        } catch (JSONException e) {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, e.getMessage());
        }

        LOG.debug("saving file: " + filename);
        if (newFolder) {
            documentService.createCaseDocument(caseId, documentName, filename, docType, docCategory, streamWriter(mimetype, fileInputStream));
            return;
        }
        documentService.createDocumentFile(folderRef, filename, filename, docType, docCategory, streamWriter(mimetype, fileInputStream));
    }

    private Consumer<ContentWriter> streamWriter(String mimetype, InputStream fileInputStream) {
        return writer -> {
            writer.setMimetype(mimetype);
            writer.putContent(fileInputStream);
        };
    }

    private NodeRef getDocumentTypeLetter() {
        return documentTypeService.getDocumentTypeByName(OpenESDHModel.DOCUMENT_TYPE_LETTER)
                .orElseThrow(() -> new WebScriptException("Document type \"letter\" not found")).getNodeRef();
    }

    private NodeRef getDocumentCategoryOther() {
        return documentCategoryService.getDocumentCategoryByName(OpenESDHModel.DOCUMENT_CATEGORY_OTHER)
                .orElseThrow(() -> new WebScriptException("Document type \"other\" not found")).getNodeRef();
    }
}
