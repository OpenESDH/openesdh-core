package dk.openesdh.repo.services;

import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by torben on 19/08/14.
 */
public interface CaseService {

    static final String DATE_FORMAT = "yyyyMMdd";
    static final String CASES = "openesdh_cases";


    /**
     * Get the root folder for storing cases
     *
     * @return NodeRef for the root folder
     */
    public NodeRef getCasesRootNodeRef();

    /**
     * Get the roles that are possible to set for the given case.
     *
     * @param caseNodeRef
     * @return Set containing the role names
     */
    public Set<String> getRoles(NodeRef caseNodeRef);

    /**
     * Get all roles for the given case (including owners role).
     *
     * @param caseNodeRef
     * @return Set containing the role names
     */
    Set<String> getAllRoles(NodeRef caseNodeRef);

    /**
     * Get the members on the case grouped by role.
     * Includes groups and users.
     *
     * @param caseNodeRef
     * @return
     */
    public Map<String, Set<String>> getMembersByRole(NodeRef caseNodeRef);

    /**
     * Get the ID number of the case.
     *
     * @param caseNodeRef
     * @return
     */
    public String getCaseId(NodeRef caseNodeRef);

    /**
     * Remove the authority from the given role group on the case.
     *
     * @param authorityName
     * @param role
     * @param caseNodeRef
     */
    public void removeAuthorityFromRole(String authorityName,
                                        String role,
                                        NodeRef caseNodeRef);

    /**
     * Add the authority to the given role group on the case.
     *
     * @param authorityName
     * @param role
     * @param caseNodeRef
     */
    public void addAuthorityToRole(String authorityName,
                                   String role,
                                   NodeRef caseNodeRef);

    /**
     * Add the list of authorities to the given role group on the case.
     *
     * @param authorities
     * @param role
     * @param caseNodeRef
     */
    public void addAuthoritiesToRole(List<NodeRef> authorities,
                                     String role,
                                     NodeRef caseNodeRef);

    /**
     * Moves an authority from one role to another on a case.
     *
     * @param authorityName
     * @param fromRole
     * @param toRole
     * @param caseNodeRef
     */
    public void changeAuthorityRole(String authorityName,
                                    String fromRole,
                                    String toRole,
                                    NodeRef caseNodeRef);

    /**
     * Return whether a user can update case roles.
     * @param user
     * @param caseNodeRef
     * @return
     */
    public boolean canUpdateCaseRoles(String user, NodeRef caseNodeRef);

    /**
     * Create a case
     *
     * @param childAssociationRef
     * @return NodeRef to the case
     */
    public void createCase(ChildAssociationRef childAssociationRef);

    /**
     * Find or create a folder for a new case
     *
     * @return NodeRef to folder
     */
    public NodeRef getCaseFolderNodeRef(NodeRef casesFolderNodeRef);

    /**
     * Return whether a node is journalized or not.
     * @param nodeRef
     * @return
     */
    boolean isJournalized(NodeRef nodeRef);

    /**
     * Journalize case and all childnodes
     *  @param caseNodeRef Noderef of the case to journalize
     * @param journalKey
     */
    public void journalize(NodeRef caseNodeRef, NodeRef journalKey);

    /**
     * Unjournalize case and all childnodes
     *
     * @param nodeRef Noderef of the case to unjournalize
     */
    public void unJournalize(NodeRef nodeRef);

}
