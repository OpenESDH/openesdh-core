package dk.openesdh.repo.services;

import com.tradeshift.test.remote.Remote;
import com.tradeshift.test.remote.RemoteTestRunner;
import dk.openesdh.repo.helper.CaseHelper;
import dk.openesdh.repo.model.OpenESDHModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.namespace.DynamicNamespacePrefixResolver;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ApplicationContextHelper;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
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
    @Qualifier("PermissionService")
    protected PermissionService permissionService;

    @Autowired
    @Qualifier("repositoryHelper")
    protected Repository repositoryHelper;

    @Autowired
    @Qualifier("retryingTransactionHelper")
    protected RetryingTransactionHelper retryingTransactionHelper;

    private CaseServiceImpl caseService = null;
    private DynamicNamespacePrefixResolver namespacePrefixResolver = new DynamicNamespacePrefixResolver(null);
    private NodeRef temporaryRepoNodeRef;
    private NodeRef temporaryCaseNodeRef;

    @Before
    public void setUp() throws Exception {

        // TODO: All of this could have been done only once
        AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER_NAME);

        caseService = new CaseServiceImpl();
        caseService.setNodeService(nodeService);
        caseService.setSearchService(searchService);
        caseService.setAuthorityService(authorityService);
        caseService.setPermissionService(permissionService);
        caseService.setRepositoryHelper(repositoryHelper);

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
            owners.add(repositoryHelper.getPerson());
            temporaryCaseNodeRef = CaseHelper.createCase(nodeService,
                    retryingTransactionHelper,
                    ADMIN_USER_NAME, temporaryRepoNodeRef, name,
                    OpenESDHModel.TYPE_CASE_SIMPLE, properties, owners, true);
        }
    }

    @After
    public void tearDown() throws Exception {
        // Remove temporary node, and all its content, also removes testcase
        nodeService.deleteNode(temporaryRepoNodeRef);
//        policyBehaviourFilter.enableBehaviour();
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
        testCaseId.append(String.format("%020d", uniqueNumber));
        assertTrue("CaseId is not on the form yyyyMMdd-xxxxxxxxxxxxxxxxxxxx", testCaseId.toString().equals(caseId));
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
    public void testSetupCaseOwners() throws Exception {
        long uniqueNumber = 1231L;
        String caseId = caseService.getCaseId(uniqueNumber);
        caseService.setupOwnersPermissionGroup(temporaryCaseNodeRef, caseId);

        String groupSuffix = "case_" + caseId + "_CaseOwners";
        String groupName = authorityService.getName(AuthorityType.GROUP, groupSuffix);
        assertNotNull("No reader group created", groupName);

        if (groupName != null) {
            authorityService.deleteAuthority(groupName);
        }
    }


    @Test
    public void testAddOwnersToPermissionGroup() throws Exception {
        long uniqueNumber = 1231L;
        String caseId = caseService.getCaseId(uniqueNumber);
        String ownersPermissionGroupName = caseService.setupOwnersPermissionGroup(temporaryCaseNodeRef, caseId);

        caseService.addOwnersToPermissionGroup(temporaryCaseNodeRef, ownersPermissionGroupName);

        String groupName = authorityService.getName(AuthorityType.GROUP, "case_" + caseId + "_CaseOwners");

        // Hack to see if 'admin' was added to the ownergroup. The exception means it already exists, and all is well
        try {
            authorityService.addAuthority(groupName, "admin");
            assertNotNull("Owner was not added to correct owner group", null);
        }
        catch (Exception e) {
        }

        if (groupName != null) {
            authorityService.deleteAuthority(groupName);
        }


    }

    @Test
    public void testGetCaseFolderNodeRef() throws Exception {
        Calendar c = Calendar.getInstance();

        NodeRef caseFolderNodeRef = caseService.getCaseFolderNodeRef(temporaryRepoNodeRef);
        int name = Integer.parseInt((String) nodeService.getProperty(caseFolderNodeRef, ContentModel.PROP_NAME));
        assertTrue("Day not correct",  name == c.get(Calendar.DATE));

        NodeRef parentRef = nodeService.getPrimaryParent(caseFolderNodeRef).getParentRef();
        name = Integer.parseInt((String)nodeService.getProperty(parentRef, ContentModel.PROP_NAME));
        assertTrue("Month not correct", name == c.get(Calendar.MONTH) + 1);

        parentRef = nodeService.getPrimaryParent(parentRef).getParentRef();
        name = Integer.parseInt((String)nodeService.getProperty(parentRef, ContentModel.PROP_NAME));
        assertTrue("Month not correct", name == c.get(Calendar.YEAR));


        String path = nodeService.getPath(caseFolderNodeRef).toPrefixString(namespacePrefixResolver);
        System.out.println(path);
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