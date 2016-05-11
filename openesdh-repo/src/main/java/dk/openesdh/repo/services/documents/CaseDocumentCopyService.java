package dk.openesdh.repo.services.documents;

import org.alfresco.service.cmr.repository.NodeRef;

import dk.openesdh.repo.model.CaseDocument;
import dk.openesdh.repo.model.CaseFolderItem;

public interface CaseDocumentCopyService {
    
    /**
     * Copies provided document to the target case
     * 
     * @param documentToMove NodeRef of the record folder of the document to move
     * @param targetCaseId
     *            Id of the case to copy the document into
     * @throws java.lang.Exception
     */
    void copyDocumentToCase(final NodeRef documentRecToCopy, final String targetCaseId) throws Exception;

    NodeRef copyDocumentToFolder(NodeRef documentRecFolderToCopy, NodeRef targetFolder, boolean copyAttachments);

    /**
     * Copies provided case document with attachments to the target folder
     * 
     * @param caseDocument
     *            the document record folder node ref to copy
     * @param targetFolder
     *            the folder to copy the document to
     * @return
     */
    NodeRef copyDocumentToFolder(NodeRef documentRecFolderToCopy, NodeRef targetFolder);

    /**
     * Moves provided document to the target case
     * 
     * @param documentToMove
     *            NodeRef of the record folder of the document to move
     * @param targetCaseId
     *            Id of the case to move the document into
     */
    void moveDocumentToCase(NodeRef documentRecFolderToMove, String targetCaseId) throws Exception;

    /**
     * moves file to case as case document
     *
     * @param caseNodeRef
     * @param title
     * @param fileName
     * @param docType
     * @param docCatagory
     * @param fileNodeRef
     * @param description
     * @return
     */
    public NodeRef moveAsCaseDocument(NodeRef caseNodeRef, NodeRef fileNodeRef, String title, String fileName,
            NodeRef docType, NodeRef docCatagory, String description);

    NodeRef copyDocumentToFolderRetainVersionLabels(CaseDocument document, NodeRef targetFolder);

    NodeRef copyDocFolderItemRetainVersionLabels(CaseFolderItem item, NodeRef targetFolder);

    NodeRef copyDocFolderItem(NodeRef documentContentRef, NodeRef targetFolder, boolean copyChildren);

    /**
     * Copies content from provided node into target document and retains
     * version label from source node.
     * 
     * @param newVersionContentRef
     * @param targetDocumentRef
     */
    void copyDocContentRetainVersionLabel(NodeRef newVersionContentRef, NodeRef targetDocumentRef);

    void moveDocumentComments(NodeRef sourceNode, NodeRef targetNode);

    /**
     * Detaches case document from a case and moves it to the specified owner
     * folder.
     * 
     * @param documentRef
     * @param newOwner
     */
    void detachCaseDocument(NodeRef documentRef, NodeRef newOwner, String comment);
}
