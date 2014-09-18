package dk.openesdh.repo.services.xsearch;

import com.tradeshift.test.remote.Remote;
import com.tradeshift.test.remote.RemoteTestRunner;
import dk.openesdh.repo.helper.CaseHelper;
import dk.openesdh.repo.model.OpenESDHModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.nodelocator.CompanyHomeNodeLocator;
import org.alfresco.repo.nodelocator.NodeLocatorService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith(RemoteTestRunner.class)
@Remote(runnerClass=SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:alfresco/application-context.xml")
public class XSearchServiceImplTest {

    @Autowired
    @Qualifier("NodeService")
    protected NodeService nodeService;

    @Autowired
    @Qualifier("SearchService")
    protected SearchService searchService;

    @Autowired
    @Qualifier("repositoryHelper")
    protected Repository repositoryHelper;

    @Autowired
    @Qualifier("nodeLocatorService")
    protected NodeLocatorService nodeLocatorService;

    @Autowired
    @Qualifier("retryingTransactionHelper")
    protected RetryingTransactionHelper retryingTransactionHelper;

    private XSearchServiceImpl xSearchService = null;

    private static final String ADMIN_USER_NAME = "admin";

    private static final String baseType = "case:base";

    private NodeRef caseNode;
    private String testCaseTitle;

    @Before
    public void setUp() throws Exception {
        // TODO: All of this could have been done only once
        AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER_NAME);

        xSearchService = new XSearchServiceImpl();
        xSearchService.setRepositoryHelper(repositoryHelper);
        xSearchService.setSearchService(searchService);

        NodeRef companyHome = nodeLocatorService.getNode(CompanyHomeNodeLocator.NAME, null, null);
        String name = "My repo case (" + System.currentTimeMillis() + ")";
        String title = "My repo case (" + System.currentTimeMillis() + ")";
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
        properties.put(ContentModel.PROP_TITLE, title);
        List<NodeRef> owners = new LinkedList<>();
        owners.add(repositoryHelper.getPerson());
        caseNode = CaseHelper.createCase(nodeService,
                retryingTransactionHelper, ADMIN_USER_NAME,
                companyHome,
                name, OpenESDHModel.TYPE_CASE_SIMPLE, properties, owners);
        testCaseTitle = title;
    }

    @After
    public void tearDown() throws Exception {
        nodeService.deleteNode(caseNode);
    }

    @Test
    public void testGetNodes() throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        params.put("filters", createTestFilters().toString());
        params.put("baseType", baseType);
        XResultSet results = xSearchService.getNodes(params);
        assertEquals(1, results.getLength());
        assertEquals(caseNode, results.getNodeRefs().get(0));
    }

    protected JSONObject createFilterObject(String name, String operator,
                                   String value) throws JSONException {
        JSONObject filter = new JSONObject();
        filter.put("name", name);
        filter.put("operator", operator);
        filter.put("value", value);
        return filter;
    }

    protected JSONArray createTestFilters() throws JSONException {
        JSONArray filters = new JSONArray();
        JSONObject titleFilter = createFilterObject("cm:title", "=", testCaseTitle);
        filters.put(titleFilter);
        return filters;
    }

    @Test
    public void testBuildQuery() throws Exception {
        JSONArray filters = createTestFilters();
        xSearchService.baseType = baseType;
        String query = xSearchService.buildQuery(filters.toString());
        System.out.println(query);
        assertEquals("@cm\\:title:" + AbstractXSearchService.quote(testCaseTitle) +
                " AND " +
                "TYPE:" + AbstractXSearchService.quote(baseType), query);

        // TODO: Test other types of filters
    }

    @Test
    public void testProcessFilterValue() throws Exception {
        JSONObject filter = new JSONObject();

        // Test JSONObject containing "dateRange" property
        JSONObject dateObj = new JSONObject();
        JSONArray dateRange = new JSONArray();
        dateRange.put("");
        dateRange.put("");
        dateObj.put("dateRange", dateRange);
        filter.put("value", dateObj);

        // Test default range values
        assertEquals("[\"MIN\" TO \"MAX\"]", xSearchService.processFilterValue
                (filter));

        // Test date range
        filter = new JSONObject();
        dateObj = new JSONObject();
        dateRange = new JSONArray();
        dateRange.put("2006-07-20T00:00:00+02:00");
        dateRange.put("2007-07-20T00:00:00+02:00");
        dateObj.put("dateRange", dateRange);
        filter.put("value", dateObj);
        assertEquals("[\"2006-07-20T00:00:00\" TO \"2007-07-20T00:00:00\"]",
                xSearchService.processFilterValue
                        (filter));

        // Test JSONArray
        filter = new JSONObject();
        JSONArray arr = new JSONArray();
        arr.put("ABC");
        arr.put("123");
        filter.put("value", arr);
        assertEquals("(\"ABC\",\"123\")", xSearchService.processFilterValue
                (filter));
    }

    @Test
    public void testProcessFilter() throws Exception {
        // Test != operator
        JSONObject filter = createFilterObject("cm:title", "!=", "blah");
        assertEquals("-@cm\\:title:\"blah\"", xSearchService.processFilter
                (filter));

        // Test empty value
        filter = createFilterObject("cm:title", "=", "");
        assertNull(xSearchService.processFilter(filter));
    }

    @Test
    public void testStripTimeZoneFromDateTime() throws Exception {
        assertEquals("2006-07-20T00:00:00",
                AbstractXSearchService.stripTimeZoneFromDateTime
                        ("2006-07-20T00:00:00+02:00"));
    }

    @Test
    public void testQuote() throws Exception {
        // Basic quoting
        assertEquals("\"Blah\"", AbstractXSearchService.quote("Blah"));
        // Escape double-quotes
        assertEquals("\"Bl\\\"ah\"", AbstractXSearchService.quote("Bl\"ah"));
        // Escape single-quotes
        assertEquals("\"Bl\\'ah\"", AbstractXSearchService.quote("Bl'ah"));
        // Escape backslash
        assertEquals("\"Bl\\\\ah\"", AbstractXSearchService.quote("Bl\\ah"));
        // Escape backslash with double-quotes
        assertEquals("\"Bl\\\\\\\"ah\"", AbstractXSearchService.quote("Bl\\\"ah"));
    }
}