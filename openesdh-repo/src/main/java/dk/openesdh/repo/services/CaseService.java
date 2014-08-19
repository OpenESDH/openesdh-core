package dk.openesdh.repo.services;

import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Created by torben on 19/08/14.
 */
public interface CaseService {
  /**
   * Find or create a folder for a new case
   * @return NodeRef to folder
   */
  NodeRef getNewCaseFolder();

  /**
   * Create a case
   * @return NodeRef to the case
   * @param childAssociationRef
   */
  public NodeRef createCase(ChildAssociationRef childAssociationRef);
}
