package dk.openesdh.repo.services;

import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;

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
     * @return NodeRef for the root folder
     */
    public NodeRef getCasesRootNodeRef();

    /**
     * Get the roles that are possible to set for the given case.
     * @param caseNodeRef
     * @return Set containing the role names
     */
    public Set<String> getRoles(NodeRef caseNodeRef);


    /**
     * Get the members on the case grouped by role.
     * Includes groups and users.
     * @param caseNodeRef
     * @return
     */
    public Map<String, Set<String>> getMembersByRole(NodeRef caseNodeRef);

    /**
     * Get the ID number of the case.
     * @param caseNodeRef
     * @return
     */
    public String getCaseId(NodeRef caseNodeRef);

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
}
