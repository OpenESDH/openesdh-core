package dk.openesdh.repo.webscripts.authorities;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.webscripts.servlet.FormData.FormField;
import org.springframework.stereotype.Component;

import com.github.dynamicextensionsalfresco.webscripts.annotations.FileField;
import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import com.github.dynamicextensionsalfresco.webscripts.resolutions.Resolution;

import dk.openesdh.repo.services.authorities.UsersService;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

@Component
@WebScript(description = "Manage users", families = { "Authorities" })
public class UsersWebScript {

    @Autowired
    @Qualifier("UsersService")
    private UsersService userService;

    @Uri(value = "/api/openesdh/users/upload", multipartProcessing = true, method = HttpMethod.POST)
    public Resolution uploadUsersCsvFile(@FileField("filedata") FormField fileField) throws JSONException {
        try {
            return WebScriptUtils.jsonResolution(
                    userService.uploadUsersCsv(fileField.getInputStream()));
        } catch (Exception e) {
            JSONObject json = new JSONObject();
            json.put("error", "true");
            json.put("message", e.getMessage());
            return WebScriptUtils.jsonResolution(json);
        }
    }

}
