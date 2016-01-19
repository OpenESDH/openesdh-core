package dk.openesdh.repo.webscripts.files;

import com.github.dynamicextensionsalfresco.webscripts.annotations.FileField;
import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.RequestParam;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.UriVariable;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import com.github.dynamicextensionsalfresco.webscripts.resolutions.Resolution;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
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
import org.springframework.extensions.webscripts.servlet.FormData;
import org.springframework.stereotype.Component;

import dk.openesdh.repo.services.authorities.GroupsService;
import dk.openesdh.repo.services.files.OeFilesService;
import dk.openesdh.repo.utils.JSONArrayCollector;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;
import static dk.openesdh.repo.webscripts.utils.WebScriptUtils.JSON;

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
                .collect(JSONArrayCollector.simple());
        return WebScriptUtils.jsonResolution(files);
    }

    @Uri(value = "/api/openesdh/files/group", method = HttpMethod.GET, defaultFormat = JSON)
    public Resolution getGroupFiles() throws IOException {
        JSONArray files = groupsService.getCurrentUserGroups()
                .stream()
                .map(filesService::getFiles)
                .flatMap(List::stream)
                .sorted((JSONObject o1, JSONObject o2) -> {
                    return ObjectUtils.compare(
                            (Date) ObjectUtils.firstNonNull(o2.get(ContentModel.PROP_MODIFIED), o2.get(ContentModel.PROP_CREATED)),
                            (Date) ObjectUtils.firstNonNull(o1.get(ContentModel.PROP_MODIFIED), o1.get(ContentModel.PROP_CREATED)));
                })
                .collect(JSONArrayCollector.simple());
        return WebScriptUtils.jsonResolution(files);
    }

    @Uri(value = "/api/openesdh/files", method = HttpMethod.POST, defaultFormat = JSON, multipartProcessing = true)
    public void uploadFile(
            @RequestParam NodeRef owner,
            @FileField("file") FormData.FormField fileField
    ) throws IOException, ParseException {
        InputStream fileInputStream = fileField.getInputStream();
        String filename = fileField.getFilename();
        String mimetype = fileField.getMimetype();
        filesService.addFile(owner, filename, mimetype, fileInputStream);
    }

    @Uri(value = "/api/openesdh/file/{store_type}/{store_id}/{id}", method = HttpMethod.DELETE, defaultFormat = JSON)
    public void delete(
            @UriVariable final String store_type,
            @UriVariable final String store_id,
            @UriVariable final String id) {
        filesService.delete(new NodeRef(store_type, store_id, id));
    }

    @Uri(value = "/api/openesdh/file/assign", method = HttpMethod.PUT, defaultFormat = JSON)
    public void move(
            @RequestParam final NodeRef nodeRef,
            @RequestParam final NodeRef owner,
            @RequestParam(required = false) final String comment) {
        filesService.move(nodeRef, owner, comment);
    }
}
