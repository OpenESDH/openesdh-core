package dk.openesdh.repo.webscripts.cases;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeService;
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

import dk.openesdh.repo.model.ContactInfo;
import dk.openesdh.repo.services.cases.PartyService;
import dk.openesdh.repo.services.contacts.ContactService;

public class CasePartyRole extends AbstractWebScript {

    private ContactService contactService;
    private PartyService partyService;
    private NodeService nodeService;

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        String method = req.getServiceMatch().getWebScript().getDescription().getMethod();
        Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
        String caseId = templateArgs.get("caseId");
        String partyRole = templateArgs.get("partyRole");

        if (StringUtils.isBlank(caseId))
            throw new WebScriptException("The caseId role can't be empty/blank");
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
            e.printStackTrace();
        }
    }

    private void get(WebScriptRequest req, WebScriptResponse res, String caseId) throws IOException, JSONException {
        Map<String, Set<ContactInfo>> contactsByRole = partyService.getContactsByRole(caseId);
        JSONArray json = buildJSON(contactsByRole);
        json.writeJSONString(res.getWriter());
    }

    private void post(WebScriptRequest req, WebScriptResponse res, String caseId, String partyRole) throws IOException, JSONException {
        JSONObject json;
        JSONParser parser = new JSONParser();
        boolean result;
        try {
            json = (JSONObject) parser.parse(req.getContent().getContent());
            JSONArray contacts = (JSONArray) json.get("contactNodeRefs");
            List<String> contactNodes = new ArrayList<>();
            for (Object nodeRef : contacts)
                contactNodes.add(nodeRef.toString());

            result = this.partyService.addContactsToParty(caseId, null, partyRole, contactNodes);

        }
        catch (ParseException pe) {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Invalid JSON: " + pe.getMessage());
        }

        if (result)
            json.writeJSONString(res.getWriter());
    }

    private void put(WebScriptRequest req, WebScriptResponse res, String caseId) throws IOException, JSONException {
        JSONObject json;
        JSONParser parser = new JSONParser();
        try {
            json = (JSONObject) parser.parse(req.getContent().getContent());
            String oldRole = json.get("fromRole").toString();
            String newRole = json.get("newRole").toString();
            String partyId = json.get("partyId").toString();

            //First we add the party to the new Role. We do this first in case of failure we wouldn't have removed the contact
            if(!this.partyService.addContactToParty(caseId, null, newRole, partyId))
                throw new WebScriptException("Unable to change "+ partyId+"'s role to "+newRole+".");

            //Then we remove from the old role after adding just in case
            if(!this.partyService.removePartyRole(caseId, partyId, oldRole))
                throw new WebScriptException("Unable to remove "+ partyId+" from the "+oldRole+".\nTry removing again or contact the system administrator");

        }
        catch (Exception ge) {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Role change failure for the following reason: " + ge.getMessage());
        }

            json.writeJSONString(res.getWriter());
    }

    private void delete(WebScriptRequest req, WebScriptResponse res, String caseId, String partyRole) throws IOException {
        /*JSONObject json;
        JSONParser parser = new JSONParser();*/
        try {
            String partyId =req.getParameter("partyId");
            /* Unable to get this to work for the moment so we retrieve what we need by query string
            json = (JSONObject) parser.parse(req.getContent().getContent());
            String partyId = json.get("partyId").toString();*/

            //Then we remove from the old role after adding just in case
            if(!this.partyService.removePartyRole(caseId, partyId, partyRole))
                throw new WebScriptException("Unable to remove "+ partyId+" from the "+partyRole+".\nTry removing again or contact the system administrator");
        }
        catch (Exception ge) {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Role deletion failure. Reason: " + ge.getMessage());
        }

//        json.writeJSONString(res.getWriter());
    }

    JSONArray buildJSON(Map<String, Set<ContactInfo>> contactsByRole) throws JSONException {
        JSONArray result = new JSONArray();

        for (Map.Entry<String, Set<ContactInfo>> entry : contactsByRole.entrySet()) {
            Set<ContactInfo> parties = entry.getValue();
            for (ContactInfo contactInfo : parties) {
                JSONObject contactObj = new JSONObject();
                contactObj.put(ContactInfo.PROP_NAME_CONTACT_TYPE, contactInfo.getType());
                contactObj.put(ContactInfo.PROP_NAME_CONTACT_ID, contactInfo.getEmail()); //TODO perhaps look into allowing the id to change in the future??
                contactObj.put(ContactInfo.PROP_NAME_CONTACT_DISPLAY_NAME, contactInfo.getName());
                contactObj.put(ContactInfo.PROP_NAME_CONTACT_NODE_REF, contactInfo.getNodeRef().toString());
                contactObj.put(ContactInfo.PROP_NAME_CONTACT_ROLE, entry.getKey());

                contactObj.put(ContactInfo.PROP_NAME_CONTACT_STREET_NAME, contactInfo.getStreetName());
                contactObj.put(ContactInfo.PROP_NAME_CONTACT_HOUSE_NUMBER, contactInfo.getHouseNumber());
                contactObj.put(ContactInfo.PROP_NAME_CONTACT_POST_CODE, contactInfo.getPostCode());
                contactObj.put(ContactInfo.PROP_NAME_CONTACT_CITY_NAME, contactInfo.getCityName());
                contactObj.put(ContactInfo.PROP_NAME_CONTACT_COUNTRY_CODE, contactInfo.getCountryCode());
                contactObj.put(ContactInfo.PROP_NAME_CONTACT_POST_BOX, contactInfo.getPostBox());
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

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    //</editor-fold>

}
