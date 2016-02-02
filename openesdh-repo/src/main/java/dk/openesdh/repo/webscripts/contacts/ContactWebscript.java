package dk.openesdh.repo.webscripts.contacts;

import static dk.openesdh.repo.webscripts.contacts.ContactUtils.addContactProperties;
import static dk.openesdh.repo.webscripts.contacts.ContactUtils.getNodeRef;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import com.github.dynamicextensionsalfresco.webscripts.resolutions.Resolution;

import dk.openesdh.repo.model.ContactType;
import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.contacts.ContactService;
import dk.openesdh.repo.webscripts.ParamUtils;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

@Component
@WebScript(description = "Contact CRUD operations", families = {"Contact"})
public class ContactWebscript {

    @Autowired
    @Qualifier("NodeService")
    private NodeService nodeService;
    @Autowired
    private ContactService contactService;

    @Uri(value = "/api/openesdh/contacts/create", method = HttpMethod.POST, defaultFormat = "json")
    public Resolution post(WebScriptRequest req, WebScriptResponse res) {
        JSONObject parsedRequest;
        try {
            //Get the information from the JSON structure from the request
            Map<QName, Serializable> typeProps = new HashMap<>();

            // Parse the JSON, if supplied
            JSONParser parser = new JSONParser();
            try {
                parsedRequest = (JSONObject) parser.parse(req.getContent().getContent());
            } catch (IOException | ParseException io) {
                throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Invalid JSON: " + io.getMessage());
            }

            String email = ParamUtils.getOrNull(parsedRequest, "email");
            if (StringUtils.isBlank(email)) {
                throw new WebScriptException(Status.STATUS_BAD_REQUEST, "The email is required to create the contact.");
            }

            String contactTypeParam = ParamUtils.getOrNull(parsedRequest, "contactType");
            if (StringUtils.isBlank(contactTypeParam)) {
                throw new WebScriptException(Status.STATUS_BAD_REQUEST, "No contact type was specified.");
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
            return WebScriptUtils.jsonResolution(obj);
        } catch (Exception ge) { //Any generic exception
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Issue creating contact: " + ge.getMessage());
        }
    }

    @Uri(value = "/api/openesdh/contact/{store_type}/{store_id}/{id}", method = HttpMethod.PUT, defaultFormat = "json")
    public Resolution put(WebScriptRequest req, WebScriptResponse res) {
        NodeRef nodeRef = getNodeRef(req);
        JSONObject parsedRequest;
        try {
            //Get the information from the JSON structure from the request
            Map<QName, Serializable> typeProps = nodeService.getProperties(nodeRef);

            // Parse the JSON, if supplied
            JSONParser parser = new JSONParser();
            try {
                parsedRequest = (JSONObject) parser.parse(req.getContent().getContent());
            } catch (IOException | ParseException e) {
                throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Invalid JSON: " + e.getMessage());
            }

            String email = ParamUtils.getOrNull(parsedRequest, "email");
            if (StringUtils.isBlank(email)) {
                throw new WebScriptException(Status.STATUS_BAD_REQUEST, "The contact email is missing. Unable to further proceed.");
            }

            QName contactTypeQName = this.nodeService.getType(nodeRef);
            ContactType contactType = contactTypeQName.equals(OpenESDHModel.TYPE_CONTACT_PERSON) ? ContactType.PERSON : ContactType.ORGANIZATION;

            addContactProperties(contactType, parsedRequest, typeProps);

            this.nodeService.setProperties(nodeRef, typeProps);

            JSONObject obj;

            if (nodeRef != null) {
                obj = buildJSON(nodeRef);
            } else {
                obj = new JSONObject();
                obj.put("message", "Contact not updated");
            }
            return WebScriptUtils.jsonResolution(obj);
        } catch (Exception ge) { //Any generic exception
            throw new WebScriptException("Issue updating contact: " + ge.getMessage());
        }
    }

    @Uri(value = {
        "/api/openesdh/contact/{store_type}/{store_id}/{id}",
        "/api/openesdh/contact?email=",
        "/api/openesdh/contact?parentNodeRefId="},
            method = HttpMethod.GET, defaultFormat = "json")
    public Resolution get(WebScriptRequest req, WebScriptResponse res) throws IOException {
        NodeRef nodeRef = getNodeRef(req);
        try {
            String parentNodeRefId = req.getParameter("parentNodeRefId");
            if (nodeRef == null && StringUtils.isNotEmpty(parentNodeRefId)) {
                return WebScriptUtils.jsonResolution(
                        getAssociations(new NodeRef(parentNodeRefId)));
            }
            String emailId = req.getParameter("email");
            if (nodeRef == null && StringUtils.isNotEmpty(emailId)) {
                nodeRef = contactService.getContactById(emailId);

                if (nodeRef == null) {
                    throw new WebScriptException("Unable to retrieve the contact by email.");
                }
            }
            return WebScriptUtils.jsonResolution(
                    buildJSON(nodeRef));
        } catch (WebScriptException | InvalidNodeRefException npe) {
            throw new WebScriptException("Unable to get the person by nodeRef because: " + npe.getMessage());
        }
    }

    @Uri(value = "/api/openesdh/contact/{store_type}/{store_id}/{id}", method = HttpMethod.DELETE, defaultFormat = "json")
    public void delete(WebScriptRequest req, WebScriptResponse res) {
        contactService.deleteContact(getNodeRef(req));
    }

    private JSONObject buildJSON(NodeRef contactNode) {
        Map<QName, Serializable> props = this.nodeService.getProperties(contactNode);
        return ContactUtils.createContactJson(contactNode, props);
    }

    private JSONArray getAssociations(NodeRef contactNode) {
        JSONArray associations = new JSONArray();
        final Serializable department = this.nodeService.getProperty(contactNode, OpenESDHModel.PROP_CONTACT_DEPARTMENT);
        Stream<NodeRef> organizationPersons = contactService.getOrganizationPersons(contactNode);
        organizationPersons.forEach(item -> {
            JSONObject personJson = buildJSON(item);
            personJson.put(OpenESDHModel.PROP_CONTACT_DEPARTMENT.getLocalName(), department);
            associations.add(personJson);
        });
        return associations;
    }

    private void createAssociation(NodeRef contactNodeRef, JSONObject parsedRequest) throws JSONException {
        if (!parsedRequest.containsKey("parentNodeRefId")) {
            return;
        }
        contactService.addPersonToOrganization(new NodeRef((String) parsedRequest.get("parentNodeRefId")), contactNodeRef);
    }
}
