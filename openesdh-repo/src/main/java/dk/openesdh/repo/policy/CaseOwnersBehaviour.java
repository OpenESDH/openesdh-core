package dk.openesdh.repo.policy;

import javax.annotation.PostConstruct;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.members.CaseMembersService;

/**
 * Created by torben on 19/08/14.
 */
@Service("caseOwnersBehaviour")
public class CaseOwnersBehaviour implements NodeServicePolicies.OnCreateAssociationPolicy,
        NodeServicePolicies.OnDeleteAssociationPolicy {

    private static Log LOGGER = LogFactory.getLog(CaseOwnersBehaviour.class);

    @Autowired
    @Qualifier("CaseMembersService")
    private CaseMembersService caseMembersService;
    @Autowired
    @Qualifier("policyComponent")
    private PolicyComponent policyComponent;
    @Autowired
    @Qualifier("NodeService")
    private NodeService nodeService;

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
    public void onCreateAssociation(final AssociationRef nodeAssocRef) {
        if (nodeAssocRef.getSourceRef() != null) {
            AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>
                    () {
                @Override
                public Object doWork() throws Exception {
                    caseMembersService.addAuthorityToRole(nodeAssocRef.getTargetRef(),
                            "CaseOwners", nodeAssocRef.getSourceRef());
                    return null;
                }
            }, AuthenticationUtil.getAdminUserName());
        }
    }

    @Override
    public void onDeleteAssociation(AssociationRef nodeAssocRef) {
        if (nodeService.exists(nodeAssocRef.getTargetRef()) && nodeService.exists(nodeAssocRef.getSourceRef())) {
            caseMembersService.removeAuthorityFromRole(nodeAssocRef.getTargetRef(),
                    "CaseOwners", nodeAssocRef.getSourceRef());
        }
    }
}
