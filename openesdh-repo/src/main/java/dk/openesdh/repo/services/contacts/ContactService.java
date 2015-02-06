package dk.openesdh.repo.services.contacts;

import dk.openesdh.exceptions.contacts.NoSuchContactException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang3.tuple.Pair;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * @author Lanre Abiwon.
 */
public interface ContactService {

    /**
     * Prefix used for authorities of type contact.
     */
    public static final String CONTACT_PREFIX = "CONTACT_";


    /**
     * The CONTACTs zone.
     */
    public static String ZONE_CONTACT = "CONTACT.STORE";


    //TODO create a new workspaces store for contacts or perhaps store them in the same store as users?
    /**
     * Get the noderef for the storage folder.
     *
     * @return NodeRef
     */
    public NodeRef getContactsStorageRoot();

    /**
     * Returns a tuple containing a boolean and nodeRef if the contact has a
     * @param contactRef
     * @return org.apache.commons.lang3.tuple.Pair<Boolean, NodeRef>
     */
/*
    public Pair<Boolean, NodeRef> hasAssociatedLogin(NodeRef contactRef);
*/

    /**
     *
     * @param email
     * @param type - Constrained to PERSON or ORGANIZATION.
     * @return the NodeRef of the newly created contact.
     */
    public NodeRef createContact(String email, String type);

    /**
     *
     * @param email
     * @param type - Constrained to PERSON or ORGANIZATION.
     * @param properties - The map of additional properties that are mapped to the aspect properties to be applied.
     * @return the NodeRef of the newly created contact.
     */
    public NodeRef createContact(String email, String type, Map<QName, Serializable> properties);

    /**
     * A property map specifying at least first name is required for this method so as to
     * satisfy the alfresco ootb requirements.
     *
     * @param email
     * @param type - Constrained to PERSON or ORGANIZATION.
     * @param properties - The map of additional properties that are mapped to the aspect properties to be applied.
     * @param authorityZones - the zones of the contact. (For future use)
     * @return the NodeRef of the newly created contact
     */
    public NodeRef createContact(String email, String type, Map<QName, Serializable> properties, Set<String> authorityZones) ;

}
