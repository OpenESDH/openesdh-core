package dk.openesdh.repo.policy;

import javax.annotation.PostConstruct;

import org.alfresco.repo.node.NodeServicePolicies.BeforeCreateNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.cases.CaseService;

/**
 * Created by torben on 19/08/14.
 */
@Service("caseBehaviour")
public class CaseBehaviour implements OnCreateNodePolicy, BeforeCreateNodePolicy {

    @Autowired
    @Qualifier("CaseService")
    private CaseService caseService;
    @Autowired
    @Qualifier("policyComponent")
    private PolicyComponent policyComponent;

    // Behaviours
    private Behaviour onCreateNode;
    private Behaviour beforeCreateNode;

    @PostConstruct
    public void init() {

        // Create behaviours
        this.onCreateNode = new JavaBehaviour(this, "onCreateNode",
                Behaviour.NotificationFrequency.TRANSACTION_COMMIT);
        this.beforeCreateNode = new JavaBehaviour(this, "beforeCreateNode",
                Behaviour.NotificationFrequency.TRANSACTION_COMMIT);

        // Bind behaviours to node policies
        this.policyComponent.bindClassBehaviour(BeforeCreateNodePolicy.QNAME, OpenESDHModel.TYPE_CASE_BASE,
                this.beforeCreateNode);

        this.policyComponent.bindClassBehaviour(OnCreateNodePolicy.QNAME, OpenESDHModel.TYPE_CASE_BASE,
                this.onCreateNode);
    }

    @Override
    public void onCreateNode(ChildAssociationRef childAssociationRef) {
        caseService.createCase(childAssociationRef);
    }

    @Override
    public void beforeCreateNode(NodeRef parentRef, QName assocTypeQName, QName assocQName, QName caseTypeQName) {
        caseService.checkCaseCreatorPermissions(caseTypeQName);
    }
}

