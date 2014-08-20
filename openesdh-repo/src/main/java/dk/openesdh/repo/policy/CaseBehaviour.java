package dk.openesdh.repo.policy;

import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.CaseService;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Created by torben on 19/08/14.
 */
public class CaseBehaviour implements NodeServicePolicies.OnCreateNodePolicy {

  private static Log LOGGER = LogFactory.getLog(CaseBehaviour.class);

  // Dependencies
  private CaseService caseService;
  private PolicyComponent policyComponent;

  // Behaviours
  private Behaviour onCreateNode;

  public void setCaseService(CaseService caseService) {
    this.caseService = caseService;
  }

  public void setPolicyComponent(PolicyComponent policyComponent) {
    this.policyComponent = policyComponent;
  }

  public void init() {

    // Create behaviours
    this.onCreateNode = new JavaBehaviour(this, "onCreateNode", Behaviour.NotificationFrequency.TRANSACTION_COMMIT);

    // Bind behaviours to node policies
    this.policyComponent.bindClassBehaviour(
        QName.createQName(NamespaceService.ALFRESCO_URI, "onCreateNode"),
        OpenESDHModel.TYPE_CASE_BASE,
        this.onCreateNode
    );
  }

  @Override
  public void onCreateNode(ChildAssociationRef childAssociationRef) {
    caseService.createCase(childAssociationRef);
  }
}
