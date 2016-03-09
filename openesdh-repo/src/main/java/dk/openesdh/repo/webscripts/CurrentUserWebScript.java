package dk.openesdh.repo.webscripts;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PersonService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.stereotype.Component;

import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import com.github.dynamicextensionsalfresco.webscripts.resolutions.Resolution;
import com.google.common.base.Joiner;

import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

@Component
@WebScript(description = "Retrieve information about the current logged in user", families = "Authentication")
public class CurrentUserWebScript {

    @Autowired
    private PersonService personService;

    @Uri(value = "/api/openesdh/currentUser", method = HttpMethod.GET, defaultFormat = "json")
    public Resolution get() {
        String userName = AuthenticationUtil.getFullyAuthenticatedUser();
        NodeRef personNodeRef = personService.getPerson(userName);
        PersonService.PersonInfo personInfo = personService.getPerson(personNodeRef);
        try {
            //returning the response in the same format as the api/forms/picker/authority/children
            //Note that some properties are just skipped as they do not make sense in the context of the user
            JSONObject obj = new JSONObject();
            obj.put("type", "cm:person");
            obj.put("nodeRef", personInfo.getNodeRef());
            obj.put("name", Joiner.on(" ").skipNulls().join(
                    personInfo.getFirstName(),
                    personInfo.getLastName()).trim()
                    + " (" + personInfo.getUserName() + ")");
            obj.put("userName", personInfo.getUserName());
            obj.put("selectable", true);
            return WebScriptUtils.jsonResolution(obj);
        } catch (Exception joe) {
            throw new WebScriptException("There was an exception getting the current authenticated user.\n"
                    + "Reason: " + joe.getLocalizedMessage());
        }
    }
}
