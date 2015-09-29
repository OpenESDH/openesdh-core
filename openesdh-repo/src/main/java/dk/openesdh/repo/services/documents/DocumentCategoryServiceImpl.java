package dk.openesdh.repo.services.documents;

import dk.openesdh.repo.model.DocumentCategory;
import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.system.OpenESDHFoldersService;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

public class DocumentCategoryServiceImpl implements DocumentCategoryService {

    public static List<String> SYSTEM_TYPES = Arrays.asList("annex");
    private OpenESDHFoldersService openESDHFoldersService;
    private NodeService nodeService;

    @Override
    public List<DocumentCategory> getDocumentCategories() {
        return nodeService.getChildAssocs(openESDHFoldersService.getDocumentCategoriesRootNodeRef())
                .stream()
                .map(assocItem -> getDocumentCategory(assocItem.getChildRef()))
                .collect(Collectors.toList());
    }

    @Override
    public DocumentCategory createOrUpdateDocumentCategory(DocumentCategory documentCategory) {
        Map<QName, Serializable> properties = getProperties(documentCategory.getNodeRef());
        if (isSystemCategory(properties)) {
            throwErrorIfSystemNameWasChanged(properties, documentCategory);
        } else {
            properties.put(OpenESDHModel.PROP_DOC_CATEGORY_SYSTEM, false);
        }
        properties.put(ContentModel.PROP_NAME, documentCategory.getName());
        properties.put(OpenESDHModel.PROP_DOC_CATEGORY_DISPLAY_NAME, documentCategory.getDisplayName());

        if (documentCategory.getNodeRef() == null) {
            ChildAssociationRef createdNode = nodeService.createNode(
                    getDocumentCategoriesRoot(),
                    ContentModel.ASSOC_CONTAINS,
                    OpenESDHModel.ASSOC_DOC_CATEGORY,
                    OpenESDHModel.TYPE_DOC_CATEGORY,
                    properties);
            documentCategory.setNodeRef(createdNode.getChildRef());
            return documentCategory;
        }
        nodeService.setProperties(documentCategory.getNodeRef(), properties);
        return documentCategory;
    }

    @Override
    public DocumentCategory getDocumentCategory(NodeRef nodeRef) {
        try {
            QName nodeType = nodeService.getType(nodeRef);
            if (!nodeType.isMatch(OpenESDHModel.TYPE_DOC_CATEGORY)) {
                throw new AlfrescoRuntimeException("Invalid type. Expected: " + OpenESDHModel.TYPE_DOC_CATEGORY + ", actual: " + nodeType.toString());
            }
            Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
            DocumentCategory type = new DocumentCategory();
            type.setNodeRef(nodeRef);
            type.setName((String) properties.get(ContentModel.PROP_NAME));
            type.setDisplayName((String) properties.get(OpenESDHModel.PROP_DOC_CATEGORY_DISPLAY_NAME));
            type.setSystemCategory((Boolean) properties.get(OpenESDHModel.PROP_DOC_CATEGORY_SYSTEM));
            return type;
        } catch (InvalidNodeRefException none) {
            //node does not exist
            return null;
        }
    }

    @Override
    public Optional<DocumentCategory> getDocumentCategoryByName(String categoryName) {
        return nodeService.getChildAssocs(getDocumentCategoriesRoot())
                .stream()
                .map(assocItem -> getDocumentCategory(assocItem.getChildRef()))
                .filter(type -> type.getName().equals(categoryName))
                .findAny();
    }

    @Override
    public void deleteDocumentCategory(DocumentCategory documentCategory) {
        if (isSystemCategory(documentCategory.getNodeRef())) {
            throw new AlfrescoRuntimeException("Cannot delete system document type.");
        }
        nodeService.deleteNode(documentCategory.getNodeRef());
    }

    private boolean isSystemCategory(Map<QName, Serializable> properties) {
        return isValueTrue(properties.get(OpenESDHModel.PROP_DOC_CATEGORY_SYSTEM));
    }

    private boolean isSystemCategory(NodeRef nodeRef) {
        return isValueTrue(nodeService.getProperty(nodeRef, OpenESDHModel.PROP_DOC_CATEGORY_SYSTEM));
    }

    private boolean isValueTrue(Serializable systemCategoryVal) {
        return systemCategoryVal != null && (Boolean) systemCategoryVal;
    }

    private void throwErrorIfSystemNameWasChanged(Map<QName, Serializable> properties, DocumentCategory documentCategory) throws AlfrescoRuntimeException {
        if (!properties.get(ContentModel.PROP_NAME).equals(documentCategory.getName())) {
            throw new AlfrescoRuntimeException("Can not change name of system document type");
        }
    }

    private Map<QName, Serializable> getProperties(NodeRef nodeRef) throws InvalidNodeRefException {
        if (nodeRef == null) {
            return new HashMap<>();
        }
        return nodeService.getProperties(nodeRef);
    }

    private NodeRef getDocumentCategoriesRoot() {
        return openESDHFoldersService.getDocumentCategoriesRootNodeRef();
    }

    public void setOpenESDHFoldersService(OpenESDHFoldersService openESDHFoldersService) {
        this.openESDHFoldersService = openESDHFoldersService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }
}
