package dk.openesdh.repo.webscripts.cases;

import dk.openesdh.repo.services.cases.CaseService;
import dk.openesdh.repo.webscripts.AbstractRESTWebscript;
import static dk.openesdh.repo.webscripts.ParamUtils.getOptionalParameter;
import static dk.openesdh.repo.webscripts.ParamUtils.getRequiredParameter;
import static dk.openesdh.repo.webscripts.ParamUtils.getRequiredParameters;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PersonService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

public class CaseMembersWebScript extends AbstractRESTWebscript {

    private CaseService caseService;
    private AuthorityService authorityService;
    private PersonService personService;

    @Override
    protected NodeRef getNodeRef(WebScriptRequest req, Map<String, String> templateArgs) {
        String caseId = templateArgs.get(WebScriptUtils.CASE_ID);
        return caseService.getCaseById(caseId);
    }

    @Override
    protected void get(NodeRef caseNodeRef, WebScriptRequest req, WebScriptResponse res) throws IOException, JSONException {
        Map<String, Set<String>> membersByRole = caseService.getMembersByRole(caseNodeRef, true, true);
        JSONArray json = buildJSON(membersByRole);
        json.write(res.getWriter());
    }

    @Override
    protected void post(NodeRef caseNodeRef, WebScriptRequest req, WebScriptResponse res) throws IOException, JSONException {
        String role = getRequiredParameter(req, "role");
        String fromRole = getOptionalParameter(req, "fromRole");

        JSONObject json = new JSONObject();
        try {
            if (fromRole != null) {
                // When "fromRole" is specified, move the authority
                String authority = getRequiredParameter(req, "authority");
                caseService.changeAuthorityRole(authority, fromRole, role, caseNodeRef);
            } else {
                String[] authorityNodeRefsStr = getRequiredParameters(req, "authorityNodeRefs");
                List<NodeRef> authorities = convertToNodeRefsList(authorityNodeRefsStr);
                caseService.addAuthoritiesToRole(authorities, role, caseNodeRef);
            }
        } catch (DuplicateChildNodeNameException e) {
            json.put("duplicate", true);
            res.setStatus(Status.STATUS_CONFLICT);
        }
        json.write(res.getWriter());
    }

    @Override
    protected void delete(NodeRef caseNodeRef, WebScriptRequest req, WebScriptResponse res) throws IOException, JSONException {
        String authority = getRequiredParameter(req, "authority");
        String role = getRequiredParameter(req, "role");

        caseService.removeAuthorityFromRole(authority, role, caseNodeRef);

        JSONObject json = new JSONObject();
        json.put("success", true);
        json.write(res.getWriter());
    }

    private JSONArray buildJSON(Map<String, Set<String>> membersByRole) throws JSONException {
        JSONArray result = new JSONArray();

        for (Map.Entry<String, Set<String>> entry : membersByRole.entrySet()) {
            Set<String> value = entry.getValue();
            for (String authority : value) {
                JSONObject memberObj = new JSONObject();
                boolean isGroup = authority.startsWith("GROUP_");
                memberObj.put("authorityType", isGroup ? "group" : "user");
                memberObj.put("authority", authority);

                NodeRef authorityNodeRef = authorityService.getAuthorityNodeRef(authority);
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

    private List<NodeRef> convertToNodeRefsList(String[] authorityNodeRefsStr) {
        List<NodeRef> authorities = new ArrayList<>();
        for (String authorityNodeRefStr : authorityNodeRefsStr) {
            authorities.add(new NodeRef(authorityNodeRefStr));
        }
        return authorities;
    }

    public void setCaseService(CaseService caseService) {
        this.caseService = caseService;
    }

    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

}
