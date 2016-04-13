package dk.openesdh.repo.services.cases;

import java.util.List;

import org.alfresco.service.Auditable;
import org.alfresco.service.cmr.repository.NodeRef;
import org.json.simple.JSONArray;

/**
 * @author Lanre Abiwon.
 */
public interface PartyService {

    String BEAN_ID = "PartyService";

    /**
     * Prefix used for authorities of type party.
     */
    static final String PARTY_PREFIX = "PARTY_";
    static final String GROUP_PREFIX = "GROUP_";

    /**
     * json fields
     */
    String FIELD_NODE_REF = "nodeRef";
    String FIELD_CONTACT = "contact";
    String FIELD_ROLE_REF = "roleRef";
    String FIELD_CONTACT_IDS = "contactIds";

    /**
     * add case party
     *
     * @param caseId
     * @param role
     * @param contactId
     *            contact email
     * @return
     */
    public List<NodeRef> addCaseParty(String caseId, NodeRef role, String... contactId);

    /**
     * main entry point to add case party
     *
     * @param caseId
     * @param role
     * @param contactIds
     * @return
     */
    public List<NodeRef> addCaseParty(String caseId, NodeRef role, List<String> contactIds);

    /**
     * updates role of the provided party
     * 
     * @param partyRef
     * @param roleRef
     */
    void updateCaseParty(NodeRef partyRef, NodeRef roleRef);

    /**
     * Removes a party from case
     *
     * @param caseId
     * @param nodeRef
     *            of the party to remove
     */
    @Auditable(parameters = { "caseId", "partyRef" }, recordable = { true, true })
    public void removeCaseParty(String caseId, NodeRef partyRef);

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

    JSONArray getCasePartiesJson(String caseId);
}
