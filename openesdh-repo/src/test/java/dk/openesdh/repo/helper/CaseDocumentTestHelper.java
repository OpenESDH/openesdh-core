package dk.openesdh.repo.helper;

import dk.openesdh.SimpleCaseModel;
import dk.openesdh.repo.model.DocumentCategory;
import dk.openesdh.repo.model.DocumentType;
import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.cases.CaseService;
import dk.openesdh.repo.services.documents.DocumentCategoryService;
import dk.openesdh.repo.services.documents.DocumentService;
import dk.openesdh.repo.services.documents.DocumentTypeService;
import dk.openesdh.repo.test.TransactionalIT;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

public class CaseDocumentTestHelper extends TransactionalIT {

    protected NodeService nodeService;

    protected Repository repositoryHelper;

    protected CaseHelper caseHelper;

    protected CaseService caseService;

    protected DocumentService documentService;

    protected DocumentTypeService documentTypeService;

    protected DocumentCategoryService documentCategoryService;

    public void setCaseService(CaseService caseService) {
        this.caseService = caseService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setRepositoryHelper(Repository repositoryHelper) {
        this.repositoryHelper = repositoryHelper;
    }

    public void setCaseHelper(CaseHelper caseHelper) {
        this.caseHelper = caseHelper;
    }

    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }

    public void setDocumentTypeService(DocumentTypeService documentTypeService) {
        this.documentTypeService = documentTypeService;
    }

    public void setDocumentCategoryService(DocumentCategoryService documentCategoryService) {
        this.documentCategoryService = documentCategoryService;
    }

    // Create temporary node for use during testing
    public NodeRef createFolder(final String folderName) {
        NodeRef folder = nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS,
                folderName);
        if (folder != null) {
            return folder;
        }

        final Map<QName, Serializable> properties = new HashMap<>();
        properties.put(ContentModel.PROP_NAME, folderName);

        folder = runAsAdmin(() -> {
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

    protected NodeRef createCase(final String caseName, NodeRef parentNodeRef, String userName,
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
        caseFolder = caseHelper.createCase(AuthenticationUtil.getAdminUserName(), parentNodeRef,
                caseName, SimpleCaseModel.TYPE_CASE_SIMPLE, properties, owners, disableBehaviour);

        return caseFolder;
    }

    public void removeNodesAndDeleteUsersInTransaction(final List<NodeRef> nodes, final List<NodeRef> cases,
            final List<String> userNames) {
        runInTransactionAsAdmin(() -> {
            cases.stream()
                    .forEach((aCase) -> removeCase(aCase));
            nodes.stream()
                    .filter(node -> node != null && nodeService.exists(node))
                    .forEach(node -> nodeService.deleteNode(node));
            userNames.stream()
                    .forEach((userName) -> caseHelper.deleteDummyUser(userName));
            return true;
        });
    }

    private void removeCase(NodeRef aCase) {
        NodeRef caseDocumentsFolder = caseService.getDocumentsFolder(aCase);
        List<ChildAssociationRef> caseDocumentsList = documentService.getDocumentsForCase(aCase);
        for (ChildAssociationRef caseDocumentAssoc : caseDocumentsList) {
            removeDocument(caseDocumentAssoc.getChildRef());
        }
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

    private DocumentType getFirstDocumentType() {
        return documentTypeService.getDocumentTypes().stream().findFirst().get();
    }

    private DocumentCategory getFirstDocumentCategory() {
        return documentCategoryService.getDocumentCategories().stream().findFirst().get();
    }

    public NodeRef createCaseDocument(final String documentName, final NodeRef caseNodeRef) {
        final Map<QName, Serializable> properties = new HashMap<>();
        properties.put(ContentModel.PROP_NAME, documentName);
        //this will be transfered to folder in DocumentBehavior
        properties.put(OpenESDHModel.PROP_DOC_TYPE, getFirstDocumentType().getNodeRef().toString());
        properties.put(OpenESDHModel.PROP_DOC_CATEGORY, getFirstDocumentCategory().getNodeRef().toString());

        return runInTransactionAsAdmin(() -> {
            NodeRef caseDocumentsFolder = caseService.getDocumentsFolder(caseNodeRef);
            return nodeService.createNode(caseDocumentsFolder, ContentModel.ASSOC_CONTAINS,
                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, documentName),
                    ContentModel.TYPE_CONTENT, properties).getChildRef();
        });
    }

    public NodeRef createCaseDocumentAttachment(String documentName, final NodeRef caseDocumentNodeRef) {
        final Map<QName, Serializable> properties = new HashMap<>();
        properties.put(ContentModel.PROP_NAME, documentName);
        return runInTransactionAsAdmin(() -> {
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
        return runInTransactionAsAdmin(() -> {
            return nodeService.createNode(targetFolderNodeRef, ContentModel.ASSOC_CONTAINS,
                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, documentName),
                    ContentModel.TYPE_CONTENT, properties).getChildRef();
        });
    }

    public String getNodePropertyString(NodeRef node, QName prop) {
        return (String) nodeService.getProperty(node, prop);
    }

}
