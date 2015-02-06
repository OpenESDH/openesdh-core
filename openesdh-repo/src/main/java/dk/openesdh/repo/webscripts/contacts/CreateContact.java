package dk.openesdh.repo.webscripts.contacts;

import dk.openesdh.repo.model.ContactInfo;
import dk.openesdh.repo.model.ContactType;
import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.contacts.ContactService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.*;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

/**
 * @author Lanre
 */
public class CreateContact extends AbstractWebScript {
    NodeService nodeService;
    ContactService contactService;

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        JSONObject parsedRequest;
        try {
            //Get the information from the JSON structure from the request
            HashMap<QName, Serializable> typeProps = new HashMap<QName, Serializable>();

            // Parse the JSON, if supplied
            JSONParser parser = new JSONParser();
            try {
                parsedRequest = (JSONObject) parser.parse(req.getContent().getContent());
            } catch (IOException io) {
                throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Invalid JSON: " + io.getMessage());
            } catch (ParseException pe) {
                throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Invalid JSON: " + pe.getMessage());
            }

            String email = getOrNull(parsedRequest, "email");
            if (StringUtils.isBlank(email))
                throw new WebScriptException("The email is required to create the contact.");

            String contactType = getOrNull(parsedRequest, "contactType");
            if (StringUtils.isBlank(contactType) || (!contactType.equalsIgnoreCase(ContactType.valueOf(StringUtils.capitalize(contactType)).toString())) )
                throw new WebScriptException("No/Incorrect contact type was specified.");

            if (contactType.equalsIgnoreCase("person")) {
                typeProps.put(OpenESDHModel.PROP_CONTACT_FIRST_NAME, getOrNull(parsedRequest, "firstName"));
                typeProps.put(OpenESDHModel.PROP_CONTACT_LAST_NAME, getOrNull(parsedRequest, "lastName"));
                typeProps.put(OpenESDHModel.PROP_CONTACT_MIDDLE_NAME, getOrNull(parsedRequest, "middleName"));
                typeProps.put(OpenESDHModel.PROP_CONTACT_CPR_NUMBER, getOrNull(parsedRequest, "cprNumber"));
                //TODO There are 2/4 more props that are boolean types to possibly add.
            }
            if (contactType.equalsIgnoreCase("organisation")) {
                typeProps.put(OpenESDHModel.PROP_CONTACT_ORGANIZATION_NAME, getOrNull(parsedRequest, "organizationName"));
                typeProps.put(OpenESDHModel.PROP_CONTACT_CVR_NUMBER, getOrNull(parsedRequest, "cvrNumber"));
            }

            //Populate the map with address properties
            getAddressProperties(parsedRequest, typeProps);

            NodeRef createdContact = contactService.createContact(email, contactType, typeProps);

            JSONObject obj = new JSONObject();

            if (createdContact != null) {
                obj.put("contactNodeRef", createdContact.toString());
                obj.put("type", this.nodeService.getProperty(createdContact, OpenESDHModel.PROP_CONTACT_TYPE));
            } else
                obj.put("message", "uncreated");

            res.getWriter().write(obj.toJSONString());
        } catch (Exception ge) { //Any generic exception
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Issue creating contact: " + ge.getMessage());
        }
    }

    /**
     * Grabbed from the org.alfresco.repo.web.scripts.discussion.AbstractDiscussionWebScript
     *
     * @param json
     * @param key
     * @return
     */
    public String getOrNull(JSONObject json, String key) {
        if (json.containsKey(key)) {
            return (String) json.get(key);
        }
        return null;
    }

    void getAddressProperties(JSONObject obj, HashMap<QName, Serializable> typeProps){

        typeProps.put(OpenESDHModel.PROP_CONTACT_ADDRESS, getOrNull(obj,"address") );
        typeProps.put(OpenESDHModel.PROP_CONTACT_ADDRESS_LINE1, getOrNull(obj,"addressLine1") );
        typeProps.put(OpenESDHModel.PROP_CONTACT_ADDRESS_LINE2, getOrNull(obj,"addressLine2") );
        typeProps.put(OpenESDHModel.PROP_CONTACT_ADDRESS_LINE3, getOrNull(obj,"addressLine3") );
        typeProps.put(OpenESDHModel.PROP_CONTACT_ADDRESS_LINE4, getOrNull(obj,"addressLine4") );
        typeProps.put(OpenESDHModel.PROP_CONTACT_ADDRESS_LINE5, getOrNull(obj,"addressLine5") );
        typeProps.put(OpenESDHModel.PROP_CONTACT_ADDRESS_LINE6, getOrNull(obj,"addressLine6") );
        typeProps.put(OpenESDHModel.PROP_CONTACT_HOUSE_NUMBER, getOrNull(obj,"houseNumber") );
        typeProps.put(OpenESDHModel.PROP_CONTACT_STREET_NAME, getOrNull(obj,"streetName") );
        typeProps.put(OpenESDHModel.PROP_CONTACT_STREET_CODE, getOrNull(obj,"streetCode") );
        typeProps.put(OpenESDHModel.PROP_CONTACT_SUITE_IDENTIFIER, getOrNull(obj,"suiteIdentifier") );
        typeProps.put(OpenESDHModel.PROP_CONTACT_FLOOR_IDENTIFIER, getOrNull(obj,"floorIdentifier") );
        typeProps.put(OpenESDHModel.PROP_CONTACT_CITY_NAME, getOrNull(obj,"cityName") );
        typeProps.put(OpenESDHModel.PROP_CONTACT_POST_CODE, getOrNull(obj,"postCode") );
        typeProps.put(OpenESDHModel.PROP_CONTACT_POST_BOX, getOrNull(obj,"postBox") );
        typeProps.put(OpenESDHModel.PROP_CONTACT_POST_DISTRICT, getOrNull(obj,"postDistrict") );
        typeProps.put(OpenESDHModel.PROP_CONTACT_COUNTRY_CODE, getOrNull(obj,"countryCode") );
        typeProps.put(OpenESDHModel.PROP_CONTACT_MUNICIPALITY_CODE, getOrNull(obj,"municipalityCode") );
        typeProps.put(OpenESDHModel.PROP_CONTACT_MAIL_SUBLOCATION_ID, getOrNull(obj,"mailDeliverySublocationIdentifier") );

    }

    //<editor-fold desc="Injected service bean setters">
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setContactService(ContactService contactService) {
        this.contactService = contactService;
    }
    //</editor-fold>

}
