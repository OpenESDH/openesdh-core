package dk.openesdh.repo.webscripts.contacts;

import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.contacts.ContactService;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.json.simple.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
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
        JSONObject result = new JSONObject();
        Map<QName, Serializable> props = this.nodeService.getProperties(contactNode);
        props.entrySet().stream()
                .filter((Map.Entry<QName, Serializable> t)
                        -> t.getValue() != null && !isKeyOfSystemModelNamepace(t.getKey()))
                .forEach((entry)
                        -> result.put(entry.getKey().getLocalName(), entry.getValue()));
        return result;
    }

    private boolean isKeyOfSystemModelNamepace(QName key) {
        return key.getNamespaceURI().equalsIgnoreCase(NamespaceService.SYSTEM_MODEL_1_0_URI);
    }

    void addAddressProperties(JSONObject fromObj, HashMap<QName, Serializable> toTypeProps) {
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

    protected void copyProperty(JSONObject fromObj, HashMap<QName, Serializable> toTypeProps, QName property) {
        toTypeProps.put(property, getOrNull(fromObj, property.getLocalName()));
    }

    protected abstract void get(NodeRef nodeRef, WebScriptRequest req, WebScriptResponse res) throws IOException;

    protected abstract void post(WebScriptRequest req, WebScriptResponse res);

    protected abstract void put(NodeRef nodeRef, WebScriptRequest req, WebScriptResponse res);

    //<editor-fold desc="Injected service bean setters">
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setContactService(ContactService contactService) {
        this.contactService = contactService;
    }
    //</editor-fold>
}
