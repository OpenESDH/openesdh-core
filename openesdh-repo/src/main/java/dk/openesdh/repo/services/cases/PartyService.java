package dk.openesdh.repo.services.cases;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.alfresco.service.cmr.repository.NodeRef;

import dk.openesdh.repo.model.ContactInfo;

/**
 * @author Lanre Abiwon.
 */
public interface PartyService {

    /**
     * Prefix used for authorities of type party.
     */
    static final String PARTY_PREFIX = "PARTY_";
    static final String GROUP_PREFIX = "GROUP_";

    /**
     *
     * @param caseId - the id of the case.
     * @param role - the role (semantic for now) of the party on the case.
     * @return the NodeRef of the newly created contact.
     */
    NodeRef createParty(String caseId, String role);

    /**
     *
     * @param caseId - the id of the case.
     * @param role - the role (semantic for now) of the party on the case.
     * @param contacts - The list of additional contacts(emails) that are to be added to the newly created party(group).
     * Skips existing contacts.
     * @return the NodeRef of the newly created party(group).
     */
    NodeRef createParty(String caseId, String role, List<String> contacts);

    /**
     * Add a contact to the specified case partyRole.
     * Note if the Role doesn't exist one is created and the contact is added.
     *
     * @param caseId the case id
     * @param partyRef the nodeRef for the party (an alternative to retrieving by party name)
     * @param partyRole the party (group) to add the contact
     * @param contact The contacts to add to the party.
     * @return
     */
    boolean addContactToParty(String caseId, NodeRef partyRef, String partyRole, String contact);

    /**
     * Add the list of contacts to the specified case partyRole.
     * Note if the Role doesn't exist one is created and the contact is added.
     *
     * @param caseId the case id
     * @param partyRef the nodeRef for the party (an alternative to retrieving by party name)
     * @param partyRole the party (group) to add the contact
     * @param contacts The list of contacts to add to the party.
     * @return
     */
    boolean addContactsToParty(String caseId, NodeRef partyRef, String partyRole, List<String> contacts);

    /**
     * Removes a party from a role. (i.e. remove contact from the case group)
     *
     * @param caseId
     * @param partyId maps to the email of the contact
     * @param role current role (group)
     * @return true if successful
     */
    boolean removePartyRole(String caseId, String partyId, String role);

    /**
     * Gets a complete list of contacts mapped to the roles they have (i.e. members of the group(s) in alfresco speak)
     *
     * @param caseId the id of the case in question.
     * @return Map<String, Set<String>>
     */
    Map<String, List<NodeRef>> getContactsByRole(String caseId);

    /**
     * Returns a simple list of parties involved in the requested case
     *
     * @param caseId the case ID
     * @return List of type PartyInfo
     */
    List<ContactInfo> getPartiesInCase(String caseId);

    /**
     * Not a real lock. Adding nodeRef's of current contact versions to case as parameter
     *
     * @param caseNodeRef
     */
    void lockCasePartiesToVersions(NodeRef caseNodeRef);

    /**
     * Not a real unlock. Removing case parameter of contact versions
     *
     * @param caseNodeRef
     */
    void unlockCaseParties(NodeRef caseNodeRef);

    enum PartyType {
        PERSON,
        ORGANIZATION
    }

    /**
     * Data pojo to carry common contact information
     *
     * @author Lanre Abiwon
     * @since 1.1
     */
    class PartyInfo {

        private final String email;
        private final String cprNumber;
        private final String firstName;
        private final String lastName;
        private final String organizationName;
        private final NodeRef nodeRef;
        private final PartyType partyType;

        //Methods of this signature are assumed to be used for setting a person
        public PartyInfo(NodeRef nodeRef, String email, String firstName, String lastName, String cprNumber) {
            this.nodeRef = nodeRef;
            this.email = email;
            this.firstName = firstName;
            this.lastName = lastName;
            this.cprNumber = cprNumber;
            this.organizationName = null;
            this.partyType = PartyType.PERSON;
        }

        //Methods of this signature are assumed to be used for setting an organization
        public PartyInfo(NodeRef nodeRef, String email, String organizationName, String cprNumber) {
            this.nodeRef = nodeRef;
            this.email = email;
            this.firstName = null;
            this.lastName = null;
            this.organizationName = organizationName;
            this.cprNumber = cprNumber;
            this.partyType = PartyType.ORGANIZATION;
        }

        public NodeRef getNodeRef() {
            return nodeRef;
        }

        public String getUserName() {
            return email;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public String getCprNumber() {
            return cprNumber;
        }

        public String getEmail() {
            return email;
        }

        public String getOrganizationName() {
            return organizationName;
        }

        public PartyType getPartyType() {
            return partyType;
        }
    }

    class CaseRole {

        private Optional<NodeRef> nodeRef = Optional.empty();
        private String name;

        public boolean isPresent() {
            return nodeRef.isPresent();
        }

        public Optional<NodeRef> getNodeRef() {
            return nodeRef;
        }

        public void setNodeRef(Optional<NodeRef> nodeRef) {
            this.nodeRef = nodeRef;
        }

        public String getName() {
            return name;
        }

        public String getFullName() {
            return GROUP_PREFIX + name;
        }

        public void setName(String dbid, String roleName) {
            this.name = PARTY_PREFIX + dbid + "_" + roleName;
        }
    }

}
