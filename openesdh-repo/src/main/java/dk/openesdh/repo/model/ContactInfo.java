package dk.openesdh.repo.model;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.repo.security.permissions.PermissionCheckValue;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * @author by Lanre Abiwon.
 */

public class ContactInfo implements PermissionCheckValue {

    private final NodeRef nodeRef;
    private final String email;
    private final String type;
    private Map<QName, Serializable> allProps;

    public static final String PROP_NAME_CONTACT_TYPE = "contactType";
    public static final String PROP_NAME_CONTACT_ID = "contactId";
    public static final String PROP_NAME_CONTACT_DISPLAY_NAME = "displayName";
    public static final String PROP_NAME_CONTACT_NODE_REF = "nodeRef";
    public static final String PROP_NAME_CONTACT_ROLE = "role";

    public static final String PROP_NAME_CONTACT_STREET_NAME = "streetName";
    public static final String PROP_NAME_CONTACT_HOUSE_NUMBER = "houseNumber";
    public static final String PROP_NAME_CONTACT_POST_CODE = "postCode";
    public static final String PROP_NAME_CONTACT_CITY_NAME = "cityName";
    public static final String PROP_NAME_CONTACT_COUNTRY_CODE = "countryCode";
    public static final String PROP_NAME_CONTACT_POST_BOX = "postBox";

    //Create this from nodeRef of the same type
    public ContactInfo(NodeRef nodeRef, ContactType type, Map<QName, Serializable> props){

        allProps = props;
        this.nodeRef = nodeRef;
        this.email = (String) allProps.get(OpenESDHModel.PROP_CONTACT_EMAIL);
        this.type = type.toString();
    }

    @Override
    public NodeRef getNodeRef() {
        return this.nodeRef;
    }

    public String getEmail() {
        return this.email;
    }

    public String getType(){
        return this.type;
    }

    public String getName(){
        if( this.type.equalsIgnoreCase("PERSON"))
            return this.allProps.get(OpenESDHModel.PROP_CONTACT_FIRST_NAME) +" " +this.allProps.get(OpenESDHModel.PROP_CONTACT_LAST_NAME);
        else
            return (String) this.allProps.get(OpenESDHModel.PROP_CONTACT_ORGANIZATION_NAME);
    }

    public String getStreetName() {
        return getStringProp(OpenESDHModel.PROP_CONTACT_STREET_NAME);
    }

    public String getHouseNumber() {
        return getStringProp(OpenESDHModel.PROP_CONTACT_HOUSE_NUMBER);
    }

    public String getPostCode() {
        return getIntPropString(OpenESDHModel.PROP_CONTACT_POST_CODE);
    }

    public String getCityName() {
        return getStringProp(OpenESDHModel.PROP_CONTACT_CITY_NAME);
    }

    public String getCountryCode() {
        return getStringProp(OpenESDHModel.PROP_CONTACT_COUNTRY_CODE);
    }

    public String getPostBox() {
        return getStringProp(OpenESDHModel.PROP_CONTACT_POST_BOX);
    }

    //Some other common properties that we might want to access on a regular basis when working with contacts
    public String getCPRNumber(){
        return getStringProp(OpenESDHModel.PROP_CONTACT_CPR_NUMBER);
    }
    public String getCVRNumber(){
        return getStringProp(OpenESDHModel.PROP_CONTACT_CVR_NUMBER);
    }
    public boolean isRegistered(){
        return hasProp(OpenESDHModel.PROP_CONTACT_REGISTERED)
                && (boolean) this.allProps.get(OpenESDHModel.PROP_CONTACT_REGISTERED);
    }
    public boolean isInternal(){
        return hasProp(OpenESDHModel.PROP_CONTACT_INTERNAL)
                && (boolean) this.allProps.get(OpenESDHModel.PROP_CONTACT_INTERNAL) && isRegistered();
    }

    private boolean hasProp(QName prop) {
        return this.allProps.keySet().contains(prop);
    }

    private String getStringProp(QName qName) {
        return (String) this.allProps.get(qName);
    }

    private String getIntPropString(QName qName) {
        Integer value = (Integer) this.allProps.get(qName);
        if(value != null){
            return value.toString();
        }
        return null;
    }
}