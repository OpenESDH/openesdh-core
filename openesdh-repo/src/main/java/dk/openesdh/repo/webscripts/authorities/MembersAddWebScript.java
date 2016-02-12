package dk.openesdh.repo.webscripts.authorities;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PersonService;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.stereotype.Component;

import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Lifecycle;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.UriVariable;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import com.github.dynamicextensionsalfresco.webscripts.resolutions.Resolution;

import dk.openesdh.repo.webscripts.ParamUtils;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

/**
 * This is a supplement to the POST method. Adds group(s) or user(s) to a group. This webscript will not create a sub
 * group if one does not already exist.
 *
 * <br />You must have "administrator" privileges to modify groups.
 * <br />If the authority is for a group and does not exist then it will not be created.
 * <br />The webscript returns Status_OK and returns the group.
 *
 */
@Component
@WebScript(description = "Add group or user to a group", families = {"Authorities"}, lifecycle = Lifecycle.INTERNAL)
public class MembersAddWebScript {

    private final Logger logger = LoggerFactory.getLogger(MembersAddWebScript.class);

    @Autowired
    private AuthorityService authorityService;
    @Autowired
    private PersonService personService;

    @Uri(value = "/api/groups/{shortName}/children", method = HttpMethod.PUT, defaultFormat = "json")
    public Resolution addMembers(
            @UriVariable final String shortName,
            WebScriptRequest req) throws JSONException {

        JSONObject parsedRequest;
        // Parse the JSON, if supplied
        JSONParser parser = new JSONParser();
        try {
            parsedRequest = (JSONObject) parser.parse(req.getContent().getContent());
        } catch (IOException | ParseException io) {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Invalid JSON: " + io.getMessage());
        }
        //The shortName is not blank
        if (StringUtils.isBlank(shortName)) {
            throw new WebScriptException("The shortName can't be empty/blank");
        }
        //Check if the group exists before trying to perform the operation
        if (!authorityService.authorityExists(authorityService.getName(AuthorityType.GROUP, shortName))) {
            throw new WebScriptException("The group (" + shortName + ") does not/ no longer exists");
        }

        String users = ParamUtils.getOrNull(parsedRequest, "users");
        String groups = ParamUtils.getOrNull(parsedRequest, "groups");
        StringBuilder failed = new StringBuilder();
        String groupFullName = "GROUP_" + shortName;

        if (StringUtils.isNotBlank(users)) {
            addUsers(users, failed, groupFullName);
        }

        if (StringUtils.isNotBlank(groups)) {
            addGroups(groups, failed, groupFullName);
        }

        Set<String> successes = authorityService.getContainedAuthorities(AuthorityType.USER, groupFullName, true);
        authorityService.getContainedAuthorities(AuthorityType.GROUP, groupFullName, true)
                .iterator()
                .forEachRemaining(successes::add);
        JSONArray children = buildJSON(successes);
        JSONObject response = new JSONObject();
        response.put("data", children);
        if (StringUtils.isNotBlank(failed.toString())) {
            response.put("failed", failed.toString()); //This should work
        }
        return WebScriptUtils.jsonResolution(response);
    }

    private void addGroups(String groups, StringBuilder failed, String groupFullName) {
        try {
            List<String> candidates = Arrays.asList(StringUtils.split(groups, ","));
            for (String group : candidates) {
                String memberFullName = "GROUP_" + group;
                if (!authorityService.authorityExists(memberFullName)) {
                    failed.append(group);
                    failed.append(" ");
                    continue;
                }
                authorityService.addAuthority(groupFullName, memberFullName);
            }
        } catch (Exception ge) {
            logger.warn("**** could not add groups to group:\n\t\t\t {} ****", ge.getMessage());
        }
    }

    private void addUsers(String users, StringBuilder failed, String groupFullName) {
        try {
            List<String> candidates = Arrays.asList(StringUtils.split(users, ","));
            for (String user : candidates) {
                if (!personService.personExists(user)) {
                    failed.append(user);
                    failed.append("\n");
                    continue;
                }
                authorityService.addAuthority(groupFullName, user);
            }
        } catch (Exception ge) {
            logger.warn("**** could not add users to group:\n\t\t\t ****", ge.getMessage());
        }
    }

    JSONArray buildJSON(Set<String> authorities) throws JSONException {
        JSONArray result = new JSONArray();

        for (String authority : authorities) {
            JSONObject authorityObj = new JSONObject();
            authorityObj.put("authorityType", AuthorityType.getAuthorityType(authority));
            authorityObj.put("shortName", authorityService.getShortName(authority));
            if (AuthorityType.getAuthorityType(authority) == AuthorityType.GROUP) {
                authorityObj.put("displayName", authorityService.getAuthorityDisplayName(authority));
                authorityObj.put("fullName", authority);
                authorityObj.put("url", "/api/groups/" + authority);
            } else {
                NodeRef pNode = personService.getPerson(authority);
                PersonService.PersonInfo personInfo = personService.getPerson(pNode);
                authorityObj.put("displayName", personInfo.getFirstName() + " " + personInfo.getLastName());
                authorityObj.put("fullName", personInfo.getFirstName() + " " + personInfo.getLastName());
                authorityObj.put("url", "/api/people/" + personInfo.getUserName());
            }

            result.put(authorityObj);
        }

        return result;
    }
}
