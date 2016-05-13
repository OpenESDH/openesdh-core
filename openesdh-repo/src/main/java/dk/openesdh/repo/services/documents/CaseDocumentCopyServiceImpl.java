package dk.openesdh.repo.services.documents;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.repo.forum.CommentService;
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
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import dk.openesdh.repo.model.CaseDocsFolder;
import dk.openesdh.repo.model.CaseDocument;
import dk.openesdh.repo.model.CaseDocumentAttachment;
import dk.openesdh.repo.model.CaseFolderItem;
import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.BehaviourFilterService;
import dk.openesdh.repo.services.TransactionRunner;
import dk.openesdh.repo.services.cases.CaseService;
import dk.openesdh.repo.services.files.OeAuthorityFilesService;
import dk.openesdh.repo.services.files.OeFilesService;
import dk.openesdh.repo.webscripts.WebScriptParams;

@Service("CaseDocumentCopyService")
public class CaseDocumentCopyServiceImpl implements CaseDocumentCopyService {

    private static final String COMMENTS_TOPIC_NAME = "Comments";

    @Autowired
    @Qualifier(DocumentService.BEAN_ID)
    private DocumentService documentService;
    @Autowired
    @Qualifier("CopyService")
    private CopyService copyService;
    @Autowired
    @Qualifier("NodeService")
    private NodeService nodeService;
    @Autowired
    @Qualifier(CaseService.BEAN_ID)
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
    @Qualifier("OeAuthorityFilesService")
    private OeAuthorityFilesService authorityFilesService;
    @Autowired
    @Qualifier("TransactionRunner")
    private TransactionRunner tr;
    @Autowired
    @Qualifier("CommentService")
    private CommentService commentService;

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
    public NodeRef copyDocumentToFolder(NodeRef documentRecFolderToCopy, NodeRef targetFolder) {
        return copyDocFolderItem(documentRecFolderToCopy, targetFolder, true);
    }

    @Override
    public NodeRef copyDocumentToFolder(NodeRef documentRecRef, NodeRef targetFolder, boolean copyWithAttachments) {

        if (copyWithAttachments) {
            return copyDocFolderItem(documentRecRef, targetFolder, copyWithAttachments);
        }

        NodeRef documentCopyRef = copyDocsFolder(documentRecRef, targetFolder);
        NodeRef mainDocRef = documentService.getMainDocument(documentRecRef);
        copyMainDocument(mainDocRef, documentCopyRef);
        return documentCopyRef;
    }

    @Override
    public NodeRef copyDocumentToFolderRetainVersionLabels(CaseDocument document, NodeRef targetFolder) {

        Map<NodeRef, NodeRef> copyMap = new HashMap<>();
        NodeRef documentRec = document.getNodeRef();
        NodeRef mainDocRef = document.getMainDocNodeRef();

        // Copy in new transaction to make sure all necessary behaviors run
        // and all aspects set
        NodeRef documentCopy = tr.runInNewTransaction(() -> {
            NodeRef documentRecCopy = copyDocsFolder(documentRec, targetFolder);
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
    public NodeRef copyDocFolderItem(NodeRef itemRef, NodeRef targetFolder, boolean copyChildren) {
        String itemName = (String) nodeService.getProperty(itemRef, ContentModel.PROP_NAME);
        return copyService.copyAndRename(itemRef, targetFolder, ContentModel.ASSOC_CONTAINS,
                QName.createQName(OpenESDHModel.DOC_URI, itemName), copyChildren);
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

    @Override
    public NodeRef copyDocFolderItemRetainVersionLabels(CaseFolderItem item, NodeRef targetFolder) {
        if (item.isFolder()) {
            NodeRef folderCopyRef = tr.runInNewTransaction(() -> {
                return copyDocsFolder(item.getNodeRef(), targetFolder);
            });
            CaseDocsFolder folder = (CaseDocsFolder) item;
            folder.getChildren()
                    .forEach(childItem -> copyDocFolderItemRetainVersionLabels(childItem, folderCopyRef));
            return folderCopyRef;
        }
        return copyDocumentToFolderRetainVersionLabels((CaseDocument) item, targetFolder);
    }

    @Override
    public void moveDocumentComments(NodeRef sourceNode, NodeRef targetNode) {
        if (!nodeService.hasAspect(sourceNode, ForumModel.ASPECT_DISCUSSABLE)) {
            return;
        }
        tr.runAsSystem(() -> {
            NodeRef targetCommentsFolder = getOrCreateCommentsFolder(targetNode);
            NodeRef sourceCommentsFolder = getCommentsFolder(sourceNode);
            nodeService.getChildAssocs(sourceCommentsFolder, ContentModel.ASSOC_CONTAINS, null).stream()
                    .map(ChildAssociationRef::getChildRef)
                    .forEach(commentRef -> moveComment(commentRef, targetCommentsFolder));
            return null;
        });
    }

    @Override
    public void detachCaseDocument(NodeRef documentRef, NodeRef newOwner, String comment) {
        NodeRef mainDocNodeRef = documentService.getMainDocument(documentRef);
        tr.runInTransaction(() -> {
            authorityFilesService.move(mainDocNodeRef, newOwner, comment);
            nodeService.setType(mainDocNodeRef, ContentModel.TYPE_CONTENT);
            moveDocumentComments(documentRef, mainDocNodeRef);
            nodeService.deleteNode(documentRef);
            return null;
        });
    }

    @Override
    public void copyCaseDocumentsFromTempAttachments(NodeRef caseNodeRef) {
        if (!nodeService.hasAspect(caseNodeRef, OpenESDHModel.ASPECT_OE_TEMP_ATTACHMENTS)) {
            return;
        }

        String jsonTempAttachmens = (String) nodeService.getProperty(caseNodeRef,
                OpenESDHModel.PROP_OE_TEMP_ATTACHMENTS);

        if (StringUtils.isEmpty(jsonTempAttachmens)) {
            nodeService.removeAspect(caseNodeRef, OpenESDHModel.ASPECT_OE_TEMP_ATTACHMENTS);
            return;
        }

        NodeRef caseDocsFolder = caseService.getDocumentsFolder(caseNodeRef);
        try {
            JSONArray tempAttachments = new JSONArray(new JSONTokener(jsonTempAttachmens));
            for (int i = 0; i < tempAttachments.length(); i++) {
                copyCaseDocFromTempAttachment(tempAttachments.getJSONObject(i), caseDocsFolder);
            }
        } catch (JSONException e) {
            throw new AlfrescoRuntimeException("Error parsing temp attachments json", e);
        }

        nodeService.removeAspect(caseNodeRef, OpenESDHModel.ASPECT_OE_TEMP_ATTACHMENTS);
    }

    private NodeRef copyCaseDocFromTempAttachment(JSONObject tempAttachment, NodeRef targetFolderRef)
            throws JSONException {
        String tmpFileName = tempAttachment.getString(OeFilesService.TMP_FILE_NAME);
        String fileName = tempAttachment.getString(OeFilesService.FILE_NAME);
        String mimeType = tempAttachment.getString(OeFilesService.MIME_TYPE);
        NodeRef docType = new NodeRef(tempAttachment.getString(WebScriptParams.DOC_TYPE));
        NodeRef docCategory = new NodeRef(tempAttachment.getString(WebScriptParams.DOC_CATEGORY));

        Optional<File> tmpFile = getTempFileByName(tmpFileName);
        if (!tmpFile.isPresent()) {
            throw new AlfrescoRuntimeException("Temp attachment file not found " + tmpFileName + " " + fileName);
        }

        return documentService.createDocumentFile(targetFolderRef, fileName, fileName, docType, docCategory,
                writer -> {
                    writer.setMimetype(mimeType);
                    writer.putContent(tmpFile.get());
                });
    }

    private Optional<File> getTempFileByName(String tmpFileName) {
        File[] files = TempFileProvider.getTempDir().listFiles((file, name) -> name.equals(tmpFileName));
        if (files.length == 0) {
            return Optional.empty();
        }
        return Optional.of(files[0]);
    }

    private NodeRef copyDocsFolder(NodeRef documentRecRef, NodeRef targetFolder) {
        return copyDocFolderItem(documentRecRef, targetFolder, false);
    }

    private NodeRef copyMainDocument(NodeRef mainDocRef, NodeRef targetDocumentRecRef) {
        NodeRef mainDocCopyRef = copyDocFolderItem(mainDocRef, targetDocumentRecRef, false);
        return mainDocCopyRef;
    }

    private NodeRef copyAttachment(NodeRef attachmentRef, NodeRef documentRecRef, NodeRef mainDocRef) {
        NodeRef attachmentCopy = copyDocFolderItem(attachmentRef, documentRecRef, false);
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
        versionService.createVersion(docCopy, versionProps);
    }

    private NodeRef getOrCreateCommentsFolder(NodeRef discussableNode) {
        if (!nodeService.hasAspect(discussableNode, ForumModel.ASPECT_DISCUSSABLE)) {
            NodeRef fakeComment = commentService.createComment(discussableNode, "fake", "fake", false);
            commentService.deleteComment(fakeComment);
        }
        return getCommentsFolder(discussableNode);
    }

    private NodeRef getCommentsFolder(NodeRef discussableNode) {
        List<ChildAssociationRef> assocs = nodeService.getChildAssocs(discussableNode, ForumModel.ASSOC_DISCUSSION,
                RegexQNamePattern.MATCH_ALL);
        ChildAssociationRef firstAssoc = assocs.get(0);
        return nodeService.getChildByName(firstAssoc.getChildRef(), ContentModel.ASSOC_CONTAINS,
                COMMENTS_TOPIC_NAME);
    }

    private void moveComment(NodeRef commentRef, NodeRef targetCommentsFolder) {
        String name = getUniqueChildName("comment");
        nodeService.moveNode(commentRef, targetCommentsFolder, ContentModel.ASSOC_CONTAINS,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(name)));
    }

    private String getUniqueChildName(String prefix) {
        return prefix + "-" + System.currentTimeMillis();
    }
}
