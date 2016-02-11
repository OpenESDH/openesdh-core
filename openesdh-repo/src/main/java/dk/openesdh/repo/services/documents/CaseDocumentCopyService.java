package dk.openesdh.repo.services.documents;

import org.alfresco.service.cmr.repository.NodeRef;

import dk.openesdh.repo.model.CaseDocument;

public interface CaseDocumentCopyService {
    
    /**
     * Copies provided document to the target case
     * 
     * @param documentToMove NodeRef of the record folder of the document to move
     * @param targetCaseId
     *            Id of the case to copy the document into
     * @throws java.lang.Exception
     */
    void copyDocumentToCase(final NodeRef documentToMove, final String targetCaseId) throws Exception;

    NodeRef copyDocumentToFolder(NodeRef documentRecFolderToCopy, NodeRef targetFolder, boolean copyAttachments);

    /**
     * Copies provided case document with attachments to the target folder
     * 
     * @param caseDocument
     *            the document record folder node ref to copy
     * @param targetFolder
     *            the folder to copy the document to
     */
    void copyDocumentToFolder(NodeRef documentRecFolderToCopy, NodeRef targetFolder);

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
}
