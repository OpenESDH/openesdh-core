package dk.openesdh.repo.services.contacts;

import dk.openesdh.exceptions.contacts.NoSuchContactException;
import dk.openesdh.repo.model.ContactInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.lang3.tuple.Pair;

/**
 * @author Lanre Abiwon.
 */
public interface ContactService {

    //TODO create a new workspaces store for contacts or perhaps store them in the same store as users?
    /**
     * Get the noderef for the storage folder.
     *
     * @return NodeRef
     */
    public NodeRef getContactsStorageRoot();

    /**
     * Retrieve the person info for an existing {@code person NodeRef}
     *
     * @param contactRef
     * @return ContactInfo (firstname, lastname, email)
     * @throws dk.openesdh.exceptions.contacts.NoSuchContactException if the contact doesn't exist
     */
    public ContactInfo getContact(NodeRef contactRef) throws NoSuchContactException;

    /**
     * Create a contact from a alfresco cm:person
     * @param personRef person nodeRef
     * @return ContactInfo
     */
    public ContactInfo createContact(NodeRef personRef);

    /**
     * Create a contact from the mandatory information
     * @param firstName
     * @param lastName
     * @param email
     * @return ContactInfo
     */
    public NodeRef createContact(String firstName, String lastName, String email);

    /**
     * Returns a tuple containing a boolean and nodeRef if the contact has a
     * @param contactRef
     * @return org.apache.commons.lang3.tuple.Pair<Boolean, NodeRef>
     */
    public Pair<Boolean, NodeRef> hasAssociatedLogin(NodeRef contactRef);

}
