package dk.openesdh.repo.services;

import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Created by torben on 19/08/14.
 */
public interface CaseService {
    /**
     * Get the root folder for storing cases
     * @return NodeRef for the root folder
     */
    public NodeRef getCasesRootNodeRef();

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
