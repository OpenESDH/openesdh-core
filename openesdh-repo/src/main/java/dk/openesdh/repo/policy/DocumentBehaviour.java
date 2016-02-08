package dk.openesdh.repo.policy;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.copy.CopyServicePolicies.BeforeCopyPolicy;
import org.alfresco.repo.copy.CopyServicePolicies.OnCopyCompletePolicy;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateChildAssociationPolicy;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.repo.version.VersionServicePolicies;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import dk.openesdh.repo.model.DocumentCategory;
import dk.openesdh.repo.model.DocumentType;
import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.BehaviourFilterService;
import dk.openesdh.repo.services.documents.DocumentCategoryService;
import dk.openesdh.repo.services.documents.DocumentService;
import dk.openesdh.repo.services.documents.DocumentTypeService;
import dk.openesdh.repo.utils.Utils;

/**
 * Created by rasmutor on 2/11/15.
 */
@Service("documentBehaviour")
public class DocumentBehaviour {

    @Autowired
    @Qualifier("NodeService")
    private NodeService nodeService;
    @Autowired
    @Qualifier("policyComponent")
    private PolicyComponent policyComponent;
    @Autowired
    @Qualifier("DocumentService")
    private DocumentService documentService;
    @Autowired
    @Qualifier("DocumentTypeService")
    private DocumentTypeService documentTypeService;
    @Autowired
    @Qualifier("DocumentCategoryService")
    private DocumentCategoryService documentCategoryService;
    @Autowired
    @Qualifier("VersionService")
    private VersionService versionService;
    @Autowired
    private BehaviourFilterService behaviourFilterService;

    @PostConstruct
    public void init() {

        // Bind behaviours to node policies
        this.policyComponent.bindAssociationBehaviour(
                NodeServicePolicies.OnCreateChildAssociationPolicy.QNAME,
                OpenESDHModel.ASPECT_DOCUMENT_CONTAINER,
                new JavaBehaviour(this, "onCreateCaseDocContentBehaviour", 
                        Behaviour.NotificationFrequency.TRANSACTION_COMMIT));

        this.policyComponent.bindAssociationBehaviour(
                OnCreateChildAssociationPolicy.QNAME,
                OpenESDHModel.TYPE_DOC_SIMPLE,
                ContentModel.ASSOC_CONTAINS,
                new JavaBehaviour(this, "onAddMainDocOrAttachmentToDocRecord", 
                        Behaviour.NotificationFrequency.TRANSACTION_COMMIT));
        
        this.policyComponent.bindClassBehaviour(
                BeforeCopyPolicy.QNAME, 
                OpenESDHModel.TYPE_DOC_SIMPLE,
                new JavaBehaviour(this, "beforeCopyCaseDocument"));

        this.policyComponent.bindClassBehaviour(
                OnCopyCompletePolicy.QNAME, 
                OpenESDHModel.TYPE_DOC_SIMPLE,
                new JavaBehaviour(this, "onCopyCaseDocumentComplete",
                        Behaviour.NotificationFrequency.TRANSACTION_COMMIT));

        this.policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnCreateNodePolicy.QNAME,
                OpenESDHModel.TYPE_DOC_BASE,
                new JavaBehaviour(this, "onCreateDocRecordBehaviour",
                        Behaviour.NotificationFrequency.TRANSACTION_COMMIT));

        this.policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME,
                OpenESDHModel.TYPE_DOC_BASE, 
                new JavaBehaviour(this, "onUpdateCaseDocumentProperties"));
        
        this.policyComponent.bindClassBehaviour(
                VersionServicePolicies.AfterCreateVersionPolicy.QNAME, 
                OpenESDHModel.ASPECT_DOC_IS_MAIN_FILE, 
                new JavaBehaviour(this, "onDocumentUploadNewVersion"));
    }

    public void onCreateDocRecordBehaviour(ChildAssociationRef childRef) {
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
            String documentName = FilenameUtils.removeExtension(fileName).trim();

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

    public void beforeCopyCaseDocument(QName classRef, NodeRef sourceNodeRef, NodeRef targetNodeRef) {
        // removing doc:main association to prevent "Behaviour should have been
        // invoked" exception from CopyService
        removeDocMainAssociation(sourceNodeRef);
    }

    public void onCopyCaseDocumentComplete(QName classRef, NodeRef docRecordNode, NodeRef targetCaseDocumentsNodeRef,
            boolean copyToNewNode, Map<NodeRef, NodeRef> copyMap) {

        // Restore doc:main association which is removed when beforeCopy is run.
        // Likewise create doc:main association for the copy of the document.
        restoreDocMainAssociationAndCopyDocMainAssociation(docRecordNode, copyMap);
    }

    public void onAddMainDocOrAttachmentToDocRecord(ChildAssociationRef childAssocRef, boolean isNewNode) {
        if (!nodeService.exists(childAssocRef.getParentRef())) {
            return;
        }
        NodeRef fileRef = childAssocRef.getChildRef();
        if (!nodeService.exists(fileRef)) {
            return;
        }

        fillEmptyTitleFromName(fileRef);

        setFirstChildAsMainDocument(childAssocRef);

        ensureAttachmentsAssociation(childAssocRef);

        // Make sure all children get the type doc:digitalFile
        nodeService.setType(fileRef, OpenESDHModel.TYPE_DOC_DIGITAL_FILE);
        // TODO Get start value, localize
        // nodeService.setProperty(fileRef, OpenESDHModel.PROP_DOC_VARIANT,
        // "Produktion");
    }
    
    public void onUpdateCaseDocumentProperties(NodeRef nodeRef, Map<QName, Serializable> before,
            Map<QName, Serializable> after) {
        String beforeStatus = (String) before.get(OpenESDHModel.PROP_OE_STATUS);
        if (beforeStatus == null) {
            return;
        }
        String afterStatus = (String) after.get(OpenESDHModel.PROP_OE_STATUS);
        if (beforeStatus.equals(afterStatus)) {
            return;
        }
        throw new AlfrescoRuntimeException("Document status cannot be "
                + "changed directly. Must call the DocumentService" + ".changeDocumentStatus method.");
    }

    public void onDocumentUploadNewVersion(NodeRef versionableNode, Version version){
        removeAllAttachments(versionableNode);
        refreshCurrentNodeVersionToSaveAssociationsHistory(versionableNode);
    }
    
    // <editor-fold desc="private methods">
    private void removeAllAttachments(NodeRef mainDocNodeRef){
        nodeService.getChildAssocs(mainDocNodeRef, OpenESDHModel.ASSOC_DOC_ATTACHMENTS, RegexQNamePattern.MATCH_ALL)
            .stream()
            .forEach(nodeService::removeChildAssociation);
    }

    private void setFirstChildAsMainDocument(ChildAssociationRef childAssocRef) {

        NodeRef docRecord = childAssocRef.getParentRef();

        if (Optional.ofNullable(getDocMainAssociation(docRecord)).isPresent()) {
            return;
        }

        NodeRef docContentRef = getDocContentFileRef(docRecord);
        nodeService.addChild(docRecord, docContentRef, OpenESDHModel.ASSOC_DOC_MAIN, OpenESDHModel.ASSOC_DOC_MAIN);
        if (!nodeService.hasAspect(docContentRef, OpenESDHModel.ASPECT_DOC_IS_MAIN_FILE)) {
            nodeService.addAspect(docContentRef, OpenESDHModel.ASPECT_DOC_IS_MAIN_FILE, null);
        }

        setDocTitleCategoryAndType(childAssocRef);
    }

    private void setDocTitleCategoryAndType(ChildAssociationRef childAssocRef) {
        NodeRef docRecord = childAssocRef.getParentRef();
        NodeRef fileRef = childAssocRef.getChildRef();

        // find a better way to do this
        /**
         * When a document is uploaded, the javascript controller upload.post.js
         * creates the document in the documents directory for the case.
         * Unfortunately it is only after creation that the document record
         * exists so the meta-data needed for the doc record is grafted on the
         * main doc then applied to the document record here and removed from
         * the main document.
         */
        String title = (String) nodeService.getProperty(fileRef, ContentModel.PROP_TITLE);
        if (title != null || nodeService.getProperty(docRecord, ContentModel.PROP_NAME)
                .equals(nodeService.getProperty(fileRef, ContentModel.PROP_NAME))) {
            nodeService.setProperty(docRecord, ContentModel.PROP_TITLE, title);
        }

        // category

        Optional<Serializable> optDocCategory = Optional
                .ofNullable(nodeService.getProperty(childAssocRef.getChildRef(), OpenESDHModel.PROP_DOC_CATEGORY));

        if (!optDocCategory.isPresent()) {
            return;// no need to set category and type if the document is being
                   // copied.
        }

        DocumentCategory documentCategory = documentCategoryService
                .getDocumentCategory((NodeRef) optDocCategory.get());
        // type
        String doc_type = nodeService.getProperty(childAssocRef.getChildRef(), OpenESDHModel.PROP_DOC_TYPE)
                .toString();
        DocumentType documentType = documentTypeService.getDocumentType(new NodeRef(doc_type));
        if (documentCategory == null || documentType == null) {
            throw new WebScriptException(
                    "The following meta-data is required for a main document:\n\tCategory\n\ttype");
        } else {
            // category
            documentService.updateDocumentCategory(docRecord, documentCategory);
            this.nodeService.removeProperty(childAssocRef.getChildRef(), OpenESDHModel.PROP_DOC_CATEGORY);
            // type
            documentService.updateDocumentType(docRecord, documentType);
            this.nodeService.removeProperty(childAssocRef.getChildRef(), OpenESDHModel.PROP_DOC_TYPE);
        }
    }

    private void ensureAttachmentsAssociation(ChildAssociationRef childAssocRef) {
        if (nodeService.hasAspect(childAssocRef.getChildRef(), OpenESDHModel.ASPECT_DOC_IS_MAIN_FILE)) {
            return;
        }
        NodeRef attachmentRef = childAssocRef.getChildRef();
        NodeRef docRecord = childAssocRef.getParentRef();

        ChildAssociationRef docMainAssociation = getDocMainAssociation(docRecord);
        if (docMainAssociation == null) {
            return;
        }

        NodeRef mainDocRef = docMainAssociation.getChildRef();
        String attachmentName = (String) nodeService.getProperty(attachmentRef, ContentModel.PROP_NAME);
        nodeService.addChild(mainDocRef, attachmentRef, OpenESDHModel.ASSOC_DOC_ATTACHMENTS,
                QName.createQName(OpenESDHModel.DOC_URI, attachmentName));
        
        //Associations do not auto sync with the version history.
        //In order to sync associations we have to recreate current version of the main document.
        refreshCurrentNodeVersionToSaveAssociationsHistory(mainDocRef);
    }
    
    private void refreshCurrentNodeVersionToSaveAssociationsHistory(NodeRef nodeRef){
        behaviourFilterService.executeWithoutBehavior(() -> {
            Version currentVersion = versionService.getCurrentVersion(nodeRef);
            versionService.deleteVersion(nodeRef, currentVersion);
            Map<String, Serializable> props = new HashMap<>();
            props.put(VersionModel.PROP_VERSION_TYPE, currentVersion.getVersionType());
            versionService.createVersion(nodeRef, props);
        });
    }

    private void fillEmptyTitleFromName(NodeRef fileRef) {
        if (nodeService.getProperty(fileRef, ContentModel.PROP_TITLE) == null) {
            nodeService.setProperty(fileRef, ContentModel.PROP_TITLE, nodeService.getProperty(fileRef, ContentModel.PROP_NAME));
        }
    }


    private void removeDocMainAssociation(NodeRef docRecordNodeRef) {
        ChildAssociationRef docMainAssociation = getDocMainAssociation(docRecordNodeRef);
        if (docMainAssociation == null) {
            return;
        }
        nodeService.removeChildAssociation(docMainAssociation);
    }

    private ChildAssociationRef getDocMainAssociation(NodeRef docFolderNodeRef) {
        List<ChildAssociationRef> childAssocList = nodeService.getChildAssocs(docFolderNodeRef,
                OpenESDHModel.ASSOC_DOC_MAIN, RegexQNamePattern.MATCH_ALL);
        if (CollectionUtils.isEmpty(childAssocList)) {
            return null;
        }
        return childAssocList.get(0);
    }

    private void restoreDocMainAssociationAndCopyDocMainAssociation(NodeRef docRecordRef,
            Map<NodeRef, NodeRef> copyMap) {
        
        String documentName = (String) nodeService.getProperty(docRecordRef, ContentModel.PROP_NAME);
        NodeRef mainDocRef = getDocContentFileRef(docRecordRef, documentName);
        
        nodeService.addChild(docRecordRef, mainDocRef, OpenESDHModel.ASSOC_DOC_MAIN,
                OpenESDHModel.ASSOC_DOC_MAIN);

        NodeRef copyDocRecordNode = copyMap.get(docRecordRef);
        nodeService.setProperty(copyDocRecordNode, ContentModel.PROP_NAME, documentName);

        ChildAssociationRef copyDocMainAssoc = getDocMainAssociation(copyDocRecordNode);
        if (copyDocMainAssoc != null) {
            // in case main doc has been set for the document copy
            return;
        }

        Optional<NodeRef> optMainDocCopy = Optional.ofNullable(copyMap.get(mainDocRef));
        if (!optMainDocCopy.isPresent()) {
            // in case only document record folder is being copied
            return;
        }
        NodeRef copyDocFileNode = copyMap.get(mainDocRef);
        nodeService.addChild(copyDocRecordNode, copyDocFileNode, OpenESDHModel.ASSOC_DOC_MAIN,
                OpenESDHModel.ASSOC_DOC_MAIN);
        nodeService.addAspect(copyDocFileNode, OpenESDHModel.ASPECT_DOC_IS_MAIN_FILE, null);
    }

    private NodeRef getDocContentFileRef(NodeRef docRecordRef) {
        String documentName = (String) nodeService.getProperty(docRecordRef, ContentModel.PROP_NAME);
        return getDocContentFileRef(docRecordRef, documentName);
    }

    private NodeRef getDocContentFileRef(NodeRef docRecordRef, String documentName) {
        QName docContentAssocName = Utils.createDocumentContentAssociationName(documentName);
        return nodeService.getChildAssocs(docRecordRef, ContentModel.ASSOC_CONTAINS, docContentAssocName).stream()
                .map(ChildAssociationRef::getChildRef).findFirst().get();
    }
    // </editor-fold>
}
