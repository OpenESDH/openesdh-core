package dk.openesdh.repo.policy;

import com.google.common.base.Strings;
import com.tradeshift.test.remote.Remote;
import com.tradeshift.test.remote.RemoteTestRunner;
import dk.openesdh.repo.helper.CaseDocumentTestHelper;
import dk.openesdh.repo.helper.CaseHelper;
import dk.openesdh.repo.model.DocumentCategory;
import dk.openesdh.repo.model.DocumentType;
import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.cases.CaseService;
import dk.openesdh.repo.services.documents.DocumentCategoryService;
import dk.openesdh.repo.services.documents.DocumentService;
import dk.openesdh.repo.services.documents.DocumentTypeService;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.mime.MimeTypes;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(RemoteTestRunner.class)
@Remote(runnerClass = SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:alfresco/application-context.xml",
        "classpath:alfresco/extension/openesdh-test-context.xml" })
public class DocumentBehaviourIT {

    private static final String TEST_ADD_DOCUMENT_NAME = "TestAddDocument";
    private static final String TEST_ADD_DOCUMENT_CONTENT = "This is a test add document content...";
    private static final String TEST_FOLDER_NAME = "DocumentServiceImpIT";
    private static final String TEST_CASE_NAME1 = "TestCase1";
    private static final String TEST_CATEGORY = "other";
    private static final String TEST_STATE = "received";

    @Autowired
    @Qualifier("NodeService")
    private NodeService nodeService;

    @Autowired
    @Qualifier("retryingTransactionHelper")
    private RetryingTransactionHelper retryingTransactionHelper;

    @Autowired
    @Qualifier("CaseService")
    private CaseService caseService;

    @Autowired
    @Qualifier("CaseDocumentTestHelper")
    private CaseDocumentTestHelper docTestHelper;

    @Autowired
    @Qualifier("DocumentService")
    private DocumentService documentService;

    @Autowired
    @Qualifier("DocumentTypeService")
    private DocumentTypeService documentTypeService;

    @Autowired
    @Qualifier("DocumentCategoryService")
    private DocumentCategoryService documentCategoryService;

    private NodeRef testFolder;
    private NodeRef testCase1;
    private NodeRef testCase1DocumentsFolder;
    private NodeRef testAddDocument;
    private DocumentType documentType;
    private DocumentCategory documentCategory;

    @Before
    public void setUp() throws Exception {
        // TODO: All of this could have been done only once
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        testFolder = docTestHelper.createFolder(TEST_FOLDER_NAME);
        testCase1 = docTestHelper.createCaseBehaviourOn(TEST_CASE_NAME1, testFolder, CaseHelper.DEFAULT_USERNAME);
        testCase1DocumentsFolder = caseService.getDocumentsFolder(testCase1);
        documentType = documentTypeService.getDocumentTypes().stream().findAny().get();
        documentCategory = documentCategoryService.getDocumentCategories().stream().findAny().get();
    }

    @After
    public void tearDown() throws Exception {
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        List<NodeRef> folders = Arrays.asList(testFolder);
        List<NodeRef> cases = Arrays.asList(testCase1);
        List<String> users = Arrays.asList(CaseHelper.DEFAULT_USERNAME);
        docTestHelper.removeNodesAndDeleteUsersInTransaction(folders, cases, users);
    }

    @Test
    public void shouldAddDocumentToCase() {
        addDocumentToCase();
        Assert.assertNotNull(
                "The node ref of the added document shouldn't be null",
                testAddDocument);

        String resultFileName = (String) nodeService.getProperty(testAddDocument, ContentModel.PROP_NAME);
        String resultExtension = FilenameUtils.getExtension(resultFileName);
        Assert.assertTrue(
                "The document should keep its original extension",
                Strings.isNullOrEmpty(resultExtension));

        Map<String, NodeRef> caseDocsFolderContent = nodeService.getChildAssocs(testCase1DocumentsFolder).stream().collect(
                        Collectors.<ChildAssociationRef, String, NodeRef> toMap(
                                assoc -> (String) nodeService.getProperty(assoc.getChildRef(),
                                        ContentModel.PROP_NAME), assoc -> assoc.getChildRef()
                        ) );

        Assert.assertFalse(
                "The added document should be put to the document record folder NOT to the case documents folder",
                caseDocsFolderContent.values().contains(testAddDocument));

        Assert.assertTrue(
                "The case documents folder should contain the added document record folder",
                hasKey(caseDocsFolderContent.keySet(), TEST_ADD_DOCUMENT_NAME));

        NodeRef addedDocRecordFolder = caseDocsFolderContent.get(TEST_ADD_DOCUMENT_NAME);
        Assert.assertEquals("The document record should get a title equal to its name", TEST_ADD_DOCUMENT_NAME, nodeService.getProperty(addedDocRecordFolder, ContentModel.PROP_TITLE));

        List<NodeRef> addedDocRecordFolderContent = nodeService.getChildAssocs(addedDocRecordFolder).stream()
                .map(ChildAssociationRef::getChildRef).collect(Collectors.toList());

        Assert.assertTrue(
                "The document record folder should contain the added document",
                addedDocRecordFolderContent.contains(testAddDocument));

        Assert.assertEquals("The document record folder should contain the added document state",
                TEST_STATE,
                nodeService.getProperty(addedDocRecordFolder, OpenESDHModel.PROP_DOC_STATE));
        Assert.assertEquals("The document record folder should contain the added document category",
                documentCategory.getNodeRef(),
                documentService.getDocumentCategory(addedDocRecordFolder).getNodeRef());
        Assert.assertEquals("The document record folder should be the added document type",
                documentType.getNodeRef(),
                documentService.getDocumentType(addedDocRecordFolder).getNodeRef());

    }

    private void addDocumentToCase() {
        final Map<QName, Serializable> properties = new HashMap<>();
        properties.put(ContentModel.PROP_NAME, TEST_ADD_DOCUMENT_NAME);
        properties.put(OpenESDHModel.PROP_DOC_TYPE, documentType.getNodeRef());
        properties.put(OpenESDHModel.PROP_DOC_CATEGORY, documentCategory.getNodeRef());
        properties.put(OpenESDHModel.PROP_DOC_STATE, TEST_STATE);
        ContentData content = ContentData.createContentProperty(TEST_ADD_DOCUMENT_CONTENT);
        content = ContentData.setMimetype(content, MimeTypes.PLAIN_TEXT);
        properties.put(ContentModel.TYPE_CONTENT, content);

        testAddDocument = AuthenticationUtil.runAsSystem(() -> retryingTransactionHelper
                .doInTransaction(() -> nodeService.createNode(testCase1DocumentsFolder,
                        ContentModel.ASSOC_CONTAINS,
                        QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, TEST_ADD_DOCUMENT_NAME),
                        ContentModel.TYPE_CONTENT, properties).getChildRef()));
    }

    public boolean hasKey(Set<String> keySet, String interest){
        return keySet.contains(interest);
    }
}