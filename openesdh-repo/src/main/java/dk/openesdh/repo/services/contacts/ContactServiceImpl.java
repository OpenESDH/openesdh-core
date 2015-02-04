package dk.openesdh.repo.services.contacts;

//import dk.openesdh.exceptions.contacts.InvalidContactTypeException;
import dk.openesdh.exceptions.contacts.InvalidContactTypeException;
import dk.openesdh.repo.model.ContactInfo;
import dk.openesdh.repo.model.OpenESDHModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Lanre Abiwon.
 */
public class ContactServiceImpl implements ContactService {

    private static final Log logger = LogFactory.getLog(ContactServiceImpl.class);
    private NodeService nodeService;
    private ContactDAOImpl contactDAO;

    private static Set<String> DEFAULT_ZONES = new HashSet<String>();

    static{
        DEFAULT_ZONES.add(AuthorityService.ZONE_APP_DEFAULT);
        DEFAULT_ZONES.add(ContactService.ZONE_CONTACT);
    }

    @Override
    public NodeRef getContactsStorageRoot() {
        return contactDAO.getAuthorityContainerRef();
    }

    @Override
    public NodeRef createContact(String email, String type) {
        return createContact(email, type, null, DEFAULT_ZONES);
    }

    @Override
    public NodeRef createContact(String email, String type, Map<QName, Serializable> properties) {
        return createContact(email, type, properties, DEFAULT_ZONES);
    }

    @Override
    public NodeRef createContact(String email, String type, Map<QName, Serializable> properties, Set<String> authorityZones) {
//        if (!type.equalsIgnoreCase(ContactInfo.ContactType.PERSON.name()) || !type.equalsIgnoreCase(ContactInfo.ContactType.ORGANIZATION.name()) )
        if (!type.equalsIgnoreCase(ContactInfo.ContactType.valueOf(StringUtils.capitalize(type)).toString()) )
            throw new InvalidContactTypeException("The type of contact is not recognised. Can only create types PERSON/ORGANIZATION");

        if(StringUtils.isEmpty(email))
            throw new NullPointerException("Email is mandatory for contact creation");

        HashMap<QName, Serializable> props = new HashMap<QName, Serializable>();
        props.put(OpenESDHModel.PROP_CONTACT_EMAIL, email);
        //Next two are for testing purposes only
//        props.put(OpenESDHModel.PROP_CONTACT_FIRST_NAME, "Morgan");
//        props.put(OpenESDHModel.PROP_CONTACT_LAST_NAME, "Freeman");
        return this.contactDAO.createContact(email, StringUtils.capitalize(type), DEFAULT_ZONES, props);
    }

    //<editor-fold desc="Injected service bean setters">
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setContactDAO(ContactDAOImpl contactDAO) {
        this.contactDAO = contactDAO;
    }
    //</editor-fold>

}