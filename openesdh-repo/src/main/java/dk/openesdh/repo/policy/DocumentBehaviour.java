package dk.openesdh.repo.policy;

import dk.openesdh.repo.model.OpenESDHModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.WebScriptException;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateChildAssociationPolicy;

/**
 * Created by rasmutor on 2/11/15.
 */
public class DocumentBehaviour implements OnCreateChildAssociationPolicy{

    private static final Log logger = LogFactory.getLog(DocumentBehaviour.class);

    private Behaviour onCreateChildAssociation;

    private PolicyComponent policyComponent;
    private NodeService nodeService;

    public void init() {
        onCreateChildAssociation = new JavaBehaviour(this, "onCreateChildAssociation", Behaviour.NotificationFrequency.TRANSACTION_COMMIT);

        this.policyComponent.bindAssociationBehaviour(
                OnCreateChildAssociationPolicy.QNAME,
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
        if (nodeService.countChildAssocs(docRecord, true) == 1) {
            //Tag the case document as the main document for the case
            nodeService.addChild(docRecord, fileRef, OpenESDHModel.ASSOC_DOC_MAIN, QName.createQName(OpenESDHModel.DOC_URI, "main"));

            //find a better way to do this
            /**
             * When a document is uploaded, the javascript controller upload.post.js
             * creates the document in the documents directory for the case. Unfortunately it is only after creation
             * that the document record exists so the meta-data needed for the doc record is grafted on the main doc
             * then applied to the document record here and removed from the main document.
             */
            String doc_category, doc_state, doc_type;
            try {
                doc_category = nodeService.getProperty(childAssocRef.getChildRef(), OpenESDHModel.PROP_DOC_CATEGORY).toString();
                doc_state = nodeService.getProperty(childAssocRef.getChildRef(), OpenESDHModel.PROP_DOC_STATE).toString();
                doc_type = nodeService.getProperty(childAssocRef.getChildRef(), OpenESDHModel.PROP_DOC_TYPE).toString();

                if (StringUtils.isAnyEmpty(doc_category, doc_state, doc_type))
                    throw new NullPointerException("Mandatory parameters missing.");
            }
            catch(NullPointerException npe){
                throw new WebScriptException(npe.getMessage() + "\nThe following meta-data is required for a main document:\n\tCategory\n\tState\n\ttype");
            }

            this.nodeService.setProperty(docRecord, OpenESDHModel.PROP_DOC_CATEGORY, doc_category);
            this.nodeService.removeProperty(childAssocRef.getChildRef(), OpenESDHModel.PROP_DOC_CATEGORY);

            this.nodeService.setProperty(docRecord, OpenESDHModel.PROP_DOC_STATE, doc_state);
            this.nodeService.removeProperty(childAssocRef.getChildRef(), OpenESDHModel.PROP_DOC_STATE);

            this.nodeService.setProperty(docRecord, OpenESDHModel.PROP_DOC_TYPE, doc_type);
            this.nodeService.removeProperty(childAssocRef.getChildRef(), OpenESDHModel.PROP_DOC_TYPE);
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
