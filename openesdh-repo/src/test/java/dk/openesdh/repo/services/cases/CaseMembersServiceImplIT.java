package dk.openesdh.repo.services.cases;

import com.tradeshift.test.remote.Remote;
import com.tradeshift.test.remote.RemoteTestRunner;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.DynamicNamespacePrefixResolver;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.transaction.TransactionService;
import org.junit.After;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import dk.openesdh.repo.helper.CaseHelper;
import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.lock.OELockService;
import dk.openesdh.repo.services.members.CaseMembersService;

@RunWith(RemoteTestRunner.class)
@Remote(runnerClass = SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:alfresco/application-context.xml")
public class CaseMembersServiceImplIT {

    @Autowired
    @Qualifier("NodeService")
    private NodeService nodeService;

    @Autowired
    @Qualifier("AuthorityService")
    private AuthorityService authorityService;

    @Autowired
    @Qualifier("PersonService")
    private PersonService personService;

    @Autowired
    @Qualifier("TransactionService")
    private TransactionService transactionService;

    @Autowired
    @Qualifier("TestCaseHelper")
    private CaseHelper caseHelper;

    @Autowired
    @Qualifier("OELockService")
    private OELockService oeLockService;

    @Autowired
    @Qualifier("CaseService")
    private CaseServiceImpl caseService;

    @Autowired
    @Qualifier("CaseMembersService")
    private CaseMembersService caseMembersService;

    private final DynamicNamespacePrefixResolver namespacePrefixResolver = new DynamicNamespacePrefixResolver(null);
    private NodeRef temporaryCaseNodeRef;
    private NodeRef dummyUser;
    private NodeRef nonAdminCreatedCaseNr;

    @Before
    public void setUp() throws Exception {

        // TODO: All of this could have been done only once
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
                    dummyUser = caseHelper.createDummyUser();
                    NodeRef adminUserNodeRef = this.personService.getPerson(OpenESDHModel.ADMIN_USER_NAME);
            authorityService.addAuthority(CaseHelper.CASE_SIMPLE_CREATOR_GROUP, CaseHelper.DEFAULT_USERNAME);

                    namespacePrefixResolver.registerNamespace(NamespaceService.APP_MODEL_PREFIX,
                            NamespaceService.APP_MODEL_1_0_URI);
                    namespacePrefixResolver.registerNamespace(OpenESDHModel.CASE_PREFIX, OpenESDHModel.CASE_URI);

                    String caseName = "adminUser createdC case";
                    temporaryCaseNodeRef = caseHelper.createSimpleCase(caseName,
                            AuthenticationUtil.getAdminUserName(), adminUserNodeRef);

                    // Create a case with a non-admin user
                    caseName = "nonAdminUserCreatedCase";
                    nonAdminCreatedCaseNr = caseHelper.createSimpleCase(caseName, CaseHelper.DEFAULT_USERNAME,
                            dummyUser);
                    return null;
                });
    }

    @After
    public void tearDown() throws Exception {
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
            // Remove temporary node, and all its content,
            // also removes test cases
            if (nonAdminCreatedCaseNr != null) {
                // Make sure it is unlocked, before deleting, since the
                // documents may have remained locked if the testClose
                // test failed.
                oeLockService.unlock(nonAdminCreatedCaseNr, true);
                nodeService.deleteNode(nonAdminCreatedCaseNr);
                nonAdminCreatedCaseNr = null;
            }
            if (temporaryCaseNodeRef != null) {
                nodeService.deleteNode(temporaryCaseNodeRef);
                temporaryCaseNodeRef = null;
            }
            caseHelper.deleteDummyUser();
            return true;
        });
    }

    @Test
    public void testAddRemoveAuthorityRole() throws Exception {
        caseService.setupPermissionGroups(temporaryCaseNodeRef, caseService.getCaseId(temporaryCaseNodeRef));
        caseMembersService.removeAuthorityFromRole(AuthenticationUtil.getAdminUserName(), CaseHelper.CASE_SIMPLE_READER_ROLE,
                temporaryCaseNodeRef);
        caseMembersService.addAuthorityToRole(AuthenticationUtil.getAdminUserName(), CaseHelper.CASE_SIMPLE_READER_ROLE,
                temporaryCaseNodeRef);
        Map<String, Set<String>> membersByRoles = caseMembersService.getMembersByRole(temporaryCaseNodeRef, true,
                false);
        assertTrue(membersByRoles.get(CaseHelper.CASE_SIMPLE_READER_ROLE).contains(AuthenticationUtil.getAdminUserName()));

        caseMembersService.removeAuthorityFromRole(AuthenticationUtil.getAdminUserName(), CaseHelper.CASE_SIMPLE_READER_ROLE,
                temporaryCaseNodeRef);
        membersByRoles = caseMembersService.getMembersByRole(temporaryCaseNodeRef, true, false);
        assertFalse(membersByRoles.get(CaseHelper.CASE_SIMPLE_READER_ROLE).contains(AuthenticationUtil.getAdminUserName()));
    }

    @Test
    public void testAddAuthoritiesToRole() throws Exception {
        caseService.setupPermissionGroups(temporaryCaseNodeRef, caseService.getCaseId(temporaryCaseNodeRef));

        caseMembersService.removeAuthorityFromRole(AuthenticationUtil.getAdminUserName(), CaseHelper.CASE_SIMPLE_READER_ROLE,
                temporaryCaseNodeRef);

        NodeRef authorityNodeRef = authorityService.getAuthorityNodeRef(AuthenticationUtil.getAdminUserName());
        List<NodeRef> authorities = new LinkedList<>();
        authorities.add(authorityNodeRef);
        caseMembersService.addAuthoritiesToRole(authorities, CaseHelper.CASE_SIMPLE_READER_ROLE, temporaryCaseNodeRef);
        Map<String, Set<String>> membersByRoles = caseMembersService.getMembersByRole(temporaryCaseNodeRef, true,
                false);
        assertTrue(membersByRoles.get(CaseHelper.CASE_SIMPLE_READER_ROLE).contains(AuthenticationUtil.getAdminUserName()));
        caseMembersService.removeAuthorityFromRole(AuthenticationUtil.getAdminUserName(), CaseHelper.CASE_SIMPLE_READER_ROLE,
                temporaryCaseNodeRef);
    }

    @Test
    public void testChangeAuthorityRole() throws Exception {
        caseService.setupPermissionGroups(temporaryCaseNodeRef, caseService.getCaseId(temporaryCaseNodeRef));
        caseMembersService.removeAuthorityFromRole(AuthenticationUtil.getAdminUserName(), CaseHelper.CASE_SIMPLE_READER_ROLE,
                temporaryCaseNodeRef);
        caseMembersService.removeAuthorityFromRole(AuthenticationUtil.getAdminUserName(), CaseHelper.CASE_SIMPLE_WRITER_ROLE,
                temporaryCaseNodeRef);
        caseMembersService.addAuthorityToRole(AuthenticationUtil.getAdminUserName(), CaseHelper.CASE_SIMPLE_READER_ROLE,
                temporaryCaseNodeRef);
        caseMembersService.changeAuthorityRole(AuthenticationUtil.getAdminUserName(), CaseHelper.CASE_SIMPLE_READER_ROLE,
                CaseHelper.CASE_SIMPLE_WRITER_ROLE, temporaryCaseNodeRef);
        Map<String, Set<String>> membersByRoles = caseMembersService.getMembersByRole(temporaryCaseNodeRef, true,
                false);
        assertFalse(membersByRoles.get(CaseHelper.CASE_SIMPLE_READER_ROLE).contains(AuthenticationUtil.getAdminUserName()));
        assertTrue(membersByRoles.get(CaseHelper.CASE_SIMPLE_WRITER_ROLE).contains(AuthenticationUtil.getAdminUserName()));
        caseMembersService.removeAuthorityFromRole(AuthenticationUtil.getAdminUserName(), CaseHelper.CASE_SIMPLE_WRITER_ROLE,
                temporaryCaseNodeRef);
    }

    @Test
    public void testGetMembersByRole() throws Exception {
        // TODO: Does this still make sense? Are behaviours responsible for
        // setting up permissions groups on temporaryCaseNodeRef?
        caseService.setupPermissionGroups(temporaryCaseNodeRef, caseService.getCaseId(temporaryCaseNodeRef));
        caseMembersService.removeAuthorityFromRole(AuthenticationUtil.getAdminUserName(), CaseHelper.CASE_SIMPLE_READER_ROLE,
                temporaryCaseNodeRef);
        caseMembersService.addAuthorityToRole(AuthenticationUtil.getAdminUserName(), CaseHelper.CASE_SIMPLE_READER_ROLE,
                temporaryCaseNodeRef);
        Map<String, Set<String>> membersByRole = caseMembersService.getMembersByRole(temporaryCaseNodeRef, true,
                false);
        assertTrue(membersByRole.get(CaseHelper.CASE_SIMPLE_READER_ROLE).contains(AuthenticationUtil.getAdminUserName()));
        caseMembersService.removeAuthorityFromRole(AuthenticationUtil.getAdminUserName(), CaseHelper.CASE_SIMPLE_READER_ROLE,
                temporaryCaseNodeRef);
        membersByRole = caseMembersService.getMembersByRole(temporaryCaseNodeRef, true, false);
        assertFalse(membersByRole.get(CaseHelper.CASE_SIMPLE_READER_ROLE).contains(AuthenticationUtil.getAdminUserName()));
    }

    @Test
    public void testGetAllMembersByRole() throws Exception {
        transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
            // caseService.setupPermissionGroups(nonAdminCreatedCaseNr,
            // caseService.getCaseId(nonAdminCreatedCaseNr));
            // caseService.removeAuthorityFromRole(AuthenticationUtil.getAdminUserName(),
            // CaseHelper.CASE_SIMPLE_READER_ROLE, nonAdminCreatedCaseNr);
            caseMembersService.addAuthorityToRole(AuthenticationUtil.getAdminUserName(), CaseHelper.CASE_SIMPLE_READER_ROLE,
                    nonAdminCreatedCaseNr);
            // System.out.println("\n\nCaseServiceImpl:440\n\t\t\t=>currentUserAuthorities : "
            // +
            // authorityService.getAuthoritiesForUser(ALICE_BEECHER).toString());

            caseMembersService.addAuthorityToRole(CaseHelper.ALICE_BEECHER, CaseHelper.CASE_SIMPLE_CREATOR_ROLE, nonAdminCreatedCaseNr);
            caseMembersService.addAuthorityToRole(CaseHelper.MIKE_JACKSON, CaseHelper.CASE_SIMPLE_WRITER_ROLE, nonAdminCreatedCaseNr);
            return null;
        });
        Map<String, Set<String>> membersByRole = caseMembersService.getMembersByRole(nonAdminCreatedCaseNr, false,
                true);
        // check everyone's permissions
        assertTrue(membersByRole.get(CaseHelper.CASE_SIMPLE_READER_ROLE).contains(AuthenticationUtil.getAdminUserName()));
        Set<String> caseOwners = membersByRole.get(CaseHelper.CASE_SIMPLE_CREATOR_ROLE);
        assertNotNull(caseOwners);
        assertTrue(caseOwners.contains(CaseHelper.ALICE_BEECHER));
        assertTrue(membersByRole.get(CaseHelper.CASE_SIMPLE_WRITER_ROLE).contains(CaseHelper.MIKE_JACKSON));
        // remove 2 out of 3 from groups

        transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
                    caseMembersService.removeAuthorityFromRole(AuthenticationUtil.getAdminUserName(),
                            CaseHelper.CASE_SIMPLE_READER_ROLE,
                            nonAdminCreatedCaseNr);
            caseMembersService.removeAuthorityFromRole(CaseHelper.MIKE_JACKSON, CaseHelper.CASE_SIMPLE_WRITER_ROLE,                            nonAdminCreatedCaseNr);
                    return null;
                });
        // retrieve and test role memeberships
        membersByRole = caseMembersService.getMembersByRole(nonAdminCreatedCaseNr, false, true);
        assertFalse(membersByRole.get(CaseHelper.CASE_SIMPLE_READER_ROLE).contains(AuthenticationUtil.getAdminUserName()));
        assertFalse(membersByRole.get(CaseHelper.CASE_SIMPLE_WRITER_ROLE).contains(CaseHelper.MIKE_JACKSON));
        assertTrue(membersByRole.get(CaseHelper.CASE_SIMPLE_CREATOR_ROLE).contains(CaseHelper.ALICE_BEECHER));

    }
}
