package dk.openesdh.repo.webscripts.files;

import static dk.openesdh.repo.webscripts.utils.WebScriptUtils.JSON;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.json.simple.JSONArray;
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
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import com.github.dynamicextensionsalfresco.webscripts.resolutions.Resolution;

import dk.openesdh.repo.services.authorities.GroupsService;
import dk.openesdh.repo.services.files.OeAuthorityFilesService;
import dk.openesdh.repo.utils.JSONArrayCollector;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

@Component
@WebScript(description = "Manage user/group files", families = {"File Tools"})
public class OeAuthorityFilesWebScript {

    @Autowired
    @Qualifier("OeAuthorityFilesService")
    private OeAuthorityFilesService authorityFilesService;
    @Autowired
    @Qualifier("GroupsService")
    private GroupsService groupsService;

    @Uri(value = "/api/openesdh/files/user", method = HttpMethod.GET, defaultFormat = JSON)
    public Resolution getUserFiles() {
        String authorityName = AuthenticationUtil.getFullyAuthenticatedUser();
        JSONArray files = authorityFilesService.getFiles(authorityName)
                .stream()
                .sorted(OeFilesWebScript.getDateDescComparator())
                .collect(JSONArrayCollector.simple());
        return WebScriptUtils.jsonResolution(files);
    }

    @Uri(value = "/api/openesdh/files/group", method = HttpMethod.GET, defaultFormat = JSON)
    public Resolution getGroupFiles() {
        JSONArray files = groupsService.getCurrentUserGroups()
                .stream()
                .map(authorityFilesService::getFiles)
                .flatMap(List::stream)
                .sorted(OeFilesWebScript.getDateDescComparator())
                .collect(JSONArrayCollector.simple());
        return WebScriptUtils.jsonResolution(files);
    }

    /**
     *
     * @param owner
     * @param comment
     * @param req - must contain FormData with one or more 'file' items
     * @throws IOException
     * @throws ParseException
     */
    @Uri(value = "/api/openesdh/files/owner", method = HttpMethod.POST, defaultFormat = JSON, multipartProcessing = true)
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
                authorityFilesService.addFile(owner, filename, mimetype, fileInputStream, comment);
            }
        }
    }

    @Uri(value = "/api/openesdh/file/assign", method = HttpMethod.PUT, defaultFormat = JSON)
    public void move(
            @RequestParam final NodeRef nodeRef,
            @RequestParam final NodeRef owner,
            @RequestParam(required = false) final String comment) {
        authorityFilesService.move(nodeRef, owner, comment);
    }
}
