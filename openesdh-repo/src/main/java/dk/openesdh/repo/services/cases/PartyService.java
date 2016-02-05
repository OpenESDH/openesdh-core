package dk.openesdh.repo.services.cases;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.alfresco.service.cmr.repository.NodeRef;

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
     * add case party
     *
     * @param caseId
     * @param role
     * @param contactId contact email
     */
    public void addCaseParty(String caseId, String role, String... contactId);

    /**
     * main entry point to add case party
     *
     * @param caseId
     * @param role
     * @param contactIds
     */
    public void addCaseParty(String caseId, String role, List<String> contactIds);

    /**
     * Removes a party from a role. (i.e. remove contact from the case group)
     *
     * @param caseId
     * @param contactId maps to the email of the contact
     * @param role casePartyRole to remove from
     */
    public void removeCaseParty(String caseId, String contactId, String role);

    /**
     * Gets a complete list of contacts mapped to the roles they have (i.e. members of the group(s) in alfresco speak)
     *
     * @param caseId the id of the case in question.
     * @return Map<String, Set<String>>
     */
    public Map<String, List<NodeRef>> getCaseParties(String caseId);

    /**
     * Not a real lock. Adding nodeRef's of current contact versions to case as parameter
     *
     * @param caseNodeRef
     */
    public void lockCasePartiesToVersions(NodeRef caseNodeRef);

    /**
     * Not a real unlock. Removing case parameter of contact versions
     *
     * @param caseNodeRef
     */
    public void unlockCaseParties(NodeRef caseNodeRef);

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

        public NodeRef getNodeRef() {
            return nodeRef.orElse(null);
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
