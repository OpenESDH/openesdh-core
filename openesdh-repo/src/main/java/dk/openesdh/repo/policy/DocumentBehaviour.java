package dk.openesdh.repo.policy;

import dk.openesdh.repo.model.DocumentType;
import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.documents.DocumentService;
import dk.openesdh.repo.services.documents.DocumentTypeService;
import dk.openesdh.repo.utils.Utils;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.copy.CopyServicePolicies.BeforeCopyPolicy;
import org.alfresco.repo.copy.CopyServicePolicies.OnCopyCompletePolicy;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateChildAssociationPolicy;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.util.CollectionUtils;

/**
 * Created by rasmutor on 2/11/15.
 */
public class DocumentBehaviour implements OnCreateChildAssociationPolicy, BeforeCopyPolicy, OnCopyCompletePolicy {

    private static final Log logger = LogFactory.getLog(DocumentBehaviour.class);
    private NodeService nodeService;
    private PolicyComponent policyComponent;
    private DocumentService documentService;
    private DocumentTypeService documentTypeService;

    private Behaviour onCreateChildAssociation;
    private Behaviour onCreateCaseDocContent;
    private Behaviour beforeCopyDocumentFolder;
    private Behaviour afterCopyDocumentFolder;

    public void init() {
        beforeCopyDocumentFolder = new JavaBehaviour(this, "beforeCopy");
        afterCopyDocumentFolder = new JavaBehaviour(this, "onCopyComplete", Behaviour.NotificationFrequency.TRANSACTION_COMMIT);

        onCreateChildAssociation = new JavaBehaviour(this, "onCreateChildAssociation", Behaviour.NotificationFrequency.TRANSACTION_COMMIT);
        onCreateCaseDocContent = new JavaBehaviour(this, "onCreateCaseDocContentBehaviour", Behaviour.NotificationFrequency.TRANSACTION_COMMIT);

        // Bind behaviours to node policies
        this.policyComponent.bindAssociationBehaviour(
                NodeServicePolicies.OnCreateChildAssociationPolicy.QNAME,
                OpenESDHModel.ASPECT_DOCUMENT_CONTAINER,
                this.onCreateCaseDocContent
        );

        this.policyComponent.bindAssociationBehaviour(
                OnCreateChildAssociationPolicy.QNAME,
                OpenESDHModel.TYPE_DOC_SIMPLE,
                ContentModel.ASSOC_CONTAINS,
                this.onCreateChildAssociation
        );

        this.policyComponent.bindClassBehaviour(
                BeforeCopyPolicy.QNAME,
                OpenESDHModel.TYPE_DOC_SIMPLE,
                this.beforeCopyDocumentFolder
        );

        this.policyComponent.bindClassBehaviour(
                OnCopyCompletePolicy.QNAME,
                OpenESDHModel.TYPE_DOC_SIMPLE,
                this.afterCopyDocumentFolder
        );

        this.policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnCreateNodePolicy.QNAME,
                OpenESDHModel.TYPE_DOC_BASE,
                new JavaBehaviour(this, "onCreateDocRecordBehaviour", Behaviour.NotificationFrequency.TRANSACTION_COMMIT)
        );
    }

    public void onCreateDocRecordBehaviour (ChildAssociationRef childRef) {
        NodeRef nodeRef = childRef.getChildRef();
        if (!nodeService.exists(childRef.getParentRef()) || !nodeService.exists(nodeRef)) {
            return;
        }

        if (!nodeService.hasAspect(nodeRef, ContentModel.ASPECT_TITLED)) {
            // Assign a default title to the document based on its name
            String title = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
            Map<QName, Serializable> props = new HashMap<>();
            props.put(ContentModel.PROP_TITLE, title);
            nodeService.addAspect(nodeRef, ContentModel.ASPECT_TITLED, props);
        }
    }

    public void onCreateCaseDocContentBehaviour(ChildAssociationRef childAssociationRef, boolean isNewNode) {
        QName childType = nodeService.getType(childAssociationRef.getChildRef());
        if (childType.equals(ContentModel.TYPE_CONTENT)) {
            NodeRef documentsFolderRef = childAssociationRef.getParentRef();
            NodeRef fileRef = childAssociationRef.getChildRef();

            String fileName = (String) nodeService.getProperty(fileRef, ContentModel.PROP_NAME);
            String documentName = FilenameUtils.removeExtension(fileName);

            // Set a temporary file name
            // This is to avoid duplicates child node exception when the
            // document record is created below
            String tempFileName = UUID.randomUUID().toString() + fileName;
            nodeService.setProperty(fileRef, ContentModel.PROP_NAME, tempFileName);

            // Create document folder
            NodeRef documentFolderRef = documentService.createDocumentFolder(documentsFolderRef, documentName).getChildRef();
            nodeService.moveNode(fileRef, documentFolderRef, ContentModel.ASSOC_CONTAINS,
                    Utils.createDocumentContentAssociationName(documentName));

            // Set the filename back to the original, after it has been moved
            nodeService.setProperty(fileRef, ContentModel.PROP_NAME, fileName);
        }
    }

    @Override
    public void beforeCopy(QName classRef, NodeRef sourceNodeRef, NodeRef targetNodeRef) {
        removeDocMainAssociation(sourceNodeRef);
    }

    @Override
    public void onCopyComplete(QName classRef, NodeRef docRecordNode, NodeRef targetCaseDocumentsNodeRef,
            boolean copyToNewNode, Map<NodeRef, NodeRef> copyMap) {

        // Restore doc:main association which is removed when beforeCopy is run.
        // Likewise create doc:main association for the copy of the document.
        restoreDocMainAssociationAndCopyDocMainAssociation(docRecordNode, copyMap);

        // Remove association with copies to make the documents independent.
        removeOriginalAssoc(docRecordNode);
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

            ChildAssociationRef childAssoc = nodeService.getChildAssocs(docRecord).get(0);
            if (!OpenESDHModel.ASSOC_DOC_MAIN.equals(
                    childAssoc.getTypeQName())) {
                // Tag the file as the main file for the record
                nodeService.addChild(docRecord, fileRef, OpenESDHModel.ASSOC_DOC_MAIN, OpenESDHModel.ASSOC_DOC_MAIN);
                nodeService.addAspect(fileRef, OpenESDHModel.ASPECT_DOC_IS_MAIN_FILE, null);
            }

            //find a better way to do this
            /**
             * When a document is uploaded, the javascript controller upload.post.js creates the document in the
             * documents directory for the case. Unfortunately it is only after creation that the document record exists
             * so the meta-data needed for the doc record is grafted on the main doc then applied to the document record
             * here and removed from the main document.
             */

            nodeService.setProperty(docRecord, ContentModel.PROP_TITLE, nodeService.getProperty(childAssocRef.getChildRef(), ContentModel.PROP_TITLE));

            String doc_category, doc_state, doc_type;
            try {
                //First check if the docRecord has any of the mandatory props. If any of these are null we handle the
                //exception in the catch.
                nodeService.getProperty(docRecord, OpenESDHModel.PROP_DOC_CATEGORY).toString();
                nodeService.getProperty(docRecord, OpenESDHModel.PROP_DOC_STATE).toString();
            } catch (NullPointerException npe) {
                //if not check that the document itself has the mandatory properties and set it on the docRecord (uploads)
                doc_category = nodeService.getProperty(childAssocRef.getChildRef(), OpenESDHModel.PROP_DOC_CATEGORY).toString();
                doc_state = nodeService.getProperty(childAssocRef.getChildRef(), OpenESDHModel.PROP_DOC_STATE).toString();
                doc_type = nodeService.getProperty(childAssocRef.getChildRef(), OpenESDHModel.PROP_DOC_TYPE).toString();
                DocumentType documentType = documentTypeService.getDocumentType(new NodeRef(doc_type));
                if (StringUtils.isAnyEmpty(doc_category, doc_state) || documentType == null) {
                    throw new WebScriptException(npe.getMessage() + "\nThe following meta-data is required for a main document:\n\tCategory\n\tState\n\ttype");
                } else {
                    this.nodeService.setProperty(docRecord, OpenESDHModel.PROP_DOC_CATEGORY, doc_category);
                    this.nodeService.removeProperty(childAssocRef.getChildRef(), OpenESDHModel.PROP_DOC_CATEGORY);
                    this.nodeService.setProperty(docRecord, OpenESDHModel.PROP_DOC_STATE, doc_state);
                    this.nodeService.removeProperty(childAssocRef.getChildRef(), OpenESDHModel.PROP_DOC_STATE);
                    documentService.updateDocumentType(docRecord, documentType);
                    this.nodeService.removeProperty(childAssocRef.getChildRef(), OpenESDHModel.PROP_DOC_TYPE);
                }
            }
        }

        // Make sure all children get the type doc:digitalFile
        nodeService.setType(fileRef, OpenESDHModel.TYPE_DOC_DIGITAL_FILE);
        // TODO Get start value, localize
//        nodeService.setProperty(fileRef, OpenESDHModel.PROP_DOC_VARIANT, "Produktion");
    }

    //<editor-fold desc="private methods">
    private void removeDocMainAssociation(NodeRef docRecordNodeRef) {
        ChildAssociationRef docMainAssociation = getDocMainAssociation(docRecordNodeRef);
        if (docMainAssociation == null) {
            return;
        }
        nodeService.removeChildAssociation(docMainAssociation);
        nodeService.removeAspect(docMainAssociation.getChildRef(), OpenESDHModel.ASPECT_DOC_IS_MAIN_FILE);
    }

    private ChildAssociationRef getDocMainAssociation(NodeRef docFolderNodeRef) {
        List<ChildAssociationRef> childAssocList = nodeService.getChildAssocs(docFolderNodeRef,
                OpenESDHModel.ASSOC_DOC_MAIN, RegexQNamePattern.MATCH_ALL);
        if (CollectionUtils.isEmpty(childAssocList)) {
            return null;
        }
        return childAssocList.get(0);
    }

    private void restoreDocMainAssociationAndCopyDocMainAssociation(NodeRef docRecordNode,
            Map<NodeRef, NodeRef> copyMap) {
        ChildAssociationRef docContentAssociation = getContentAssociation(docRecordNode);
        NodeRef docFileRef = docContentAssociation.getChildRef();
        nodeService.addChild(docRecordNode, docFileRef, OpenESDHModel.ASSOC_DOC_MAIN,
                OpenESDHModel.ASSOC_DOC_MAIN);
        nodeService.addAspect(docFileRef, OpenESDHModel.ASPECT_DOC_IS_MAIN_FILE, null);

        NodeRef copyDocRecordNode = copyMap.get(docRecordNode);

        String documentName = (String) nodeService.getProperty(docRecordNode, ContentModel.PROP_NAME);
        nodeService.setProperty(copyDocRecordNode, ContentModel.PROP_NAME, documentName);

        ChildAssociationRef copyDocMainAssoc = getDocMainAssociation(copyDocRecordNode);
        if (copyDocMainAssoc != null) {
            return;
        }

        NodeRef copyDocFileNode = copyMap.get(docFileRef);
        nodeService.addChild(copyDocRecordNode, copyDocFileNode, OpenESDHModel.ASSOC_DOC_MAIN,
                OpenESDHModel.ASSOC_DOC_MAIN);
        nodeService.addAspect(copyDocFileNode, OpenESDHModel.ASPECT_DOC_IS_MAIN_FILE, null);
    }

    private void removeOriginalAssoc(NodeRef docRecordNode) {
        List<AssociationRef> assocList = nodeService.getSourceAssocs(docRecordNode, ContentModel.ASSOC_ORIGINAL);
        for (AssociationRef assoc : assocList) {
            nodeService.removeAssociation(assoc.getSourceRef(), assoc.getTargetRef(), ContentModel.ASSOC_ORIGINAL);
        }
    }

    private ChildAssociationRef getContentAssociation(NodeRef docFolderNodeRef) {

        String documentName = (String) nodeService.getProperty(docFolderNodeRef, ContentModel.PROP_NAME);
        QName docContentAssocName = Utils.createDocumentContentAssociationName(documentName);
        List<ChildAssociationRef> childAssocList = nodeService.getChildAssocs(docFolderNodeRef,
                ContentModel.ASSOC_CONTAINS, docContentAssocName);

        if (CollectionUtils.isEmpty(childAssocList)) {
            return null;
        }

        return childAssocList.get(0);
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }

    public void setDocumentTypeService(DocumentTypeService documentTypeService) {
        this.documentTypeService = documentTypeService;
    }
}
