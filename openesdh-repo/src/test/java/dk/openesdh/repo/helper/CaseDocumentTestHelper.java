package dk.openesdh.repo.helper;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import dk.openesdh.repo.model.DocumentCategory;
import dk.openesdh.repo.model.DocumentType;
import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.TransactionRunner;
import dk.openesdh.repo.services.cases.CaseService;
import dk.openesdh.repo.services.documents.DocumentCategoryService;
import dk.openesdh.repo.services.documents.DocumentService;
import dk.openesdh.repo.services.documents.DocumentTypeService;
import dk.openesdh.simplecase.model.SimpleCaseModel;

@Service("CaseDocumentTestHelper")
public class CaseDocumentTestHelper {

    @Autowired
    private TransactionRunner transactionRunner;
    @Autowired
    @Qualifier("NodeService")
    private NodeService nodeService;
    @Autowired
    @Qualifier("repositoryHelper")
    private Repository repositoryHelper;
    @Autowired
    @Qualifier("TestCaseHelper")
    private CaseHelper caseHelper;
    @Autowired
    @Qualifier(CaseService.BEAN_ID)
    private CaseService caseService;
    @Autowired
    @Qualifier(DocumentService.BEAN_ID)
    private DocumentService documentService;
    @Autowired
    @Qualifier("DocumentTypeService")
    private DocumentTypeService documentTypeService;
    @Autowired
    @Qualifier("DocumentCategoryService")
    private DocumentCategoryService documentCategoryService;

    // Create temporary node for use during testing
    public NodeRef createFolder(final String folderName) {
        NodeRef folder = nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS,
                folderName);
        if (folder != null) {
            return folder;
        }

        final Map<QName, Serializable> properties = new HashMap<>();
        properties.put(ContentModel.PROP_NAME, folderName);

        folder = transactionRunner.runAsAdmin(() -> {
            return nodeService.createNode(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS,
                    QName.createQName(OpenESDHModel.CASE_URI, folderName), ContentModel.TYPE_FOLDER, properties)
                    .getChildRef();
        });

        return folder;
    }

    /**
     * Creates a simple case with behavior turned off
     */
    public NodeRef createCaseBehaviourOff(final String caseName, NodeRef parentNodeRef, String userName) {
        return createCase(caseName, parentNodeRef, userName, true);
    }

    /**
     * Creates a simple case with behavior turned on
     */
    public NodeRef createCaseBehaviourOn(final String caseName, NodeRef parentNodeRef, String userName) {
        return createCase(caseName, parentNodeRef, userName, false);
    }

    private NodeRef createCase(final String caseName, NodeRef parentNodeRef, String userName,
            boolean disableBehaviour) {
        NodeRef caseFolder = nodeService.getChildByName(repositoryHelper.getCompanyHome(),
                ContentModel.ASSOC_CONTAINS, caseName);
        if (caseFolder != null) {
            return caseFolder;
        }

        LinkedList<NodeRef> owners = new LinkedList<>();
        owners.add(caseHelper.getDummyUser(userName));

        final Map<QName, Serializable> properties = new HashMap<>();
        properties.put(ContentModel.PROP_NAME, getNodePropertyString(parentNodeRef, ContentModel.PROP_NAME));
        caseFolder = caseHelper.createCase(parentNodeRef, caseName, SimpleCaseModel.TYPE_CASE_SIMPLE,
                properties, owners, disableBehaviour);

        return caseFolder;
    }

    public void removeNodesAndDeleteUsersInTransaction(final List<NodeRef> nodes, final List<NodeRef> cases,
            final List<String> userNames) {
        transactionRunner.runInTransaction(() -> {
            return transactionRunner.runAsSystem(() -> {
                cases.stream()
                        .filter(Objects::nonNull)
                        .forEach(this::removeCase);
                nodes.stream()
                        .filter(Objects::nonNull)
                        .filter(nodeService::exists)
                        .forEach(nodeService::deleteNode);
                userNames.stream()
                        .filter(Objects::nonNull)
                        .forEach(caseHelper::deleteDummyUser);
                return true;
            });
        });
    }

    private void removeCase(NodeRef aCase) {
        NodeRef caseDocumentsFolder = caseService.getDocumentsFolder(aCase);
        List<ChildAssociationRef> caseDocumentsList = documentService.getDocumentsForCase(aCase);
        caseDocumentsList.forEach((caseDocumentAssoc) -> {
            removeDocument(caseDocumentAssoc.getChildRef());
        });
        nodeService.deleteNode(caseDocumentsFolder);
        nodeService.deleteNode(aCase);
    }

    private void removeDocument(NodeRef documentFolderRef) {
        Set<QName> types = new HashSet<>();
        types.add(ContentModel.TYPE_CONTENT);
        List<ChildAssociationRef> docFilesList = nodeService.getChildAssocs(documentFolderRef, types);
        for (ChildAssociationRef docFile : docFilesList) {
            nodeService.deleteNode(docFile.getChildRef());
        }
        nodeService.deleteNode(documentFolderRef);
    }

    public DocumentType getFirstDocumentType() {
        return documentTypeService.getClassifValues().stream().findFirst().get();
    }

    public DocumentCategory getFirstDocumentCategory() {
        return documentCategoryService.getClassifValues().stream().findFirst().get();
    }

    public NodeRef createCaseDocument(final String documentName, final NodeRef caseNodeRef) {
        NodeRef caseDocumentsFolder = caseService.getDocumentsFolder(caseNodeRef);
        return createCaseDocumentInFolder(documentName, caseDocumentsFolder);
    }

    public NodeRef createCaseDocumentInFolder(final String documentName, final NodeRef targetFolderRef) {
        final Map<QName, Serializable> properties = new HashMap<>();
        properties.put(ContentModel.PROP_NAME, documentName);
        //this will be transfered to folder in DocumentBehavior
        properties.put(OpenESDHModel.PROP_DOC_TYPE, getFirstDocumentType().getNodeRef().toString());
        properties.put(OpenESDHModel.PROP_DOC_CATEGORY, getFirstDocumentCategory().getNodeRef().toString());

        return transactionRunner.runInTransactionAsAdmin(() -> {
            return nodeService.createNode(targetFolderRef, ContentModel.ASSOC_CONTAINS,
                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, documentName),
                    ContentModel.TYPE_CONTENT, properties).getChildRef();
        });
    }

    public NodeRef createCaseDocFolder(String folderName, NodeRef targetFolderRef) {
        final Map<QName, Serializable> properties = new HashMap<>();
        properties.put(ContentModel.PROP_NAME, folderName);
        properties.put(ContentModel.PROP_TITLE, folderName);
        return transactionRunner.runInTransactionAsAdmin(() -> {
            NodeRef nodeRef = nodeService.createNode(targetFolderRef, ContentModel.ASSOC_CONTAINS,
                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, folderName), ContentModel.TYPE_FOLDER,
                    properties).getChildRef();
            nodeService.addAspect(nodeRef, OpenESDHModel.ASPECT_DOCUMENT_CONTAINER, null);
            return nodeRef;
        });
    }

    public NodeRef createCaseDocumentAttachment(String documentName, final NodeRef caseDocumentNodeRef) {
        final Map<QName, Serializable> properties = new HashMap<>();
        properties.put(ContentModel.PROP_NAME, documentName);
        return transactionRunner.runInTransactionAsAdmin(() -> {
            return nodeService.createNode(caseDocumentNodeRef, ContentModel.ASSOC_CONTAINS,
                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, documentName),
                    ContentModel.TYPE_CONTENT, properties).getChildRef();
        });
    }

    public NodeRef createDocument(String documentName, NodeRef targetFolderNodeRef) {
        NodeRef docNodeRef = nodeService.getChildByName(targetFolderNodeRef, ContentModel.ASSOC_CONTAINS,
                documentName);
        if (docNodeRef != null) {
            return docNodeRef;
        }
        final Map<QName, Serializable> properties = new HashMap<>();
        properties.put(ContentModel.PROP_NAME, documentName);
        return transactionRunner.runInTransactionAsAdmin(() -> {
            return nodeService.createNode(targetFolderNodeRef, ContentModel.ASSOC_CONTAINS,
                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, documentName),
                    ContentModel.TYPE_CONTENT, properties).getChildRef();
        });
    }

    public String getNodePropertyString(NodeRef node, QName prop) {
        return (String) nodeService.getProperty(node, prop);
    }

}
