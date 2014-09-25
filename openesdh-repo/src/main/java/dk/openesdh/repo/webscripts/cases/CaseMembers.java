package dk.openesdh.repo.webscripts.cases;

import dk.openesdh.repo.services.CaseService;
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
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
        String method = req.getServiceMatch().getWebScript().getDescription()
                .getMethod();
        try {
            if (method.equals("GET")) {
                get(req, res);
            } else if (method.equals("POST")) {
                post(req, res);
            } else if (method.equals("DELETE")) {
                delete(req, res);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void get(WebScriptRequest req, WebScriptResponse res) throws
            IOException, JSONException {
        NodeRef caseNodeRef = new NodeRef(req.getParameter("nodeRef"));
        Map<String, Set<String>> membersByRole = caseService.getMembersByRole(caseNodeRef);
        JSONArray json = buildJSON(membersByRole);
        json.write(res.getWriter());
    }

    private void post(WebScriptRequest req, WebScriptResponse res) throws IOException, JSONException {
        JSONObject json = new JSONObject();
        NodeRef caseNodeRef = new NodeRef(req.getParameter("nodeRef"));
        String authority = req.getParameter("authority");
        String role = req.getParameter("role");
        String fromRole = req.getParameter("fromRole");
        try {
            if (fromRole != null) {
                // When "fromRole" is specified, move the authority
                caseService.changeAuthorityRole(authority, fromRole, role, caseNodeRef);
            } else {
                caseService.addAuthorityToRole(authority, role, caseNodeRef);
            }
        } catch (DuplicateChildNodeNameException e) {
            json.put("duplicate", true);
            res.setStatus(Status.STATUS_CONFLICT);
        }

        json.write(res.getWriter());
    }

    private void delete(WebScriptRequest req, WebScriptResponse res) throws IOException, JSONException {
        NodeRef caseNodeRef = new NodeRef(req.getParameter("nodeRef"));
        String authority = req.getParameter("authority");
        String role = req.getParameter("role");
        caseService.removeAuthorityFromRole(authority, role, caseNodeRef);
        JSONObject json = new JSONObject();
        json.put("success", true);
        json.write(res.getWriter());
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
                // TODO: Output displayName
//                PersonService p;
//                p.getPerson(new NodeRef()).getFirstName();
                memberObj.put("authorityName", authorityService
                        .getAuthorityDisplayName(authority));
                memberObj.put("role", entry.getKey());
                result.put(memberObj);
            }

        }

        return result;
    }


}
