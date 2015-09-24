package dk.openesdh.repo.services.documents;

import dk.openesdh.repo.model.DocumentType;
import java.util.List;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Created by Arnas on 21/09/15.
 */
public interface DocumentTypeService {

    /**
     * Get document type by document NodeRef
     *
     * @param docNodeRef
     * @return DocumentType
     */
    public DocumentType getDocumentTypeOfDocument(NodeRef docNodeRef);

    /**
     * Set document type for document
     *
     * @param docNodeRef
     * @param type
     */
    public void updateDocumentType(NodeRef docNodeRef, DocumentType type);

    /**
     * get available document types
     *
     * @return
     */
    public List<DocumentType> getDocumentTypes();

    /**
     * Create/Update
     *
     * @param documentType
     * @return
     */
    public DocumentType saveDocumentType(DocumentType documentType);

    /**
     * Read
     *
     * @param nodeRef
     * @return
     */
    public DocumentType getDocumentType(NodeRef nodeRef);
    public DocumentType getDocumentTypeByName(String typeName);

    /**
     * Delete
     *
     * @param documentType
     */
    public void deleteDocumentType(DocumentType documentType);
}
