package dk.openesdh.repo.webscripts.groups;

import org.alfresco.repo.domain.permissions.Authority;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PersonService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.*;

import java.io.IOException;
import java.util.*;

public class Members extends AbstractWebScript {

    private AuthorityService authorityService;
    private PersonService personService;
    // Logger
    private static final Log logger = LogFactory.getLog(Members.class);

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        String method = req.getServiceMatch().getWebScript().getDescription().getMethod();
        Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
        String shortName = templateArgs.get("shortName");
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
        if (!authorityService.authorityExists(authorityService.getName(AuthorityType.GROUP, shortName)))
            throw new WebScriptException("The group (" + shortName + ") does not/ no longer exists");

        try {
            switch (method) {
                case "PUT":
                    addMembers(req, res, shortName, parsedRequest);
                    break;
            }
        } catch (JSONException e) {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Invalid JSON: " + e.getMessage());
        }
    }

    private void addMembers(WebScriptRequest req, WebScriptResponse res, String groupShortName, JSONObject parsedRequest) throws IOException, JSONException {
        String users = getOrNull(parsedRequest, "users");
        String groups = getOrNull(parsedRequest, "groups");
        StringBuffer failed = new StringBuffer();
        String groupFullName ="GROUP_"+ groupShortName;

        if (StringUtils.isNotBlank(users)) {
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
                logger.warn("**** could not add users to group:\n\t\t\t" + ge.getMessage() + " ****");
            }
        }

        if (StringUtils.isNotBlank(groups)) {
            try {
                List<String> candidates = Arrays.asList(StringUtils.split(groups, ","));
                for (String group : candidates) {
                    String memberFullName = "GROUP_"+ group;
                    if (!authorityService.authorityExists(memberFullName)) {
                        failed.append(group);
                        failed.append(" ");
                        continue;
                    }
                    authorityService.addAuthority(groupFullName, memberFullName);
                }
            } catch (Exception ge) {
                logger.warn("**** could not add users to group:\n\t\t\t" + ge.getMessage() + " ****");
            }
        }
        Set <String>successes = authorityService.getContainedAuthorities(AuthorityType.USER, groupFullName, true);
        authorityService.getContainedAuthorities(AuthorityType.GROUP, groupFullName, true).iterator().forEachRemaining(successes::add);
        JSONArray children = buildJSON(successes);
        JSONObject response =new JSONObject();
        response.put("data", children);
        if(StringUtils.isNotBlank(failed.toString()) ){
            response.put("failed", failed.toString()); //This should work
        }
        response.writeJSONString(res.getWriter());

    }

    JSONArray buildJSON(Set<String> authorities) throws JSONException {
        JSONArray result = new JSONArray();

        for (String authority : authorities) {
            JSONObject authorityObj = new JSONObject();
            authorityObj.put("authorityType", AuthorityType.getAuthorityType(authority));
            authorityObj.put("shortName", authorityService.getShortName(authority));
            if(AuthorityType.getAuthorityType(authority) == AuthorityType.GROUP) {
                authorityObj.put("displayName", authorityService.getAuthorityDisplayName(authority));
                authorityObj.put("fullName", authority);
                authorityObj.put("url", "/api/groups/"+authority);
            }
            else {
                NodeRef pNode = personService.getPerson(authority);
                PersonService.PersonInfo personInfo = personService.getPerson(pNode);
                authorityObj.put("displayName", personInfo.getFirstName()+" "+ personInfo.getLastName());
                authorityObj.put("fullName", personInfo.getFirstName()+" "+ personInfo.getLastName());
                authorityObj.put("url", "/api/people/"+personInfo.getUserName());
            }

            result.put(authorityObj);
        }

        return result;
    }

    /**
     * Grabbed from the org.alfresco.repo.web.scripts.discussion.AbstractDiscussionWebScript
     *
     * @param json
     * @param key
     * @return
     */
    public String getOrNull(JSONObject json, String key) {
        if (json.containsKey(key)) {
            return Objects.toString(json.get(key));
        }
        return null;
    }

    //<editor-fold desc="Injected bean service setters">
    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }
    //</editor-fold>
}
