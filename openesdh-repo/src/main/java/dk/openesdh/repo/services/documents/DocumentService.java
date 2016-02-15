package dk.openesdh.repo.services.documents;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.json.JSONException;
import org.json.JSONObject;

import dk.openesdh.repo.model.CaseDocument;
import dk.openesdh.repo.model.CaseDocumentAttachment;
import dk.openesdh.repo.model.DocumentCategory;
import dk.openesdh.repo.model.DocumentStatus;
import dk.openesdh.repo.model.DocumentType;
import dk.openesdh.repo.model.ResultSet;
import dk.openesdh.repo.services.HasStatus;
import dk.openesdh.repo.webscripts.documents.Documents;

/**
 * Created by torben on 11/09/14.
 */
public interface DocumentService extends HasStatus<DocumentStatus> {

    public static final String DOCUMENT_STORED_IN_CASE_MESSAGE = "The document or it's copy has already been stored in the case ";

    /**
     * Gets the main document node in case document (i.e. The content with the doc:main aspect inside the folder)
     *
     * @param caseDocNodeRef The actual case document nodeRef (i.e. The case card folder)
     * @return
     */
    public NodeRef getMainDocument(NodeRef caseDocNodeRef);

    /**
     * Get the owner of the document
     *
     * @param caseDocNodeRef the Case document node
     * @return a PersonInfo structure from which we can query various properties of the person
     */
    public PersonService.PersonInfo getDocumentOwner(NodeRef caseDocNodeRef);

    /**
     * Get the person marked as being responsible for the document. We return a list to leave room for the
     * future possibility that there might be more than one person responsible for a document
     *
     * @param caseDocNodeRef the Case document node
     * @return a PersonInfo structure from which we can query various properties of the person
     */
    public List<PersonService.PersonInfo> getDocResponsibles(NodeRef caseDocNodeRef);

    /**
     * Check if the node is a document (extends doc:base).
     *
     * @param nodeRef
     * @return
     */
    boolean isDocNode(NodeRef nodeRef);

    public List<ChildAssociationRef> getDocumentsForCase(NodeRef nodeRef);

    JSONObject buildJSON(List<ChildAssociationRef> childAssociationRefs, Documents documents, NodeRef caseNodeRef);

    public ChildAssociationRef createDocumentFolder(final NodeRef documentsFolder, final String name);

    public ChildAssociationRef createDocumentFolder(final NodeRef documentsFolder, final String name, Map<QName, Serializable> props);

    /**
     *
     * @param caseId
     * @param title
     * @param fileName
     * @param docType nodeRefId
     * @param docCatagory nodeRefId
     * @param contentWriter writer -> {writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN); writer.putContent(content);}
     * @return Created document folder
     */
    public NodeRef createCaseDocument(String caseId, String title, String fileName,
            NodeRef docType, NodeRef docCatagory, Consumer<ContentWriter> contentWriter);

    /**
     *
     * @param caseNodeRef
     * @param title
     * @param fileName
     * @param docType nodeRefId
     * @param docCatagory nodeRefId
     * @param contentWriter writer -> {writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN); writer.putContent(content);}
     * @return Created document folder
     */
    public NodeRef createCaseDocument(NodeRef caseNodeRef, String title, String fileName,
            NodeRef docType, NodeRef docCatagory, Consumer<ContentWriter> contentWriter);

    /**
     *
     * @param documentFolder
     * @param title
     * @param fileName
     * @param docType nodeRefId
     * @param docCatagory nodeRefId
     * @param contentWriter writer -> {writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN); writer.putContent(content);}
     * @return Created document folder
     */
    public NodeRef createDocumentFile(NodeRef documentFolder, String title, String fileName,
            NodeRef docType, NodeRef docCatagory, Consumer<ContentWriter> contentWriter);

    /**
     * This method gets the <code>case:simple</code> NodeRef for the case which contains the given NodeRef.
     * If the given NodeRef is not contained within a case, then <code>null</code> is returned.
     *
     * @param nodeRef the node whose containing case is to be found.
     * @return NodeRef case node reference or null if node is not within a case
     */
    public NodeRef getCaseNodeRef(NodeRef nodeRef);

    /**
     * Get the attachments of the document record.
     *
     * @param docRecordNodeRef
     * @return
     */
    List<NodeRef> getAttachments(NodeRef docRecordNodeRef);

    /**
     * Retrieves case documents with attachments
     *
     * @param caseId
     * id of the case to retrieve documents with attachments for
     * @return list of the case documents with attachments
     */
    public List<CaseDocument> getCaseDocumentsWithAttachments(String caseId);

    /**
     * Updates case document properties
     *
     * @param caseDocument
     * case document to update
     */
    public void updateCaseDocumentProperties(CaseDocument caseDocument);

    public List<NodeRef> findCaseDocuments(String filter, int size);

    /**
     * Get document type by document NodeRef
     *
     * @param docNodeRef
     * @return DocumentType
     */
    public DocumentType getDocumentType(NodeRef docNodeRef);

    /**
     * Set document type for document
     *
     * @param docNodeRef
     * @param type
     */
    public void updateDocumentType(NodeRef docNodeRef, DocumentType type);

    /**
     * Get document category by document NodeRef
     *
     * @param docNodeRef
     * @return DocumentCategory
     */
    public DocumentCategory getDocumentCategory(NodeRef docNodeRef);

    /**
     * Set document type for document
     *
     * @param docNodeRef
     * @param category
     */
    public void updateDocumentCategory(NodeRef docNodeRef, DocumentCategory category);

    /**
     * Retrieves document record nodeRef for provided document or attachment
     *
     * @param docOrAttachmentNodeRef
     * nodeRef of the document or attachment to get the document
     * record for
     * @return nodeRef of the document record
     */
    public NodeRef getDocRecordNodeRef(NodeRef docOrAttachmentNodeRef);

    /**
     * Checks if provided name exists in folder and adds a counter to make it unique
     *
     * @param inFolder
     * @param name
     * @param isUniqueWithoutExtension checking the uniqueness with or without extension
     * @return
     */
    public String getUniqueName(NodeRef inFolder, String name, boolean isUniqueWithoutExtension);

    /**
     * Retrieves document path for edit on line
     *
     * @param docOrAttachmentNodeRef
     * nodeRef of the document or attachment
     * @return path of the document for edit on line
     */
    public String getDocumentEditOnlinePath(NodeRef docOrAttachmentNodeRef);

    /**
     * Retrieves lock state of case document or attachment
     *
     * @param docOrAttachmentNodeRef
     * nodeRef of the document or attachment
     * @return lock state of case document or attachment
     * @throws JSONException
     */
    public JSONObject getDocumentEditLockState(NodeRef docOrAttachmentNodeRef) throws JSONException;

    /**
     * Retrieves attachments of the provided case document version
     *
     * @param mainDocVersionNodeRef
     * @param startIndex
     * @param pageSize
     * @return list of the case document attachments
     */
    public ResultSet<CaseDocumentAttachment> getDocumentVersionAttachments(NodeRef mainDocVersionNodeRef, int startIndex,
            int pageSize);

    /**
     * Retrieves attachments associations for the provided main document
     * nodeRef.
     * 
     * @param mainDocNodeRef
     * @return
     */
    public List<ChildAssociationRef> getAttachmentsAssoc(NodeRef mainDocNodeRef);

    /**
     * Retrieves case document with attachments
     * 
     * @param docRecordNodeRef
     * @return
     */
    CaseDocument getCaseDocument(NodeRef docRecordNodeRef);

}
