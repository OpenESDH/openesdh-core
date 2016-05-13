package dk.openesdh.repo.webscripts.files;

import static dk.openesdh.repo.webscripts.utils.WebScriptUtils.JSON;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.util.TempFileProvider;
import org.apache.commons.lang.StringUtils;
import org.springframework.extensions.webscripts.servlet.FormData.FormField;
import org.springframework.stereotype.Component;

import com.github.dynamicextensionsalfresco.webscripts.annotations.FileField;
import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.UriVariable;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import com.github.dynamicextensionsalfresco.webscripts.resolutions.Resolution;

import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.files.OeFilesService;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

@Component
@WebScript(description = "Manage temp files", families = { "File Tools" })
public class TempFilesWebScript {

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
            result.put("exists", "true");
        }
        return WebScriptUtils.jsonResolution(result);
    }
}
