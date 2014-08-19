package dk.openesdh.repo.services;

import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.logging.Logger;

/**
 * Created by torben on 19/08/14.
 */
public class CaseServiceImpl implements CaseService {

  private Logger logger = Logger.getLogger(String.valueOf(this.getClass()));

  @Autowired
  @Qualifier("NodeService")
  protected NodeService nodeService;

  @Autowired
  @Qualifier("SearchService")
  protected SearchService searchService;

  @Autowired
  @Qualifier("AuthorityService")
  protected AuthorityService authorityService;

  @Autowired
  @Qualifier("PermissionService")
  protected PermissionService permissionService;


  @Override
  public NodeRef getNewCaseFolder() {
    return null;
  }

  @Override
  public NodeRef createCase(ChildAssociationRef childAssociationRef) {
    logger.info("Create Case was called");
    return null;
  }
}
