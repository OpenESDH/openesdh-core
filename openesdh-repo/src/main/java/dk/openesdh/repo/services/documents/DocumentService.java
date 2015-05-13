package dk.openesdh.repo.services.documents;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.json.JSONObject;

import dk.openesdh.repo.webscripts.documents.Documents;

/**
 * Created by torben on 11/09/14.
 */
public interface DocumentService {

    public static final String DOCUMENT_STORED_IN_CASE_MESSAGE = "The document has already been stored in the case ";

    /**
     * Gets the main document node in case document (i.e. The content with the doc:main aspect inside the folder)
     * @param caseDocNodeRef The actual case document nodeRef (i.e. The case card folder)
     * @return
     */
    public NodeRef getMainDocument(NodeRef caseDocNodeRef);

    /**
     * Get the owner of the document
     * @param caseDocNodeRef the Case document node
     * @return a PersonInfo structure from which we can query various properties of the person
     */
    public PersonService.PersonInfo getDocumentOwner(NodeRef caseDocNodeRef);

    /**
     * Get the person marked as being responsible for the document. We return a list to leave room for the
     * future possibility that there might be more than one person responsible for a document
     * @param caseDocNodeRef the Case document node
     * @return a PersonInfo structure from which we can query various properties of the person
     */
    public List<PersonService.PersonInfo> getDocResponsibles(NodeRef caseDocNodeRef);

    public java.util.List<ChildAssociationRef> getDocumentsForCase(NodeRef nodeRef);

    JSONObject buildJSON(List<ChildAssociationRef> childAssociationRefs, Documents documents, NodeRef caseNodeRef);


    public ChildAssociationRef createDocumentFolder(final NodeRef documentsFolder, final String name);

    public ChildAssociationRef createDocumentFolder(final NodeRef documentsFolder, final String name, Map<QName, Serializable> props);

    /**
     * This method gets the <code>case:simple</code> NodeRef for the case which contains the given NodeRef.
     * If the given NodeRef is not contained within a case, then <code>null</code> is returned.
     *
     * @param nodeRef   the node whose containing case is to be found.
     * @return NodeRef  case node reference or null if node is not within a case
     */
    public NodeRef getCaseNodeRef(NodeRef nodeRef);

    /**
     * Get the attachments of the document record.
     * @param docRecordNodeRef
     * @return
     */
    List<NodeRef> getAttachments(NodeRef docRecordNodeRef);
    
    /**
	 * Moves provided document to the target case
	 * 
	 * @param documentToMove
	 *            NodeRef of the document to move
	 * @param targetCaseId
	 *            Id of the case to move the document into
	 */
	public void moveDocumentToCase(final NodeRef documentToMove,
			final String targetCaseId) throws Exception;

	/**
	 * Copies provided document to the target case
	 * 
	 * @param documentToCopy
	 *            NodeRef of the document to copy
	 * @param targetCaseId
	 *            Id of the case to copy the document into
	 */
	public void copyDocumentToCase(final NodeRef documentToMove,
			final String targetCaseId) throws Exception;
}
