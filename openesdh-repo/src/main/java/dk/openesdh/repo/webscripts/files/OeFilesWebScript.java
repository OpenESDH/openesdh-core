package dk.openesdh.repo.webscripts.files;

import static dk.openesdh.repo.webscripts.utils.WebScriptUtils.JSON;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.servlet.FormData;
import org.springframework.extensions.webscripts.servlet.FormData.FormField;
import org.springframework.stereotype.Component;

import com.github.dynamicextensionsalfresco.webscripts.annotations.FileField;
import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.RequestParam;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.UriVariable;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import com.github.dynamicextensionsalfresco.webscripts.resolutions.Resolution;

import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.files.OeFilesService;
import dk.openesdh.repo.utils.JSONArrayCollector;
import dk.openesdh.repo.webscripts.WebScriptParams;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

@Component
@WebScript(description = "Manage files", families = {"File Tools"})
public class OeFilesWebScript {

    @Autowired
    @Qualifier("OeFilesService")
    private OeFilesService filesService;

    /**
     * get files assigned to node
     *
     * @param store_type
     * @param store_id
     * @param id
     * @return
     */
    @Uri(value = "/api/openesdh/files/{store_type}/{store_id}/{id}", method = HttpMethod.GET, defaultFormat = JSON)
    public Resolution getFiles(
            @UriVariable final String store_type,
            @UriVariable final String store_id,
            @UriVariable final String id) {

        JSONArray files = filesService.getFiles(new NodeRef(store_type, store_id, id))
                .stream()
                .sorted(getDateDescComparator())
                .collect(JSONArrayCollector.simple());
        return WebScriptUtils.jsonResolution(files);
    }

    /**
     * delete file by nodeRef
     *
     * @param store_type
     * @param store_id
     * @param id
     */
    @Uri(value = "/api/openesdh/file/{store_type}/{store_id}/{id}", method = HttpMethod.DELETE, defaultFormat = JSON)
    public void delete(
            @UriVariable final String store_type,
            @UriVariable final String store_id,
            @UriVariable final String id) {
        filesService.delete(new NodeRef(store_type, store_id, id));
    }

    /**
     *
     * @param nodeRef - assign file to node
     * @param comment
     * @param req - must contain FormData with one or more 'file' items
     * @throws IOException
     * @throws ParseException
     */
    @Uri(value = "/api/openesdh/files", method = HttpMethod.POST, defaultFormat = JSON, multipartProcessing = true)
    public void uploadMultipleFiles(
            final @RequestParam NodeRef nodeRef,
            final @RequestParam(required = false) String comment,
            WebScriptRequest req
    ) throws IOException, ParseException {
        final FormData form = (FormData) req.parseContent();
        if (form == null || !form.getIsMultiPart()) {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "");
        }
        for (FormData.FormField field : form.getFields()) {
            if ("file".equals(field.getName())) {
                InputStream fileInputStream = field.getInputStream();
                String filename = field.getFilename();
                String mimetype = field.getMimetype();
                filesService.addFile(nodeRef, filename, mimetype, fileInputStream, comment);
            }
        }
    }

    @Uri(value = "/api/openesdh/case/{caseId}/addFile", method = HttpMethod.PUT, defaultFormat = JSON)
    public void addFileToCase(
            @UriVariable(WebScriptParams.CASE_ID) final String caseId,
            @RequestParam(WebScriptParams.NODE_REF) final NodeRef file,
            @RequestParam(WebScriptParams.TITLE) final String title,
            @RequestParam(WebScriptParams.DOC_TYPE) final NodeRef docType,
            @RequestParam(WebScriptParams.DOC_CATEGORY) final NodeRef docCategory,
            @RequestParam(value = WebScriptParams.DESCRIPTION, required = false) final String description) {
        filesService.addToCase(caseId, file, title, docType, docCategory, description);
    }

    @Uri(value = "/api/openesdh/files/upload/tmp", multipartProcessing = true, method = HttpMethod.POST, defaultFormat = WebScriptUtils.JSON)
    public Resolution uploadTempFile(@FileField("filedata") FormField fileField) throws Exception {
        if (fileField == null) {
            return null; // aborted upload
        }
        File tmpFile = TempFileProvider.createTempFile(fileField.getInputStream(),
                OpenESDHModel.OPENESDH_REPO_MODULE_ID, StringUtils.EMPTY);
        Map<String, String> result = new HashMap<>();
        result.put(OeFilesService.TMP_FILE_NAME, tmpFile.getName());
        result.put(OeFilesService.FILE_NAME, fileField.getFilename());
        result.put(OeFilesService.MIME_TYPE, fileField.getMimetype());
        return WebScriptUtils.jsonResolution(result);
    }

    @Uri(value = "/api/openesdh/files/tmp/{tmpFileName}", defaultFormat = JSON)
    public Resolution getTempFile(@UriVariable final String tmpFileName) {
        Map<String, String> result = new HashMap<>();
        result.put("exists", "false");
        File[] files = TempFileProvider.getTempDir().listFiles((file, name) -> name.equals(tmpFileName));
        if (files.length > 0) {
            File f = files[0];
            result.put("exists", "true");
        }
        return WebScriptUtils.jsonResolution(result);
    }

    public static Comparator<JSONObject> getDateDescComparator() {
        return (JSONObject o1, JSONObject o2) -> {
            return ObjectUtils.compare(
                    getModifiedOrCreated(o2),
                    getModifiedOrCreated(o1));
        };
    }

    public static Long getModifiedOrCreated(JSONObject o) {
        return (Long) ObjectUtils.firstNonNull(
                o.get(ContentModel.PROP_MODIFIED.getLocalName()),
                o.get(ContentModel.PROP_CREATED.getLocalName()));
    }
}
