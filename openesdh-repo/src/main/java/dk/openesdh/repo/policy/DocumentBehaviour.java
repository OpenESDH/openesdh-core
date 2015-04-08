package dk.openesdh.repo.policy;

import dk.openesdh.repo.model.OpenESDHModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.*;
import org.alfresco.repo.version.VersionRevertCallback;
import org.alfresco.repo.version.VersionRevertDetails;
import org.alfresco.repo.version.VersionServicePolicies;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.Map;

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
                ContentModel.ASSOC_CONTAINS,
                this.onCreateChildAssociation
        );
    }

    @Override
    public void onCreateChildAssociation(ChildAssociationRef childAssocRef, boolean isNewNode) {
//        System.out.println("\n\t\tThe number of nodes: "+ nodeService.countChildAssocs(childAssocRef.getParentRef(), true)+"\n");
        if (!nodeService.exists(childAssocRef.getParentRef())) {
            return;
        }
        NodeRef fileRef = childAssocRef.getChildRef();
        if (!nodeService.exists(fileRef)) {
            return;
        }
        NodeRef docRecord = childAssocRef.getParentRef();
        // Set the first child as main document
        if (nodeService.countChildAssocs(docRecord, true) == 1) {//TODO does it have to be primary assocs?
            //Tag the case document as the main document for the case
            nodeService.addChild(docRecord, fileRef, OpenESDHModel.ASSOC_DOC_MAIN, QName.createQName(OpenESDHModel.DOC_URI, "main"));

            //find a better way to do this
            /**
             * When a document is uploaded, the javascript controller upload.post.js
             * creates this document in the documents directory. Unfortunately it is only after creation
             * that the document record exists so the meta-data needed for the doc record is grafted on the main doc
             * then applied to the document record here and removed from the main document.
             */
            Serializable categoryProperty = nodeService.getProperty(childAssocRef.getChildRef(), OpenESDHModel.PROP_DOC_CATEGORY);
            if (categoryProperty != null && StringUtils.isNotEmpty(categoryProperty.toString())){
                String doc_category = categoryProperty.toString();
                this.nodeService.setProperty(docRecord, OpenESDHModel.PROP_DOC_CATEGORY, doc_category);
                this.nodeService.removeProperty(childAssocRef.getChildRef(), OpenESDHModel.PROP_DOC_CATEGORY);
            }
            Serializable stateProperty = nodeService.getProperty(childAssocRef.getChildRef(),OpenESDHModel.PROP_DOC_STATE);
            if (stateProperty != null && StringUtils.isNotEmpty(stateProperty.toString())){
                String doc_state = stateProperty.toString();
                this.nodeService.setProperty(docRecord, OpenESDHModel.PROP_DOC_STATE, doc_state);
                this.nodeService.removeProperty(childAssocRef.getChildRef(), OpenESDHModel.PROP_DOC_STATE);
            }

            Serializable typeProperty = nodeService.getProperty(childAssocRef.getChildRef(),OpenESDHModel.PROP_DOC_TYPE);
            if (typeProperty != null && StringUtils.isNotEmpty(typeProperty.toString())){
                String doc_type = typeProperty.toString();
                this.nodeService.setProperty(docRecord, OpenESDHModel.PROP_DOC_TYPE, doc_type);
                this.nodeService.removeProperty(childAssocRef.getChildRef(), OpenESDHModel.PROP_DOC_TYPE);
            }
        }

        // Make sure all children get the type doc:digitalFile
        nodeService.setType(fileRef, OpenESDHModel.TYPE_DOC_DIGITAL_FILE);
        // TODO Get start value, localize
        nodeService.setProperty(fileRef, OpenESDHModel.PROP_DOC_VARIANT, "Produktion");
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }
}
