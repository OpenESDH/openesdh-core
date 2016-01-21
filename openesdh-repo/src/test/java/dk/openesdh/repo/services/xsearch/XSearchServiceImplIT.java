package dk.openesdh.repo.services.xsearch;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.nodelocator.CompanyHomeNodeLocator;
import org.alfresco.repo.nodelocator.NodeLocatorService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.tradeshift.test.remote.Remote;
import com.tradeshift.test.remote.RemoteTestRunner;

import dk.openesdh.SimpleCaseModel;
import dk.openesdh.repo.helper.CaseHelper;
import dk.openesdh.repo.services.cases.CaseService;

@RunWith(RemoteTestRunner.class)
@Remote(runnerClass = SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:alfresco/application-context.xml")
public class XSearchServiceImplIT {

    @Autowired
    @Qualifier("NodeService")
    private NodeService nodeService;

    @Autowired
    @Qualifier("SearchService")
    private SearchService searchService;

    @Autowired
    @Qualifier("repositoryHelper")
    private Repository repositoryHelper;

    @Autowired
    @Qualifier("nodeLocatorService")
    private NodeLocatorService nodeLocatorService;

    @Autowired
    @Qualifier("TestCaseHelper")
    private CaseHelper caseHelper;

    @Autowired
    @Qualifier("CaseService")
    private CaseService caseService;

    private XSearchServiceImpl xSearchService = null;

    private static final String BASE_TYPE = "base:case";

    private NodeRef caseNode;
    private String testCaseTitle;
    private String caseId;

    @Before
    public void setUp() throws Exception {
        // TODO: All of this could have been done only once
        String admin = AuthenticationUtil.getAdminUserName();
        AuthenticationUtil.setFullyAuthenticatedUser(admin);

        xSearchService = new XSearchServiceImpl();
        xSearchService.setRepositoryHelper(repositoryHelper);
        xSearchService.setSearchService(searchService);

        NodeRef companyHome = nodeLocatorService.getNode(CompanyHomeNodeLocator.NAME, null, null);
        String name = "My repo case (" + System.currentTimeMillis() + ")";
        String title = "My repo case (" + System.currentTimeMillis() + ")";
        Map<QName, Serializable> properties = new HashMap<>();
        properties.put(ContentModel.PROP_TITLE, title);
        List<NodeRef> owners = new LinkedList<>();
        owners.add(repositoryHelper.getPerson());
        caseNode = caseHelper.createCase(admin,
                companyHome,
                name, SimpleCaseModel.TYPE_CASE_SIMPLE, properties, owners);
        testCaseTitle = title;
        caseId = caseService.getCaseId(caseNode);
    }

    @After
    public void tearDown() throws Exception {
        nodeService.deleteNode(caseNode);
    }

//    @Test
    public void testGetNodes() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("filters", createTestFilters(testCaseTitle, caseId).toString());
        params.put("baseType", BASE_TYPE);
        XResultSet results = xSearchService.getNodes(params);
        assertEquals(1, results.getLength());
        assertEquals(caseNode, results.getNodeRefs().get(0));
    }

    private JSONObject createFilterObject(String name, String operator,
            String value) throws JSONException {
        JSONObject filter = new JSONObject();
        filter.put("name", name);
        filter.put("operator", operator);
        filter.put("value", value);
        return filter;
    }

    @Test
    public void testBuildQuery() throws Exception {
        JSONArray filters = createTestFilters(testCaseTitle, null);
        String query = xSearchService.buildQuery(BASE_TYPE, filters.toString());
        assertEquals("TYPE:" + AbstractXSearchService.quote(BASE_TYPE) + " AND ("
                + "@cm\\:title:" + AbstractXSearchService.quote(testCaseTitle) + ")", query);
    }

    @Test
    public void testBuildQueryWithAND() throws Exception {
        buildQueryWith(XSearchServiceImpl.FilterType.AND);
    }
    
    @Test
    public void testBuildQueryWithOR() throws Exception {
        buildQueryWith(XSearchServiceImpl.FilterType.OR);
    }

    private void buildQueryWith(XSearchServiceImpl.FilterType filterType) throws Exception {
        JSONArray filters = createTestFilters(testCaseTitle, caseId);

        String query = xSearchService.buildQuery(BASE_TYPE, filters.toString(), filterType);
        assertEquals("TYPE:" + AbstractXSearchService.quote(BASE_TYPE) + " AND ("
                + "@cm\\:title:" + AbstractXSearchService.quote(testCaseTitle) + " " + filterType.name() + " "
                + "@oe\\:id:" + AbstractXSearchService.quote(caseId) + ")",
                query);
    }

    private JSONArray createTestFilters(String fTitle, String fId) throws JSONException {
        JSONArray filters = new JSONArray();
        if (fTitle != null) {
            filters.put(createFilterObject("cm:title", "=", fTitle));
        }
        if (fId != null) {
            filters.put(createFilterObject("oe:id", "=", fId));
        }
        return filters;
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
        assertEquals("[\"MIN\" TO \"MAX\"]", xSearchService.processFilterValue(filter));

        // Test date range
        filter = new JSONObject();
        dateObj = new JSONObject();
        dateRange = new JSONArray();
        dateRange.put("2006-07-20T00:00:00+02:00");
        dateRange.put("2007-07-20T00:00:00+02:00");
        dateObj.put("dateRange", dateRange);
        filter.put("value", dateObj);
        assertEquals("[\"2006-07-20T00:00:00\" TO \"2007-07-20T00:00:00\"]",
                xSearchService.processFilterValue(filter));

        // Test JSONArray
        filter = new JSONObject();
        JSONArray arr = new JSONArray();
        arr.put("ABC");
        arr.put("123");
        filter.put("value", arr);
        assertEquals("(\"ABC\",\"123\")", xSearchService.processFilterValue(filter));
    }

    @Test
    public void testProcessFilter() throws Exception {
        // Test != operator
        JSONObject filter = createFilterObject("cm:title", "!=", "blah");
        assertEquals("-@cm\\:title:\"blah\"", xSearchService.processFilter(filter).get());

        // Test empty value
        filter = createFilterObject("cm:title", "=", "");
        Assert.assertFalse(xSearchService.processFilter(filter).isPresent());
    }

    @Test
    public void testStripTimeZoneFromDateTime() throws Exception {
        assertEquals("2006-07-20T00:00:00",
                AbstractXSearchService.stripTimeZoneFromDateTime("2006-07-20T00:00:00+02:00"));
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
