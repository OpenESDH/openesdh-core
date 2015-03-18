package dk.openesdh.repo.webscripts.contacts;

import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.contacts.ContactService;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespacePrefixResolverProvider;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.NotImplementedException;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Lanre
 */
public class ContactAbstractWebscript extends AbstractWebScript{
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
        try {
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
        } catch (JSONException e) {
            e.printStackTrace();
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
        try {
                Map<QName, Serializable> props = this.nodeService.getProperties(contactNode);

                for (Map.Entry<QName, Serializable> entry : props.entrySet()) {
                    Serializable value = entry.getValue();
                    QName key = entry.getKey();
                    String localName = key.getLocalName();
                    if (value != null && !key.getNamespaceURI().equalsIgnoreCase(NamespaceService.SYSTEM_MODEL_1_0_URI)) {
                            result.put(localName, value);
                    }
                }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    void getAddressProperties(JSONObject obj, HashMap<QName, Serializable> typeProps){

        typeProps.put(OpenESDHModel.PROP_CONTACT_ADDRESS, getOrNull(obj,"streetName") );
        typeProps.put(OpenESDHModel.PROP_CONTACT_ADDRESS_LINE1, getOrNull(obj,"addressLine1") );
        typeProps.put(OpenESDHModel.PROP_CONTACT_ADDRESS_LINE2, getOrNull(obj,"addressLine2") );
        typeProps.put(OpenESDHModel.PROP_CONTACT_ADDRESS_LINE3, getOrNull(obj,"addressLine3") );
        typeProps.put(OpenESDHModel.PROP_CONTACT_ADDRESS_LINE4, getOrNull(obj,"addressLine4") );
        typeProps.put(OpenESDHModel.PROP_CONTACT_ADDRESS_LINE5, getOrNull(obj,"addressLine5") );
        typeProps.put(OpenESDHModel.PROP_CONTACT_ADDRESS_LINE6, getOrNull(obj,"addressLine6") );
        typeProps.put(OpenESDHModel.PROP_CONTACT_HOUSE_NUMBER, getOrNull(obj,"houseNumber") );
        typeProps.put(OpenESDHModel.PROP_CONTACT_STREET_NAME, getOrNull(obj,"streetName") );
        typeProps.put(OpenESDHModel.PROP_CONTACT_STREET_CODE, getOrNull(obj,"streetCode") );
        typeProps.put(OpenESDHModel.PROP_CONTACT_SUITE_IDENTIFIER, getOrNull(obj,"suite") );
        typeProps.put(OpenESDHModel.PROP_CONTACT_FLOOR_IDENTIFIER, getOrNull(obj,"floorNumber") );
        typeProps.put(OpenESDHModel.PROP_CONTACT_CITY_NAME, getOrNull(obj,"city") );
        typeProps.put(OpenESDHModel.PROP_CONTACT_POST_CODE, getOrNull(obj,"postCode") );
        typeProps.put(OpenESDHModel.PROP_CONTACT_POST_BOX, getOrNull(obj,"postBox") );
        typeProps.put(OpenESDHModel.PROP_CONTACT_POST_DISTRICT, getOrNull(obj,"postDistrict") );
        typeProps.put(OpenESDHModel.PROP_CONTACT_COUNTRY_CODE, getOrNull(obj,"countryCode") );
        typeProps.put(OpenESDHModel.PROP_CONTACT_MUNICIPALITY_CODE, getOrNull(obj,"municipalityCode") );
        typeProps.put(OpenESDHModel.PROP_CONTACT_MAIL_SUBLOCATION_ID, getOrNull(obj,"mailDeliverySublocationIdentifier") );

    }

    protected void get(NodeRef nodeRef, WebScriptRequest req, WebScriptResponse res) throws IOException, JSONException {
        throw new NotImplementedException();
    }

    protected void post( WebScriptRequest req, WebScriptResponse res) throws IOException, JSONException {
        throw new NotImplementedException();
    }

    protected void put(NodeRef nodeRef, WebScriptRequest req, WebScriptResponse res) throws IOException, JSONException {
        throw new NotImplementedException();
    }

/*
    protected void delete(NodeRef nodeRef, WebScriptRequest req, WebScriptResponse res) throws IOException, JSONException {
        throw new NotImplementedException();
    }
*/

    //<editor-fold desc="Injected service bean setters">
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setContactService(ContactService contactService) {
        this.contactService = contactService;
    }
    //</editor-fold>
}
