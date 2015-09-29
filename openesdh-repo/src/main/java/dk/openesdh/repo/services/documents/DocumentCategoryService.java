package dk.openesdh.repo.services.documents;

import dk.openesdh.repo.model.DocumentCategory;
import java.util.List;
import java.util.Optional;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Created by Arnas on 21/09/28.
 */
public interface DocumentCategoryService {

    /**
     * get available document categories
     *
     * @return
     */
    public List<DocumentCategory> getDocumentCategories();

    /**
     * Create/Update
     *
     * @param documentCategory
     * @return
     */
    public DocumentCategory createOrUpdateDocumentCategory(DocumentCategory documentCategory);

    /**
     * Read
     *
     * @param nodeRef
     * @return
     */
    public DocumentCategory getDocumentCategory(NodeRef nodeRef);

    public Optional<DocumentCategory> getDocumentCategoryByName(String categoryName);

    /**
     * Delete
     *
     * @param documentCategory
     */
    public void deleteDocumentCategory(DocumentCategory documentCategory);
}
