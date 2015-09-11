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

            addContactProperties(contactType, parsedRequest, typeProps);

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

            QName contactTypeQName = this.nodeService.getType(contactNodeRef);
            ContactType contactType = contactTypeQName.equals(OpenESDHModel.TYPE_CONTACT_PERSON) ? ContactType.PERSON : ContactType.ORGANIZATION;

            addContactProperties(contactType, parsedRequest, typeProps);

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

    @Override
    protected void delete(NodeRef nodeRef, WebScriptRequest req, WebScriptResponse res) {
        try {
            this.nodeService.deleteNode(nodeRef);
        } catch (Exception ge) { //Any generic exception
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Issue deleting contact: " + ge.getMessage());
        }
    }
}
