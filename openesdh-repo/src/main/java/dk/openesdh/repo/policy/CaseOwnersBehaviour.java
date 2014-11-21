package dk.openesdh.repo.policy;

import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.cases.CaseService;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by torben on 19/08/14.
 */
public class CaseOwnersBehaviour implements NodeServicePolicies.OnCreateAssociationPolicy,
        NodeServicePolicies.OnDeleteAssociationPolicy {

    private static Log LOGGER = LogFactory.getLog(CaseOwnersBehaviour.class);

    // Dependencies
    private CaseService caseService;
    private PolicyComponent policyComponent;
    private NodeService nodeService;

    // Behaviours
    private Behaviour onCreateAssociation;
    private Behaviour onDeleteAssociation;

    public void setCaseService(CaseService caseService) {
        this.caseService = caseService;
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void init() {

        // Create behaviours
        this.onCreateAssociation = new JavaBehaviour(this, "onCreateAssociation", Behaviour.NotificationFrequency.TRANSACTION_COMMIT);
        this.onDeleteAssociation= new JavaBehaviour(this,
                "onDeleteAssociation",
                Behaviour.NotificationFrequency.TRANSACTION_COMMIT);

        // Bind behaviours to node policies
        this.policyComponent.bindAssociationBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onCreateAssociation"),
                OpenESDHModel.TYPE_CASE_BASE,
                OpenESDHModel.ASSOC_CASE_OWNERS,
                this.onCreateAssociation
        );
        this.policyComponent.bindAssociationBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI,
                        "onDeleteAssociation"),
                OpenESDHModel.TYPE_CASE_BASE,
                OpenESDHModel.ASSOC_CASE_OWNERS,
                this.onDeleteAssociation
        );
    }

    @Override
    public void onCreateAssociation(AssociationRef nodeAssocRef) {
        if (nodeAssocRef.getSourceRef() != null) {
            caseService.addAuthorityToRole(nodeAssocRef.getTargetRef(),
                    "CaseOwners", nodeAssocRef.getSourceRef());
        }
    }

    @Override
    public void onDeleteAssociation(AssociationRef nodeAssocRef) {
        if (nodeService.exists(nodeAssocRef.getTargetRef()) && nodeService.exists(nodeAssocRef.getSourceRef())) {
            caseService.removeAuthorityFromRole(nodeAssocRef.getTargetRef(),
                    "CaseOwners", nodeAssocRef.getSourceRef());
        }
    }
}
