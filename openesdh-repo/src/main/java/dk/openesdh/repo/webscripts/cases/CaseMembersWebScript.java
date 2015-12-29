package dk.openesdh.repo.webscripts.cases;

import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.RequestParam;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.UriVariable;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import com.github.dynamicextensionsalfresco.webscripts.resolutions.Resolution;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.stereotype.Component;

import dk.openesdh.repo.services.cases.CaseService;
import dk.openesdh.repo.services.members.CaseMembersService;
import static dk.openesdh.repo.webscripts.ParamUtils.checkRequiredParam;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

@Component
@WebScript(description = "Case Members managment", families = {"Case Tools"})
public class CaseMembersWebScript {

    @Autowired
    private CaseService caseService;
    @Autowired
    private CaseMembersService caseMembersService;
    @Autowired
    private AuthorityService authorityService;
    @Autowired
    private PersonService personService;

    @Uri(value = "/api/openesdh/case/{caseId}/members", method = HttpMethod.GET, defaultFormat = "json")
    public Resolution get(@UriVariable final String caseId) throws JSONException {
        NodeRef caseNodeRef = caseService.getCaseById(caseId);
        Map<String, Set<String>> membersByRole = caseMembersService.getMembersByRole(caseNodeRef, true, true);
        return WebScriptUtils.jsonResolution(buildJSON(membersByRole));
    }

    @Uri(value = "/api/openesdh/case/{caseId}/members", method = HttpMethod.POST, defaultFormat = "json")
    public void post(
            @UriVariable final String caseId,
            @RequestParam(required = true) final String role,
            @RequestParam(required = false) final String fromRole,
            @RequestParam(required = false) final String authority,
            @RequestParam(required = false) final String[] authorityNodeRefs,
            WebScriptRequest req) throws JSONException {
        NodeRef caseNodeRef = caseService.getCaseById(caseId);
        try {
            if (fromRole != null) {
                // When "fromRole" is specified, move the authority
                checkRequiredParam(authority, "authority");
                caseMembersService.changeAuthorityRole(authority, fromRole, role, caseNodeRef);
            } else {
                checkRequiredParam(authorityNodeRefs, "authorityNodeRefs");
                List<NodeRef> authorities = convertToNodeRefsList(authorityNodeRefs);
                caseMembersService.addAuthoritiesToRole(authorities, role, caseNodeRef);
            }
        } catch (DuplicateChildNodeNameException e) {
            throw new WebScriptException(Status.STATUS_CONFLICT, "dublicate", e);
        }
    }

    @Uri(value = "/api/openesdh/case/{caseId}/member", method = HttpMethod.DELETE, defaultFormat = "json")
    public Resolution delete(
            @UriVariable final String caseId,
            @RequestParam final String role,
            @RequestParam final String authority
    ) throws IOException, JSONException {
        NodeRef caseNodeRef = caseService.getCaseById(caseId);
        caseMembersService.removeAuthorityFromRole(authority, role, caseNodeRef);
        return WebScriptUtils.jsonResolution(new JSONObject().put("success", true));
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
}
