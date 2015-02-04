package dk.openesdh.repo.services.cases;

import com.tradeshift.test.remote.Remote;
import com.tradeshift.test.remote.RemoteTestRunner;
import dk.openesdh.repo.helper.CaseHelper;
import dk.openesdh.repo.model.OpenESDHModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.CategoryService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.*;
import org.alfresco.service.namespace.DynamicNamespacePrefixResolver;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.Assert.*;

@RunWith(RemoteTestRunner.class)
@Remote(runnerClass = SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:alfresco/application-context.xml")
public class CaseServiceImplTest {


//    private static final ApplicationContext APPLICATION_CONTEXT = ApplicationContextHelper.getApplicationContext(new String[]{"classpath:alfresco/application-context.xml"});

    private static final String ADMIN_USER_NAME = "admin";
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

    private CaseServiceImpl caseService = null;
    private DynamicNamespacePrefixResolver namespacePrefixResolver = new DynamicNamespacePrefixResolver(null);
    private NodeRef temporaryRepoNodeRef;
    private NodeRef temporaryCaseNodeRef;
    private NodeRef dummyUser;
    private NodeRef behaviourOnCaseNodeRef;

    @Before
    public void setUp() throws Exception {

        // TODO: All of this could have been done only once
        AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER_NAME);

        dummyUser = caseHelper.createDummyUser();

        AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER_NAME);

        caseService = new CaseServiceImpl();
        caseService.setNodeService(nodeService);
        caseService.setSearchService(searchService);
        caseService.setAuthorityService(authorityService);
        caseService.setOwnableService(ownableService);
        caseService.setPermissionService(permissionService);
        caseService.setRepositoryHelper(repositoryHelper);
        caseService.setTransactionService(transactionService);
        caseService.setDictionaryService(dictionaryService);
        caseService.setLockService(lockService);

        namespacePrefixResolver.registerNamespace(NamespaceService.APP_MODEL_PREFIX, NamespaceService.APP_MODEL_1_0_URI);
        namespacePrefixResolver.registerNamespace(OpenESDHModel.CASE_PREFIX, OpenESDHModel.CASE_URI);

        Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
        String name = "unittest_tmp";
        temporaryRepoNodeRef = nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS, name);
        if (temporaryRepoNodeRef == null) {
            // Create temporary node for use during testing
            properties.put(ContentModel.PROP_NAME, name);
            temporaryRepoNodeRef = nodeService.createNode(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS, QName.createQName(OpenESDHModel.CASE_URI, name), ContentModel.TYPE_FOLDER, properties).getChildRef();
        }

        name = "unittest_case";
        temporaryCaseNodeRef = nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS, name);
        if (temporaryCaseNodeRef == null) {
            LinkedList<NodeRef> owners = new LinkedList<>();
            owners.add(dummyUser);
            temporaryCaseNodeRef = caseHelper.createCase(
                    ADMIN_USER_NAME, temporaryRepoNodeRef, name,
                    OpenESDHModel.TYPE_CASE_SIMPLE, properties, owners, true);
        }

        // Create a case with the behaviour on
        name = "unittest_case_behaviour_on";
        LinkedList<NodeRef> owners = new LinkedList<>();
        owners.add(dummyUser);
        behaviourOnCaseNodeRef = caseHelper.createCase(
                CaseHelper.DEFAULT_USERNAME, repositoryHelper.getUserHome(dummyUser), name,
                OpenESDHModel.TYPE_CASE_SIMPLE, properties, owners);

        AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER_NAME);
    }

    @After
    public void tearDown() throws Exception {
        AuthenticationUtil.setFullyAuthenticatedUser(CaseHelper.ADMIN_USER_NAME);

        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Boolean>() {
            public Boolean execute() throws Throwable {
                // Remove temporary node, and all its content,
                // also removes test cases
                if (temporaryRepoNodeRef != null) {
                    nodeService.deleteNode(temporaryRepoNodeRef);
                }
                if (behaviourOnCaseNodeRef != null) {
                    nodeService.deleteNode(behaviourOnCaseNodeRef);
                }
                caseHelper.deleteDummyUser();
                return true;
            }
        });
    }

    @Test
    public void testGetParentCase() throws Exception {
        NodeRef documentsFolder = caseService.getDocumentsFolder
                (behaviourOnCaseNodeRef);

        assertEquals("Get parent case of case documents folder is correct",
                behaviourOnCaseNodeRef, caseService.getParentCase(documentsFolder));

        assertNull("Get parent case of non-case node is null",
                caseService.getParentCase(caseService.getCasesRootNodeRef()));

        assertNull("Get parent case of case node is null",
                caseService.getParentCase(behaviourOnCaseNodeRef));
    }

    @Test
    public void testAssignCaseIDRule() throws Exception {
        NodeRef documentsFolder = caseService.getDocumentsFolder
                (behaviourOnCaseNodeRef);

        // Create a test document
        String name = "test.doc";
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
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
                caseService.getCaseId(behaviourOnCaseNodeRef),
                nodeService.getProperty(documentNodeRef, OpenESDHModel.PROP_OE_CASE_ID));
    }

    @Test
    public void testGetCasesRootNodeRef() throws Exception {
        NodeRef casesRootNodeRef = caseService.getCasesRootNodeRef();
        assertNotNull("Cases root noderef does not exist", casesRootNodeRef);
        String path = nodeService.getPath(casesRootNodeRef).toPrefixString(namespacePrefixResolver);
        assertTrue("Cases root noderef is not in the right place", "/app:company_home/case:openesdh_cases".equals(path));
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
        NodeRef y = caseService.getCasePathNodeRef(temporaryRepoNodeRef, Calendar.YEAR);
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
    public void testBehaviourOnAddOwnersToPermissionGroup() throws Exception {
        String groupName = caseService.getCaseRoleGroupName(caseService.getCaseId(behaviourOnCaseNodeRef), "CaseOwners");

        assertTrue("Creating a case should add the users in case owners " +
                "association to the CaseOwners group", authorityService
                .getContainedAuthorities(
                AuthorityType.USER,
                groupName,
                false).contains(CaseHelper.DEFAULT_USERNAME));
    }

    @Test
    public void testBehaviourOnAddRemoveOwner() throws Exception {
        AuthenticationUtil.setFullyAuthenticatedUser(CaseHelper.DEFAULT_USERNAME);

        String groupName = caseService.getCaseRoleGroupName(caseService.getCaseId(behaviourOnCaseNodeRef), "CaseOwners");
        NodeRef adminNodeRef = personService.getPerson(ADMIN_USER_NAME);

        // Add admin to owners
        nodeService.createAssociation(behaviourOnCaseNodeRef,
                adminNodeRef,
                OpenESDHModel.ASSOC_CASE_OWNERS);

        assertTrue("Adding case owner should add them to CaseOwners group",
                authorityService.getContainedAuthorities(
                        AuthorityType.USER,
                        groupName,
                        false).contains(ADMIN_USER_NAME));

        // Remove admin from owners
        nodeService.removeAssociation(behaviourOnCaseNodeRef,
                adminNodeRef, OpenESDHModel.ASSOC_CASE_OWNERS);

        assertFalse("Removing case owner should remove them from CaseOwners " +
                "group", authorityService
                .getContainedAuthorities(
                        AuthorityType.USER,
                        groupName,
                        false).contains(ADMIN_USER_NAME));
    }

    @Test
    public void testGetCaseFolderNodeRef() throws Exception {
        Calendar c = Calendar.getInstance();

        NodeRef caseFolderNodeRef = caseService.getCaseFolderNodeRef(temporaryRepoNodeRef);
        int name = Integer.parseInt((String) nodeService.getProperty(caseFolderNodeRef, ContentModel.PROP_NAME));
        assertTrue("Day not correct", name == c.get(Calendar.DATE));

        NodeRef parentRef = nodeService.getPrimaryParent(caseFolderNodeRef).getParentRef();
        name = Integer.parseInt((String) nodeService.getProperty(parentRef, ContentModel.PROP_NAME));
        assertTrue("Month not correct", name == c.get(Calendar.MONTH) + 1);

        parentRef = nodeService.getPrimaryParent(parentRef).getParentRef();
        name = Integer.parseInt((String) nodeService.getProperty(parentRef, ContentModel.PROP_NAME));
        assertTrue("Month not correct", name == c.get(Calendar.YEAR));


        String path = nodeService.getPath(caseFolderNodeRef).toPrefixString(namespacePrefixResolver);
//        assertTrue("Cases folder noderef is not in the right place", "/app:company_home/case:openesdh_cases".equals(path));

        /*
        Calendar c = Calendar.getInstance();
        assertTrue("Year node does not have the correct value", c.get(Calendar.YEAR) == Integer.parseInt((String) nodeService.getProperty(y, ContentModel.PROP_NAME)));
        NodeRef m = caseService.getCasePathNodeRef(y, Calendar.MONTH);
        assertTrue("Month node does not have the correct value", (c.get(Calendar.MONTH) + 1) == Integer.parseInt((String) nodeService.getProperty(m, ContentModel.PROP_NAME)));
        NodeRef d = caseService.getCasePathNodeRef(m, Calendar.DATE);
        assertTrue("Day node does not have the correct value", c.get(Calendar.DATE) == Integer.parseInt((String) nodeService.getProperty(d, ContentModel.PROP_NAME)));
        */


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
        caseService.removeAuthorityFromRole(ADMIN_USER_NAME, "CaseSimpleReader",
                temporaryCaseNodeRef);
        caseService.addAuthorityToRole(ADMIN_USER_NAME, "CaseSimpleReader",
                temporaryCaseNodeRef);
        Map<String, Set<String>> membersByRoles = caseService.getMembersByRole(temporaryCaseNodeRef, true, false);
        assertTrue(membersByRoles.get("CaseSimpleReader").contains(ADMIN_USER_NAME));

        caseService.removeAuthorityFromRole(ADMIN_USER_NAME, "CaseSimpleReader",
                temporaryCaseNodeRef);
        membersByRoles = caseService.getMembersByRole(temporaryCaseNodeRef,true, false);
        assertFalse(membersByRoles.get("CaseSimpleReader").contains
                (ADMIN_USER_NAME));
    }


    @Test
    public void testAddAuthoritiesToRole() throws Exception {
        caseService.setupPermissionGroups(temporaryCaseNodeRef,
                caseService.getCaseId(temporaryCaseNodeRef));

        caseService.removeAuthorityFromRole(ADMIN_USER_NAME, "CaseSimpleReader",
                temporaryCaseNodeRef);

        NodeRef authorityNodeRef = authorityService.getAuthorityNodeRef(ADMIN_USER_NAME);
        List<NodeRef> authorities = new LinkedList<>();
        authorities.add(authorityNodeRef);
        caseService.addAuthoritiesToRole(authorities, "CaseSimpleReader",
                temporaryCaseNodeRef);
        Map<String, Set<String>> membersByRoles = caseService.getMembersByRole(temporaryCaseNodeRef, true, false);
        assertTrue(membersByRoles.get("CaseSimpleReader").contains
                (ADMIN_USER_NAME));
        caseService.removeAuthorityFromRole(ADMIN_USER_NAME, "CaseSimpleReader",
                temporaryCaseNodeRef);
    }

    @Test
    public void testChangeAuthorityRole() throws Exception {
        caseService.setupPermissionGroups(temporaryCaseNodeRef,
                caseService.getCaseId(temporaryCaseNodeRef));
        caseService.removeAuthorityFromRole(ADMIN_USER_NAME, "CaseSimpleReader",
                temporaryCaseNodeRef);
        caseService.removeAuthorityFromRole(ADMIN_USER_NAME,
                "CaseSimpleWriter",
                temporaryCaseNodeRef);
        caseService.addAuthorityToRole(ADMIN_USER_NAME,
                "CaseSimpleReader", temporaryCaseNodeRef);
        caseService.changeAuthorityRole(ADMIN_USER_NAME,
                "CaseSimpleReader", "CaseSimpleWriter", temporaryCaseNodeRef);
        Map<String, Set<String>> membersByRoles = caseService.getMembersByRole(temporaryCaseNodeRef, true, false);
        assertFalse(membersByRoles.get("CaseSimpleReader").contains(ADMIN_USER_NAME));
        assertTrue(membersByRoles.get("CaseSimpleWriter").contains(ADMIN_USER_NAME));
        caseService.removeAuthorityFromRole(ADMIN_USER_NAME,
                "CaseSimpleWriter", temporaryCaseNodeRef);
    }

    @Test
    public void testGetMembersByRole() throws Exception {
        caseService.setupPermissionGroups(temporaryCaseNodeRef,
                caseService.getCaseId(temporaryCaseNodeRef));
        caseService.removeAuthorityFromRole(ADMIN_USER_NAME,
                "CaseSimpleReader", temporaryCaseNodeRef);
        caseService.addAuthorityToRole(ADMIN_USER_NAME,
                "CaseSimpleReader", temporaryCaseNodeRef);
        Map<String, Set<String>> membersByRole = caseService.getMembersByRole(temporaryCaseNodeRef, true, false);
        assertTrue(membersByRole.get("CaseSimpleReader").contains(ADMIN_USER_NAME));
        caseService.removeAuthorityFromRole(ADMIN_USER_NAME,
                "CaseSimpleReader", temporaryCaseNodeRef);
        membersByRole = caseService.getMembersByRole(temporaryCaseNodeRef, true, false);
        assertFalse(membersByRole.get("CaseSimpleReader").contains(ADMIN_USER_NAME));
    }

    @Test
    public void testGetAllMembersByRole() throws Exception {
        caseService.setupPermissionGroups(temporaryCaseNodeRef, caseService.getCaseId(temporaryCaseNodeRef));
        caseService.removeAuthorityFromRole(ADMIN_USER_NAME, "CaseSimpleReader", temporaryCaseNodeRef);
        caseService.addAuthorityToRole(ADMIN_USER_NAME, "CaseSimpleReader", temporaryCaseNodeRef);
        caseService.addAuthorityToRole(USER_NAME_1, "CaseOwners", temporaryCaseNodeRef);
        caseService.addAuthorityToRole(USER_NAME_2, "CaseSimpleWriter", temporaryCaseNodeRef);

        Map<String, Set<String>> membersByRole = caseService.getMembersByRole(temporaryCaseNodeRef, false, false);
        //check everyone's permissions
        assertTrue(membersByRole.get("CaseSimpleReader").contains(ADMIN_USER_NAME));
        assertTrue(membersByRole.get("CaseOwners").contains(USER_NAME_1));
        assertTrue(membersByRole.get("CaseSimpleWriter").contains(USER_NAME_2));
        //remove 2 out of 3 from groups
        caseService.removeAuthorityFromRole(ADMIN_USER_NAME, "CaseSimpleReader", temporaryCaseNodeRef);
        caseService.removeAuthorityFromRole(USER_NAME_2, "CaseSimpleWriter", temporaryCaseNodeRef);

        //retrieve and test role memeberships
        membersByRole = caseService.getMembersByRole(temporaryCaseNodeRef, false, false);
        assertFalse(membersByRole.get("CaseSimpleReader").contains(ADMIN_USER_NAME));
        assertFalse(membersByRole.get("CaseSimpleWriter").contains(USER_NAME_2));
        assertTrue(membersByRole.get("CaseOwners").contains(USER_NAME_1));
    }

    @Test
    public void testJournalize() throws Exception {
        AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER_NAME);

        // TODO: Use real journal key categories from bootstrapped XML file
        // Create a test journal key category
        String categoryName = "Test Journal Key";
        String rootCategoryName = "journalKeys";
        Collection<ChildAssociationRef> rootCategories = categoryService.getRootCategories(
                repositoryHelper.getCompanyHome().getStoreRef(),
                ContentModel.ASPECT_GEN_CLASSIFIABLE,
                rootCategoryName, true);
        NodeRef rootCategory = rootCategories.iterator().next().getChildRef();
        NodeRef journalKey = rootCategory;
//        ChildAssociationRef categoryAssoc = categoryService.getCategory(rootCategory,
//                ContentModel.ASPECT_GEN_CLASSIFIABLE, categoryName);
//        if (categoryAssoc != null) {
//            journalKey = categoryAssoc.getChildRef();
//        } else {
//            journalKey = categoryService.createCategory(rootCategory, categoryName);
//        }

        assertFalse("Case node has journalized aspect although it is not " +
                "journalized", nodeService.hasAspect
                (behaviourOnCaseNodeRef,
                        OpenESDHModel.ASPECT_OE_JOURNALIZED));
        assertFalse("Case isJournalized returns true for an unjournalized " +
                "case", caseService.isJournalized(behaviourOnCaseNodeRef));

        final String originalTitle = (String) nodeService.getProperty(behaviourOnCaseNodeRef,
                ContentModel.PROP_TITLE);

        AuthenticationUtil.setFullyAuthenticatedUser(CaseHelper.DEFAULT_USERNAME);

        assertFalse("Case should not be journalized when initially created",
                caseService.isJournalized(behaviourOnCaseNodeRef));

        try {
            caseService.unJournalize(behaviourOnCaseNodeRef);
            fail("Should not be able to unjournalize an unjournalized case");
        } catch (Exception e) {
        }

        caseService.journalize(behaviourOnCaseNodeRef, journalKey);

        // Test that journalized properties got set
        assertTrue("Case isJournalized returns false for a journalized " +
                "case", caseService.isJournalized(behaviourOnCaseNodeRef));
        assertTrue("Case node does not have journalized aspect after it has " +
                "been journalized", nodeService.hasAspect
                (behaviourOnCaseNodeRef,
                        OpenESDHModel.ASPECT_OE_JOURNALIZED));
        assertEquals("Case journalizedBy is not set correctly",
                nodeService.getProperty(behaviourOnCaseNodeRef,
                        OpenESDHModel.PROP_OE_JOURNALIZED_BY),
                AuthenticationUtil.getFullyAuthenticatedUser());
        assertEquals("Case journalKey is not set correctly",
                nodeService.getProperty(behaviourOnCaseNodeRef,
                        OpenESDHModel.PROP_OE_JOURNALKEY),
                journalKey);

        // Test that the owner cannot write to a journalized case
        try {
            nodeService.setProperty(behaviourOnCaseNodeRef,
                    ContentModel.PROP_TITLE, "new title");
            fail("A property could be updated on a journalized case");
        } catch (Exception e) {
        }

        try {
            // Test that a document cannot be added to a journalized case
            NodeRef doc = createDocument(caseService.getDocumentsFolder
                    (behaviourOnCaseNodeRef), "testdoc");
            fail("A document could be added to a journalized case");
        } catch (Exception e) {
        }

        // Test that the owner cannot change permissions on the case
        try {
            caseService.removeAuthorityFromRole(CaseHelper.DEFAULT_USERNAME,
                    "CaseOwners", behaviourOnCaseNodeRef);
            fail("An authority could be removed from a role on a journalized case");
        } catch (Exception e) {
        }

        // Test that the owner cannot add an authority to a role on the case
        try {
            caseService.addAuthorityToRole("admin",
                    "CaseSimpleReader", behaviourOnCaseNodeRef);
            fail("An authority could be added to a role on a journalized case");
        } catch (Exception e) {
        }

        // Test that a user can still read from the journalized case
        assertEquals(nodeService.getProperty(behaviourOnCaseNodeRef,
                ContentModel.PROP_TITLE), originalTitle);

        assertTrue(caseService.isJournalized(behaviourOnCaseNodeRef));

        // Test that a case cannot be journalized twice
        try {
            caseService.journalize(behaviourOnCaseNodeRef, journalKey);
            fail("Should not be able to journalize a journalized case");
        } catch (Exception e) {
        }

        try {
            caseService.unJournalize(behaviourOnCaseNodeRef);
            fail("Should not be able to unjournalizea case as a regular " +
                    "user");
        } catch (Exception e) {
        }


        AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER_NAME);
        caseService.unJournalize(behaviourOnCaseNodeRef);

        AuthenticationUtil.setFullyAuthenticatedUser(CaseHelper.DEFAULT_USERNAME);

        assertFalse("Case isJournalized returns true for an unjournalized " +
                "case", caseService.isJournalized(behaviourOnCaseNodeRef));

        // Test that a user can write again: these would throw exceptions if
        // they failed.
        nodeService.setProperty(behaviourOnCaseNodeRef,
                ContentModel.PROP_TITLE, "new title");
        nodeService.setProperty(behaviourOnCaseNodeRef,
                ContentModel.PROP_TITLE, originalTitle);

        assertFalse("Case node has journalized aspect after being " +
                "unjournalized", nodeService.hasAspect(behaviourOnCaseNodeRef,
                OpenESDHModel.ASPECT_OE_JOURNALIZED));

        AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER_NAME);

        // Delete test journal key categories
//        categoryService.deleteCategory(journalKey);
        categoryService.deleteCategory(rootCategory);
    }

    private NodeRef createDocument(NodeRef parent, String name) {
        Map<QName, Serializable> properties = new HashMap<>();
        properties.put(ContentModel.PROP_NAME, name);
        return nodeService.createNode(
                caseService.getDocumentsFolder(behaviourOnCaseNodeRef),
                ContentModel.ASSOC_CONTAINS, QName.createQName
                        (OpenESDHModel.CASE_URI, name),
                ContentModel.TYPE_CONTENT, properties).getChildRef();
    }

    @Test
    public void testSetupCase() throws Exception {
        /*
        // Used for removing aspects, in the absence of the javascript console.
        SearchParameters sp = new SearchParameters();
        sp.addStore(repositoryHelper.getCompanyHome().getStoreRef());
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery("TYPE:\"case:base\"");
        ResultSet rs = searchService.query(sp);
        try {
            List<NodeRef> cases = rs.getNodeRefs();
            for (int i = 0; i < cases.size(); i++) {
                NodeRef nodeRef = cases.get(i);
                nodeService.removeAspect(nodeRef, ContentModel.ASPECT_UNDELETABLE);
            }
        }
        finally {
            rs.close();
        }
        */
    }
}