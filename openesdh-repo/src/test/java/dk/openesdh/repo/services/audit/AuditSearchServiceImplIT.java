package dk.openesdh.repo.services.audit;

import com.tradeshift.test.remote.Remote;
import com.tradeshift.test.remote.RemoteTestRunner;
import dk.openesdh.repo.helper.CaseHelper;
import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.xsearch.LastModifiedByMeSearchServiceImpl;
import dk.openesdh.repo.services.xsearch.XResultSet;
import org.json.simple.JSONArray;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.nodelocator.NodeLocatorService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.audit.AuditService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.*;
import org.alfresco.service.transaction.TransactionService;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


/**
 * Created by flemming on 18/08/14.
 */

@RunWith(RemoteTestRunner.class)
@Remote(runnerClass = SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:alfresco/application-context.xml")
@Ignore
public class AuditSearchServiceImplIT {

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
    @Qualifier("dictionaryService")
    protected DictionaryService dictionaryService;

    @Autowired
    @Qualifier("auditService")
    protected AuditService auditService;


    private AuditSearchServiceImpl auditSearchService = null;

    private String caseATitle = "caseA";
    private NodeRef caseA;
    private NodeRef owner;

    private Map<String, Boolean> validKeys;


    @Before
    public void setUp() throws Exception {

        validKeys = new HashMap<String, Boolean>();
        validKeys.put("/esdh/transaction/action=CREATE", true);
        validKeys.put("/esdh/transaction/action=DELETE", true);
        validKeys.put("/esdh/transaction/action=CHECK IN", false);

        auditSearchService = new AuditSearchServiceImpl(validKeys);
        auditSearchService.setAuditService(auditService);


        owner = caseHelper.createDummyUser();
        System.out.println("test");
        caseA = caseHelper.createSimpleCase(caseATitle,
                CaseHelper.ADMIN_USER_NAME,
                owner);

//        String DATE_FORMAT = "yyyyMMdd";
//        DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
//        Date date = new Date();
//        String d = dateFormat.format(date);
//        Long caseAnodedbid = (Long) nodeService.getProperty(caseA, ContentModel.PROP_NODE_DBID);
//        final String adminGroup = authorityService.getName(AuthorityType.GROUP, "case_" + d + "-" + caseAnodedbid + "_CaseSimpleReader");
//
//
//        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Boolean>() {
//
//            public Boolean execute() throws Throwable {
//
//                authorityService.addAuthority(adminGroup, caseHelper.DEFAULT_USERNAME);
//
//                return true;
//            }
//        });
       // AuthenticationUtil.setFullyAuthenticatedUser(CaseHelper.ADMIN_USER_NAME);
//
    }

    @Test
    public void testAuditLog() throws Exception {


        System.out.println("nodeRef:" + caseA);
        JSONArray result = auditSearchService.getAuditLogByCaseNodeRef(caseA,1000);

        System.out.println(result.toJSONString());




//        assertEquals("Get parent case of case documents folder is correct",
//                behaviourOnCaseNodeRef, caseService.getParentCase(documentsFolder));
//
//        assertNull("Get parent case of non-case node is null",
//                caseService.getParentCase(caseService.getCasesRootNodeRef()));
//
//        assertEquals("Get parent case of case node is case node",
//                behaviourOnCaseNodeRef, caseService.getParentCase(behaviourOnCaseNodeRef));
    }

    private void getCreatedDateFromAuditLog() {

    }

    private void getCreatedByFromAuditLog() {

    }

    private void getDeleteDateFromAuditLog(){

    }








    @After
    public void tearDown() throws Exception {

        AuthenticationUtil.setFullyAuthenticatedUser(CaseHelper.ADMIN_USER_NAME);

        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Boolean>() {

            public Boolean execute() throws Throwable {

                caseHelper.deleteDummyUser();
                System.out.println("damn: " + caseA);
                nodeService.deleteNode(caseA);
                System.out.println("damn2");
                return true;
            }
        });
    }
}
