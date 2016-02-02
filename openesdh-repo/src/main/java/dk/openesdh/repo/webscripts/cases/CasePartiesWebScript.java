package dk.openesdh.repo.webscripts.cases;

import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.RequestParam;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Transaction;
import com.github.dynamicextensionsalfresco.webscripts.annotations.TransactionType;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.UriVariable;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import com.github.dynamicextensionsalfresco.webscripts.resolutions.Resolution;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.stereotype.Component;

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
        Map<String, List<NodeRef>> contactsByRole = partyService.getContactsByRole(caseId);
        JSONArray json = buildJSON(contactsByRole);
        return WebScriptUtils.jsonResolution(json);
    }

    @Uri(value = "/api/openesdh/case/{caseId}/party/{partyRole}", method = HttpMethod.POST, defaultFormat = "json")
    public Resolution post(
            @UriVariable final String caseId,
            @UriVariable final String partyRole,
            WebScriptRequest req
    ) throws ParseException, IOException {
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(req.getContent().getContent());
        JSONArray contacts = (JSONArray) json.get("contactNodeRefs");
        List contactNodes = Arrays.asList(contacts.toArray(new String[0]));
        partyService.addContactsToParty(caseId, null, partyRole, contactNodes);
        return WebScriptUtils.jsonResolution(json);
    }

    @Uri(value = "/api/openesdh/case/{caseId}/party", method = HttpMethod.PUT, defaultFormat = "json")
    public Resolution put(
            @UriVariable final String caseId,
            WebScriptRequest req
    ) throws ParseException, IOException {
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(req.getContent().getContent());
        String oldRole = json.get("oldRole").toString();
        String newRole = json.get("newRole").toString();
        String partyId = json.get("partyId").toString();

        if (!partyService.removePartyRole(caseId, partyId, oldRole)) {
            throw new WebScriptException("Unable to remove " + partyId + " from the " + oldRole + ".\nTry removing again or contact the system administrator");
        }

        if (!partyService.addContactToParty(caseId, null, newRole, partyId)) {
            throw new WebScriptException("Unable to change " + partyId + "'s role to " + newRole + ".");
        }
        return WebScriptUtils.jsonResolution(json);

    }

    @Uri(value = "/api/openesdh/case/{caseId}/party/{partyRole}", method = HttpMethod.DELETE, defaultFormat = "json")
    public void delete(
            @UriVariable final String caseId,
            @UriVariable final String partyRole,
            @RequestParam final String partyId
    ) {
        try {
            //Then we remove from the old role after adding just in case
            if (!partyService.removePartyRole(caseId, partyId, partyRole)) {
                throw new WebScriptException("Unable to remove " + partyId + " from the " + partyRole + ".\n"
                        + "Try removing again or contact the system administrator");
            }
        } catch (Exception ge) {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Role deletion failure. Reason: " + ge.getMessage());
        }
    }

    private JSONArray buildJSON(Map<String, List<NodeRef>> contactsByRole) throws JSONException {
        JSONArray result = new JSONArray();
        for (Map.Entry<String, List<NodeRef>> entry : contactsByRole.entrySet()) {
            List<NodeRef> contacts = entry.getValue();
            for (NodeRef contactRef : contacts) {
                JSONObject contactObj = new JSONObject();
//                NodeRef contactRef = contactService.getContactById(contact);
                ContactInfo contactInfo = contactService.getContactInfo(contactRef);

                contactObj.put("contactType", contactInfo.getType());
                contactObj.put("contactId", contactInfo.getEmail()); //TODO perhaps look into allowing the id to change in the future??
                contactObj.put("displayName", contactInfo.getName());
                contactObj.put("nodeRef", contactInfo.getNodeRef().toString());
                contactObj.put("role", entry.getKey());
                result.add(contactObj);
            }
        }
        return result;
    }
}
