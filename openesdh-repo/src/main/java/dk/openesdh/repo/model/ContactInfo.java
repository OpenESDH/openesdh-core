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

    private final NodeRef nodeRef;
    private final String email;
    private final String type;
    private Map<QName, Serializable> allProps;

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