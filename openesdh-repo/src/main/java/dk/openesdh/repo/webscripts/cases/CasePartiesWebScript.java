package dk.openesdh.repo.webscripts.cases;

import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.stereotype.Component;

import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.RequestParam;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Transaction;
import com.github.dynamicextensionsalfresco.webscripts.annotations.TransactionType;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.UriVariable;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import com.github.dynamicextensionsalfresco.webscripts.resolutions.Resolution;

import dk.openesdh.repo.model.ContactInfo;
import dk.openesdh.repo.services.cases.PartyService;
import dk.openesdh.repo.services.contacts.ContactService;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

@Component
@WebScript(description = "Manage case parties", families = {"Case Tools"})
public class CasePartiesWebScript {

    @Autowired
    private ContactService contactService;
    @Autowired
    private PartyService partyService;

    @Transaction(TransactionType.REQUIRED)
    @Uri(value = "/api/openesdh/case/{caseId}/parties", method = HttpMethod.GET, defaultFormat = "json")
    public Resolution get(@UriVariable final String caseId) throws JSONException {
        Map<String, List<NodeRef>> caseParties = partyService.getCaseParties(caseId);
        JSONArray json = buildJSON(caseParties);
        return WebScriptUtils.jsonResolution(json);
    }

    @Uri(value = "/api/openesdh/case/{caseId}/party/{partyRole}", method = HttpMethod.POST, defaultFormat = "json")
    public void post(
            @UriVariable final String caseId,
            @UriVariable final String partyRole,
            WebScriptRequest req) {
        JSONObject json = WebScriptUtils.readJson(req);
        JSONArray contacts = (JSONArray) json.get("contactNodeRefs");
        partyService.addCaseParty(caseId, partyRole, contacts);
    }

    @Uri(value = "/api/openesdh/case/{caseId}/party", method = HttpMethod.PUT, defaultFormat = "json")
    public void put(
            @UriVariable final String caseId,
            WebScriptRequest req) {
        JSONObject json = WebScriptUtils.readJson(req);
        String oldRole = json.get("oldRole").toString();
        String newRole = json.get("newRole").toString();
        String partyId = json.get("partyId").toString();
        partyService.removeCaseParty(caseId, partyId, oldRole);
        partyService.addCaseParty(caseId, newRole, partyId);
    }

    @Uri(value = "/api/openesdh/case/{caseId}/party/{partyRole}", method = HttpMethod.DELETE, defaultFormat = "json")
    public void delete(
            @UriVariable final String caseId,
            @UriVariable final String partyRole,
            @RequestParam final String partyId) {
        partyService.removeCaseParty(caseId, partyId, partyRole);
    }

    private JSONArray buildJSON(Map<String, List<NodeRef>> contactsByRole) {
        JSONArray result = new JSONArray();
        for (Map.Entry<String, List<NodeRef>> entry : contactsByRole.entrySet()) {
            List<NodeRef> contacts = entry.getValue();
            contacts.stream()
                    .map(contactService::getContactInfo)
                    .map(ContactInfo::toJSONObject)
                    .map((contactObj) -> {
                        contactObj.put("role", entry.getKey());
                        return contactObj;
                    }).forEach(result::add);
        }
        return result;
    }
}
