package dk.openesdh.repo.services.xsearch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.tradeshift.test.remote.Remote;
import com.tradeshift.test.remote.RemoteTestRunner;

import dk.openesdh.repo.helper.CaseHelper;
import dk.openesdh.repo.services.TransactionRunner;
import dk.openesdh.repo.services.members.CaseMembersService;

/**
 * Created by flemming on 18/08/14.
 */
@RunWith(RemoteTestRunner.class)
@Remote(runnerClass = SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:alfresco/application-context.xml")
public class UserInvolvedSearchServiceImplIT {

    @Autowired
    @Qualifier("NodeService")
    private NodeService nodeService;

    @Autowired
    @Qualifier("TestCaseHelper")
    private CaseHelper caseHelper;

    @Autowired
    private TransactionRunner transactionRunner;

    @Autowired
    @Qualifier("CaseMembersService")
    private CaseMembersService caseMembersService;

    @Autowired
    @Qualifier("UserInvolvedSearchService")
    private UserInvolvedSearchServiceImpl userInvolvedSearchService;

    private static final String CASE_A_TITLE = "caseA";
    private NodeRef caseA;
    private NodeRef owner;

    @Before
    public void setUp() throws Exception {
        AuthenticationUtil.setFullyAuthenticatedUser(CaseHelper.ADMIN_USER_NAME);
        owner = caseHelper.createDummyUser();
        caseA = caseHelper.createSimpleCase(CASE_A_TITLE, owner);
        transactionRunner.runInTransaction(() -> {
            caseMembersService.addAuthorityToRole(owner, "CaseSimpleReader", caseA);
            return null;
        });
        //NOTICE: Should really wait for index to pick up changes (but this would effectively re-implements the tests below).
    }

    @Test
    public void testGetCaseGroupsNodedbid() {
        Set<String> caseGroupsNodeids = userInvolvedSearchService.getCaseGroupsNodedbid(CaseHelper.DEFAULT_USERNAME);
        Long caseAnodedbid = (Long) nodeService.getProperty(caseA, ContentModel.PROP_NODE_DBID);
        // is the user member of a group as expected
        assertTrue(caseGroupsNodeids.contains(caseAnodedbid.toString()));
    }

    @Test
    public void testGetNodes() throws InterruptedException {
        Map<String, String> params = new HashMap();
        params.put("user", CaseHelper.DEFAULT_USERNAME);
        params.put("filter", "");
        params.put("baseType", "");

        XResultSet nodes = null;
        int sleepCount = 0;
        int maxSleepCount = 120;
        //wait for the search index to pick up changes.
        do {
            nodes = userInvolvedSearchService.getNodes(params, 0, 100, "", true);
            if (nodes.getLength() == 0) {
                sleepCount++;
                if (sleepCount > maxSleepCount) {
                    break;
                }
                Thread.sleep(1000);
            }
        } while (nodes.getLength() == 0);
        // we expect there to be only one - caseA
        List<NodeRef> list = nodes.getNodeRefs();
        NodeRef nodeRef = list.get(0);

        assertEquals(CASE_A_TITLE, nodeService.getProperty(nodeRef, ContentModel.PROP_TITLE));
    }

    @After
    public void tearDown() throws Exception {
        transactionRunner.runInTransactionAsAdmin(() -> {
            caseHelper.deleteDummyUser();
            nodeService.deleteNode(caseA);
            return true;
        });
    }
}
