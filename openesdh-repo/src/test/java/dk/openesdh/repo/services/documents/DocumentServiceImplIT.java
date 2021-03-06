package dk.openesdh.repo.services.documents;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.QName;
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

import com.tradeshift.test.remote.Remote;
import com.tradeshift.test.remote.RemoteTestRunner;

import dk.openesdh.repo.helper.CaseDocumentTestHelper;
import dk.openesdh.repo.helper.CaseHelper;
import dk.openesdh.repo.model.CaseDocument;
import dk.openesdh.repo.model.CaseDocumentAttachment;
import dk.openesdh.repo.model.DocumentCategory;
import dk.openesdh.repo.model.DocumentStatus;
import dk.openesdh.repo.model.DocumentType;
import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.model.ResultSet;
import dk.openesdh.repo.services.cases.CaseService;
import dk.openesdh.repo.services.lock.OELockService;

@RunWith(RemoteTestRunner.class)
@Remote(runnerClass = SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:alfresco/application-context.xml", "classpath:alfresco/extension/openesdh-test-context.xml"})
public class DocumentServiceImplIT {

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
    @Qualifier(DocumentService.BEAN_ID)
    private DocumentService documentService;

    @Autowired
    @Qualifier("OELockService")
    private OELockService oeLockService;

    @Autowired
    @Qualifier("CheckOutCheckInService")
    private CheckOutCheckInService checkOutCheckInService;

    @Autowired
    @Qualifier("ContentService")
    private ContentService contentService;

    @Autowired
    @Qualifier("DocumentTypeService")
    private DocumentTypeServiceImpl documentTypeService;

    @Autowired
    @Qualifier("DocumentCategoryService")
    private DocumentCategoryServiceImpl documentCategoryService;

    @Autowired
    @Qualifier("VersionService")
    private VersionService versionService;

    private static final String TEST_TITLE = "TEST TITLE";
    private static final String TEST_FOLDER_NAME = "DocumentServiceImpIT";
    private static final String TEST_CASE_NAME1 = "TestCase1";
    private static final String TEST_CASE_NAME2 = "TestCase2";
    private static final String TEST_DOCUMENT_NAME = "TestDocument";
    private static final String TEST_DOCUMENT_FILE_NAME = TEST_DOCUMENT_NAME + ".txt";
    private static final String TEST_DOCUMENT_NAME2 = "TestDocument2";
    private static final String TEST_DOCUMENT_FILE_NAME2 = TEST_DOCUMENT_NAME2 + ".txt";
    private static final String TEST_DOCUMENT_FILE_NAME3 = TEST_DOCUMENT_NAME2 + ".txt";

    private static final String TEST_DOCUMENT_ATTACHMENT_NAME = "TestDocumentAttachment";
    private static final String TEST_DOCUMENT_ATTACHMENT_FILE_NAME = TEST_DOCUMENT_ATTACHMENT_NAME + ".txt";

    private static final String TEST_DOCUMENT_ATTACHMENT_NAME2 = "TestDocumentAttachment2";
    private static final String TEST_DOCUMENT_ATTACHMENT_FILE_NAME2 = TEST_DOCUMENT_ATTACHMENT_NAME2 + ".txt";

    private NodeRef testFolder;
    private NodeRef testCase1;
    private NodeRef testCase2;
    private NodeRef testDocument;
    private NodeRef testDocumentAttachment;
    private NodeRef testDocumentRecFolder;
    private NodeRef testDocument2;
    private NodeRef testDocumentRecFolder2;
    private NodeRef testDocument3;
    private NodeRef testDocumentRecFolder3;
    private NodeRef testDocumentAttachment3;

    @Before
    public void setUp() throws Exception {
        // TODO: All of this could have been done only once
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        testFolder = docTestHelper.createFolder(TEST_FOLDER_NAME);
        testCase1 = docTestHelper.createCaseBehaviourOn(TEST_CASE_NAME1, testFolder, CaseHelper.DEFAULT_USERNAME);
        testCase2 = docTestHelper.createCaseBehaviourOn(TEST_CASE_NAME2, testFolder, CaseHelper.DEFAULT_USERNAME);

        testDocument = docTestHelper.createCaseDocument(TEST_DOCUMENT_FILE_NAME, testCase1);

        testDocumentRecFolder = nodeService.getPrimaryParent(testDocument).getParentRef();
        testDocumentAttachment = docTestHelper.createCaseDocumentAttachment(TEST_DOCUMENT_ATTACHMENT_FILE_NAME,
                testDocumentRecFolder);
    }

    @After
    public void tearDown() throws Exception {
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        List<NodeRef> folders = Arrays.asList(new NodeRef[]{testFolder});
        List<NodeRef> cases = Arrays.asList(new NodeRef[]{testCase1, testCase2});
        List<String> users = Arrays.asList(new String[]{CaseHelper.DEFAULT_USERNAME});
        try {
            docTestHelper.removeNodesAndDeleteUsersInTransaction(folders, cases, users);
        } catch (Exception ignored) {
        }
    }

    @Test
    public void shouldCreateDocumentAttachmentVersionAndRetrieveSeveralVersions() {
        NodeRef workingCopy = checkOutCheckInService.checkout(testDocumentAttachment);
        ContentWriter writer = contentService.getWriter(workingCopy, ContentModel.PROP_CONTENT, true);
        writer.setMimetype("text");
        writer.putContent("some new content");
        checkOutCheckInService.checkin(workingCopy, null);

        ResultSet<CaseDocumentAttachment> attachments = documentService.getDocumentVersionAttachments(testDocument, 0, 1000);
        Assert.assertEquals("Wrong number of document attachments retrieved.", 1, attachments.getTotalItems());
        Assert.assertEquals("Wrong attachment current version.", "1.1", attachments.getResultList().get(0)
                .getVersionLabel());
        Assert.assertEquals("Wrong attachment previous version.", "1.0", attachments.getResultList().get(0)
                .getVersions().get(0).getVersionLabel());
    }

    @Test
    public void shouldCreateAndRetrieveDocumentsWithAttachments() {

        testDocument2 = docTestHelper.createCaseDocument(TEST_DOCUMENT_FILE_NAME2, testCase1);
        testDocumentRecFolder2 = nodeService.getPrimaryParent(testDocument2).getParentRef();
        docTestHelper.createCaseDocumentAttachment(TEST_DOCUMENT_ATTACHMENT_FILE_NAME2,
                testDocumentRecFolder2);

        String caseId = caseService.getCaseId(testCase1);
        List<CaseDocument> caseDocuments = documentService.getCaseDocumentsWithAttachments(caseId);
        Assert.assertEquals("Wrong number of case documents retrieved", 2, caseDocuments.size());
        Assert.assertEquals("Wrong number of the first document attachments retrieved", 1, caseDocuments.get(0)
                .getAttachments().size());
        Assert.assertEquals("Wrong number of the second document attachments retrieved", 1, caseDocuments.get(1)
                .getAttachments().size());
    }

    @Test
    public void shouldCreateDocumentVersionWithAttachmentCorrespondence() {
        NodeRef workingCopy = checkOutCheckInService.checkout(testDocument);
        ContentWriter writer = contentService.getWriter(workingCopy, ContentModel.PROP_CONTENT, true);
        writer.setMimetype("text");
        writer.putContent("some new content");
        checkOutCheckInService.checkin(workingCopy, null);

        ResultSet<CaseDocumentAttachment> currentVersionAttachments = documentService
                .getDocumentVersionAttachments(testDocument, 0, 1000);
        Assert.assertEquals("New version of the case document shouldn't contain attachments.", 0,
                currentVersionAttachments.getResultList().size());

        NodeRef docVersionRef = versionService.getVersionHistory(testDocument).getVersion("1.0")
                .getFrozenStateNodeRef();
        List<CaseDocumentAttachment> previousVersionAttachments = documentService.getDocumentVersionAttachments(
                docVersionRef, 0, 1000).getResultList();
        Assert.assertEquals("Previous version of the case document should contain attachments.", 1,
                previousVersionAttachments.size());
        Assert.assertEquals("Wrong attachment found for the previous version of the document",
                testDocumentAttachment.toString(), previousVersionAttachments.get(0).getNodeRef());

    }

    @Test
    public void finalizeUnfinalizeDocument() throws Exception {
        testDocument3 = docTestHelper.createCaseDocument(TEST_DOCUMENT_FILE_NAME3, testCase1);
        testDocumentRecFolder3 = nodeService.getPrimaryParent(testDocument3).getParentRef();
        testDocumentAttachment3 = docTestHelper.createCaseDocumentAttachment(TEST_DOCUMENT_ATTACHMENT_FILE_NAME2, testDocumentRecFolder3);
        Assert.assertEquals("Document initially has DRAFT status", DocumentStatus.DRAFT, documentService.getNodeStatus(testDocumentRecFolder3));
        documentService.changeNodeStatus(testDocumentRecFolder3, DocumentStatus.FINAL);
        Assert.assertEquals("Finalized document has FINAL status", DocumentStatus.FINAL, documentService.getNodeStatus(testDocumentRecFolder3));

        Assert.assertTrue("Document record is locked after being finalized.", oeLockService.isLocked(testDocumentRecFolder3));
        Assert.assertTrue("Document file is locked after being finalized", oeLockService.isLocked(testDocument3));
        Assert.assertTrue("Document attachment is locked after being finalized", oeLockService.isLocked(testDocumentAttachment3));

        try {
            // Try to update the finalized document
            NodeRef workingCopy = checkOutCheckInService.checkout(testDocument3);
            ContentWriter writer = contentService.getWriter(workingCopy, ContentModel.PROP_CONTENT, true);
            writer.setMimetype("text");
            writer.putContent("some new content");
            checkOutCheckInService.checkin(workingCopy, null);
            Assert.fail("Expected to get an exception thrown when trying to add a new version to a finalized document");
        } catch (Exception ignored) {
        }

        documentService.changeNodeStatus(testDocumentRecFolder3, DocumentStatus.DRAFT);
        Assert.assertEquals("Unfinalized document has DRAFT status", DocumentStatus.DRAFT, documentService.getNodeStatus(testDocumentRecFolder3));

        Assert.assertFalse("Document record is locked after being finalized but should be unlocked.", oeLockService.isLocked(testDocumentRecFolder3));
        Assert.assertFalse("Document file is locked after being finalized but should be unlocked.", oeLockService.isLocked(testDocument3));
        Assert.assertFalse("Document attachment is locked after being finalized but should be unlocked", oeLockService.isLocked(testDocumentAttachment3));
    }

    //TODO: Add this test back after demo: see OPENE-278
//    @Test
//    public void finalizeNonAcceptableFormatDocument() throws Exception {
//        // Make sure that you get an exception when trying to finalize a
//        // document which is not an allowed finalizable type
//        // (e.g. application/json, etc..)
//        testDocument4 = docTestHelper.createCaseDocument(TEST_DOCUMENT_FILE_NAME4, testCase1);
//        testDocumentRecFolder4 = nodeService.getPrimaryParent(testDocument4).getParentRef();
//
//        // Try to update the finalized document
//        NodeRef workingCopy = checkOutCheckInService.checkout(testDocument4);
//        ContentWriter writer = contentService.getWriter(workingCopy, ContentModel.PROP_CONTENT, true);
//        writer.setMimetype(MimetypeMap.MIMETYPE_JSON);
//        writer.putContent("{'thisShouldNotBeAbleToBeFinalized': 1}");
//        checkOutCheckInService.checkin(workingCopy, null);
//
//        expectedException.expect(AutomaticFinalizeFailureException.class);
//        documentService.changeNodeStatus(testDocumentRecFolder4, DocumentStatus.FINAL);
//    }
    @Test
    public void shouldUpdateCaseDocumentProperties() {
        NodeRef caseDocNodeRef = testDocumentRecFolder;
        CaseDocument document = new CaseDocument();
        document.setNodeRef(caseDocNodeRef);
        document.setTitle(TEST_TITLE);

        DocumentType documentType2 = getSecondItemFrom(documentTypeService.getClassifValues());
        document.setType(documentType2);

        DocumentCategory documentCategory2 = getSecondItemFrom(documentCategoryService.getClassifValues());
        document.setCategory(documentCategory2);

        documentService.updateCaseDocumentProperties(document);

        Map<QName, Serializable> props = nodeService.getProperties(caseDocNodeRef);
        Assert.assertEquals("Document category should be updated",
                documentCategory2.getNodeRef(),
                documentService.getDocumentCategory(caseDocNodeRef).getNodeRef());
        Assert.assertEquals("Document type should be updated",
                documentType2.getNodeRef(),
                documentService.getDocumentType(caseDocNodeRef).getNodeRef());
        Assert.assertEquals("Document title should be updated",
                TEST_TITLE,
                props.get(ContentModel.PROP_TITLE));
    }

    private <T> T getSecondItemFrom(List<T> collection) {
        return collection.stream().skip(1).findFirst().get();
    }

    @Test
    public void shouldCreateCaseDocumentInsideOfOwnFolder() {
        int size = documentService.getDocumentsForCase(testCase2).size();
        NodeRef createdDocFolderNode = creeateCaseTestDocument(testCase2);
        Assert.assertTrue(nodeService.getType(createdDocFolderNode).isMatch(OpenESDHModel.TYPE_DOC_SIMPLE));
        Assert.assertEquals("Size of documents increased by 1", size + 1, documentService.getDocumentsForCase(testCase2).size());
    }

    @Test
    public void shouldCreateMoreThan2DocumentsWithSameName() {
        int repeats = 3;
        int size = documentService.getDocumentsForCase(testCase2).size();
        for (int i = 0; i < repeats; i++) {
            creeateCaseTestDocument(testCase2);
        }
        Assert.assertEquals("Size of documents increased by 2", size + repeats, documentService.getDocumentsForCase(testCase2).size());
    }

    private NodeRef creeateCaseTestDocument(NodeRef caseNodeRef) {
        return documentService.createCaseDocument(caseNodeRef,
                TEST_DOCUMENT_NAME, TEST_DOCUMENT_FILE_NAME,
                documentTypeService.getClassifValueByName(OpenESDHModel.DOCUMENT_TYPE_LETTER).get().getNodeRef(),
                documentCategoryService.getClassifValueByName(OpenESDHModel.DOCUMENT_CATEGORY_OTHER).get().getNodeRef(),
                writer -> {
                    writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
                    writer.putContent("Some content");
                });
    }
}
