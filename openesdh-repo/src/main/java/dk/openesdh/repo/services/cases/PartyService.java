package dk.openesdh.repo.services.cases;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.Pair;

import dk.openesdh.repo.model.ContactInfo;

/**
 * @author Lanre Abiwon.
 */
public interface PartyService {

    /**
     * Prefix used for authorities of type party.
     */
    public static final String PARTY_PREFIX = "PARTY_";

    /**
     *
     * @param caseId - the id of the case.
     * @param role - the role (semantic for now) of the party on the case.
     * @return the NodeRef of the newly created contact.
     */
    public NodeRef createParty(String caseId, String role);

    /**
     *
     * @param caseId - the id of the case.
     * @param role - the role (semantic for now) of the party on the case.
     * @param contacts - The map of additional contacts that are to be added to the newly created party(group).
     * @return the NodeRef of the newly created party(group).
     */
    public NodeRef createParty(String caseId, String role, List<String> contacts );

    /**
     * Add a contact to the specified case partyRole.
     * Note if the Role doesn't exist one is created and the contact is added.
     * @param caseId the case id
     * @param partyRef the nodeRef for the party (an alternative to retrieving by party name)
     * @param partyRole the party (group) to add the contact
     * @param contact The contacts to add to the party.
     * @return
     */
    public boolean addContactToParty(String caseId, NodeRef partyRef, String partyRole, String contact);

    /**
     * Add the list of contacts to the specified case partyRole.
     * Note if the Role doesn't exist one is created and the contact is added.
     * @param caseId the case id
     * @param partyRef the nodeRef for the party (an alternative to retrieving by party name)
     * @param partyRole the party (group) to add the contact
     * @param contacts The list of contacts to add to the party.
     * @return
     */
    public boolean addContactsToParty(String caseId, NodeRef partyRef, String partyRole, List<String> contacts);

    /**
     * Removes a party from a role. (i.e. remove contact from the case group)
     * @param caseId
     * @param partyId maps to the email of the contact
     * @param role current role (group)
     * @return true if successful
     */
    public boolean removePartyRole(String caseId, String partyId, String role);

    /**
     *
     * @param caseId - the case id
     * @param roleName -  the role to check for
     * @return pair<Boolean, NodeRef>
     */
    public Pair<Boolean, NodeRef> roleExists(String caseId, String roleName);

    /**
     * Get a specific party role (i.e. group) for a case.
     * @param caseNodeRef The case nodeRef
     * @param caseId The case Id
     * @param partyRole the required role
     * @return a NodeRef or null depending on whether it exists
     */
    public NodeRef getCaseParty(NodeRef caseNodeRef, String caseId, String partyRole);

    /**
     * Gets a complete list of contacts mapped to the roles they have (i.e. members of the group(s) in alfresco speak)
     * @param caseId the id of the case in question.
     * @return Map<String, Set<ContactInfo>>
     */
    public Map<String, Set<ContactInfo>> getContactsByRole(String caseId);


}
