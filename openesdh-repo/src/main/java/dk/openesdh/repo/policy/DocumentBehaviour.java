package dk.openesdh.repo.policy;

import dk.openesdh.repo.model.OpenESDHModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by rasmutor on 2/11/15.
 */
public class DocumentBehaviour implements NodeServicePolicies.OnCreateChildAssociationPolicy {

    private static final Log LOG = LogFactory.getLog(DocumentBehaviour.class);

    private Behaviour onCreateChildAssociation;

    private PolicyComponent policyComponent;
    private NodeService nodeService;

    public void init() {
        onCreateChildAssociation = new JavaBehaviour(this, "onCreateChildAssociation", Behaviour.NotificationFrequency.TRANSACTION_COMMIT);

        this.policyComponent.bindAssociationBehaviour(
                NodeServicePolicies.OnCreateChildAssociationPolicy.QNAME,
                OpenESDHModel.TYPE_DOC_SIMPLE,
                this.onCreateChildAssociation
        );
    }

    @Override
    public void onCreateChildAssociation(ChildAssociationRef childAssocRef, boolean isNewNode) {
//        System.out.println("\n\t\tThe number of nodes: "+ nodeService.countChildAssocs(childAssocRef.getParentRef(), true)+"\n");
        if (nodeService.countChildAssocs(childAssocRef.getParentRef(), true) == 1) {//TODO does it have to be primary assocs?
            NodeRef fileRef = childAssocRef.getChildRef();
            if (nodeService.exists(fileRef)) {
                //Tag the case document as the main document for the case
                nodeService.addAspect(fileRef, OpenESDHModel.ASPECT_CASE_MAIN_DOC, null);
                nodeService.setType(fileRef, OpenESDHModel.TYPE_DOC_DIGITAL_FILE);
                // TODO Get start value, localize
                nodeService.setProperty(fileRef, OpenESDHModel.PROP_DOC_VARIANT, "Produktion");
            }
        }
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }
}
