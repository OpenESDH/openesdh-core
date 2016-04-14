package dk.openesdh.repo.webscripts.authorities;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.servlet.FormData.FormField;
import org.springframework.stereotype.Component;

import com.github.dynamicextensionsalfresco.webscripts.annotations.FileField;
import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.UriVariable;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import com.github.dynamicextensionsalfresco.webscripts.resolutions.Resolution;

import dk.openesdh.repo.exceptions.DomainException;
import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.NodeInfoService;
import dk.openesdh.repo.services.authorities.UserSavingContext;
import dk.openesdh.repo.services.authorities.UsersService;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

@Component
@WebScript(description = "Manage users", families = {"Authorities"})
public class UsersWebScript {

    private static final String CSV_HEADER = "User Name,First Name,Last Name,E-mail Address,,Password,Company,Job Title,Location,Telephone,Mobile,Skype,IM,Google User Name,Address,Address Line 2,Address Line 3,Post Code,Telephone,Fax,Email,Member of groups\n";

    @Autowired
    @Qualifier("UsersService")
    private UsersService userService;
    @Autowired
    @Qualifier("NodeInfoService")
    private NodeInfoService nodeInfoService;
    @Autowired
    @Qualifier("AuthorityService")
    private AuthorityService authorityService;

    @Uri(value = "/api/openesdh/user/{userName}", method = HttpMethod.GET, defaultFormat = WebScriptUtils.JSON)
    public Resolution getUser(@UriVariable("userName") String userName) {
        NodeRef user = authorityService.getAuthorityNodeRef(userName);
        return getUserResolution(user);
    }

    private Resolution getUserResolution(NodeRef nodeRef) {
        return WebScriptUtils.jsonResolution(userService.getUserJson(nodeRef));
    }

    @Uri(value = "/api/openesdh/user/{userName}", method = HttpMethod.POST, defaultFormat = WebScriptUtils.JSON)
    public Resolution saveUser(@UriVariable("userName") String userName, WebScriptRequest req) throws JSONException, IOException {
        JSONObject json = new JSONObject(req.getContent().getContent());
        Map<QName, Serializable> props = getUserProperties(json, userName);
        boolean enableAccount = (json.has("disableAccount") && json.getBoolean("disableAccount")) == false;
        NodeRef createdUser = userService.createUser(props, enableAccount, getUserAssociations(json));
        return getUserResolution(createdUser);
    }

    @Uri(value = "/api/openesdh/user/{userName}", method = HttpMethod.PUT, defaultFormat = WebScriptUtils.JSON)
    public Resolution updateUser(@UriVariable("userName") String userName, WebScriptRequest req) throws JSONException, IOException {
        JSONObject json = new JSONObject(req.getContent().getContent());
        Map<QName, Serializable> props = getUserProperties(json, userName);
        boolean enableAccount = (json.has("disableAccount") && json.getBoolean("disableAccount")) == false;
        NodeRef createdUser = userService.updateUser(props, enableAccount, getUserAssociations(json));
        return getUserResolution(createdUser);
    }

    private Map<QName, Serializable> getUserProperties(JSONObject json, String userName) throws JSONException {
        JSONObject cmJSON = json.getJSONObject(NamespaceService.CONTENT_MODEL_PREFIX);
        cmJSON.put("userName", userName);
        //check
        checkMandatoryValue(cmJSON, "userName", "USER.ERRORS.NO_USERNAME");
        checkMandatoryValue(cmJSON, "firstName", "USER.ERRORS.NO_FIRSTNAME");
        checkMandatoryValue(cmJSON, "email", "USER.ERRORS.NO_EMAIL");

        return nodeInfoService.getNodePropertiesFromJSON(json);
    }

    private List<UserSavingContext.Assoc> getUserAssociations(JSONObject json) throws JSONException {
        List<UserSavingContext.Assoc> assoc = new ArrayList<>();
        NodeRef managerOrNull = null;
        if (json.has("assoc")) {
            JSONObject assocJSON = json.getJSONObject("assoc");
            managerOrNull = assocJSON.has("manager") && NodeRef.isNodeRef(assocJSON.getString("manager"))
                    ? new NodeRef(assocJSON.getString("manager"))
                    : null;
        }
        assoc.add(new UserSavingContext.Assoc(
                OpenESDHModel.ASPECT_OE_MANAGEABLE,
                OpenESDHModel.ASSOC_OE_MANAGER,
                managerOrNull));
        return assoc;
    }

    private void checkMandatoryValue(JSONObject json, String field, String errCode) throws JSONException {
        if (json.isNull(field) || json.getString(field).isEmpty()) {
            throw new DomainException(errCode).forField(field);
        }
    }

    @Uri(value = "/api/openesdh/users/upload", multipartProcessing = true, method = HttpMethod.POST, defaultFormat = WebScriptUtils.JSON)
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
}
