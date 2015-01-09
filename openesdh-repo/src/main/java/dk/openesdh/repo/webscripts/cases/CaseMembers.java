package dk.openesdh.repo.webscripts.cases;

import dk.openesdh.repo.services.cases.CaseService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PersonService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CaseMembers extends AbstractWebScript {

    private CaseService caseService;
    private AuthorityService authorityService;
    private PersonService personService;

    public void setCaseService(CaseService caseService) {
        this.caseService = caseService;
    }

    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        String method = req.getServiceMatch().getWebScript().getDescription().getMethod();
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
        Map<String, Set<String>> membersByRole = caseService.getMembersByRole(caseNodeRef, true, false);
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
                String[] authorityNodeRefsStr = req.getParameterValues("authorityNodeRefs");
                if (authorityNodeRefsStr != null) {
                    List<NodeRef> authorities = new LinkedList<>();
                    for (String authorityNodeRefStr : authorityNodeRefsStr) {
                        authorities.add(new NodeRef(authorityNodeRefStr));
                    }
                    caseService.addAuthoritiesToRole(authorities, role, caseNodeRef);
                } else {
                    caseService.addAuthorityToRole(authority, role, caseNodeRef);
                }
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

    JSONArray buildJSON(Map<String, Set<String>> membersByRole) throws JSONException {
        JSONArray result = new JSONArray();

        for (Map.Entry<String, Set<String>> entry : membersByRole.entrySet()) {
            Set<String> value = entry.getValue();
            for (String authority : value) {
                JSONObject memberObj = new JSONObject();
                boolean isGroup = authority.startsWith("GROUP_");
                memberObj.put("authorityType", isGroup ? "group" : "user");
                memberObj.put("authority", authority);

                NodeRef authorityNodeRef = authorityService.getAuthorityNodeRef
                        (authority);

                String displayName;
                if (isGroup) {
                    displayName = authorityService.getAuthorityDisplayName(authority);
                } else {
                    PersonService.PersonInfo personInfo = personService.getPerson(authorityNodeRef);
                    displayName = personInfo.getFirstName() + " " + personInfo.getLastName();
                }
                memberObj.put("displayName", displayName);
                memberObj.put("role", entry.getKey());
                memberObj.put("nodeRef", authorityNodeRef);
                result.put(memberObj);
            }

        }

        return result;
    }


}
