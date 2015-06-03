package dk.openesdh.repo.services.cases;

import dk.openesdh.repo.model.ContactInfo;
import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.repo.security.permissions.PermissionCheckValue;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Lanre Abiwon.
 */
public interface PartyService {

    /**
     * Prefix used for authorities of type party.
     */
    String PARTY_PREFIX = "PARTY_";

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
     * @param contacts - The map of additional contacts that are to be added to the newly created party(group).
     * @return the NodeRef of the newly created party(group).
     */
    NodeRef createParty(String caseId, String role, List<String> contacts);

    /**
     * Add a contact to the specified case partyRole.
     * Note if the Role doesn't exist one is created and the contact is added.
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
     * @param caseId the case id
     * @param partyRef the nodeRef for the party (an alternative to retrieving by party name)
     * @param partyRole the party (group) to add the contact
     * @param contacts The list of contacts to add to the party.
     * @return
     */
    boolean addContactsToParty(String caseId, NodeRef partyRef, String partyRole, List<String> contacts);

    /**
     * Removes a party from a role. (i.e. remove contact from the case group)
     * @param caseId
     * @param partyId maps to the email of the contact
     * @param role current role (group)
     * @return true if successful
     */
    boolean removePartyRole(String caseId, String partyId, String role);

    /**
     *
     * @param caseId - the case id
     * @param roleName -  the role to check for
     * @return pair<Boolean, NodeRef>
     */
    Pair<Boolean, NodeRef> roleExists(String caseId, String roleName);

    /**
     * Get a specific party role (i.e. group) for a case.
     * @param caseNodeRef The case nodeRef
     * @param caseId The case Id
     * @param partyRole the required role
     * @return a NodeRef or null depending on whether it exists
     */
    NodeRef getCaseParty(NodeRef caseNodeRef, String caseId, String partyRole);

    /**
     * Gets a complete list of contacts mapped to the roles they have (i.e. members of the group(s) in alfresco speak)
     * @param caseId the id of the case in question.
     * @return Map<String, Set<String>>
     */
    Map<String, Set<String>> getContactsByRole(String caseId);

    /**
     * Returns a simple list of parties involved in the requested case
     * @param caseId  the case ID
     * @return List of type PartyInfo
     */
    List<ContactInfo> getPartiesInCase(String caseId);

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
            this.partyType= PartyType.PERSON;
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


        public NodeRef getNodeRef()
        {
            return nodeRef;
        }

        public String getUserName()
        {
            return email;
        }

        public String getFirstName()
        {
            return firstName;
        }

        public String getLastName()
        {
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


}
