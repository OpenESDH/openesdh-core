package dk.openesdh.repo.policy;

import java.io.Serializable;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.TransactionRunner;
import dk.openesdh.repo.services.cases.CaseOwnersService;
import dk.openesdh.repo.services.cases.CasePermission;
import dk.openesdh.repo.services.cases.CasePermissionService;
import dk.openesdh.repo.services.members.CaseMembersService;

/**
 * Created by torben on 19/08/14.
 */
@Service("caseOwnersBehaviour")
public class CaseOwnersBehaviour implements NodeServicePolicies.OnCreateAssociationPolicy,
        NodeServicePolicies.OnDeleteAssociationPolicy {

    @Autowired
    @Qualifier("CaseMembersService")
    private CaseMembersService caseMembersService;
    @Autowired
    @Qualifier("CaseOwnersService")
    private CaseOwnersService caseOwnersService;
    @Autowired
    @Qualifier("policyComponent")
    private PolicyComponent policyComponent;
    @Autowired
    @Qualifier("NodeService")
    private NodeService nodeService;
    @Autowired
    @Qualifier("policyBehaviourFilter")
    private BehaviourFilter behaviourFilter;
    @Autowired
    private TransactionRunner transactionRunner;
    @Autowired
    private CasePermissionService casePermissionService;

    // Behaviours
    private Behaviour onCreateAssociation;
    private Behaviour onDeleteAssociation;

    @PostConstruct
    public void init() {

        // Create behaviours
        this.onCreateAssociation = new JavaBehaviour(this, "onCreateAssociation", Behaviour.NotificationFrequency.TRANSACTION_COMMIT);
        this.onDeleteAssociation= new JavaBehaviour(this,
                "onDeleteAssociation",
                Behaviour.NotificationFrequency.TRANSACTION_COMMIT);

        // Bind behaviours to node policies
        this.policyComponent.bindAssociationBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onCreateAssociation"),
                OpenESDHModel.TYPE_CASE_BASE, OpenESDHModel.ASSOC_CASE_OWNERS, this.onCreateAssociation);
        this.policyComponent.bindAssociationBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onDeleteAssociation"),
                OpenESDHModel.TYPE_CASE_BASE, OpenESDHModel.ASSOC_CASE_OWNERS, this.onDeleteAssociation);
    }

    @Override
    public void onCreateAssociation(final AssociationRef nodeAssocRef) {
        if (nodeAssocRef.getSourceRef() != null) {
            transactionRunner.runAsAdmin(() -> {
                caseMembersService.addAuthorityToRole(nodeAssocRef.getTargetRef(),
                        getOwnerPermissionName(nodeAssocRef.getSourceRef()),
                        nodeAssocRef.getSourceRef());
                syncOwnersProperty(nodeAssocRef.getSourceRef());
                return null;
            });
        }
    }

    @Override
    public void onDeleteAssociation(AssociationRef nodeAssocRef) {
        if (nodeService.exists(nodeAssocRef.getTargetRef()) && nodeService.exists(nodeAssocRef.getSourceRef())) {
            caseMembersService.removeAuthorityFromRole(nodeAssocRef.getTargetRef(),
                    getOwnerPermissionName(nodeAssocRef.getSourceRef()),
                    nodeAssocRef.getSourceRef());
            syncOwnersProperty(nodeAssocRef.getSourceRef());
        }
    }

    private String getOwnerPermissionName(NodeRef nodeRef) {
        return casePermissionService.getPermissionName(nodeRef, CasePermission.OWNER);
    }

    private void syncOwnersProperty(NodeRef caseNodeRef) {
        Set<String> owners = caseOwnersService.getCaseOwnersAuthorityNames(caseNodeRef);
        behaviourFilter.disableBehaviour(caseNodeRef);
        nodeService.setProperty(caseNodeRef, OpenESDHModel.PROP_OE_OWNERS, (Serializable) owners);
        behaviourFilter.enableBehaviour(caseNodeRef);
    }
}
