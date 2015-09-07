package dk.openesdh.repo.webscripts.contacts;

import dk.openesdh.repo.model.ContactType;
import dk.openesdh.repo.model.OpenESDHModel;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * @author Lanre Abiwon
 */
public class ContactWebscript extends ContactAbstractWebscript {

    @Override
    public void post(WebScriptRequest req, WebScriptResponse res) {
        JSONObject parsedRequest;
        try {
            //Get the information from the JSON structure from the request
            HashMap<QName, Serializable> typeProps = new HashMap<>();

            // Parse the JSON, if supplied
            JSONParser parser = new JSONParser();
            try {
                parsedRequest = (JSONObject) parser.parse(req.getContent().getContent());
            } catch (IOException | ParseException io) {
                throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Invalid JSON: " + io.getMessage());
            }

            String email = getOrNull(parsedRequest, "email");
            if (StringUtils.isBlank(email)) {
                throw new WebScriptException("The email is required to create the contact.");
            }

            String contactTypeParam = getOrNull(parsedRequest, "contactType");
            if (StringUtils.isBlank(contactTypeParam)) {
                throw new WebScriptException("No contact type was specified.");
            }
            ContactType contactType = ContactType.getContactType(contactTypeParam);
            switch (contactType) {
                case PERSON:
                    typeProps.put(OpenESDHModel.PROP_CONTACT_EMAIL, getOrNull(parsedRequest, "email"));
                    typeProps.put(OpenESDHModel.PROP_CONTACT_FIRST_NAME, getOrNull(parsedRequest, "firstName"));
                    typeProps.put(OpenESDHModel.PROP_CONTACT_LAST_NAME, getOrNull(parsedRequest, "lastName"));
                    typeProps.put(OpenESDHModel.PROP_CONTACT_MIDDLE_NAME, getOrNull(parsedRequest, "middleName"));
                    typeProps.put(OpenESDHModel.PROP_CONTACT_CPR_NUMBER, getOrNull(parsedRequest, "cprNumber"));
                    //TODO There are 2/4 more props that are boolean types to possibly add.
                    break;
                case ORGANIZATION:
                    typeProps.put(OpenESDHModel.PROP_CONTACT_ORGANIZATION_NAME, getOrNull(parsedRequest, "organizationName"));
                    typeProps.put(OpenESDHModel.PROP_CONTACT_EMAIL, getOrNull(parsedRequest, "email"));
                    typeProps.put(OpenESDHModel.PROP_CONTACT_CVR_NUMBER, getOrNull(parsedRequest, "cvrNumber"));
                    break;
                default:
                    throw new WebScriptException("Incorrect contact type was specified.");
            }

            //Populate the map with address properties
            addAddressProperties(parsedRequest, typeProps);
            NodeRef createdContact = contactService.createContact(email, contactTypeParam, typeProps);

            createAssociation(createdContact, parsedRequest);

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
    public void put(NodeRef contactNodeRef, WebScriptRequest req, WebScriptResponse res) {
        JSONObject parsedRequest;
        try {
            //Get the information from the JSON structure from the request
            HashMap<QName, Serializable> typeProps = new HashMap<>();

            // Parse the JSON, if supplied
            JSONParser parser = new JSONParser();
            try {
                parsedRequest = (JSONObject) parser.parse(req.getContent().getContent());
            } catch (IOException | ParseException e) {
                throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Invalid JSON: " + e.getMessage());
            }

            String email = getOrNull(parsedRequest, "email");
            if (StringUtils.isBlank(email)) {
                throw new WebScriptException("The contact email is missing. Unable to further proceed.");
            }

            QName contactType = this.nodeService.getType(contactNodeRef);
            if (contactType.equals(OpenESDHModel.TYPE_CONTACT_PERSON)) {
                copyProperty(parsedRequest, typeProps, OpenESDHModel.PROP_CONTACT_EMAIL);
                copyProperty(parsedRequest, typeProps, OpenESDHModel.PROP_CONTACT_FIRST_NAME);
                copyProperty(parsedRequest, typeProps, OpenESDHModel.PROP_CONTACT_LAST_NAME);
                copyProperty(parsedRequest, typeProps, OpenESDHModel.PROP_CONTACT_MIDDLE_NAME);
                copyProperty(parsedRequest, typeProps, OpenESDHModel.PROP_CONTACT_CPR_NUMBER);
                //TODO There are 2/4 more props that are boolean types to possibly add.
            }
            if (contactType.equals(OpenESDHModel.TYPE_CONTACT_ORGANIZATION)) {
                copyProperty(parsedRequest, typeProps, OpenESDHModel.PROP_CONTACT_ORGANIZATION_NAME);
                copyProperty(parsedRequest, typeProps, OpenESDHModel.PROP_CONTACT_EMAIL);
                copyProperty(parsedRequest, typeProps, OpenESDHModel.PROP_CONTACT_CVR_NUMBER);
            }

            //Populate the map with address properties
            addAddressProperties(parsedRequest, typeProps);

            this.nodeService.setProperties(contactNodeRef, typeProps);

            JSONObject obj;

            if (contactNodeRef != null) {
                obj = buildJSON(contactNodeRef);
            } else {
                obj = new JSONObject();
                obj.put("message", "Contact not updated");
            }
            obj.writeJSONString(res.getWriter());

        } catch (Exception ge) { //Any generic exception
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Issue updating contact: " + ge.getMessage());
        }
    }

    @Override
    public void get(NodeRef nodeRef, WebScriptRequest req, WebScriptResponse res) throws IOException {
        try {
            String parentNodeRefId = req.getParameter("parentNodeRefId");
            if (nodeRef == null && StringUtils.isNotEmpty(parentNodeRefId)) {
                getAssociations(new NodeRef(parentNodeRefId))
                        .writeJSONString(res.getWriter());
                return;
            }

            String emailId = req.getParameter("email");
            if (nodeRef == null && StringUtils.isNotEmpty(emailId)) {
                nodeRef = contactService.getContactById(emailId);

                if (nodeRef == null) {
                    throw new WebScriptException("Unable to retrieve the contact by email.");
                }
            }
            buildJSON(nodeRef).writeJSONString(res.getWriter());
        } catch (WebScriptException | InvalidNodeRefException npe) {
            throw new AlfrescoRuntimeException("Unable to get the person by nodeRef because: " + npe.getMessage());
        }
    }

    private void createAssociation(NodeRef contactNodeRef, JSONObject parsedRequest) throws JSONException {
        if (!parsedRequest.containsKey("parentNodeRefId")) {
            return;
        }
        nodeService.createAssociation(new NodeRef(getOrNull(parsedRequest, "parentNodeRefId")), contactNodeRef, OpenESDHModel.ASSOC_CONTACT_MEMBERS);
    }

}
