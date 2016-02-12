package dk.openesdh.repo.services.contacts;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import dk.openesdh.repo.model.ContactInfo;
import dk.openesdh.repo.model.ContactType;

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

    /**
     * Get the noderef for the storage folder.
     *
     * @return NodeRef
     */
    public NodeRef getContactsStorageRoot();

    /**
     * Returns the type of contact as an enum of the ContactType
     *
     * @param contact
     * @return
     */
    public ContactType getContactType(NodeRef contact);

    /**
     * Checks if the contact has an address aspec then retrieves the address values
     *
     * @param contactRef The noderef representing the contact
     * @return Map of the address props
     */
    public Map<QName, Serializable> getAddress(NodeRef contactRef);

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
    public NodeRef createContact(String email, String type, Map<QName, Serializable> properties, Set<String> authorityZones);

    /**
     * Gets a contact by id (usuallly the email)
     *
     * @param id
     * @return the nodeRef representing the contact
     */
    public NodeRef getContactById(String id);

    /**
     * Returns a list of contacts that match the id and type.
     * (Will add a filter afterwards)
     *
     * @param id the id of the contact
     * @param type the type of Contact
     * @return List<ContactInfo>
     */
    public List<ContactInfo> getContactByFilter(String id, String type);

    /**
     * Returns a ContactInfo for provided nodeRef of the contact
     *
     * @param nodeRef
     * - The nodeRef of the contact to return
     * @return ContactInfo
     */
    public ContactInfo getContactInfo(NodeRef nodeRef);

    /**
     * Adds contact person to contact organization
     *
     * @param organization
     * @param person
     * @return organization
     */
    public NodeRef addPersonToOrganization(NodeRef organization, NodeRef person);

    /**
     * Gets stream of person contacts associated with organization contact
     *
     * @param organizationNodeRef
     * @return stream of personNodeRef's
     */
    public Stream<NodeRef> getOrganizationPersons(NodeRef organizationNodeRef);

    /**
     * deletes contact, or throws error if contact is used in closed case
     *
     * @param nodeRef
     */
    public void deleteContact(NodeRef nodeRef);
}
