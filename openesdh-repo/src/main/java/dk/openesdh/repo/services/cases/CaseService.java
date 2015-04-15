package dk.openesdh.repo.services.cases;

import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created by torben on 19/08/14.
 */
public interface CaseService {
    static final String DATE_FORMAT = "yyyyMMdd";

    static final String CASES = "openesdh_cases";

    static final Pattern CASE_ID_PATTERN = Pattern.compile("\\d+-(\\d+)");


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
    public Set<String> getAllRoles(NodeRef caseNodeRef);

    /**
     * Get a list of case db-id's where the given authority has the given role.
     * @param authorityNodeRef
     * @param role
     * @return
     */
    List<Long> getCaseDbIdsWhereAuthorityHasRole(NodeRef authorityNodeRef, String role);

    /**
     * Get the members on the case grouped by role.
     * Includes groups and users. If noExpandGroups,
     * then only all authorities within the immediate
     * group is returned else include users of subgroups
     * instead of just immediate groups.
     *
     * @param caseNodeRef
     * @param noExpandGroups expand subgroups
     * @param includeOwner inculde case owner
     * @return
     */
    public Map<String, Set<String>> getMembersByRole(NodeRef caseNodeRef, boolean noExpandGroups, boolean includeOwner);

    /**
     * Get the ID number of the case.
     *
     * @param caseNodeRef
     * @return
     */
    public String getCaseId(NodeRef caseNodeRef);

    /**
     * Returns a case given the ID number of the case.
     *
     * @param caseId
     * @return the nodeRef for a case corresponding with the ID supplied or null
     */
    public NodeRef getCaseById(String caseId);

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

    public void removeAuthorityFromRole(NodeRef authorityNodeRef,
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

    public void addAuthorityToRole(NodeRef authorityNodeRef,
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
     * Return whether or not the user can journalize the node.
     * @param user
     * @param nodeRef
     * @return
     */
    public boolean canJournalize(String user, NodeRef nodeRef);

    /**
     * Return whether or not the user can unjournalize the node.
     * @param user
     * @param nodeRef
     * @return
     */
    public boolean canUnJournalize(String user, NodeRef nodeRef);

    /**
     * Return whether a node is journalized or not.
     * @param nodeRef
     * @return
     */
    public boolean isJournalized(NodeRef nodeRef);

    /**
     * Journalize node and all child nodes
     * @param nodeRef NodeRef of the node to journalize
     * @param journalKey
     */
    public void journalize(NodeRef nodeRef, NodeRef journalKey);

    /**
     * Unjournalize node and all child nodes
     *
     * @param nodeRef NodeRef of the node to unjournalize
     */
    public void unJournalize(NodeRef nodeRef);

    /**
     * Return whether or not the node is a case node.
     * @param nodeRef
     * @return
     */
    public boolean isCaseNode(NodeRef nodeRef);


    /**
     * Return whether or not the node is a doc which exists within a case.
     * @param nodeRef
     * @return
     */
    public boolean isCaseDocNode(NodeRef nodeRef);

    /**
     * Get the parent case of the given node, or null if the node does not
     * have a parent which is a case. The parent does not have to be immediate.
     * @param nodeRef
     * @return
     */
    public NodeRef getParentCase(NodeRef nodeRef);

    /**
     * Get the documents folder of the given case.
     * @param caseNodeRef
     * @return
     */
    public NodeRef getDocumentsFolder(NodeRef caseNodeRef);

    public Map<String, Object> getSearchDefinition(QName caseType);

    public JSONArray buildConstraintsJSON(ConstraintDefinition constraint) throws JSONException;
}
