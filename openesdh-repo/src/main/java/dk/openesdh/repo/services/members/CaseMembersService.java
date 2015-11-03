package dk.openesdh.repo.services.members;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;

public interface CaseMembersService {

    /**
     * Get the members on the case grouped by role. Includes groups and users.
     * If noExpandGroups, then only all authorities within the immediate group
     * is returned else include users of subgroups instead of just immediate
     * groups.
     *
     * @param caseNodeRef
     * @param noExpandGroups
     *            expand subgroups
     * @param includeOwner
     *            inculde case owner
     * @return
     */
    Map<String, Set<String>> getMembersByRole(NodeRef caseNodeRef, boolean noExpandGroups, boolean includeOwner);

    /**
     * Remove the authority from the given role group on the case.
     *
     * @param authorityName
     * @param role
     * @param caseNodeRef
     */
    void removeAuthorityFromRole(String authorityName, String role, NodeRef caseNodeRef);

    void removeAuthorityFromRole(NodeRef authorityNodeRef, String role, NodeRef caseNodeRef);

    /**
     * Add the authority to the given role group on the case.
     *
     * @param authorityName
     * @param role
     * @param caseNodeRef
     */
    void addAuthorityToRole(String authorityName, String role, NodeRef caseNodeRef);

    void addAuthorityToRole(NodeRef authorityNodeRef, String role, NodeRef caseNodeRef);

    /**
     * Add the list of authorities to the given role group on the case.
     *
     * @param authorities
     * @param role
     * @param caseNodeRef
     */
    void addAuthoritiesToRole(List<NodeRef> authorities, String role, NodeRef caseNodeRef);

    /**
     * Moves an authority from one role to another on a case.
     *
     * @param authorityName
     * @param fromRole
     * @param toRole
     * @param caseNodeRef
     */
    void changeAuthorityRole(String authorityName, String fromRole, String toRole, NodeRef caseNodeRef);

    /**
     * Get a list of case db-id's where the given authority has the given role.
     *
     * @param authorityNodeRef
     * @param role
     * @return
     */
    List<Long> getCaseDbIdsWhereAuthorityHasRole(NodeRef authorityNodeRef, String role);
}
