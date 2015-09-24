package dk.openesdh.repo.services.cases;

import com.tradeshift.test.remote.Remote;
import com.tradeshift.test.remote.RemoteTestRunner;
import dk.openesdh.repo.helper.CaseDocumentTestHelper;
import dk.openesdh.repo.helper.CaseHelper;
import dk.openesdh.repo.model.CaseStatus;
import dk.openesdh.repo.model.DocumentStatus;
import dk.openesdh.repo.model.DocumentType;
import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.documents.DocumentService;
import dk.openesdh.repo.services.documents.DocumentTypeService;
import dk.openesdh.repo.services.lock.OELockService;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.search.CategoryService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.DynamicNamespacePrefixResolver;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(RemoteTestRunner.class)
@Remote(runnerClass = SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:alfresco/application-context.xml")
public class CaseServiceImplIT {

    //<editor-fold desc="Injected Autowired services">
    //    private static final String ADMIN_USER_NAME = "admin";
    private static final String USER_NAME_1 = "abeecher";
    private static final String USER_NAME_2 = "mjackson";

    @Autowired
    @Qualifier("NodeService")
    protected NodeService nodeService;

    @Autowired
    @Qualifier("SearchService")
    protected SearchService searchService;

    @Autowired
    @Qualifier("AuthorityService")
    protected AuthorityService authorityService;

    @Autowired
    @Qualifier("PersonService")
    protected PersonService personService;

    @Autowired
    @Qualifier("PermissionService")
    protected PermissionService permissionService;

    @Autowired
    @Qualifier("OwnableService")
    protected OwnableService ownableService;

    @Autowired
    @Qualifier("LockService")
    protected LockService lockService;

    @Autowired
    @Qualifier("repositoryHelper")
    protected Repository repositoryHelper;

    @Autowired
    @Qualifier("TransactionService")
    protected TransactionService transactionService;

    @Autowired
    @Qualifier("DictionaryService")
    protected DictionaryService dictionaryService;

    @Autowired
    @Qualifier("CategoryService")
    protected CategoryService categoryService;

    @Autowired
    @Qualifier("TestCaseHelper")
    protected CaseHelper caseHelper;

    @Autowired
    @Qualifier("CaseDocumentTestHelper")
    protected CaseDocumentTestHelper docTestHelper;

    @Autowired
    private ActionService actionService;

    @Autowired
    private RuleService ruleService;

    @Autowired
    @Qualifier("DocumentService")
    private DocumentService documentService;

    @Autowired
    @Qualifier("OELockService")
    private OELockService oeLockService;

    @Autowired
    @Qualifier("CaseService")
    protected CaseServiceImpl caseService;

    @Autowired
    @Qualifier("DocumentTypeService")
    protected DocumentTypeService documentTypeService;
    //</editor-fold>

    private static final String ALICE_BEECHER = "abeecher";
    private static final String MIKE_JACKSON = "mjackson";
  
    private DynamicNamespacePrefixResolver namespacePrefixResolver = new DynamicNamespacePrefixResolver(null);
    private NodeRef casesRootNoderef;
    protected NodeRef temporaryCaseNodeRef;
    private NodeRef dummyUser;
    protected NodeRef nonAdminCreatedCaseNr;
    private DocumentType documentType;

    @Before
    public void setUp() throws Exception {

        // TODO: All of this could have been done only once
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
            dummyUser = caseHelper.createDummyUser();
            NodeRef adminUserNodeRef = this.personService.getPerson(OpenESDHModel.ADMIN_USER_NAME);
                    authorityService.addAuthority(CaseHelper.CASE_CREATOR_GROUP, CaseHelper.DEFAULT_USERNAME);

            casesRootNoderef = caseService.getCasesRootNodeRef();

            namespacePrefixResolver.registerNamespace(NamespaceService.APP_MODEL_PREFIX, NamespaceService.APP_MODEL_1_0_URI);
            namespacePrefixResolver.registerNamespace(OpenESDHModel.CASE_PREFIX, OpenESDHModel.CASE_URI);

            documentType = documentTypeService.getDocumentTypes().stream().findFirst().get();

            final Map<QName, Serializable> properties = new HashMap<>();

            String caseName = "adminUser createdC case";
            temporaryCaseNodeRef = caseHelper.createSimpleCase(caseName, AuthenticationUtil.getAdminUserName(), adminUserNodeRef);

            // Create a case with a non-admin user
            caseName = "nonAdminUserCreatedCase";
            LinkedList<NodeRef> owners = new LinkedList<>();
            owners.add(dummyUser);
            nonAdminCreatedCaseNr = caseHelper.createSimpleCase(caseName, CaseHelper.DEFAULT_USERNAME, dummyUser);

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
    public void testGetParentCase() throws Exception {
        NodeRef documentsFolder = caseService.getDocumentsFolder(nonAdminCreatedCaseNr);

        assertEquals("Get parent case of case documents folder is correct",
                nonAdminCreatedCaseNr, caseService.getParentCase(documentsFolder));

        assertNull("Get parent case of non-case node is null",
                caseService.getParentCase(caseService.getCasesRootNodeRef()));

        assertEquals("Get parent case of case node is case node",
                nonAdminCreatedCaseNr, caseService.getParentCase(nonAdminCreatedCaseNr));
    }

    //TODO - Do we still need this?
    //@Test
    public void testAssignCaseIDRule() throws Exception {
        NodeRef documentsFolder = caseService.getDocumentsFolder(nonAdminCreatedCaseNr);

        // Create a test document
        String name = "test.doc";
        Map<QName, Serializable> properties = new HashMap<>();
        properties.put(ContentModel.PROP_NAME, name);
        NodeRef documentNodeRef = nodeService.createNode(documentsFolder,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name),
                ContentModel.TYPE_CONTENT,
                properties).getChildRef();

        assertTrue("oe:caseId aspect is set on case documents",
                nodeService.hasAspect(documentNodeRef,
                        OpenESDHModel.ASPECT_OE_CASE_ID));
        assertEquals("caseId is assigned correctly",
                caseService.getCaseId(nonAdminCreatedCaseNr),
                nodeService.getProperty(documentNodeRef, OpenESDHModel.PROP_OE_CASE_ID));
    }

    @Test
    public void testGetCasesRootNodeRef() throws Exception {
        NodeRef casesRootNodeRef = caseService.getCasesRootNodeRef();
        assertNotNull("Cases root noderef does not exist", casesRootNodeRef);
    }

    @Test
    public void testGetCaseId() throws Exception {
        long uniqueNumber = 1231L;
        String caseId = caseService.getCaseId(uniqueNumber);

        DateFormat dateFormat = new SimpleDateFormat(CaseService.DATE_FORMAT);
        Date date = new Date();
        StringBuilder testCaseId = new StringBuilder(dateFormat.format(date));
        testCaseId.append("-");
        testCaseId.append(uniqueNumber);
        assertEquals("CaseId is not on the form yyyyMMdd-xxxxxx", testCaseId.toString(), caseId);
    }

    @Test
    public void testGetCasePathNodeRef() throws Exception {
        Calendar c = Calendar.getInstance();
        NodeRef y = caseService.getCasePathNodeRef(casesRootNoderef, Calendar.YEAR);
        assertTrue("Year node does not have the correct value", c.get(Calendar.YEAR) == Integer.parseInt((String) nodeService.getProperty(y, ContentModel.PROP_NAME)));
        NodeRef m = caseService.getCasePathNodeRef(y, Calendar.MONTH);
        assertTrue("Month node does not have the correct value", (c.get(Calendar.MONTH) + 1) == Integer.parseInt((String) nodeService.getProperty(m, ContentModel.PROP_NAME)));
        NodeRef d = caseService.getCasePathNodeRef(m, Calendar.DATE);
        assertTrue("Day node does not have the correct value", c.get(Calendar.DATE) == Integer.parseInt((String) nodeService.getProperty(d, ContentModel.PROP_NAME)));
    }

    @Test
    public void testSetupPermissionGroups() throws Exception {
        long uniqueNumber = 1231L;
        String caseId = caseService.getCaseId(uniqueNumber);
        caseService.setupPermissionGroups(temporaryCaseNodeRef, caseId);

        String groupSuffix = "case_" + caseId + "_CaseSimpleReader";
        String groupName = authorityService.getName(AuthorityType.GROUP, groupSuffix);
        assertNotNull("No reader group created", groupName);
        if (groupName != null) {
            authorityService.deleteAuthority(groupName);
        }

        groupSuffix = "case_" + caseId + "_CaseSimpleWriter";
        groupName = authorityService.getName(AuthorityType.GROUP, groupSuffix);
        assertNotNull("No writer group created", groupName);
        if (groupName != null) {
            authorityService.deleteAuthority(groupName);
        }
    }

    @Test
    public void testBehaviourOnAddRemoveOwner() throws Exception {
//        AuthenticationUtil.setFullyAuthenticatedUser(CaseHelper.DEFAULT_USERNAME);

        String groupName = caseService.getCaseRoleGroupName(caseService.getCaseId(nonAdminCreatedCaseNr), "CaseOwners");
        NodeRef adminNodeRef = personService.getPerson(AuthenticationUtil.getAdminUserName());

        // Add admin to owners
        nodeService.createAssociation(nonAdminCreatedCaseNr,
                adminNodeRef,
                OpenESDHModel.ASSOC_CASE_OWNERS);

        assertTrue("Adding case owner should add them to CaseOwners group",
                authorityService.getContainedAuthorities(
                        AuthorityType.USER,
                        groupName,
                        false).contains(AuthenticationUtil.getAdminUserName()));

        // Remove admin from owners
        nodeService.removeAssociation(nonAdminCreatedCaseNr,
                adminNodeRef, OpenESDHModel.ASSOC_CASE_OWNERS);

        assertFalse("Removing case owner should remove them from CaseOwners " +
                "group", authorityService
                .getContainedAuthorities(
                        AuthorityType.USER,
                        groupName,
                        false).contains(AuthenticationUtil.getAdminUserName()));
    }

    @Test
    public void testGetCaseFolderNodeRef() throws Exception {
        Calendar c = Calendar.getInstance();

        NodeRef caseFolderNodeRef = caseService.getCaseFolderNodeRef(casesRootNoderef);
        int name = Integer.parseInt((String) nodeService.getProperty(caseFolderNodeRef, ContentModel.PROP_NAME));
        assertTrue("Day not correct", name == c.get(Calendar.DATE));

        NodeRef parentRef = nodeService.getPrimaryParent(caseFolderNodeRef).getParentRef();
        name = Integer.parseInt((String) nodeService.getProperty(parentRef, ContentModel.PROP_NAME));
        assertTrue("Month not correct", name == c.get(Calendar.MONTH) + 1);

        parentRef = nodeService.getPrimaryParent(parentRef).getParentRef();
        name = Integer.parseInt((String) nodeService.getProperty(parentRef, ContentModel.PROP_NAME));
        assertTrue("Month not correct", name == c.get(Calendar.YEAR));
    }

    @Test
    public void testGetRoles() throws Exception {
        long uniqueNumber = 1231L;
        String caseId = caseService.getCaseId(uniqueNumber);
        caseService.setupPermissionGroups(temporaryCaseNodeRef, caseId);
        Set<String> permissionGroups = caseService.getRoles(temporaryCaseNodeRef);
        assertTrue(permissionGroups.contains("CaseSimpleReader"));
        assertTrue(permissionGroups.contains("CaseSimpleWriter"));
    }

    @Test
    public void testAddRemoveAuthorityRole() throws Exception {
        caseService.setupPermissionGroups(temporaryCaseNodeRef,
                caseService.getCaseId(temporaryCaseNodeRef));
        caseService.removeAuthorityFromRole(AuthenticationUtil.getAdminUserName(), "CaseSimpleReader",
                temporaryCaseNodeRef);
        caseService.addAuthorityToRole(AuthenticationUtil.getAdminUserName(), "CaseSimpleReader",
                temporaryCaseNodeRef);
        Map<String, Set<String>> membersByRoles = caseService.getMembersByRole(temporaryCaseNodeRef, true, false);
        assertTrue(membersByRoles.get("CaseSimpleReader").contains(AuthenticationUtil.getAdminUserName()));

        caseService.removeAuthorityFromRole(AuthenticationUtil.getAdminUserName(), "CaseSimpleReader",
                temporaryCaseNodeRef);
        membersByRoles = caseService.getMembersByRole(temporaryCaseNodeRef,true, false);
        assertFalse(membersByRoles.get("CaseSimpleReader").contains
                (AuthenticationUtil.getAdminUserName()));
    }

    @Test
    public void testAddAuthoritiesToRole() throws Exception {
        caseService.setupPermissionGroups(temporaryCaseNodeRef,
                caseService.getCaseId(temporaryCaseNodeRef));

        caseService.removeAuthorityFromRole(AuthenticationUtil.getAdminUserName(), "CaseSimpleReader",
                temporaryCaseNodeRef);

        NodeRef authorityNodeRef = authorityService.getAuthorityNodeRef(AuthenticationUtil.getAdminUserName());
        List<NodeRef> authorities = new LinkedList<>();
        authorities.add(authorityNodeRef);
        caseService.addAuthoritiesToRole(authorities, "CaseSimpleReader",
                temporaryCaseNodeRef);
        Map<String, Set<String>> membersByRoles = caseService.getMembersByRole(temporaryCaseNodeRef, true, false);
        assertTrue(membersByRoles.get("CaseSimpleReader").contains
                (AuthenticationUtil.getAdminUserName()));
        caseService.removeAuthorityFromRole(AuthenticationUtil.getAdminUserName(), "CaseSimpleReader",
                temporaryCaseNodeRef);
    }

    @Test
    public void testChangeAuthorityRole() throws Exception {
        caseService.setupPermissionGroups(temporaryCaseNodeRef,
                caseService.getCaseId(temporaryCaseNodeRef));
        caseService.removeAuthorityFromRole(AuthenticationUtil.getAdminUserName(), "CaseSimpleReader",
                temporaryCaseNodeRef);
        caseService.removeAuthorityFromRole(AuthenticationUtil.getAdminUserName(),
                "CaseSimpleWriter",
                temporaryCaseNodeRef);
        caseService.addAuthorityToRole(AuthenticationUtil.getAdminUserName(),
                "CaseSimpleReader", temporaryCaseNodeRef);
        caseService.changeAuthorityRole(AuthenticationUtil.getAdminUserName(),
                "CaseSimpleReader", "CaseSimpleWriter", temporaryCaseNodeRef);
        Map<String, Set<String>> membersByRoles = caseService.getMembersByRole(temporaryCaseNodeRef, true, false);
        assertFalse(membersByRoles.get("CaseSimpleReader").contains(AuthenticationUtil.getAdminUserName()));
        assertTrue(membersByRoles.get("CaseSimpleWriter").contains(AuthenticationUtil.getAdminUserName()));
        caseService.removeAuthorityFromRole(AuthenticationUtil.getAdminUserName(),
                "CaseSimpleWriter", temporaryCaseNodeRef);
    }

    @Test
    public void testGetMembersByRole() throws Exception {
        //TODO: Does this still make sense? Are behaviours responsible for setting up permissions groups on temporaryCaseNodeRef?
        caseService.setupPermissionGroups(temporaryCaseNodeRef,
                caseService.getCaseId(temporaryCaseNodeRef));
        caseService.removeAuthorityFromRole(AuthenticationUtil.getAdminUserName(),
                "CaseSimpleReader", temporaryCaseNodeRef);
        caseService.addAuthorityToRole(AuthenticationUtil.getAdminUserName(),
                "CaseSimpleReader", temporaryCaseNodeRef);
        Map<String, Set<String>> membersByRole = caseService.getMembersByRole(temporaryCaseNodeRef, true, false);
        assertTrue(membersByRole.get("CaseSimpleReader").contains(AuthenticationUtil.getAdminUserName()));
        caseService.removeAuthorityFromRole(AuthenticationUtil.getAdminUserName(),
                "CaseSimpleReader", temporaryCaseNodeRef);
        membersByRole = caseService.getMembersByRole(temporaryCaseNodeRef, true, false);
        assertFalse(membersByRole.get("CaseSimpleReader").contains(AuthenticationUtil.getAdminUserName()));
    }

    @Test
    public void testGetAllMembersByRole() throws Exception {
        transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
            //caseService.setupPermissionGroups(nonAdminCreatedCaseNr, caseService.getCaseId(nonAdminCreatedCaseNr));
            //caseService.removeAuthorityFromRole(AuthenticationUtil.getAdminUserName(), "CaseSimpleReader", nonAdminCreatedCaseNr);
            caseService.addAuthorityToRole(AuthenticationUtil.getAdminUserName(), "CaseSimpleReader", nonAdminCreatedCaseNr);
            //System.out.println("\n\nCaseServiceImpl:440\n\t\t\t=>currentUserAuthorities : " + authorityService.getAuthoritiesForUser(ALICE_BEECHER).toString());

            caseService.addAuthorityToRole(ALICE_BEECHER, "CaseOwners", nonAdminCreatedCaseNr);
            caseService.addAuthorityToRole(MIKE_JACKSON, "CaseSimpleWriter", nonAdminCreatedCaseNr);
            return null;
        });
        Map<String, Set<String>> membersByRole = caseService.getMembersByRole(nonAdminCreatedCaseNr, false, true);
        //check everyone's permissions
        assertTrue(membersByRole.get("CaseSimpleReader").contains(AuthenticationUtil.getAdminUserName()));
        Set<String> caseOwners = membersByRole.get("CaseOwners");
        assertNotNull(caseOwners);
        assertTrue(caseOwners.contains(ALICE_BEECHER));
        assertTrue(membersByRole.get("CaseSimpleWriter").contains(MIKE_JACKSON));
        //remove 2 out of 3 from groups

        transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
            caseService.removeAuthorityFromRole(AuthenticationUtil.getAdminUserName(), "CaseSimpleReader", nonAdminCreatedCaseNr);
            caseService.removeAuthorityFromRole(MIKE_JACKSON, "CaseSimpleWriter", nonAdminCreatedCaseNr);
            return null;
        });
        //retrieve and test role memeberships
        membersByRole = caseService.getMembersByRole(nonAdminCreatedCaseNr, false, true);
        assertFalse(membersByRole.get("CaseSimpleReader").contains(AuthenticationUtil.getAdminUserName()));
        assertFalse(membersByRole.get("CaseSimpleWriter").contains(MIKE_JACKSON));
        assertTrue(membersByRole.get("CaseOwners").contains(ALICE_BEECHER));


    }

    @Test
    public void testClose() throws Exception {
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        assertFalse("Case node has locked aspect although it is not " +
                "closed", nodeService.hasAspect
                (nonAdminCreatedCaseNr,
                        OpenESDHModel.ASPECT_OE_LOCKED));
        final String originalTitle = (String) nodeService.getProperty(nonAdminCreatedCaseNr,
                ContentModel.PROP_TITLE);

        assertFalse("Case should not be closed when initially created",
                caseService.isLocked(nonAdminCreatedCaseNr));

        AuthenticationUtil.setFullyAuthenticatedUser(CaseHelper.DEFAULT_USERNAME);

        caseService.changeNodeStatus(nonAdminCreatedCaseNr, CaseStatus.ACTIVE);

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        assertEquals("Initial status is active", caseService.getNodeStatus(nonAdminCreatedCaseNr), CaseStatus.ACTIVE);

        // Add a document
        NodeRef docFileNodeRef = docTestHelper.createCaseDocument(UUID.randomUUID().toString(), nonAdminCreatedCaseNr, documentType);
        NodeRef docRecordNodeRef = nodeService.getPrimaryParent(docFileNodeRef).getParentRef();

        caseService.changeNodeStatus(nonAdminCreatedCaseNr, CaseStatus.CLOSED);

        assertEquals("Status after closing is closed", caseService.getNodeStatus
                (nonAdminCreatedCaseNr), CaseStatus.CLOSED);

        // Test that locked properties got set
        assertTrue("Case isLocked returns true for a closed " +
                "case", caseService.isLocked(nonAdminCreatedCaseNr));
        assertTrue("Case node has locked aspect after it has " +
                "been closed", nodeService.hasAspect
                (nonAdminCreatedCaseNr,
                        OpenESDHModel.ASPECT_OE_LOCKED));
        assertEquals("Case lockedBy is set correctly",
                nodeService.getProperty(nonAdminCreatedCaseNr,
                        OpenESDHModel.PROP_OE_LOCKED_BY),
                AuthenticationUtil.getFullyAuthenticatedUser());

        // Test that document got finalized
        assertEquals("Document in finalized case has FINAL status",
                DocumentStatus.FINAL, documentService.getNodeStatus(docRecordNodeRef));
        assertEquals("Document in finalized case is locked", true,
                oeLockService.isLocked(docRecordNodeRef));

        // Test that the owner cannot write to a closed case
        try {
            nodeService.setProperty(nonAdminCreatedCaseNr,
                    ContentModel.PROP_TITLE, "new title");
            fail("A property could be updated on a closed case");
        } catch (Exception e) {
        }

        try {
            // Test that a document cannot be added to a closed case
            NodeRef doc = docTestHelper.createCaseDocument(UUID.randomUUID().toString(), nonAdminCreatedCaseNr, documentType);
            fail("A document could be added to a closed case");
        } catch (Exception e) {
        }

        // Test that the owner cannot change permissions on the case
        try {
            caseService.removeAuthorityFromRole(CaseHelper.DEFAULT_USERNAME,
                    "CaseOwners", nonAdminCreatedCaseNr);
            fail("An authority could be removed from a role on a closed case");
        } catch (Exception e) {
        }

        // Test that the owner cannot add an authority to a role on the case
        try {
            caseService.addAuthorityToRole(OpenESDHModel.ADMIN_USER_NAME,
                            "CaseSimpleReader", nonAdminCreatedCaseNr);
            fail("An authority could be added to a role on a closed case");
        } catch (Exception e) {
        }

        // Test that a user can still read from the closed case
        assertEquals(nodeService.getProperty(nonAdminCreatedCaseNr,
                ContentModel.PROP_TITLE), originalTitle);

        assertTrue(caseService.isLocked(nonAdminCreatedCaseNr));

        // Close the case again. This shouldn't do anything.
        caseService.changeNodeStatus(nonAdminCreatedCaseNr, CaseStatus.CLOSED);

        AuthenticationUtil.setFullyAuthenticatedUser(CaseHelper.DEFAULT_USERNAME);

        try {
            caseService.changeNodeStatus(nonAdminCreatedCaseNr, CaseStatus.ACTIVE);
            fail("Should not be able to set closed case to active as a " +
                    "regular user");
        } catch (Exception e) {
        }


        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        caseService.changeNodeStatus(nonAdminCreatedCaseNr, CaseStatus.ACTIVE);

        assertEquals("Status after reopening is active", caseService.getNodeStatus(nonAdminCreatedCaseNr), CaseStatus.ACTIVE);

        assertFalse("Case isLocked returns false for a reopened case" +
                "case", caseService.isLocked(nonAdminCreatedCaseNr));

        assertEquals("Document in finalized case still has FINAL status after reopening",
                DocumentStatus.FINAL, documentService.getNodeStatus(docRecordNodeRef));

        // Put the document back in draft status. This is done so that
        // tear-down will not result in a NodeLockedException when
        // deleting the case.
        documentService.changeNodeStatus(docRecordNodeRef, DocumentStatus.DRAFT);

        assertEquals("Document in finalized case can be changed back to " +
                        "DRAFT status after reopening", DocumentStatus.DRAFT, documentService.getNodeStatus(docRecordNodeRef));


//        AuthenticationUtil.setFullyAuthenticatedUser(CaseHelper.DEFAULT_USERNAME);

        // Test that a user can write again: these would throw exceptions if
        // they failed.
        nodeService.setProperty(nonAdminCreatedCaseNr,
                ContentModel.PROP_TITLE, "new title");
        nodeService.setProperty(nonAdminCreatedCaseNr,
                ContentModel.PROP_TITLE, originalTitle);

        assertFalse("Case node does not have the locked aspect after being " +
                        "reopened",
                nodeService.hasAspect(nonAdminCreatedCaseNr,
                        OpenESDHModel.ASPECT_OE_LOCKED));
    }

    @Test
    public void passivate() throws Exception {
        caseService.changeNodeStatus(nonAdminCreatedCaseNr, CaseStatus.PASSIVE);
        assertEquals("Status is passive after being passivated", caseService
                .getNodeStatus(nonAdminCreatedCaseNr), CaseStatus.PASSIVE);
    }

    @Test
    public void shouldReturnWritePermissionsForOwner() {
        AuthenticationUtil.setFullyAuthenticatedUser(CaseHelper.DEFAULT_USERNAME);
        String caseId = caseService.getCaseId(nonAdminCreatedCaseNr);
        List<String> permissions = caseService.getCaseUserPermissions(caseId);
        assertTrue("Case owner should contain permissions for the case", permissions.contains("CaseOwners"));
    }
}
