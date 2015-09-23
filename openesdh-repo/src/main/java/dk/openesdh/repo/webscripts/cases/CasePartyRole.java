package dk.openesdh.repo.webscripts.cases;

import dk.openesdh.repo.model.ContactInfo;
import dk.openesdh.repo.services.cases.PartyService;
import dk.openesdh.repo.services.contacts.ContactService;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

public class CasePartyRole extends AbstractWebScript {

    private ContactService contactService;
    private PartyService partyService;

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        String method = req.getServiceMatch().getWebScript().getDescription().getMethod();
        Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
        String caseId = templateArgs.get("caseId");
        String partyRole = templateArgs.get("partyRole");

        if (StringUtils.isBlank(caseId)) {
            throw new WebScriptException("The caseId role can't be empty/blank");
        }
        try {
            switch (method) {
                case "GET":
                    get(req, res, caseId);
                    break;
                case "POST":
                    post(req, res, caseId, partyRole);
                    break;
                case "PUT":
                    put(req, res, caseId);
                    break;
                case "DELETE":
                    delete(req, res, caseId, partyRole);
                    break;
            }
        } catch (JSONException e) {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Invalid JSON: " + e.getMessage());
        }
    }

    private void get(WebScriptRequest req, WebScriptResponse res, String caseId) throws IOException, JSONException {
        Map<String, Set<String>> contactsByRole = partyService.getContactsByRole(caseId);
        JSONArray json = buildJSON(contactsByRole);
        json.writeJSONString(res.getWriter());
    }

    private void post(WebScriptRequest req, WebScriptResponse res, String caseId, String partyRole) throws IOException {
        try {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(req.getContent().getContent());
            JSONArray contacts = (JSONArray) json.get("contactNodeRefs");
            List contactNodes = Arrays.asList(contacts.toArray(new String[0]));
            boolean result = this.partyService.addContactsToParty(caseId, null, partyRole, contactNodes);
            if (result) {
                json.writeJSONString(res.getWriter());
            }
        } catch (ParseException pe) {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Invalid JSON: " + pe.getMessage());
        }
    }

    private void put(WebScriptRequest req, WebScriptResponse res, String caseId) throws IOException {
        try {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(req.getContent().getContent());
            String oldRole = json.get("oldRole").toString();
            String newRole = json.get("newRole").toString();
            String partyId = json.get("partyId").toString();

            if (!this.partyService.removePartyRole(caseId, partyId, oldRole)) {
                throw new WebScriptException("Unable to remove " + partyId + " from the " + oldRole + ".\nTry removing again or contact the system administrator");
            }

            if (!this.partyService.addContactToParty(caseId, null, newRole, partyId)) {
                throw new WebScriptException("Unable to change " + partyId + "'s role to " + newRole + ".");
            }
            json.writeJSONString(res.getWriter());
        } catch (Exception ge) {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Role change failure for the following reason: " + ge.getMessage());
        }

    }

    private void delete(WebScriptRequest req, WebScriptResponse res, String caseId, String partyRole) throws IOException {
        try {
            String partyId = req.getParameter("partyId");
            //Then we remove from the old role after adding just in case
            if (!this.partyService.removePartyRole(caseId, partyId, partyRole)) {
                throw new WebScriptException("Unable to remove " + partyId + " from the " + partyRole + ".\nTry removing again or contact the system administrator");
            }
        } catch (Exception ge) {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Role deletion failure. Reason: " + ge.getMessage());
        }
    }

    JSONArray buildJSON(Map<String, Set<String>> contactsByRole) throws JSONException {
        JSONArray result = new JSONArray();
        for (Map.Entry<String, Set<String>> entry : contactsByRole.entrySet()) {
            Set<String> contacts = entry.getValue();
            for (String contact : contacts) {
                JSONObject contactObj = new JSONObject();
                NodeRef contactRef = this.contactService.getContactById(contact);
                ContactInfo contactInfo = this.contactService.getContactInfo(contactRef);

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

    //<editor-fold desc="Injected bean service setters">
    public void setContactService(ContactService contactService) {
        this.contactService = contactService;
    }

    public void setPartyService(PartyService partyService) {
        this.partyService = partyService;
    }

    //</editor-fold>
}
