package dk.openesdh.repo.services.files;

import static org.alfresco.repo.security.authentication.AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser;
import static org.alfresco.repo.security.authentication.AuthenticationUtil.setFullyAuthenticatedUser;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.forum.CommentService;
import org.alfresco.repo.security.authority.script.ScriptAuthorityService;
import org.alfresco.repo.security.authority.script.ScriptGroup;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PermissionService;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.tradeshift.test.remote.Remote;
import com.tradeshift.test.remote.RemoteTestRunner;

import dk.openesdh.repo.helper.CaseDocumentTestHelper;
import dk.openesdh.repo.helper.CaseHelper;
import dk.openesdh.repo.model.CaseDocument;
import dk.openesdh.repo.services.TransactionRunner;
import dk.openesdh.repo.services.cases.CaseService;
import dk.openesdh.repo.services.documents.DocumentService;
import dk.openesdh.repo.services.system.OpenESDHFoldersService;

@RunWith(RemoteTestRunner.class)
@Remote(runnerClass = SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:alfresco/application-context.xml", "classpath:alfresco/extension/openesdh-test-context.xml"})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class OeAuthorityFilesServiceImplIT {

    @Autowired
    @Qualifier("OeAuthorityFilesService")
    private OeAuthorityFilesServiceImpl authorityFilesService;
    @Autowired
    @Qualifier("OeFilesService")
    private OeFilesService filesService;
    @Autowired
    @Qualifier("TestCaseHelper")
    private CaseHelper caseHelper;
    @Autowired
    @Qualifier("authorityServiceScript")
    private ScriptAuthorityService scriptAuthorityService;
    @Autowired
    @Qualifier("AuthorityService")
    private AuthorityService authorityService;
    @Autowired
    @Qualifier("NodeService")
    private NodeService nodeService;
    @Autowired
    private OpenESDHFoldersService openESDHFoldersService;
    @Autowired
    @Qualifier("CaseDocumentTestHelper")
    private CaseDocumentTestHelper caseDocTestHelper;
    @Autowired
    @Qualifier(CaseService.BEAN_ID)
    private CaseService caseService;
    @Autowired
    @Qualifier(DocumentService.BEAN_ID)
    private DocumentService documentService;
    @Autowired
    @Qualifier("TransactionRunner")
    private TransactionRunner tr;
    @Autowired
    @Qualifier("CommentService")
    private CommentService commentService;
    @Autowired
    @Qualifier("ContentService")
    private ContentService contentService;

    private static final String USER_OWNER1 = "fileTestOwner1";
    private static final String USER_OWNER2 = "fileTestOwner2";
    private static final String GROUP1_WITH_OWNER1 = "fileTestGroup1";
    private static final String TEST_FILES_CASE = "Test files case";
    private final InputStream fileBytes = new ByteArrayInputStream("Test file content".getBytes());
    private NodeRef file1;
    private NodeRef owner1;
    private NodeRef owner2;
    private String testComment;
    private String testFileTitle;

    @Before
    public void setUp() {
        setAdminUserAsFullyAuthenticatedUser();
        owner1 = caseHelper.createDummyUser(USER_OWNER1);
        owner2 = caseHelper.createDummyUser(USER_OWNER2);
        long timestamp = new Date().getTime();
        testComment = "Test Comment On file " + timestamp;
        testFileTitle = "test_file_" + timestamp + ".txt";
        file1 = authorityFilesService.addFile(owner1, testFileTitle, MimetypeMap.MIMETYPE_TEXT_PLAIN, fileBytes, testComment);
    }

    @After
    public void tearDown() {
        tr.runInTransactionAsAdmin(() -> {
            try {
                if (nodeService.exists(file1)) {
                    filesService.delete(file1);
                }
            } finally {
                caseHelper.deleteDummyUser(USER_OWNER1);
                caseHelper.deleteDummyUser(USER_OWNER2);

                authorityFilesService.getAuthorityFolder(USER_OWNER1).ifPresent(nodeService::deleteNode);
                authorityFilesService.getAuthorityFolder(USER_OWNER2).ifPresent(nodeService::deleteNode);
            }
            return null;
        });
    }

    @Test
    public void a_file_is_commented() {
        setFullyAuthenticatedUser(USER_OWNER1);
        JSONObject file = filesService.getFile(file1);
        JSONArray comments = (JSONArray) file.get("comments");
        assertEquals("file has 1 comment", 1, comments.size());
        assertEquals("comment is correct", testComment, ((JSONObject) comments.get(0)).get("comment"));
    }

    @Test
    public void owner1_gets_his_files() {
        setFullyAuthenticatedUser(USER_OWNER1);
        List<JSONObject> files = authorityFilesService.getFiles(USER_OWNER1);
        assertEquals(USER_OWNER1 + " has one file", 1, files.size());
    }

    @Test(expected = AccessDeniedException.class)
    public void owner2_doesnt_get_owner1s_files() {
        setFullyAuthenticatedUser(USER_OWNER2);
        authorityFilesService.getFiles(USER_OWNER1);
    }

    @Test
    public void owner1_gets_his_file_by_id() {
        setFullyAuthenticatedUser(USER_OWNER1);
        assertNotNull(USER_OWNER1 + " gets his file by id", filesService.getFile(file1));
    }

    @Test(expected = AccessDeniedException.class)
    public void owner2_doesnt_get_owner1s_file() {
        setFullyAuthenticatedUser(USER_OWNER2);
        filesService.getFile(file1);
    }

    public void testAddFile() {
        //tested in setUp()
    }

    @Test
    public void owner2_gets_his_file_after_move() {
        setFullyAuthenticatedUser(USER_OWNER1);
        authorityFilesService.move(file1, owner2, "Some comment");

        setFullyAuthenticatedUser(USER_OWNER2);
        List<JSONObject> files = authorityFilesService.getFiles(USER_OWNER2);
        assertEquals(USER_OWNER2 + " has one file", 1, files.size());
    }

    @Test
    public void owner1_doesnt_get_owner2s_file_after_move() {
        setFullyAuthenticatedUser(USER_OWNER1);
        authorityFilesService.move(file1, owner2, "Some comment");
        assertTrue(USER_OWNER1 + " has no files after move", authorityFilesService.getFiles(USER_OWNER1).isEmpty());
    }

    @Test
    public void owner1_gets_his_file_as_group_member() {
        final String GROUP1 = PermissionService.GROUP_PREFIX + GROUP1_WITH_OWNER1;
        try {
            setAdminUserAsFullyAuthenticatedUser();
            tr.runInTransactionAsAdmin(() -> {
                ScriptGroup group = scriptAuthorityService.createRootGroup(GROUP1_WITH_OWNER1, GROUP1_WITH_OWNER1);
                authorityService.addAuthority(group.getFullName(), USER_OWNER1);
                return group.getFullName();
            });
            NodeRef group1 = authorityService.getAuthorityNodeRef(GROUP1);

            setFullyAuthenticatedUser(USER_OWNER1);
            authorityFilesService.move(file1, group1, "moving to group");

            List<JSONObject> files = authorityFilesService.getFiles(GROUP1);
            assertEquals(USER_OWNER1 + " has one file from group", 1, files.size());
        } finally {
            //cleanup
            tr.runInTransactionAsAdmin(() -> {
                authorityService.deleteAuthority(GROUP1);
                authorityFilesService.getAuthorityFolder(GROUP1).ifPresent(nodeService::deleteNode);
                return null;
            });
        }
    }

    @Test
    public void owner1_can_access_his_folder_and_file() {
        setFullyAuthenticatedUser(USER_OWNER1);
        //check access to files folder ALLOWED
        NodeRef filesFolder = openESDHFoldersService.getFilesRootNodeRef();
        //check access to user files folder ALLOWED
        NodeRef userFilesFolder = nodeService.getChildByName(filesFolder, ContentModel.ASSOC_CONTAINS, USER_OWNER1);
        //check access to file ALLOWED
        nodeService.getProperty(file1, ContentModel.PROP_NAME);
    }

    @Test(expected = AccessDeniedException.class)
    public void owner2_cant_access_owner1s_folder() {
        setFullyAuthenticatedUser(USER_OWNER2);
        //check access to files folder ALLOWED
        NodeRef filesFolder = openESDHFoldersService.getFilesRootNodeRef();
        //check access to user files folder DENIED
        nodeService.getChildByName(filesFolder, ContentModel.ASSOC_CONTAINS, USER_OWNER1);
    }

    @Test(expected = AccessDeniedException.class)
    public void owner2_cant_access_owner1s_file() {
        setFullyAuthenticatedUser(USER_OWNER2);
        //check access to file DENIED
        nodeService.getProperty(file1, ContentModel.PROP_NAME);
    }

    @Test
    public void shouldMoveFileWithCommentsToCase() {

        NodeRef caseRef = caseDocTestHelper.createCaseBehaviourOn(TEST_FILES_CASE,
                openESDHFoldersService.getCasesRootNodeRef(), USER_OWNER1);
        String caseId = caseService.getCaseId(caseRef);
        try {

            tr.runInTransaction(() -> {
                filesService.addToCase(caseId, file1, testFileTitle, caseDocTestHelper.getFirstDocumentType().getNodeRef(),
                        caseDocTestHelper.getFirstDocumentCategory().getNodeRef(), null);
                return null;
            });
            List<CaseDocument> docs = documentService.getCaseDocumentsWithAttachments(caseId);
            Assert.assertEquals("The file should be move to case. Wrong number of case docs", 1, docs.size());
            CaseDocument doc = docs.get(0);
            Assert.assertEquals("Wrong title of the file moved to case", testFileTitle, doc.getTitle());

            List<NodeRef> commentRefs = commentService.listComments(doc.getNodeRef(), new PagingRequest(100))
                    .getPage();
            Assert.assertEquals("Wrong number of comments moved to case doc", 1, commentRefs.size());

            ContentReader reader = contentService.getReader(commentRefs.get(0), ContentModel.PROP_CONTENT);
            Assert.assertEquals("Wrong case document comment", testComment, reader.getContentString());

        } finally {
            caseDocTestHelper.removeNodesAndDeleteUsersInTransaction(Collections.emptyList(),
                    Arrays.asList(caseRef), Collections.emptyList());
        }
    }
}
