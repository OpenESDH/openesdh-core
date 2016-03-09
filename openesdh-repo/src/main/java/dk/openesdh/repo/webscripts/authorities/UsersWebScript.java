package dk.openesdh.repo.webscripts.authorities;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.servlet.FormData.FormField;
import org.springframework.stereotype.Component;

import com.github.dynamicextensionsalfresco.webscripts.annotations.FileField;
import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.UriVariable;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import com.github.dynamicextensionsalfresco.webscripts.resolutions.Resolution;

import dk.openesdh.repo.services.authorities.UsersService;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

@Component
@WebScript(description = "Manage users", families = { "Authorities" })
public class UsersWebScript {

    private static final String CSV_HEADER = "User Name,First Name,Last Name,E-mail Address,,Password,Company,Job Title,Location,Telephone,Mobile,Skype,IM,Google User Name,Address,Address Line 2,Address Line 3,Post Code,Telephone,Fax,Email,Member of groups\n";

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

    @Uri(value = "/api/openesdh/users/upload/sample", method = HttpMethod.GET)
    public void downloadCsvFileSample(WebScriptResponse res) throws IOException {
        res.addHeader("Content-Disposition", "attachment; filename=ExampleUserUpload.csv");
        res.getWriter().append(CSV_HEADER);
    }

    @Uri(value = "/api/openesdh/users/{userId}/emailfeeddisabled/{emailFeedDisabled}", method = HttpMethod.PUT)
    public Resolution setEmailFeedDisabled(@UriVariable("userId") String userId,
            @UriVariable("emailFeedDisabled") boolean emailFeedDisabled) {
        userService.setEmailFeedDisabled(userId, emailFeedDisabled);
        return WebScriptUtils.jsonResolution("OK");
    }

}
