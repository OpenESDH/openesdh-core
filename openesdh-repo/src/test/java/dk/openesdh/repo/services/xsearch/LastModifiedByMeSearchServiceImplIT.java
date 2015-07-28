package dk.openesdh.repo.services.xsearch;

import com.tradeshift.test.remote.Remote;
import com.tradeshift.test.remote.RemoteTestRunner;
import dk.openesdh.repo.helper.CaseHelper;
import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.cases.CaseService;
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


/**
 * Created by flemming on 18/08/14.
 */

@RunWith(RemoteTestRunner.class)
@Remote(runnerClass = SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:alfresco/application-context.xml")
public class LastModifiedByMeSearchServiceImplIT {

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

    @Autowired
    @Qualifier("CaseService")
    protected CaseService caseService;



    private PermissionService permissionService;


    @Autowired
    @Qualifier("LastModifiedByMeSearchService")
    protected LastModifiedByMeSearchServiceImpl lastModifiedByMeSearchService;
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

        caseService.addAuthorityToRole(owner, "CaseSimpleReader", caseA);
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

    //@Test
    public void testGetNodes() {

        Map<String, String> params = new HashMap();
        params.put("user", CaseHelper.DEFAULT_USERNAME);
        params.put("filter", "");
        params.put("baseType", "");

        XResultSet nodes = lastModifiedByMeSearchService.getNodes(params, 0, 100, "", true);

        // we expect there to be only one - caseA
        List<NodeRef> list = nodes.getNodeRefs();
        NodeRef nodeRef = list.get(0);

        // TODO extend to support if there are multiple cases returned, then check for a case with title=caseATitle

        assertEquals(caseATitle, nodeService.getProperty(nodeRef, ContentModel.PROP_TITLE));

        Long fromTime = new DateTime().toDate().getTime() + new Long(OpenESDHModel.MYCASES_DAYS_IN_THE_PAST);
        DateTimeUtils.setCurrentMillisFixed(fromTime);

        nodes = lastModifiedByMeSearchService.getNodes(params, 0, 100, "", true);

        // we expect there to be no cases
        // TODO extend to support multiple returned cases, the case with title=caseATitle should not be found in the list

        list = nodes.getNodeRefs();
        assertEquals(list.size(), 0);

        DateTimeUtils.setCurrentMillisSystem();
    }
}
