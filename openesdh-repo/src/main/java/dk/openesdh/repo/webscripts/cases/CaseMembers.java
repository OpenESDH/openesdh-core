package dk.openesdh.repo.webscripts.cases;

import dk.openesdh.repo.services.CaseService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.namespace.QName;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.Set;

public class CaseMembers extends AbstractWebScript {

    private CaseService caseService;
    private AuthorityService authorityService;

    public void setCaseService(CaseService caseService) {
        this.caseService = caseService;
    }

    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        NodeRef caseNodeRef = new NodeRef(req.getParameter("nodeRef"));
        Map<String, Set<String>> membersByRole = caseService.getMembersByRole(caseNodeRef);
        try {
            JSONArray json = buildJSON(membersByRole);
            json.write(res.getWriter());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    JSONArray buildJSON(Map<String, Set<String>> membersByRole) throws
            JSONException {
        JSONArray result = new JSONArray();

        for (Map.Entry<String, Set<String>> entry : membersByRole.entrySet()) {
            Set<String> value = entry.getValue();
            for (String authority : value) {
                JSONObject memberObj = new JSONObject();
//                {authorityType: "user", "authority": "admin", "authorityName": "Administrator", role: "casesimplewriter"},
//                    {authorityType: "user", "authority": "abeecher", "authorityName": "Alice Beecher", role: "casesimplereader"}
//                ]
                memberObj.put("authorityType", authority.startsWith("GROUP") ?
                        "group" : "user");
                memberObj.put("authority", authority);
                memberObj.put("authorityName", authorityService.getAuthorityDisplayName(authority));
                memberObj.put("role", entry.getKey());
                result.put(memberObj);
            }
        }

        return result;
    }


}
