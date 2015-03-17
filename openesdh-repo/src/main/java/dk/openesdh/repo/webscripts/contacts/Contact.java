package dk.openesdh.repo.webscripts.contacts;

import dk.openesdh.repo.model.ContactType;
import dk.openesdh.repo.model.OpenESDHModel;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.repository.NodeRef;
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
 * @author Lanre Abiwon
 */
public class Contact extends ContactAbstractWebscript {

    @Override
    public void post(WebScriptRequest req, WebScriptResponse res) throws IOException {
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
                typeProps.put(OpenESDHModel.PROP_CONTACT_EMAIL, getOrNull(parsedRequest, "email"));
                typeProps.put(OpenESDHModel.PROP_CONTACT_FIRST_NAME, getOrNull(parsedRequest, "firstName"));
                typeProps.put(OpenESDHModel.PROP_CONTACT_LAST_NAME, getOrNull(parsedRequest, "lastName"));
                typeProps.put(OpenESDHModel.PROP_CONTACT_MIDDLE_NAME, getOrNull(parsedRequest, "middleName"));
                typeProps.put(OpenESDHModel.PROP_CONTACT_CPR_NUMBER, getOrNull(parsedRequest, "cprNumber"));
                //TODO There are 2/4 more props that are boolean types to possibly add.
            }
            if (contactType.equalsIgnoreCase("organization")) {
                typeProps.put(OpenESDHModel.PROP_CONTACT_ORGANIZATION_NAME, getOrNull(parsedRequest, "organizationName"));
                typeProps.put(OpenESDHModel.PROP_CONTACT_CVR_NUMBER, getOrNull(parsedRequest, "cvrNumber"));
            }

            //Populate the map with address properties
            getAddressProperties(parsedRequest, typeProps);
            NodeRef createdContact = contactService.createContact(email, contactType, typeProps);
            JSONObject obj;

            if (createdContact != null) {
                obj = buildJSON(createdContact);
            } else {
                obj = new JSONObject();
                obj.put("message", "uncreated");
            }
            obj.writeJSONString(res.getWriter());

        } catch (Exception ge) { //Any generic exception
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Issue creating contact: " + ge.getMessage());
        }
    }

    @Override
    public void get(NodeRef nodeRef, WebScriptRequest req, WebScriptResponse res) throws IOException{
        try{
            String emailId = req.getParameter("email");
            if (nodeRef == null && StringUtils.isNotEmpty(emailId)) {
                nodeRef = contactService.getContactById(emailId);

                if(nodeRef == null)
                    throw new WebScriptException("Unable to retrieve the contact by email.");
            }
            QName contactType = this.nodeService.getType(nodeRef);
            String cTypeString = contactType.equals(OpenESDHModel.TYPE_CONTACT_PERSON) ? "PERSON" : "ORGANIZATION";

            JSONObject simpleObj = buildJSON(nodeRef);
            simpleObj.put("type", cTypeString);
            simpleObj.writeJSONString(res.getWriter());
        }
        catch (NullPointerException npe){
            throw new AlfrescoRuntimeException("Unable to get the person by nodeRef because: "+ npe.getMessage());
        }
    }

}
