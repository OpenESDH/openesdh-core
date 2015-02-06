package dk.openesdh.repo.model;

import org.alfresco.repo.security.permissions.PermissionCheckValue;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author by Lanre Abiwon.
 */

public class ContactInfo implements PermissionCheckValue {

    private NodeService nodeService;
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public enum ContactType { PERSON, ORGANIZATION }

    private final NodeRef nodeRef;
    private final String email;
    private final ContactType type;
    private Map<QName, Serializable> allProps;

    public ContactInfo(NodeRef nodeRef, String email, String type) {
        this.nodeRef = nodeRef;
        this.email = email;
        this.type = type.equalsIgnoreCase(ContactType.PERSON.name()) ? ContactType.PERSON : ContactType.ORGANIZATION;
        allProps = this.nodeService.getProperties(this.nodeRef);
    }

    @Override
    public NodeRef getNodeRef() {
        return nodeRef;
    }

    public String getEmail() {
        return email;
    }

    public String getType(){
        return type.name();
    }

    public Map<QName,Serializable> getAddress(){
        Map<QName,Serializable> addressProps = new HashMap<>();
        if(this.nodeService.hasAspect(this.nodeRef, OpenESDHModel.ASPECT_CONTACT_ADDRESS)){
            addressProps.put(OpenESDHModel.PROP_CONTACT_HOUSE_NUMBER, this.allProps.get(OpenESDHModel.PROP_CONTACT_HOUSE_NUMBER));
            addressProps.put(OpenESDHModel.PROP_CONTACT_ADDRESS, this.allProps.get(OpenESDHModel.PROP_CONTACT_ADDRESS));
            addressProps.put(OpenESDHModel.PROP_CONTACT_ADDRESS_LINE1, this.allProps.get(OpenESDHModel.PROP_CONTACT_ADDRESS_LINE1));
            addressProps.put(OpenESDHModel.PROP_CONTACT_ADDRESS_LINE2, this.allProps.get(OpenESDHModel.PROP_CONTACT_ADDRESS_LINE2));
            addressProps.put(OpenESDHModel.PROP_CONTACT_ADDRESS_LINE3, this.allProps.get(OpenESDHModel.PROP_CONTACT_ADDRESS_LINE3));
            addressProps.put(OpenESDHModel.PROP_CONTACT_ADDRESS_LINE4, this.allProps.get(OpenESDHModel.PROP_CONTACT_ADDRESS_LINE4));
            addressProps.put(OpenESDHModel.PROP_CONTACT_ADDRESS_LINE5, this.allProps.get(OpenESDHModel.PROP_CONTACT_ADDRESS_LINE5));
            addressProps.put(OpenESDHModel.PROP_CONTACT_ADDRESS_LINE6, this.allProps.get(OpenESDHModel.PROP_CONTACT_ADDRESS_LINE6));
            addressProps.put(OpenESDHModel.PROP_CONTACT_STREET_NAME, this.allProps.get(OpenESDHModel.PROP_CONTACT_STREET_NAME));
            addressProps.put(OpenESDHModel.PROP_CONTACT_STREET_CODE, this.allProps.get(OpenESDHModel.PROP_CONTACT_STREET_CODE));
            addressProps.put(OpenESDHModel.PROP_CONTACT_FLOOR_IDENTIFIER, this.allProps.get(OpenESDHModel.PROP_CONTACT_FLOOR_IDENTIFIER));
            addressProps.put(OpenESDHModel.PROP_CONTACT_SUITE_IDENTIFIER, this.allProps.get(OpenESDHModel.PROP_CONTACT_SUITE_IDENTIFIER));
            addressProps.put(OpenESDHModel.PROP_CONTACT_CITY_NAME, this.allProps.get(OpenESDHModel.PROP_CONTACT_CITY_NAME));
            addressProps.put(OpenESDHModel.PROP_CONTACT_POST_CODE, this.allProps.get(OpenESDHModel.PROP_CONTACT_POST_CODE));
            addressProps.put(OpenESDHModel.PROP_CONTACT_POST_BOX, this.allProps.get(OpenESDHModel.PROP_CONTACT_POST_BOX));
            addressProps.put(OpenESDHModel.PROP_CONTACT_POST_DISTRICT, this.allProps.get(OpenESDHModel.PROP_CONTACT_POST_DISTRICT));
            addressProps.put(OpenESDHModel.PROP_CONTACT_MUNICIPALITY_CODE, this.allProps.get(OpenESDHModel.PROP_CONTACT_MUNICIPALITY_CODE));
            addressProps.put(OpenESDHModel.PROP_CONTACT_COUNTRY_CODE, this.allProps.get(OpenESDHModel.PROP_CONTACT_COUNTRY_CODE));
        }
        return addressProps;
    }

    //Some other common properties that we might want to access on a regular basis when working with contacts
    public String getCPRNumber(){
            return (String) this.allProps.get(OpenESDHModel.PROP_CONTACT_CPR_NUMBER);
    }
    public String getCVRNumber(){
            return (String) this.allProps.get(OpenESDHModel.PROP_CONTACT_CVR_NUMBER);
    }
    public boolean isRegistered(){
        return (boolean)this.allProps.get(OpenESDHModel.PROP_CONTACT_REGISTERED);
    }
    public boolean isInternal(){
        return (boolean)this.allProps.get(OpenESDHModel.PROP_CONTACT_INTERNAL) && (boolean)this.allProps.get(OpenESDHModel.PROP_CONTACT_REGISTERED);
    }
}