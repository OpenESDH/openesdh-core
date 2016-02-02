package dk.openesdh.repo.webscripts.contacts;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.json.simple.JSONObject;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import com.google.gdata.util.common.base.Joiner;

import dk.openesdh.repo.model.ContactType;
import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.webscripts.ParamUtils;

public class ContactUtils {

    private static final String NODE_ID = "id";
    private static final String STORE_ID = "store_id";
    private static final String STORE_TYPE = "store_type";

    public static JSONObject createContactJson(NodeRef contactNode, Map<QName, Serializable> props) {
        JSONObject result = new JSONObject();
        result.put("nodeRefId", contactNode.toString());
        result.put("storeType", contactNode.getStoreRef().getProtocol());
        result.put("storeId", contactNode.getStoreRef().getIdentifier());
        result.put("id", contactNode.getId());
        result.put("version", props.get(ContentModel.PROP_VERSION_LABEL));
        props.entrySet().stream()
                .filter(t -> t.getValue() != null)
                .filter(t -> !isKeyOfSystemModelNamepace(t.getKey()))
                .filter(t -> !t.getKey().equals(OpenESDHModel.PROP_CONTACT_LOCKED_IN_CASES))
                .map(entry -> {
                    if (entry.getValue() instanceof Date) {
                        entry.setValue(((Date) entry.getValue()).getTime());
                    }
                    return entry;
                })
                .forEach((entry)
                        -> result.put(entry.getKey().getLocalName(), entry.getValue()));
        result.put("address", getAddress(props));
        return result;
    }

    public static String getAddress(Map<QName, Serializable> props) {
        return Joiner.on(", ").skipNulls().join(
                props.get(OpenESDHModel.PROP_CONTACT_ADDRESS_LINE1),
                props.get(OpenESDHModel.PROP_CONTACT_ADDRESS_LINE2));
    }

    private static boolean isKeyOfSystemModelNamepace(QName key) {
        return key.getNamespaceURI().equalsIgnoreCase(NamespaceService.SYSTEM_MODEL_1_0_URI);
    }

    static NodeRef getNodeRef(WebScriptRequest req) {
        Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
        NodeRef nodeRef = null;
        String storeType = templateArgs.get(STORE_TYPE);
        String storeId = templateArgs.get(STORE_ID);
        String nodeId = templateArgs.get(NODE_ID);
        if (storeType != null && storeId != null && nodeId != null) {
            nodeRef = new NodeRef(storeType, storeId, nodeId);
        }
        return nodeRef;
    }

    static void addContactProperties(ContactType contactType, JSONObject fromParsedRequest, Map<QName, Serializable> toTypeProps) {
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

    static void addAddressProperties(JSONObject fromObj, Map<QName, Serializable> toTypeProps) {
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

    static void copyProperty(JSONObject fromObj, Map<QName, Serializable> toTypeProps, QName property) {
        toTypeProps.put(property, ParamUtils.getOrNull(fromObj, property.getLocalName()));
    }
}
