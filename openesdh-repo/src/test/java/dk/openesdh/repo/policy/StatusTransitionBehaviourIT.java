package dk.openesdh.repo.policy;

import com.tradeshift.test.remote.Remote;
import com.tradeshift.test.remote.RemoteTestRunner;
import dk.openesdh.repo.helper.CaseHelper;
import dk.openesdh.repo.model.CaseStatus;
import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.cases.CaseService;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.transaction.TransactionService;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

@RunWith(RemoteTestRunner.class)
@Remote(runnerClass = SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:alfresco/application-context.xml",
        "classpath:alfresco/extension/openesdh-test-context.xml"})
public class StatusTransitionBehaviourIT {

    private static final String TEST_CASE_NAME1 = "TestCase1";

    @Autowired
    @Qualifier("NodeService")
    protected NodeService nodeService;

    @Autowired
    @Qualifier("retryingTransactionHelper")
    protected RetryingTransactionHelper retryingTransactionHelper;

    @Autowired
    @Qualifier("CaseService")
    protected CaseService caseService;

    @Autowired
    @Qualifier("TransactionService")
    protected TransactionService transactionService;

    @Autowired
    @Qualifier("TestCaseHelper")
    protected CaseHelper caseHelper;

    private NodeRef owner;
    private NodeRef caseNodeRef;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        owner = caseHelper.createDummyUser();
        caseNodeRef = caseHelper.createSimpleCase(TEST_CASE_NAME1,
                CaseHelper.ADMIN_USER_NAME,
                owner);
    }

    @After
    public void tearDown() throws Exception {
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Boolean>() {

            public Boolean execute() throws Throwable {
                if (owner != null) {
                    caseHelper.deleteDummyUser();
                }
                if (caseNodeRef != null) {
                    nodeService.deleteNode(caseNodeRef);
                }
                return true;
            }
        });
    }

    public void testTransition(String newStatus) {
        performTransition(newStatus);
        assertEquals(caseService.getStatus(caseNodeRef), newStatus);
    }

    private void performTransition(String newStatus) {
        nodeService.setProperty(caseNodeRef, OpenESDHModel.PROP_OE_STATUS, newStatus);
    }

    @Test
    public void testValidTransitions() {
        assertEquals("Case is initially active", caseService.getStatus(caseNodeRef), CaseStatus.ACTIVE);
        testTransition(CaseStatus.ACTIVE);
        testTransition(CaseStatus.PASSIVE);
        testTransition(CaseStatus.ACTIVE);
        testTransition(CaseStatus.CLOSED);
        testTransition(CaseStatus.ACTIVE);
        testTransition(CaseStatus.CLOSED);
        testTransition(CaseStatus.PASSIVE);
        testTransition(CaseStatus.CLOSED);
    }

    @Test
    public void testInvalidTransition1() {
        testTransition(CaseStatus.ACTIVE);
        thrown.expect(AlfrescoRuntimeException.class);
        performTransition(CaseStatus.ARCHIVED);
    }

    @Test
    public void testInvalidTransition2() {
        testTransition(CaseStatus.PASSIVE);
        thrown.expect(AlfrescoRuntimeException.class);
        performTransition(CaseStatus.ARCHIVED);
    }

    @Test
    public void testInvalidTransition3() {
        testTransition(CaseStatus.ARCHIVED);
        thrown.expect(AlfrescoRuntimeException.class);
        performTransition(CaseStatus.ACTIVE);
    }

    @Test
    public void testInvalidTransition4() {
        testTransition(CaseStatus.ARCHIVED);
        thrown.expect(AlfrescoRuntimeException.class);
        performTransition(CaseStatus.PASSIVE);
    }

    @Test
    public void testInvalidTransition5() {
        testTransition(CaseStatus.ARCHIVED);
        thrown.expect(AlfrescoRuntimeException.class);
        performTransition(CaseStatus.CLOSED);
    }
}