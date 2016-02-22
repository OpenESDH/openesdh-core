package dk.openesdh.repo.services.documents;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import dk.openesdh.repo.model.CaseDocument;
import dk.openesdh.repo.model.CaseDocumentAttachment;
import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.BehaviourFilterService;
import dk.openesdh.repo.services.TransactionRunner;
import dk.openesdh.repo.services.cases.CaseService;

@Service("CaseDocumentCopyService")
public class CaseDocumentCopyServiceImpl implements CaseDocumentCopyService {

    @Autowired
    @Qualifier("DocumentService")
    private DocumentService documentService;
    @Autowired
    @Qualifier("CopyService")
    private CopyService copyService;
    @Autowired
    @Qualifier("NodeService")
    private NodeService nodeService;
    @Autowired
    @Qualifier("CaseService")
    private CaseService caseService;
    @Autowired
    private BehaviourFilterService behaviourFilterService;
    @Autowired
    @Qualifier("CaseDocumentVersionService")
    private VersionService versionService;
    @Autowired
    @Qualifier("CheckOutCheckInService")
    private CheckOutCheckInService checkOutCheckInService;
    @Autowired
    @Qualifier("ContentService")
    private ContentService contentService;
    @Autowired
    @Qualifier("TransactionRunner")
    private TransactionRunner tr;

    @Override
    public void moveDocumentToCase(NodeRef documentRecFolderToMove, String targetCaseId) throws Exception {

        NodeRef targetCase = getTargetCase(targetCaseId);
        NodeRef targetCaseDocumentsFolder = caseService.getDocumentsFolder(targetCase);

        if (isCaseContainsDocument(targetCaseDocumentsFolder, documentRecFolderToMove)) {
            throw new Exception(DocumentService.DOCUMENT_STORED_IN_CASE_MESSAGE + targetCaseId);
        }

        String documentFolderName = (String) nodeService.getProperty(documentRecFolderToMove,
                ContentModel.PROP_NAME);

        nodeService.moveNode(documentRecFolderToMove, targetCaseDocumentsFolder, ContentModel.ASSOC_CONTAINS,
                QName.createQName(OpenESDHModel.DOC_URI, documentFolderName));
    }

    @Override
    public NodeRef moveAsCaseDocument(NodeRef caseNodeRef, NodeRef fileNodeRef, String title, String fileName,
            NodeRef docType, NodeRef docCatagory, String description) {
        NodeRef caseDocumentsFolder = caseService.getDocumentsFolder(caseNodeRef);
        String name = documentService.getUniqueName(caseDocumentsFolder,
                DocumentServiceImpl.sanitizeName(StringUtils.defaultIfEmpty(fileName, title)),
                true);
        behaviourFilterService.executeWithoutBehavior(fileNodeRef, () -> {
            Map<QName, Serializable> props = nodeService.getProperties(fileNodeRef);
            props.put(OpenESDHModel.PROP_DOC_TYPE, docType.toString());
            props.put(OpenESDHModel.PROP_DOC_CATEGORY, docCatagory.toString());
            if (StringUtils.isNotEmpty(description)) {
                props.put(ContentModel.PROP_DESCRIPTION, description);
            }
            nodeService.setProperties(fileNodeRef, props);
        });
        ChildAssociationRef movedNode = nodeService.moveNode(fileNodeRef, caseDocumentsFolder,
                ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name));

        behaviourFilterService.executeWithoutBehavior(fileNodeRef,
                // update file name if it was changed due to uniqueness
                () -> nodeService.setProperty(fileNodeRef, ContentModel.PROP_NAME, name));
        return movedNode.getChildRef();
    }

    @Override
    public void copyDocumentToCase(NodeRef documentRecFolderToCopy, String targetCaseId) throws Exception {

        NodeRef targetCase = getTargetCase(targetCaseId);
        NodeRef targetCaseDocumentsFolder = caseService.getDocumentsFolder(targetCase);

        if (isCaseContainsDocument(targetCaseDocumentsFolder, documentRecFolderToCopy)) {
            throw new Exception(DocumentService.DOCUMENT_STORED_IN_CASE_MESSAGE + targetCaseId);
        }

        copyDocumentToFolder(documentRecFolderToCopy, targetCaseDocumentsFolder);
    }
    
    @Override
    public void copyDocumentToFolder(NodeRef documentRecFolderToCopy, NodeRef targetFolder) {
        copyDocument(documentRecFolderToCopy, targetFolder, true);
    }

    @Override
    public NodeRef copyDocumentToFolder(NodeRef documentRecRef, NodeRef targetFolder, boolean copyWithAttachments) {

        if (copyWithAttachments) {
            return copyDocument(documentRecRef, targetFolder, copyWithAttachments);
        }

        NodeRef documentCopyRef = copyDocumentRecord(documentRecRef, targetFolder);
        NodeRef mainDocRef = documentService.getMainDocument(documentRecRef);
        copyMainDocument(mainDocRef, documentCopyRef);
        return documentCopyRef;
    }

    @Override
    public NodeRef copyDocumentToFolderRetainVersionLabels(CaseDocument document, NodeRef targetFolder) {

        Map<NodeRef, NodeRef> copyMap = new HashMap<>();
        NodeRef documentRec = new NodeRef(document.getNodeRef());
        NodeRef mainDocRef = new NodeRef(document.getMainDocNodeRef());

        // Copy in new transaction to make sure all necessary behaviors run
        // and all aspects set
        NodeRef documentCopy = tr.runInNewTransaction(() -> {
            NodeRef documentRecCopy = copyDocumentRecord(documentRec, targetFolder);
            NodeRef mainDocCopy = copyMainDocument(mainDocRef, documentRecCopy);
            copyMap.put(mainDocRef, mainDocCopy);
            document.getAttachments()
                    .stream()
                    .map(CaseDocumentAttachment::getNodeRef)
                    .map(NodeRef::new)
                    .forEach(attachment -> copyMap.put(attachment, copyAttachment(attachment, documentRecCopy, mainDocCopy)));
            return documentRecCopy;    
        });
        
        copyMap.entrySet()
            .stream()
            .forEach(entry -> retainVersionLabelDeleteCurrentVersion(entry.getKey(), entry.getValue()));
        
        return documentCopy;
    }

    @Override
    public NodeRef copyDocument(NodeRef documentContentRef, NodeRef targetFolder, boolean copyChildren) {
        String documentName = (String) nodeService.getProperty(documentContentRef, ContentModel.PROP_NAME);
        return copyService.copyAndRename(documentContentRef, targetFolder, ContentModel.ASSOC_CONTAINS,
                QName.createQName(OpenESDHModel.DOC_URI, documentName), copyChildren);
    }

    @Override
    public void copyDocContentRetainVersionLabel(NodeRef newVersionContentRef, NodeRef targetDocumentRef) {
        ContentReader newContent = contentService.getReader(newVersionContentRef, ContentModel.PROP_CONTENT);
        if (Objects.isNull(newContent)) {
            return;
        }
        Boolean autoVersion = (Boolean) this.nodeService.getProperty(targetDocumentRef,
                ContentModel.PROP_AUTO_VERSION);
        if (!Objects.isNull(autoVersion) && autoVersion == true) {
            throw new AlfrescoRuntimeException(
                    "Cannot retain version label since the target object has auto version enabled.");
        }
        NodeRef workingCopy = checkOutCheckInService.checkout(targetDocumentRef);
        ContentWriter writer = contentService.getWriter(workingCopy, ContentModel.PROP_CONTENT, true);
        writer.setMimetype(newContent.getMimetype());
        writer.putContent(newContent);
        checkOutCheckInService.checkin(workingCopy, null);
        retainVersionLabel(newVersionContentRef, targetDocumentRef);
    }

    private NodeRef copyDocumentRecord(NodeRef documentRecRef, NodeRef targetFolder) {
        return copyDocument(documentRecRef, targetFolder, false);
    }

    private NodeRef copyMainDocument(NodeRef mainDocRef, NodeRef targetDocumentRecRef) {
        NodeRef mainDocCopyRef = copyDocument(mainDocRef, targetDocumentRecRef, false);
        return mainDocCopyRef;
    }

    private NodeRef copyAttachment(NodeRef attachmentRef, NodeRef documentRecRef, NodeRef mainDocRef) {
        NodeRef attachmentCopy = copyDocument(attachmentRef, documentRecRef, false);
        String attachmentName = (String) nodeService.getProperty(attachmentCopy, ContentModel.PROP_NAME);
        nodeService.addChild(mainDocRef, attachmentCopy, OpenESDHModel.ASSOC_DOC_ATTACHMENTS,
                QName.createQName(OpenESDHModel.DOC_URI, attachmentName));
        return attachmentCopy;
    }

    private NodeRef getTargetCase(String targetCaseId) throws Exception {
        try {
            return caseService.getCaseById(targetCaseId);
        } catch (Exception e) {
            throw new Exception("Error trying to get target case for the case id: " + targetCaseId, e);
        }
    }

    private boolean isCaseContainsDocument(NodeRef targetCaseDocumentsFolder, NodeRef documentRecFolderToCopy) {
        return nodeService.getChildAssocs(targetCaseDocumentsFolder)
                .stream()
                .map(ChildAssociationRef::getChildRef)
                .filter(child -> child.equals(documentRecFolderToCopy)
                        || isDocumentCopy(child, documentRecFolderToCopy))
                .findAny().isPresent();
    }

    private boolean isDocumentCopy(NodeRef docRef1, NodeRef docRef2) {
        Optional<NodeRef> original = nodeService.getTargetAssocs(docRef1, ContentModel.ASSOC_ORIGINAL)
                .stream()
                .map(AssociationRef::getTargetRef)
                .filter(target -> target.equals(docRef2))
                .findAny();
        if (original.isPresent()) {
            return true;
        }

        return nodeService.getTargetAssocs(docRef2, ContentModel.ASSOC_ORIGINAL).stream()
                .map(AssociationRef::getTargetRef).filter(target -> target.equals(docRef1))
                .findAny()
                .isPresent();
    }

    private void retainVersionLabelDeleteCurrentVersion(NodeRef docOriginal, NodeRef docCopy) {
        behaviourFilterService.executeWithoutBehavior(() -> {
            Version copyInitVersion = versionService.getCurrentVersion(docCopy);
            versionService.deleteVersion(docCopy, copyInitVersion);
            retainVersionLabel(docOriginal, docCopy);
        });
    }

    private void retainVersionLabel(NodeRef docOriginal, NodeRef docCopy) {
        Version originalVersion = versionService.getCurrentVersion(docOriginal);
        Map<String, Serializable> versionProps = new HashMap<>();
        versionProps.put(OpenESDHModel.RETAIN_VERSION_LABEL, originalVersion.getVersionLabel());
    }
}
