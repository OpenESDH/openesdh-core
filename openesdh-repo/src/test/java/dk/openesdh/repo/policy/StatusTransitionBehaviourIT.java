package dk.openesdh.repo.policy;

import static org.junit.Assert.assertEquals;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.junit.After;
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

import dk.openesdh.repo.helper.CaseHelper;
import dk.openesdh.repo.services.TransactionRunner;
import dk.openesdh.repo.model.CaseStatus;
import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.cases.CaseService;

import static org.junit.Assert.assertEquals;

@RunWith(RemoteTestRunner.class)
@Remote(runnerClass = SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:alfresco/application-context.xml",
    "classpath:alfresco/extension/openesdh-test-context.xml"})
public class StatusTransitionBehaviourIT {

    private static final String TEST_CASE_NAME1 = "TestCase1";

    @Autowired
    @Qualifier("NodeService")
    private NodeService nodeService;

    @Autowired
    @Qualifier("CaseService")
    private CaseService caseService;

    @Autowired
    private TransactionRunner transactionRunner;

    @Autowired
    @Qualifier("TestCaseHelper")
    private CaseHelper caseHelper;

    private NodeRef owner;
    private NodeRef caseNodeRef;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        owner = caseHelper.createDummyUser();
        caseNodeRef = caseHelper.createSimpleCase(TEST_CASE_NAME1, owner);
    }

    @After
    public void tearDown() throws Exception {
        transactionRunner.runInTransactionAsAdmin(() -> {
            if (owner != null) {
                caseHelper.deleteDummyUser();
            }
            if (caseNodeRef != null) {
                nodeService.deleteNode(caseNodeRef);
            }
            return true;
        });
    }

    @Test
    public void testSetToSameStatus() {
        assertEquals("Case is initially active", caseService.getNodeStatus(caseNodeRef), CaseStatus.ACTIVE);

        // This should be ok.
        nodeService.setProperty(caseNodeRef, OpenESDHModel.PROP_OE_STATUS,
                CaseStatus.ACTIVE);
    }

    @Test
    public void testChangeStatusDirectly() {
        assertEquals("Case is initially active", caseService.getNodeStatus(caseNodeRef), CaseStatus.ACTIVE);

        // Expect an exception to be thrown because one should not be
        // allowed to directly change the status.
        thrown.expect(AlfrescoRuntimeException.class);
        nodeService.setProperty(caseNodeRef, OpenESDHModel.PROP_OE_STATUS,
                CaseStatus.PASSIVE);
    }
}
