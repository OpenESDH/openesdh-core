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
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

public class DocumentTypeServiceImpl implements DocumentTypeService {

    private OpenESDHFoldersService openESDHFoldersService;
    private NodeService nodeService;

    @Override
    public DocumentType getDocumentTypeOfDocument(NodeRef docNodeRef) {
        Optional<NodeRef> typeNode = getDocumentTypeOfDoc(docNodeRef);
        if (typeNode.isPresent()) {
            return getDocumentType(typeNode.get());
        }
        return null;
    }

    @Override
    public void updateDocumentType(NodeRef docNodeRef, DocumentType type) {
        nodeService.setAssociations(docNodeRef, OpenESDHModel.ASSOC_DOC_TYPE, Arrays.asList(type.getNodeRef()));
    }

    private Optional<NodeRef> getDocumentTypeOfDoc(NodeRef docNodeRef) throws InvalidNodeRefException {
        Optional<AssociationRef> assocRef = nodeService.getTargetAssocs(docNodeRef, OpenESDHModel.ASSOC_DOC_TYPE).stream().findFirst();
        return assocRef.isPresent()
                ? Optional.of(assocRef.get().getTargetRef())
                : Optional.empty();
    }

    @Override
    public DocumentType saveDocumentType(DocumentType documentType) {
        Map<QName, Serializable> properties = new HashMap<>();
        properties.put(ContentModel.PROP_NAME, documentType.getName());
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
            return type;
        } catch (InvalidNodeRefException none) {
            //node does not exist
            return null;
        }
    }

    @Override
    public DocumentType getDocumentTypeByName(String typeName) {
        Optional<DocumentType> typeByName = nodeService
                .getChildAssocs(getDocumentTypesRoot())
                .stream()
                .filter(assocItem -> assocItem.getQName().getLocalName().equals(typeName))
                .map(assocItem -> getDocumentType(assocItem.getChildRef()))
                .findAny();
        return typeByName.orElse(null);
    }

    @Override
    public void deleteDocumentType(DocumentType documentType) {
        if (!nodeService.getSourceAssocs(documentType.getNodeRef(), OpenESDHModel.TYPE_DOC_TYPE).isEmpty()) {
            throw new AlfrescoRuntimeException("Cannot delete document type. It is associated with existing documents");
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
