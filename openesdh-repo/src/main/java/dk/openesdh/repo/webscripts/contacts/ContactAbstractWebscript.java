package dk.openesdh.repo.webscripts.contacts;

import dk.openesdh.repo.model.ContactType;
import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.contacts.ContactService;
import dk.openesdh.repo.webscripts.utils.ContactUtils;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * @author Lanre
 */
public abstract class ContactAbstractWebscript extends AbstractWebScript {

    private static final String NODE_ID = "id";
    private static final String STORE_ID = "store_id";
    private static final String STORE_TYPE = "store_type";

    protected NodeService nodeService;
    protected ContactService contactService;

    protected abstract void get(NodeRef nodeRef, WebScriptRequest req, WebScriptResponse res) throws IOException;

    protected abstract void post(WebScriptRequest req, WebScriptResponse res);

    protected abstract void put(NodeRef nodeRef, WebScriptRequest req, WebScriptResponse res);

    protected abstract void delete(NodeRef nodeRef, WebScriptRequest req, WebScriptResponse res);

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
        NodeRef nodeRef = null;
        String storeType = templateArgs.get(STORE_TYPE);
        String storeId = templateArgs.get(STORE_ID);
        String nodeId = templateArgs.get(NODE_ID);
        if (storeType != null && storeId != null && nodeId != null) {
            nodeRef = new NodeRef(storeType, storeId, nodeId);
        }

        String method = req.getServiceMatch().getWebScript().getDescription().getMethod();
        switch (method) {
            case "GET":
                get(nodeRef, req, res);
                break;
            case "POST":
                post(req, res);
                break;
            case "PUT":
                put(nodeRef, req, res);
                break;
            case "DELETE":
                delete(nodeRef, req, res);
                break;
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
            return Objects.toString(json.get(key));
        }
        return null;
    }

    public JSONObject buildJSON(NodeRef contactNode) {
        Map<QName, Serializable> props = this.nodeService.getProperties(contactNode);
        return ContactUtils.createContactJson(contactNode, props);
    }

    protected JSONArray getAssociations(NodeRef contactNode) {
        JSONArray associations = new JSONArray();
        final Serializable department = this.nodeService.getProperty(contactNode, OpenESDHModel.PROP_CONTACT_DEPARTMENT);
        contactService.getOrganizationPersons(contactNode).forEach(item -> {
            JSONObject personJson = buildJSON(item);
            personJson.put(OpenESDHModel.PROP_CONTACT_DEPARTMENT.getLocalName(), department);
            associations.add(personJson);
        });
        return associations;
    }

    protected void createAssociation(NodeRef contactNodeRef, JSONObject parsedRequest) throws JSONException {
        if (!parsedRequest.containsKey("parentNodeRefId")) {
            return;
        }
        contactService.addPersonToOrganization(new NodeRef(getOrNull(parsedRequest, "parentNodeRefId")), contactNodeRef);
    }

    void addContactProperties(ContactType contactType, JSONObject fromParsedRequest, HashMap<QName, Serializable> toTypeProps) {
        switch (contactType) {
            case PERSON:
                copyProperty(fromParsedRequest, toTypeProps, OpenESDHModel.PROP_CONTACT_FIRST_NAME);
                copyProperty(fromParsedRequest, toTypeProps, OpenESDHModel.PROP_CONTACT_LAST_NAME);
                copyProperty(fromParsedRequest, toTypeProps, OpenESDHModel.PROP_CONTACT_MIDDLE_NAME);
                copyProperty(fromParsedRequest, toTypeProps, OpenESDHModel.PROP_CONTACT_CPR_NUMBER);
                //TODO There are 2/4 more props that are boolean types to possibly add.
                break;
            case ORGANIZATION:
                copyProperty(fromParsedRequest, toTypeProps, OpenESDHModel.PROP_CONTACT_ORGANIZATION_NAME);
                copyProperty(fromParsedRequest, toTypeProps, OpenESDHModel.PROP_CONTACT_CVR_NUMBER);
                copyProperty(fromParsedRequest, toTypeProps, OpenESDHModel.PROP_CONTACT_DEPARTMENT);
                break;
            default:
                throw new WebScriptException("Incorrect contact type was specified.");
        }
        copyProperty(fromParsedRequest, toTypeProps, OpenESDHModel.PROP_CONTACT_EMAIL);
        copyProperty(fromParsedRequest, toTypeProps, OpenESDHModel.PROP_CONTACT_PHONE);
        copyProperty(fromParsedRequest, toTypeProps, OpenESDHModel.PROP_CONTACT_MOBILE);
        copyProperty(fromParsedRequest, toTypeProps, OpenESDHModel.PROP_CONTACT_WEBSITE);
        copyProperty(fromParsedRequest, toTypeProps, OpenESDHModel.PROP_CONTACT_LINKEDIN);
        copyProperty(fromParsedRequest, toTypeProps, OpenESDHModel.PROP_CONTACT_IM);
        copyProperty(fromParsedRequest, toTypeProps, OpenESDHModel.PROP_CONTACT_NOTES);

        //Populate the map with address properties
        addAddressProperties(fromParsedRequest, toTypeProps);
    }

    private void addAddressProperties(JSONObject fromObj, HashMap<QName, Serializable> toTypeProps) {
        copyProperty(fromObj, toTypeProps, OpenESDHModel.PROP_CONTACT_ADDRESS);
        copyProperty(fromObj, toTypeProps, OpenESDHModel.PROP_CONTACT_ADDRESS_LINE1);
        copyProperty(fromObj, toTypeProps, OpenESDHModel.PROP_CONTACT_ADDRESS_LINE2);
        copyProperty(fromObj, toTypeProps, OpenESDHModel.PROP_CONTACT_ADDRESS_LINE3);
        copyProperty(fromObj, toTypeProps, OpenESDHModel.PROP_CONTACT_ADDRESS_LINE4);
        copyProperty(fromObj, toTypeProps, OpenESDHModel.PROP_CONTACT_ADDRESS_LINE5);
        copyProperty(fromObj, toTypeProps, OpenESDHModel.PROP_CONTACT_ADDRESS_LINE6);
        copyProperty(fromObj, toTypeProps, OpenESDHModel.PROP_CONTACT_HOUSE_NUMBER);
        copyProperty(fromObj, toTypeProps, OpenESDHModel.PROP_CONTACT_STREET_NAME);
        copyProperty(fromObj, toTypeProps, OpenESDHModel.PROP_CONTACT_STREET_CODE);
        copyProperty(fromObj, toTypeProps, OpenESDHModel.PROP_CONTACT_SUITE_IDENTIFIER);
        copyProperty(fromObj, toTypeProps, OpenESDHModel.PROP_CONTACT_FLOOR_IDENTIFIER);
        copyProperty(fromObj, toTypeProps, OpenESDHModel.PROP_CONTACT_CITY_NAME);
        copyProperty(fromObj, toTypeProps, OpenESDHModel.PROP_CONTACT_POST_CODE);
        copyProperty(fromObj, toTypeProps, OpenESDHModel.PROP_CONTACT_POST_BOX);
        copyProperty(fromObj, toTypeProps, OpenESDHModel.PROP_CONTACT_POST_DISTRICT);
        copyProperty(fromObj, toTypeProps, OpenESDHModel.PROP_CONTACT_COUNTRY_CODE);
        copyProperty(fromObj, toTypeProps, OpenESDHModel.PROP_CONTACT_MUNICIPALITY_CODE);
        copyProperty(fromObj, toTypeProps, OpenESDHModel.PROP_CONTACT_MAIL_SUBLOCATION_ID);
    }

    void copyProperty(JSONObject fromObj, HashMap<QName, Serializable> toTypeProps, QName property) {
        toTypeProps.put(property, getOrNull(fromObj, property.getLocalName()));
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
