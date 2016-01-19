package dk.openesdh.repo.services.files;

import com.tradeshift.test.remote.Remote;
import com.tradeshift.test.remote.RemoteTestRunner;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import org.alfresco.repo.content.MimetypeMap;
import static org.alfresco.repo.security.authentication.AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser;
import static org.alfresco.repo.security.authentication.AuthenticationUtil.setFullyAuthenticatedUser;
import org.alfresco.repo.security.authority.script.ScriptAuthorityService;
import org.alfresco.repo.security.authority.script.ScriptGroup;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.transaction.TransactionService;
import org.json.simple.JSONObject;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import dk.openesdh.repo.helper.CaseHelper;
import dk.openesdh.repo.services.RunInTransactionAsAdmin;

@RunWith(RemoteTestRunner.class)
@Remote(runnerClass = SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:alfresco/application-context.xml", "classpath:alfresco/extension/openesdh-test-context.xml"})
public class FilesServiceImplIT implements RunInTransactionAsAdmin {

    @Autowired
    private FilesServiceImpl filesService;
    @Autowired
    @Qualifier("TestCaseHelper")
    private CaseHelper caseHelper;
    @Autowired
    @Qualifier("TransactionService")
    private TransactionService transactionService;
    @Autowired
    @Qualifier("authorityServiceScript")
    private ScriptAuthorityService scriptAuthorityService;
    @Autowired
    @Qualifier("AuthorityService")
    private AuthorityService authorityService;
    @Autowired
    @Qualifier("NodeService")
    private NodeService nodeService;

    private static final String USER_OWNER1 = "fileTestOwner1";
    private static final String USER_OWNER2 = "fileTestOwner2";
    private static final String GROUP1_WITH_OWNER1 = "fileTestGroup1";
    private final InputStream fileBytes = new ByteArrayInputStream("Test file content".getBytes());
    private NodeRef file1;
    private NodeRef owner1;
    private NodeRef owner2;

    @Before
    public void setUp() {
        setAdminUserAsFullyAuthenticatedUser();
        owner1 = caseHelper.createDummyUser(USER_OWNER1);
        owner2 = caseHelper.createDummyUser(USER_OWNER2);

        file1 = filesService.addFile(owner1, "test_file_" + new Date().getTime() + ".txt", MimetypeMap.MIMETYPE_TEXT_PLAIN, fileBytes);
    }

    @After
    public void tearDown() {
        runInTransactionAsAdmin(() -> {
            try {
                filesService.delete(file1);
            } finally {
                caseHelper.deleteDummyUser(USER_OWNER1);
                caseHelper.deleteDummyUser(USER_OWNER2);

                filesService.getAuthorityFolder(USER_OWNER1).ifPresent(nodeService::deleteNode);
                filesService.getAuthorityFolder(USER_OWNER2).ifPresent(nodeService::deleteNode);
                return null;
            }
        });
    }

    @Test
    public void owner1_gets_his_file() {
        setFullyAuthenticatedUser(USER_OWNER1);
        List<JSONObject> files = filesService.getFiles(USER_OWNER1);
        assertEquals(USER_OWNER1 + " has one file", 1, files.size());
    }

    @Test(expected = AccessDeniedException.class)
    public void owner2_doesnt_get_owner1s_files() {
        setFullyAuthenticatedUser(USER_OWNER2);
        filesService.getFiles(USER_OWNER1);
    }

    public void testAddFile() {
        //tested in setUp()
    }

    @Test
    public void testDelete() {
        //tested in tearDown()
    }

    @Test
    public void owner2_gets_his_file_after_move() {
        setFullyAuthenticatedUser(USER_OWNER1);
        filesService.move(file1, owner2, "Some comment");

        setFullyAuthenticatedUser(USER_OWNER2);
        List<JSONObject> files = filesService.getFiles(USER_OWNER2);
        assertEquals(USER_OWNER2 + " has one file", 1, files.size());
    }

    @Test
    public void owner1_doesnt_get_owner2s_file_after_move() {
        setFullyAuthenticatedUser(USER_OWNER1);
        filesService.move(file1, owner2, "Some comment");
        assertTrue(USER_OWNER1 + " has no files after move", filesService.getFiles(USER_OWNER1).isEmpty());
    }

    @Test
    public void owner1_gets_his_file_as_group_member() {
        final String GROUP1 = PermissionService.GROUP_PREFIX + GROUP1_WITH_OWNER1;
        try {
            setAdminUserAsFullyAuthenticatedUser();
            runInTransactionAsAdmin(() -> {
                ScriptGroup group = scriptAuthorityService.createRootGroup(GROUP1_WITH_OWNER1, GROUP1_WITH_OWNER1);
                authorityService.addAuthority(group.getFullName(), USER_OWNER1);
                return group.getFullName();
            });
            NodeRef group1 = authorityService.getAuthorityNodeRef(GROUP1);

            setFullyAuthenticatedUser(USER_OWNER1);
            filesService.move(file1, group1, "moving to group");

            List<JSONObject> files = filesService.getFiles(GROUP1);
            assertEquals(USER_OWNER1 + " has one file from group", 1, files.size());
        } finally {
            //cleanup
            runInTransactionAsAdmin(() -> {
                authorityService.deleteAuthority(GROUP1);
                filesService.getAuthorityFolder(GROUP1).ifPresent(nodeService::deleteNode);
                return null;
            });
        }
    }

    @Override
    public TransactionService getTransactionService() {
        return transactionService;
    }

}
