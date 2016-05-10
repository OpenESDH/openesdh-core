package dk.openesdh.repo.services.documents;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.CollectionUtils;

import com.tradeshift.test.remote.Remote;
import com.tradeshift.test.remote.RemoteTestRunner;

import dk.openesdh.repo.helper.CaseDocumentTestHelper;
import dk.openesdh.repo.helper.CaseHelper;
import dk.openesdh.repo.services.cases.CaseService;
import dk.openesdh.repo.services.files.OeAuthorityFilesService;

@RunWith(RemoteTestRunner.class)
@Remote(runnerClass = SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:alfresco/application-context.xml",
        "classpath:alfresco/extension/openesdh-test-context.xml" })
public class CaseDocumentCopyServiceImplIT {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    @Qualifier("NodeService")
    private NodeService nodeService;

    @Autowired
    @Qualifier(CaseService.BEAN_ID)
    private CaseService caseService;

    @Autowired
    @Qualifier("CaseDocumentTestHelper")
    private CaseDocumentTestHelper docTestHelper;

    @Autowired
    @Qualifier("CaseDocumentCopyService")
    private CaseDocumentCopyService caseDocumentCopyService;
    @Autowired
    @Qualifier("PersonService")
    private PersonService personService;
    @Autowired
    @Qualifier("OeAuthorityFilesService")
    private OeAuthorityFilesService authorityFilesService;

    @Autowired
    @Qualifier(DocumentService.BEAN_ID)
    private DocumentService documentService;

    private static final String TEST_FOLDER_NAME = "DocumentServiceImpIT";
    private static final String TEST_CASE_NAME1 = "TestCase1";
    private static final String TEST_CASE_NAME2 = "TestCase2";
    private static final String TEST_DOCUMENT_NAME = "TestDocument";
    private static final String TEST_DOCUMENT_FILE_NAME = TEST_DOCUMENT_NAME + ".txt";

    private NodeRef testFolder;
    private NodeRef testCase1;
    private NodeRef testCase2;
    private NodeRef testDocument;
    private NodeRef testDocumentRecFolder;
    private NodeRef adminNodeRef;

    @Before
    public void setUp() throws Exception {
        // TODO: All of this could have been done only once
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        adminNodeRef = personService.getPerson(AuthenticationUtil.getAdminUserName());
        testFolder = docTestHelper.createFolder(TEST_FOLDER_NAME);
        testCase1 = docTestHelper.createCaseBehaviourOn(TEST_CASE_NAME1, testFolder, CaseHelper.DEFAULT_USERNAME);
        testCase2 = docTestHelper.createCaseBehaviourOn(TEST_CASE_NAME2, testFolder, CaseHelper.DEFAULT_USERNAME);

        testDocument = docTestHelper.createCaseDocument(TEST_DOCUMENT_FILE_NAME, testCase1);

        testDocumentRecFolder = nodeService.getPrimaryParent(testDocument).getParentRef();
    }

    @After
    public void tearDown() throws Exception {
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        List<NodeRef> folders = Arrays.asList(new NodeRef[] { testFolder, testDocument });
        List<NodeRef> cases = Arrays.asList(new NodeRef[] { testCase1, testCase2 });
        List<String> users = Arrays.asList(new String[] { CaseHelper.DEFAULT_USERNAME });
        try {
            docTestHelper.removeNodesAndDeleteUsersInTransaction(folders, cases, users);
        } catch (Exception ignored) {
        }
    }

    @Test
    public void shouldMoveDocumentFromCase1ToCase2() throws Exception {
        String testCase2Id = caseService.getCaseId(testCase2);
        caseDocumentCopyService.moveDocumentToCase(testDocumentRecFolder, testCase2Id);

        List<ChildAssociationRef> testCase1DocsList = documentService.getDocumentsForCase(testCase1);
        Assert.assertTrue("Test Case1 shouldn't contain any documents", CollectionUtils.isEmpty(testCase1DocsList));

        List<ChildAssociationRef> testCase2DocsList = documentService.getDocumentsForCase(testCase2);
        Assert.assertFalse("Test Case2 shouldn't be empty and should contain documents",
                CollectionUtils.isEmpty(testCase2DocsList));

        Assert.assertEquals("Test Case2 should contain 1 document", 1, testCase2DocsList.size());
        NodeRef docRef = testCase2DocsList.get(0).getChildRef();

        String documentName = docTestHelper.getNodePropertyString(docRef, ContentModel.PROP_NAME);
        Assert.assertEquals("Wrong test document name", TEST_DOCUMENT_NAME, documentName);
    }

    @Test
    public void shouldDeclineMovingDocumentToTheSameCase() throws Exception {
        String testCase1Id = caseService.getCaseId(testCase1);
        thrown.expect(Exception.class);
        thrown.expectMessage(DocumentService.DOCUMENT_STORED_IN_CASE_MESSAGE + testCase1Id);
        caseDocumentCopyService.moveDocumentToCase(testDocumentRecFolder, testCase1Id);
    }

    @Test
    public void shouldCopyDocumentFromCase1ToCase2() throws Exception {

        Assert.assertTrue("Test case 2 should be empty before copy operation",
                CollectionUtils.isEmpty(documentService.getDocumentsForCase(testCase2)));

        final String testCase2Id = caseService.getCaseId(testCase2);
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        caseDocumentCopyService.copyDocumentToCase(testDocumentRecFolder, testCase2Id);

        List<ChildAssociationRef> testCase1DocsList = documentService.getDocumentsForCase(testCase1);
        Assert.assertFalse("Test case1 shouldn't be empty and should still contain documents",
                CollectionUtils.isEmpty(testCase1DocsList));

        NodeRef docFromTetsCase1Ref = testCase1DocsList.get(0).getChildRef();
        String docFromTestCase1Name = docTestHelper.getNodePropertyString(docFromTetsCase1Ref,
                ContentModel.PROP_NAME);
        Assert.assertEquals("Wrong test document name from case 1", TEST_DOCUMENT_NAME, docFromTestCase1Name);

        List<ChildAssociationRef> testCase2DocsList = documentService.getDocumentsForCase(testCase2);
        Assert.assertFalse("Test Case2 shouldn't be empty and should contain documents",
                CollectionUtils.isEmpty(testCase2DocsList));

        Assert.assertEquals("Test Case2 should contain 1 document", 1, testCase2DocsList.size());
        NodeRef docRef = testCase2DocsList.get(0).getChildRef();

        String documentName = docTestHelper.getNodePropertyString(docRef, ContentModel.PROP_NAME);
        Assert.assertEquals("Wrong test document name from case 2", TEST_DOCUMENT_NAME, documentName);

    }

    @Test
    public void shouldDeclineCopyDocumentToTheSameCase() throws Exception {
        final String testCase1Id = caseService.getCaseId(testCase1);

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        thrown.expect(Exception.class);
        thrown.expectMessage(DocumentService.DOCUMENT_STORED_IN_CASE_MESSAGE + testCase1Id);
        caseDocumentCopyService.copyDocumentToCase(testDocumentRecFolder, testCase1Id);

    }

    @Test
    public void shouldDeclineCopyDocumentIfSameExistsInTargetCase() throws Exception {

        final String testCase2Id = caseService.getCaseId(testCase2);
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        caseDocumentCopyService.copyDocumentToCase(testDocumentRecFolder, testCase2Id);

        try {
            caseDocumentCopyService.copyDocumentToCase(testDocumentRecFolder, testCase2Id);
            Assert.fail(
                    "The copy document should fail here and throw an exception, since a doc with the same name exists.");
        } catch (Exception e) {
            Assert.assertEquals("Unexpected exception throw while copying document to the same case.",
                    DocumentService.DOCUMENT_STORED_IN_CASE_MESSAGE + testCase2Id, e.getMessage());
            Assert.assertTrue("The exception is thrown which is OK", true);
        }
    }

    @Test
    public void shouldDetachCaseDocumentAndMoveToOwnerFolder() {
        NodeRef docRecordRef = documentService.getDocRecordNodeRef(testDocument);
        NodeRef mainDocRef = testDocument;
        caseDocumentCopyService.detachCaseDocument(docRecordRef, adminNodeRef, StringUtils.EMPTY);
        Optional<NodeRef> caseDocumentRef = documentService.getDocumentsForCase(testCase1)
                .stream()
                .map(ChildAssociationRef::getChildRef)
                .filter(docRef -> docRef.equals(docRecordRef))
                .findAny();
        Assert.assertFalse("Detached document should be removed from case", caseDocumentRef.isPresent());

        NodeRef authorityFolder = authorityFilesService.getAuthorityFolder(AuthenticationUtil.getAdminUserName()).get();
        
        Optional<NodeRef> detachedDocRef = nodeService.getChildAssocs(authorityFolder, ContentModel.ASSOC_CONTAINS, null)
                .stream()
                .map(ChildAssociationRef::getChildRef)
                .filter(docRef -> docRef.equals(mainDocRef))
                .findAny();
        Assert.assertTrue("Detached document should be moved to owner folder", detachedDocRef.isPresent());

        Assert.assertEquals("Detached document type should be changed to CONTENT", ContentModel.TYPE_CONTENT,
                nodeService.getType(mainDocRef));
    }
}
