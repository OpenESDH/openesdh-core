package dk.openesdh.repo.webscripts.files;

import static dk.openesdh.repo.webscripts.utils.WebScriptUtils.JSON;

import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
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
import org.springframework.stereotype.Component;

import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.RequestParam;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.UriVariable;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import com.github.dynamicextensionsalfresco.webscripts.resolutions.Resolution;

import dk.openesdh.repo.services.authorities.GroupsService;
import dk.openesdh.repo.services.files.OeFilesService;
import dk.openesdh.repo.utils.JSONArrayCollector;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

@Component
@WebScript(description = "Manage files", families = {"File Tools"})
public class OeFilesWebScript {

    @Autowired
    private OeFilesService filesService;
    @Autowired
    @Qualifier("GroupsService")
    private GroupsService groupsService;

    @Uri(value = "/api/openesdh/files", method = HttpMethod.GET, defaultFormat = JSON)
    public Resolution getUserFiles() throws IOException {
        String authorityName = AuthenticationUtil.getFullyAuthenticatedUser();
        JSONArray files = filesService.getFiles(authorityName)
                .stream()
                .sorted(getDateDescComparator())
                .collect(JSONArrayCollector.simple());
        return WebScriptUtils.jsonResolution(files);
    }

    @Uri(value = "/api/openesdh/files/group", method = HttpMethod.GET, defaultFormat = JSON)
    public Resolution getGroupFiles() throws IOException {
        JSONArray files = groupsService.getCurrentUserGroups()
                .stream()
                .map(filesService::getFiles)
                .flatMap(List::stream)
                .sorted(getDateDescComparator())
                .collect(JSONArrayCollector.simple());
        return WebScriptUtils.jsonResolution(files);
    }

    @Uri(value = "/api/openesdh/file/{store_type}/{store_id}/{id}", method = HttpMethod.DELETE, defaultFormat = JSON)
    public void delete(
            @UriVariable final String store_type,
            @UriVariable final String store_id,
            @UriVariable final String id) {
        filesService.delete(new NodeRef(store_type, store_id, id));
    }

    /**
     *
     * @param owner
     * @param comment
     * @param req - must contain FormData with one or more 'file' items
     * @throws IOException
     * @throws ParseException
     */
    @Uri(value = "/api/openesdh/files", method = HttpMethod.POST, defaultFormat = JSON, multipartProcessing = true)
    public void uploadMultipleFiles(
            final @RequestParam NodeRef owner,
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
                filesService.addFile(owner, filename, mimetype, fileInputStream, comment);
            }
        }
    }

    @Uri(value = "/api/openesdh/file/{store_type}/{store_id}/{id}", method = HttpMethod.GET, defaultFormat = JSON)
    public Resolution get(
            @UriVariable final String store_type,
            @UriVariable final String store_id,
            @UriVariable final String id) {
        JSONObject file = filesService.getFile(new NodeRef(store_type, store_id, id));
        return WebScriptUtils.jsonResolution(file);
    }

    @Uri(value = "/api/openesdh/file/assign", method = HttpMethod.PUT, defaultFormat = JSON)
    public void move(
            @RequestParam final NodeRef nodeRef,
            @RequestParam final NodeRef owner,
            @RequestParam(required = false) final String comment) {
        filesService.move(nodeRef, owner, comment);
    }

    @Uri(value = "/api/openesdh/case/{caseId}/addFile", method = HttpMethod.PUT, defaultFormat = JSON)
    public void addFileToCase(
            @UriVariable(WebScriptUtils.CASE_ID) final String caseId,
            @RequestParam(WebScriptUtils.NODE_REF) final NodeRef file,
            @RequestParam(WebScriptUtils.TITLE) final String title,
            @RequestParam(WebScriptUtils.DOC_TYPE) final NodeRef docType,
            @RequestParam(WebScriptUtils.DOC_CATEGORY) final NodeRef docCategory,
            @RequestParam(value = WebScriptUtils.DESCRIPTION, required = false) final String description) {
        filesService.addToCase(caseId, file, title, docType, docCategory, description);
    }

    private Comparator<JSONObject> getDateDescComparator() {
        return (JSONObject o1, JSONObject o2) -> {
            return ObjectUtils.compare(
                    getModifiedOrCreated(o2),
                    getModifiedOrCreated(o1));
        };
    }

    private Long getModifiedOrCreated(JSONObject o) {
        return (Long) ObjectUtils.firstNonNull(
                o.get(ContentModel.PROP_MODIFIED.getLocalName()),
                o.get(ContentModel.PROP_CREATED.getLocalName()));
    }
}
