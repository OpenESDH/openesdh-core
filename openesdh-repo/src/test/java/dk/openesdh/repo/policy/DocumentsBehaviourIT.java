package dk.openesdh.repo.policy;

import static org.hamcrest.core.IsCollectionContaining.hasItem;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.hamcrest.MatcherAssert;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.base.Strings;
import com.tradeshift.test.remote.Remote;
import com.tradeshift.test.remote.RemoteTestRunner;

import dk.openesdh.repo.helper.CaseDocumentTestHelper;
import dk.openesdh.repo.helper.CaseHelper;
import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.cases.CaseService;

@RunWith(RemoteTestRunner.class)
@Remote(runnerClass = SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:alfresco/application-context.xml",
        "classpath:alfresco/extension/openesdh-test-context.xml" })
public class DocumentsBehaviourIT {

    private static final String TEST_ADD_DOCUMENT_NAME = "TestAddDocument";
    private static final String TEST_ADD_DOCUMENT_CONTENT = "This is a test add document content...";
    private static final String TEST_FOLDER_NAME = "DocumentServiceImpIT";
    private static final String TEST_CASE_NAME1 = "TestCase1";

    @Autowired
    @Qualifier("NodeService")
    protected NodeService nodeService;

    @Autowired
    @Qualifier("retryingTransactionHelper")
    protected RetryingTransactionHelper retryingTransactionHelper;

    @Autowired
    @Qualifier("CaseService")
    protected CaseService caseService;

    @Autowired
    @Qualifier("CaseDocumentTestHelper")
    protected CaseDocumentTestHelper docTestHelper;

    private NodeRef testFolder;
    private NodeRef testCase1;
    private NodeRef testCase1DocumentsFolder;
    private NodeRef testAddDocument;

    @Before
    public void setUp() throws Exception {

        // TODO: All of this could have been done only once
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        testFolder = docTestHelper.createFolder(TEST_FOLDER_NAME);
        testCase1 = docTestHelper.createCaseBehaviourOn(TEST_CASE_NAME1, testFolder, CaseHelper.DEFAULT_USERNAME);
        testCase1DocumentsFolder = caseService.getDocumentsFolder(testCase1);
    }

    @After
    public void tearDown() throws Exception {
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        List<NodeRef> folders = Arrays.asList(new NodeRef[] { testFolder });
        List<NodeRef> cases = Arrays.asList(new NodeRef[] { testCase1 });
        List<String> users = Arrays.asList(new String[] { CaseHelper.DEFAULT_USERNAME });
        docTestHelper.removeNodesAndDeleteUsersInTransaction(folders, cases, users);
    }

    @Test
    public void shouldAddDocumentToCase() {

        addDocumentToCase();

        Assert.assertNotNull("The node ref of the added document shouldn't be null", testAddDocument);

        String resultFileName = (String) nodeService.getProperty(testAddDocument, ContentModel.PROP_NAME);
        String resultExtension = FilenameUtils.getExtension(resultFileName);
        Assert.assertFalse("The added document extension should not be empty",
                Strings.isNullOrEmpty(resultExtension));
        Assert.assertEquals("Wrong extension of the added document", "txt", resultExtension);

        Map<String, NodeRef> caseDocsFolderContent = nodeService
                .getChildAssocs(testCase1DocumentsFolder)
                .stream()
                .collect(
                        Collectors.<ChildAssociationRef, String, NodeRef> toMap(assoc -> (String) nodeService
                                .getProperty(assoc.getChildRef(), ContentModel.PROP_NAME), assoc -> assoc
                                .getChildRef()));

        Assert.assertFalse(
                "The added document should be put to the document record folder NOT to the case documents folder",
                caseDocsFolderContent.values().contains(testAddDocument));

        MatcherAssert.assertThat("The case documents folder should contain the added document record folder",
                caseDocsFolderContent.keySet(), hasItem(TEST_ADD_DOCUMENT_NAME));

        NodeRef addedDocRecordFolder = caseDocsFolderContent.get(TEST_ADD_DOCUMENT_NAME);
        List<NodeRef> addedDocRecordFolderContent = nodeService.getChildAssocs(addedDocRecordFolder).stream()
                .map(docAssoc -> docAssoc.getChildRef()).collect(Collectors.toList());

        MatcherAssert.assertThat("The document record folder should contain the added document",
                addedDocRecordFolderContent, hasItem(testAddDocument));
    }

    private void addDocumentToCase() {
        final Map<QName, Serializable> properties = new HashMap<>();
        properties.put(ContentModel.PROP_NAME, TEST_ADD_DOCUMENT_NAME);
        properties.put(OpenESDHModel.PROP_DOC_TYPE, "letter");
        properties.put(OpenESDHModel.PROP_DOC_CATEGORY, "other");
        properties.put(OpenESDHModel.PROP_DOC_STATE, "received");
        ContentData content = ContentData.createContentProperty(TEST_ADD_DOCUMENT_CONTENT);
        content = ContentData.setMimetype(content, MimeTypes.PLAIN_TEXT);
        properties.put(ContentModel.TYPE_CONTENT, content);

        testAddDocument = AuthenticationUtil.runAsSystem(() -> retryingTransactionHelper
                .doInTransaction(() -> nodeService.createNode(testCase1DocumentsFolder,
                        ContentModel.ASSOC_CONTAINS,
                        QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, TEST_ADD_DOCUMENT_NAME),
                        ContentModel.TYPE_CONTENT, properties).getChildRef()));
    }
}
