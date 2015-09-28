package dk.openesdh.repo.services.documents;

import dk.openesdh.repo.model.DocumentType;
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

public class DocumentTypeServiceImpl implements DocumentTypeService {

    public static List<String> SYSTEM_TYPES = Arrays.asList("invoice", "letter");
    private OpenESDHFoldersService openESDHFoldersService;
    private NodeService nodeService;

    @Override
    public DocumentType saveDocumentType(DocumentType documentType) {
        Map<QName, Serializable> properties = getProperties(documentType.getNodeRef());
        if (isSystemType(properties)) {
            throwErrorIfSystemNameWasChanged(properties, documentType);
        } else {
            properties.put(OpenESDHModel.PROP_DOC_TYPE_SYSTEM, false);
        }
        properties.put(ContentModel.PROP_NAME, documentType.getName());
        properties.put(OpenESDHModel.PROP_DOC_TYPE_DISPLAY_NAME, documentType.getDisplayName());

        if (documentType.getNodeRef() == null) {
            ChildAssociationRef createdNode = nodeService.createNode(
                    getDocumentTypesRoot(),
                    ContentModel.ASSOC_CONTAINS,
                    OpenESDHModel.ASSOC_DOC_TYPE,
                    OpenESDHModel.TYPE_DOC_TYPE,
                    properties);
            documentType.setNodeRef(createdNode.getChildRef());
            return documentType;
        }
        nodeService.setProperties(documentType.getNodeRef(), properties);
        return documentType;
    }

    private boolean isSystemType(Map<QName, Serializable> properties) {
        return properties.get(OpenESDHModel.PROP_DOC_TYPE_SYSTEM) != null
                && (Boolean) properties.get(OpenESDHModel.PROP_DOC_TYPE_SYSTEM);
    }

    private void throwErrorIfSystemNameWasChanged(Map<QName, Serializable> properties, DocumentType documentType) throws AlfrescoRuntimeException {
        if (!properties.get(ContentModel.PROP_NAME).equals(documentType.getName())) {
            throw new AlfrescoRuntimeException("Can not change name of system document type");
        }
    }

    private Map<QName, Serializable> getProperties(NodeRef nodeRef) throws InvalidNodeRefException {
        if (nodeRef == null) {
            return new HashMap<>();
        }
        return nodeService.getProperties(nodeRef);
    }

    private NodeRef getDocumentTypesRoot() {
        return openESDHFoldersService.getDocumentTypesRootNodeRef();
    }

    @Override
    public DocumentType getDocumentType(NodeRef nodeRef) {
        try {
            QName nodeType = nodeService.getType(nodeRef);
            if (!nodeType.isMatch(OpenESDHModel.TYPE_DOC_TYPE)) {
                throw new AlfrescoRuntimeException("Invalid type. Expected: " + OpenESDHModel.TYPE_DOC_TYPE + ", actual: " + nodeType.toString());
            }
            Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
            DocumentType type = new DocumentType();
            type.setNodeRef(nodeRef);
            type.setName((String) properties.get(ContentModel.PROP_NAME));
            type.setDisplayName((String) properties.get(OpenESDHModel.PROP_DOC_TYPE_DISPLAY_NAME));
            type.setSystemType((Boolean) properties.get(OpenESDHModel.PROP_DOC_TYPE_SYSTEM));
            return type;
        } catch (InvalidNodeRefException none) {
            //node does not exist
            return null;
        }
    }

    @Override
    public Optional<DocumentType> getDocumentTypeByName(String typeName) {
        return nodeService.getChildAssocs(getDocumentTypesRoot())
                .stream()
                .map(assocItem -> getDocumentType(assocItem.getChildRef()))
                .filter(type -> type.getName().equals(typeName))
                .findAny();
    }

    @Override
    public void deleteDocumentType(DocumentType documentType) {
        if ((Boolean) nodeService.getProperty(documentType.getNodeRef(), OpenESDHModel.PROP_DOC_TYPE_SYSTEM)) {
            throw new AlfrescoRuntimeException("Cannot delete system document type.");
        }
        nodeService.deleteNode(documentType.getNodeRef());

    }

    @Override
    public List<DocumentType> getDocumentTypes() {
        return nodeService.getChildAssocs(openESDHFoldersService.getDocumentTypesRootNodeRef())
                .stream()
                .map(assocItem -> getDocumentType(assocItem.getChildRef()))
                .collect(Collectors.toList());
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setOpenESDHFoldersService(OpenESDHFoldersService openESDHFoldersService) {
        this.openESDHFoldersService = openESDHFoldersService;
    }

}
