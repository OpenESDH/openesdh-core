package dk.openesdh.repo.webscripts.contacts;

import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.contacts.ContactService;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
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
            return (String) json.get(key);
        }
        return null;
    }

    public JSONObject buildJSON(NodeRef contactNode) {
        JSONObject result = new JSONObject();
        Map<QName, Serializable> props = this.nodeService.getProperties(contactNode);
        props.entrySet().stream().forEach((entry) -> {
            Serializable value = entry.getValue();
            QName key = entry.getKey();
            String localName = key.getLocalName();
            if (value != null && !isKeyOfSystemModelNamepace(key)) {
                result.put(localName, value);
            }
        });
        return result;
    }

    private boolean isKeyOfSystemModelNamepace(QName key) {
        return key.getNamespaceURI().equalsIgnoreCase(NamespaceService.SYSTEM_MODEL_1_0_URI);
    }

    void addAddressProperties(JSONObject fromObj, HashMap<QName, Serializable> toTypeProps) {
        toTypeProps.put(OpenESDHModel.PROP_CONTACT_ADDRESS, getOrNull(fromObj, "streetName"));
        toTypeProps.put(OpenESDHModel.PROP_CONTACT_ADDRESS_LINE1, getOrNull(fromObj, "addressLine1"));
        toTypeProps.put(OpenESDHModel.PROP_CONTACT_ADDRESS_LINE2, getOrNull(fromObj, "addressLine2"));
        toTypeProps.put(OpenESDHModel.PROP_CONTACT_ADDRESS_LINE3, getOrNull(fromObj, "addressLine3"));
        toTypeProps.put(OpenESDHModel.PROP_CONTACT_ADDRESS_LINE4, getOrNull(fromObj, "addressLine4"));
        toTypeProps.put(OpenESDHModel.PROP_CONTACT_ADDRESS_LINE5, getOrNull(fromObj, "addressLine5"));
        toTypeProps.put(OpenESDHModel.PROP_CONTACT_ADDRESS_LINE6, getOrNull(fromObj, "addressLine6"));
        toTypeProps.put(OpenESDHModel.PROP_CONTACT_HOUSE_NUMBER, getOrNull(fromObj, "houseNumber"));
        toTypeProps.put(OpenESDHModel.PROP_CONTACT_STREET_NAME, getOrNull(fromObj, "streetName"));
        toTypeProps.put(OpenESDHModel.PROP_CONTACT_STREET_CODE, getOrNull(fromObj, "streetCode"));
        toTypeProps.put(OpenESDHModel.PROP_CONTACT_SUITE_IDENTIFIER, getOrNull(fromObj, "suite"));
        toTypeProps.put(OpenESDHModel.PROP_CONTACT_FLOOR_IDENTIFIER, getOrNull(fromObj, "floorNumber"));
        toTypeProps.put(OpenESDHModel.PROP_CONTACT_CITY_NAME, getOrNull(fromObj, "city"));
        toTypeProps.put(OpenESDHModel.PROP_CONTACT_POST_CODE, getOrNull(fromObj, "postCode"));
        toTypeProps.put(OpenESDHModel.PROP_CONTACT_POST_BOX, getOrNull(fromObj, "postBox"));
        toTypeProps.put(OpenESDHModel.PROP_CONTACT_POST_DISTRICT, getOrNull(fromObj, "postDistrict"));
        toTypeProps.put(OpenESDHModel.PROP_CONTACT_COUNTRY_CODE, getOrNull(fromObj, "countryCode"));
        toTypeProps.put(OpenESDHModel.PROP_CONTACT_MUNICIPALITY_CODE, getOrNull(fromObj, "municipalityCode"));
        toTypeProps.put(OpenESDHModel.PROP_CONTACT_MAIL_SUBLOCATION_ID, getOrNull(fromObj, "mailDeliverySublocationIdentifier"));

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
