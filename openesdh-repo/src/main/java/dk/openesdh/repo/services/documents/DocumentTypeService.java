package dk.openesdh.repo.services.documents;

import dk.openesdh.repo.model.DocumentType;
import dk.openesdh.repo.services.system.MultiLanguageValue;
import java.util.List;
import java.util.Optional;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Created by Arnas on 21/09/15.
 */
public interface DocumentTypeService {

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
     * @param mlDisplayNames
     * @return
     */
    public DocumentType createOrUpdateDocumentType(DocumentType documentType, MultiLanguageValue mlDisplayNames);

    /**
     * Read
     *
     * @param nodeRef
     * @return
     */
    public DocumentType getDocumentType(NodeRef nodeRef);
    public Optional<DocumentType> getDocumentTypeByName(String typeName);

    /**
     * returns display names of all saved locales
     *
     * @param nodeRef
     * @return
     */
    public MultiLanguageValue getMultiLanguageDisplayNames(NodeRef nodeRef);

    /**
     * Delete
     *
     * @param documentType
     */
    public void deleteDocumentType(DocumentType documentType);
}
