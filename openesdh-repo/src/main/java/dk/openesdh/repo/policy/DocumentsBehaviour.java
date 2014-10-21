package dk.openesdh.repo.policy;

import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.documents.DocumentService;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by torben on 19/08/14.
 */
public class DocumentsBehaviour implements NodeServicePolicies.OnCreateChildAssociationPolicy {

    private static Log LOGGER = LogFactory.getLog(DocumentsBehaviour.class);

    // Dependencies
    private DocumentService documentService;
    private NodeService nodeService;
    private PolicyComponent policyComponent;

    // Behaviours
    private Behaviour onCreateChildAssociation;

    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void init() {
        // Create behaviours
        this.onCreateChildAssociation = new JavaBehaviour(this, "onCreateChildAssociation", Behaviour.NotificationFrequency.TRANSACTION_COMMIT);

        // Bind behaviours to node policies
        this.policyComponent.bindAssociationBehaviour(
                NodeServicePolicies.OnCreateChildAssociationPolicy.QNAME,
                OpenESDHModel.ASPECT_DOCUMENT_CONTAINER,
                this.onCreateChildAssociation
        );
        /*
        this.policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnCreateChildAssociationPolicy.QNAME,
                ContentModel.TYPE_CONTENT,
                this.onCreateChildAssociation
        );
        */
    }

//    @Override
    public void onCreateNode(ChildAssociationRef childAssociationRef) {
        documentService.createDocument(childAssociationRef);
    }

    @Override
    public void onCreateChildAssociation(ChildAssociationRef childAssocRef, boolean isNewNode) {
        QName childType = nodeService.getType(childAssocRef.getChildRef());
        if(childType.equals(ContentModel.TYPE_CONTENT)) {
            documentService.createDocument(childAssocRef);
        }
    }
}

