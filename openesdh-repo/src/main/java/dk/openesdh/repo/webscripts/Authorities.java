package dk.openesdh.repo.webscripts;

import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.util.Set;

public class Authorities extends AbstractWebScript {

    private AuthorityService authorityService;

    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws
            IOException {
        String authority = req.getParameter("authority");
        Set<String> authorities = authorityService.findAuthorities
                (AuthorityType.GROUP,
                null, true, authority, null);
        // TODO: Find using personService to get full name search
        authorities.addAll(authorityService.findAuthorities(AuthorityType.USER,
                null, false, authority, null));
        JSONArray json = null;
        try {
            json = buildJSON(authorities);
            json.write(res.getWriter());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    JSONArray buildJSON(Set<String> authorities) throws
            JSONException {
        JSONArray result = new JSONArray();

        for (String authority : authorities) {
            JSONObject authorityObj = new JSONObject();
            authorityObj.put("authorityType", authority.startsWith("GROUP") ?
                    "group" : "user");
            authorityObj.put("authority", authority);
            authorityObj.put("displayName", authorityService.getAuthorityDisplayName(authority));
            result.put(authorityObj);
        }

        return result;
    }
}
