package dk.openesdh.repo.helper;

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
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;

import dk.openesdh.SimpleCaseModel;
import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.cases.CaseService;
import dk.openesdh.repo.services.documents.DocumentService;

public class CaseDocumentTestHelper {

    protected NodeService nodeService;

    protected Repository repositoryHelper;

    protected CaseHelper caseHelper;

    protected TransactionService transactionService;

    protected CaseService caseService;

    protected DocumentService documentService;

    protected RetryingTransactionHelper retryingTransactionHelper;

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

    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    public void setRetryingTransactionHelper(RetryingTransactionHelper retryingTransactionHelper) {
        this.retryingTransactionHelper = retryingTransactionHelper;
    }

    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
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
        folder = AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<NodeRef>() {
            @Override
            public NodeRef doWork() throws Exception {
                return nodeService
                        .createNode(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS,
                                QName.createQName(OpenESDHModel.CASE_URI, folderName), ContentModel.TYPE_FOLDER,
                                properties).getChildRef();
            }
        }, AuthenticationUtil.getAdminUserName());

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
        if(caseFolder != null){
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

        transactionService.getRetryingTransactionHelper().doInTransaction(
                new RetryingTransactionHelper.RetryingTransactionCallback<Boolean>() {
                    public Boolean execute() throws Throwable {
                        // Remove temporary node, and all its content,
                        // also removes test cases

                        for (NodeRef aCase : cases) {
                            removeCase(aCase);
                        }

                        for (NodeRef node : nodes) {
                            if (node != null && nodeService.exists(node)) {
                                nodeService.deleteNode(node);
                            }
                        }

                        for (String userName : userNames) {
                            caseHelper.deleteDummyUser(userName);
                        }


                        return true;
                    }
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

    public NodeRef createCaseDocument(final String documentName, final NodeRef caseNodeRef) {
        
        final Map<QName, Serializable> properties = new HashMap<>();
        properties.put(ContentModel.PROP_NAME, documentName);
        properties.put(OpenESDHModel.PROP_DOC_TYPE, "letter");
        properties.put(OpenESDHModel.PROP_DOC_CATEGORY, "other");
        properties.put(OpenESDHModel.PROP_DOC_STATE, "received");
        
        return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<NodeRef>() {
            @Override
            public NodeRef doWork() throws Exception {
                return retryingTransactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>() {
                    @Override
                    public NodeRef execute() throws Throwable {
                        final NodeRef caseDocumentsFolder = caseService.getDocumentsFolder(caseNodeRef);
                        return nodeService.createNode(caseDocumentsFolder, ContentModel.ASSOC_CONTAINS,
                                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, documentName),
                                ContentModel.TYPE_CONTENT, properties).getChildRef();
                    }
                });
            }
        }, AuthenticationUtil.getAdminUserName());
    }

    public String getNodePropertyString(NodeRef node, QName prop) {
        return (String) nodeService.getProperty(node, prop);
    }
}
