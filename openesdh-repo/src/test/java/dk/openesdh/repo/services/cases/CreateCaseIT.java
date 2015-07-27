package dk.openesdh.repo.services.cases;

import static org.alfresco.repo.security.authentication.AuthenticationUtil.getAdminUserName;
import static org.alfresco.repo.security.authentication.AuthenticationUtil.runAs;
import static org.alfresco.repo.security.authentication.AuthenticationUtil.setFullyAuthenticatedUser;
import static org.hamcrest.core.Is.isA;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.tradeshift.test.remote.Remote;
import com.tradeshift.test.remote.RemoteTestRunner;

import dk.openesdh.repo.helper.CaseHelper;
import dk.openesdh.repo.model.OpenESDHModel;

/**
 * Created by rasmutor on 6/30/15.
 */
@RunWith(RemoteTestRunner.class)
@Remote(runnerClass = SpringJUnit4ClassRunner.class)
@ContextConfiguration({
        "classpath:alfresco/application-context.xml",
        "/test-context.xml"
})
public class CreateCaseIT {

    @Autowired
    @Qualifier("repositoryHelper")
    private Repository repository;

    @Autowired
    @Qualifier("NodeService")
    private NodeService nodeService;

    @Autowired
    @Qualifier("CaseService")
    private CaseService caseService;

    @Autowired
    @Qualifier("personService")
    protected PersonService personService;

    @Autowired
    @Qualifier("retryingTransactionHelper")
    protected RetryingTransactionHelper retryingTransactionHelper;

    @Autowired
    private AuthorityService authorityService;

    private static final String CREATOR_ROLE = "CaseTestCreator";
    private static final String READER_ROLE = "CaseTestReader";
    private static final String WRITER_ROLE = "CaseTestWriter";

    @Autowired
    @Qualifier("TestCaseHelper")
    private CaseHelper caseHelper;

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void getCaseFolderShouldCreateTheYearMonthDayFolderHierarchy() throws Exception {
        setFullyAuthenticatedUser(getAdminUserName());
        NodeRef casesRootNode = retryingTransactionHelper.doInTransaction(() -> {
            NodeRef root = caseService.getCasesRootNodeRef();
            assertNotNull(root);
            NodeRef c = caseService.getCaseFolderNodeRef(root);
            assertNotNull(c);
            return root;
        });

        LocalDate today = LocalDate.now();
        NodeRef year = nodeService.getChildByName(casesRootNode, ContentModel.ASSOC_CONTAINS, Integer.toString(today.getYear()));
        assertNotNull(year);
        NodeRef month = nodeService.getChildByName(year, ContentModel.ASSOC_CONTAINS, Integer.toString(today.getMonthValue()));
        assertNotNull(month);
        NodeRef day = nodeService.getChildByName(month, ContentModel.ASSOC_CONTAINS, Integer.toString(today.getDayOfMonth()));
        assertNotNull(day);
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void shouldFailCausedByMissingPermissions() throws Exception {
        expectedException.expectCause(isA(AccessDeniedException.class));

        setFullyAuthenticatedUser(getAdminUserName());

        retryingTransactionHelper.doInTransaction(() -> {
            authorityService.removeAuthority(PermissionService.GROUP_PREFIX + CREATOR_ROLE, getAdminUserName());
            final String name = UUID.randomUUID().toString();
            final Map<QName, Serializable> properties = new HashMap<>();

            List<NodeRef> owners = Arrays.asList(personService.getPerson(getAdminUserName()));

            caseHelper.createCase(getAdminUserName(), repository.getCompanyHome(), name, OpenESDHModel.TYPE_CASE_BASE, properties, owners);
            return null;
        });
    }

    @Test
    public void aCreatedCaseNodeShallHaveAnIdPropertyAndADocumentsFolder() throws Exception {
        setFullyAuthenticatedUser(getAdminUserName());

        NodeRef caseNode = retryingTransactionHelper.doInTransaction(() -> {
            giveUserCreateAccess(getAdminUserName());

            final String name = UUID.randomUUID().toString();
            final Map<QName, Serializable> properties = new HashMap<>();

            List<NodeRef> owners = Arrays.asList(personService.getPerson(getAdminUserName()));

            return caseHelper.createCase(getAdminUserName(), repository.getCompanyHome(), name, OpenESDHModel.TYPE_CASE_BASE, properties, owners);
        });

        Map<QName, Serializable> childProps = nodeService.getProperties(caseNode);
        assertTrue(childProps.containsKey(OpenESDHModel.PROP_OE_ID));

        NodeRef documentsFolder = nodeService.getChildByName(caseNode, ContentModel.ASSOC_CONTAINS, OpenESDHModel.DOCUMENTS_FOLDER_NAME);
        assertNotNull(documentsFolder);
        Set<QName> aspects = nodeService.getAspects(documentsFolder);
        assertTrue(aspects.contains(OpenESDHModel.ASPECT_DOCUMENT_CONTAINER));
    }

    @Test
    public void testCreateCaseAsOrdinaryUser() throws Exception {
        String userName = "abeecher";
        enableTestUser(userName);

        giveUserCreateAccess(userName);
        giveUserReadAccess(userName);

        setFullyAuthenticatedUser(userName);

        NodeRef caseNode = retryingTransactionHelper.doInTransaction(() -> {
            final String name = UUID.randomUUID().toString();
            final Map<QName, Serializable> properties = new HashMap<>();

            NodeRef personNode = personService.getPerson(userName);

            List<NodeRef> owners = Arrays.asList(personNode);
            NodeRef homeFolder = (NodeRef) nodeService.getProperty(personNode, ContentModel.PROP_HOMEFOLDER);

            return caseHelper.createCase(userName, homeFolder, name, OpenESDHModel.TYPE_CASE_BASE, properties, owners);
        });

        String id = (String) nodeService.getProperty(caseNode, OpenESDHModel.PROP_OE_ID);

        NodeRef caseFolderNodeRef = caseService.getCaseFolderNodeRef(caseService.getCasesRootNodeRef());
        NodeRef caseFolder = nodeService.getChildByName(caseFolderNodeRef, ContentModel.ASSOC_CONTAINS, id);
        assertNotNull(caseFolder);
        assertTrue(caseService.isCaseNode(caseFolder));
    }

    @Test
    public void givenWriteAccessANormalUserCanChangeCaseStatus() throws Exception {
        String testUser = "abeecher";
        enableTestUser(testUser);
        giveUserWriteAccess(testUser);
        giveUserCreateAccess(getAdminUserName());

        NodeRef personNode = personService.getPerson(getAdminUserName());
        List<NodeRef> owners = Arrays.asList(personNode);
        final Map<QName, Serializable> properties = new HashMap<>();
        NodeRef caseNode = caseHelper.createCase(getAdminUserName(), repository.getCompanyHome(), "my test case", OpenESDHModel.TYPE_CASE_BASE, properties, owners);

        setFullyAuthenticatedUser(testUser);

        nodeService.setProperty(caseNode, OpenESDHModel.PROP_OE_STATUS, "pending");
    }

    private void enableTestUser(String userName) {
        retryingTransactionHelper.doInTransaction(() -> {
            Map<QName,Serializable> properties = new HashMap<>();
            properties.put(ContentModel.PROP_ENABLED, true);
            personService.setPersonProperties(userName, properties, true);
            return null;
        });
    }

    private void giveUserReadAccess(final String userName) {
        runInTransactionAsAdmin(() -> {
            if (!authorityService.authorityExists(PermissionService.GROUP_PREFIX + READER_ROLE)) {
                authorityService.createAuthority(AuthorityType.GROUP, READER_ROLE);
            }
            if (!authorityService.getAuthoritiesForUser(userName).contains(PermissionService.GROUP_PREFIX + READER_ROLE)) {
                authorityService.addAuthority(PermissionService.GROUP_PREFIX + READER_ROLE, userName);
            }
            return null;
        });
    }

    private void giveUserWriteAccess(final String userName) {
        runInTransactionAsAdmin(() -> {
            if (!authorityService.authorityExists(PermissionService.GROUP_PREFIX + WRITER_ROLE)) {
                authorityService.createAuthority(AuthorityType.GROUP, WRITER_ROLE);
            }
            if (!authorityService.getAuthoritiesForUser(userName).contains(PermissionService.GROUP_PREFIX + WRITER_ROLE)) {
                authorityService.addAuthority(PermissionService.GROUP_PREFIX + WRITER_ROLE, userName);
            }
            return null;
        });
    }

    private void giveUserCreateAccess(final String userName) {
        runInTransactionAsAdmin(() -> {
            if (!authorityService.authorityExists(PermissionService.GROUP_PREFIX + CREATOR_ROLE)) {
                authorityService.createAuthority(AuthorityType.GROUP, CREATOR_ROLE);
            }
            if (!authorityService.getAuthoritiesForUser(userName).contains(PermissionService.GROUP_PREFIX + CREATOR_ROLE)) {
                authorityService.addAuthority(PermissionService.GROUP_PREFIX + CREATOR_ROLE, userName);
            }
            return null;
        });
    }

    private <R> R runInTransactionAsAdmin(RetryingTransactionCallback<R> callBack) {
        return runAsAdmin(() -> retryingTransactionHelper.doInTransaction(callBack));
    }

    private <R> R runAsAdmin(RunAsWork<R> callback) {
        return runAs(callback, getAdminUserName());
    }
}
