package dk.openesdh.repo.services.xsearch;

import com.tradeshift.test.remote.Remote;
import com.tradeshift.test.remote.RemoteTestRunner;
import dk.openesdh.repo.helper.CaseHelper;
import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.cases.CaseService;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.nodelocator.CompanyHomeNodeLocator;
import org.alfresco.repo.nodelocator.NodeLocatorService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.*;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.ibatis.annotations.Case;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * Created by flemming on 18/08/14.
 */

@RunWith(RemoteTestRunner.class)
@Remote(runnerClass = SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:alfresco/application-context.xml")
public class UserInvolvedSearchServiceImplIT {

    @Autowired
    @Qualifier("authorityService")
    protected AuthorityService authorityService;

    @Autowired
    @Qualifier("NodeService")
    protected NodeService nodeService;

    @Autowired
    @Qualifier("repositoryHelper")
    protected Repository repositoryHelper;

    @Autowired
    @Qualifier("SearchService")
    protected SearchService searchService;

    @Autowired
    @Qualifier("nodeLocatorService")
    protected NodeLocatorService nodeLocatorService;

    @Autowired
    @Qualifier("retryingTransactionHelper")
    protected RetryingTransactionHelper retryingTransactionHelper;


    @Autowired
    @Qualifier("authenticationService")
    protected AuthenticationService authenticationService;

    @Autowired
    @Qualifier("personService")
    protected PersonService personService;

    @Autowired
    @Qualifier("TestCaseHelper")
    protected CaseHelper caseHelper;

    @Autowired
    @Qualifier("transactionService")
    protected TransactionService transactionService;


    @Autowired
    @Qualifier("CaseService")
    protected CaseService caseService;

    @Autowired
    @Qualifier("UserInvolvedSearchService")
    protected UserInvolvedSearchServiceImpl userInvolvedSearchService;

    private String caseATitle = "caseA";
    private NodeRef caseA;
    private NodeRef owner;


    @Before
    public void setUp() throws Exception {
        AuthenticationUtil.setFullyAuthenticatedUser(CaseHelper.ADMIN_USER_NAME);
        owner = caseHelper.createDummyUser();
        caseA = caseHelper.createSimpleCase(caseATitle,
                CaseHelper.ADMIN_USER_NAME,
                owner);

        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>() {
            @Override
            public Void execute() throws Throwable {
                caseService.addAuthorityToRole(owner, "CaseSimpleReader", caseA);
                return null;
            }
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

        assertEquals(caseATitle, nodeService.getProperty(nodeRef, ContentModel.PROP_TITLE));


    }

    @After
    public void tearDown() throws Exception {

        AuthenticationUtil.setFullyAuthenticatedUser(CaseHelper.ADMIN_USER_NAME);

        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Boolean>() {

            public Boolean execute() throws Throwable {

                caseHelper.deleteDummyUser();
                nodeService.deleteNode(caseA);

                return true;
            }
        });
    }
}
