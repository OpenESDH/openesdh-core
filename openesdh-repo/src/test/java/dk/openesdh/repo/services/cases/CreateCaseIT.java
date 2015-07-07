package dk.openesdh.repo.services.cases;

import com.tradeshift.test.remote.Remote;
import com.tradeshift.test.remote.RemoteTestRunner;
import dk.openesdh.repo.model.OpenESDHModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.*;

import static org.hamcrest.core.Is.isA;

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

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void getCaseFolderShouldCreateTheYearMonthDayFolderHierarchy() throws Exception {
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        NodeRef casesRootNode = retryingTransactionHelper.doInTransaction(() -> {
            NodeRef root = caseService.getCasesRootNodeRef();
            Assert.assertNotNull(root);
            NodeRef c = caseService.getCaseFolderNodeRef(root);
            Assert.assertNotNull(c);
            return root;
        });

        LocalDate today = LocalDate.now();
        NodeRef year = nodeService.getChildByName(casesRootNode, ContentModel.ASSOC_CONTAINS, Integer.toString(today.getYear()));
        Assert.assertNotNull(year);
        NodeRef month = nodeService.getChildByName(year, ContentModel.ASSOC_CONTAINS, Integer.toString(today.getMonthValue()));
        Assert.assertNotNull(month);
        NodeRef day = nodeService.getChildByName(month, ContentModel.ASSOC_CONTAINS, Integer.toString(today.getDayOfMonth()));
        Assert.assertNotNull(day);
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void shouldFailCausedByMissingPermissions() throws Exception {
        expectedException.expectCause(isA(AccessDeniedException.class));

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        retryingTransactionHelper.doInTransaction(() -> {
            authorityService.removeAuthority(PermissionService.GROUP_PREFIX + "CaseTestCreator", AuthenticationUtil.getAdminUserName());
            final String name = UUID.randomUUID().toString();
            final Map<QName, Serializable> properties = new HashMap<>();
            properties.put(ContentModel.PROP_NAME, name);

            ChildAssociationRef childAssocRef = nodeService.createNode(
                    repository.getCompanyHome(),
                    ContentModel.ASSOC_CONTAINS,
                    QName.createQName(OpenESDHModel.CASE_URI, name),
                    OpenESDHModel.TYPE_CASE_BASE,
                    properties);
            NodeRef child = childAssocRef.getChildRef();
            List<NodeRef> owners = new ArrayList<>();
            NodeRef person = personService.getPerson(AuthenticationUtil.getAdminUserName());
            owners.add(person);
            nodeService.setAssociations(child, OpenESDHModel.ASSOC_CASE_OWNERS, owners);
            return null;
        });
    }

    @Test
    public void aCreatedCaseNodeShallHaveAnIdPropertyAndADocumentsFolder() throws Exception {
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        NodeRef caseNode = retryingTransactionHelper.doInTransaction(() -> {
            if (!authorityService.authorityExists(PermissionService.GROUP_PREFIX + "CaseTestCreator")) {
                authorityService.createAuthority(AuthorityType.GROUP, "CaseTestCreator");
            }
            if (!authorityService.getAuthoritiesForUser(AuthenticationUtil.getAdminUserName()).contains(PermissionService.GROUP_PREFIX + "CaseTestCreator")) {
                authorityService.addAuthority(PermissionService.GROUP_PREFIX + "CaseTestCreator", AuthenticationUtil.getAdminUserName());
            }

            final String name = UUID.randomUUID().toString();
            final Map<QName, Serializable> properties = new HashMap<>();
            properties.put(ContentModel.PROP_NAME, name);

            ChildAssociationRef childAssocRef = nodeService.createNode(
                    repository.getCompanyHome(),
                    ContentModel.ASSOC_CONTAINS,
                    QName.createQName(OpenESDHModel.CASE_URI, name),
                    OpenESDHModel.TYPE_CASE_BASE,
                    properties);

            NodeRef child = childAssocRef.getChildRef();
            List<NodeRef> owners = new ArrayList<>();
            NodeRef person = personService.getPerson(AuthenticationUtil.getAdminUserName());
            owners.add(person);
            nodeService.setAssociations(child, OpenESDHModel.ASSOC_CASE_OWNERS, owners);
            return child;
        });

        Map<QName, Serializable> childProps = nodeService.getProperties(caseNode);
        Assert.assertTrue(childProps.containsKey(OpenESDHModel.PROP_OE_ID));

        NodeRef documentsFolder = nodeService.getChildByName(caseNode, ContentModel.ASSOC_CONTAINS, OpenESDHModel.DOCUMENTS_FOLDER_NAME);
        Assert.assertNotNull(documentsFolder);
        Set<QName> aspects = nodeService.getAspects(documentsFolder);
        Assert.assertTrue(aspects.contains(OpenESDHModel.ASPECT_DOCUMENT_CONTAINER));
    }

    @Test
    public void testCreateCaseAsOrdinaryUser() throws Exception {
        String userName = "abeecher";
        enableTestUser(userName);

        AuthenticationUtil.runAs(() -> retryingTransactionHelper.doInTransaction(() -> {
            if (!authorityService.authorityExists(PermissionService.GROUP_PREFIX + "CaseTestReader")) {
                authorityService.createAuthority(AuthorityType.GROUP, "CaseTestReader");
            }
            if (!authorityService.getAuthoritiesForUser(userName).contains(PermissionService.GROUP_PREFIX + "CaseTestReader")) {
                authorityService.addAuthority(PermissionService.GROUP_PREFIX + "CaseTestReader", userName);
            }
            return null;
        }), AuthenticationUtil.getAdminUserName());

        AuthenticationUtil.setFullyAuthenticatedUser(userName);

        NodeRef caseNode = retryingTransactionHelper.doInTransaction(() -> {
            if (!authorityService.authorityExists(PermissionService.GROUP_PREFIX + "CaseTestCreator")) {
                authorityService.createAuthority(AuthorityType.GROUP, "CaseTestCreator");
            }
            if (!authorityService.getAuthoritiesForUser(userName).contains(PermissionService.GROUP_PREFIX + "CaseTestCreator")) {
                authorityService.addAuthority(PermissionService.GROUP_PREFIX + "CaseTestCreator", userName);
            }

            final String name = UUID.randomUUID().toString();
            final Map<QName, Serializable> properties = new HashMap<>();
            properties.put(ContentModel.PROP_NAME, name);

            NodeRef personNode = personService.getPerson(userName);
            NodeRef homeFolder = (NodeRef) nodeService.getProperty(personNode, ContentModel.PROP_HOMEFOLDER);
            ChildAssociationRef childAssocRef = nodeService.createNode(
                    homeFolder,
                    ContentModel.ASSOC_CONTAINS,
                    QName.createQName(OpenESDHModel.CASE_URI, name),
                    OpenESDHModel.TYPE_CASE_BASE,
                    properties);

            NodeRef child = childAssocRef.getChildRef();
            List<NodeRef> owners = new ArrayList<>();
            NodeRef person = personService.getPerson(AuthenticationUtil.getAdminUserName());
            owners.add(person);
            nodeService.setAssociations(child, OpenESDHModel.ASSOC_CASE_OWNERS, owners);
            return child;
        });

        String id = (String) nodeService.getProperty(caseNode, OpenESDHModel.PROP_OE_ID);

        NodeRef caseFolderNodeRef = caseService.getCaseFolderNodeRef(caseService.getCasesRootNodeRef());
        NodeRef caseFolder = nodeService.getChildByName(caseFolderNodeRef, ContentModel.ASSOC_CONTAINS, id);
        Assert.assertNotNull(caseFolder);
        Assert.assertTrue(caseService.isCaseNode(caseFolder));
    }

    private void enableTestUser(String userName) {
        retryingTransactionHelper.doInTransaction(() -> {
            NodeRef person = personService.getPerson(userName);
            Map<QName,Serializable> properties = new HashMap<>();
            properties.put(ContentModel.PROP_ENABLED, true);
            personService.setPersonProperties(userName, properties, true);
            return null;
        });
    }
}
