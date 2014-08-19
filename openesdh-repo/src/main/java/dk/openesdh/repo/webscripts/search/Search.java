package dk.openesdh.repo.webscripts.search;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.search.impl.querymodel.impl.lucene.LuceneQueryBuilder;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.*;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.queryParser.QueryParser;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Search extends AbstractWebScript {
    static Logger log = Logger.getLogger(Search.class);

    protected SearchService searchService;
    protected NodeService nodeService;
    protected NamespaceService namespaceService;
    protected Repository repository;

    public static int DEFAULT_PAGE_SIZE = 25;

    protected String baseType;

    public void execute(WebScriptRequest req, WebScriptResponse res)
            throws IOException {
        try {
            baseType = req.getParameter("baseType");
            if (baseType == null) {
                throw new AlfrescoRuntimeException("Must specify a baseType parameter");
            }

            String filtersJSON = req.getParameter("filters");
            String query = buildQuery(filtersJSON);
            System.out.println("Search: " + query);

            int startIndex = 0;
            int pageSize = DEFAULT_PAGE_SIZE;

            String rangeHeader = req.getHeader("x-range");
            Range range = parseRangeHeader(rangeHeader);
            if (range != null) {
                log.debug("Range: " + range.startIndex + " - " + range.endIndex);
                startIndex = range.startIndex;
                pageSize = range.endIndex - range.startIndex;
            }

            SearchParameters.SortDefinition sortDefinition =
                    getSortDefinition(req.getParameter("sortBy"));

            SearchParameters sp = new SearchParameters();
            sp.addStore(repository.getCompanyHome().getStoreRef());
            sp.setLanguage(SearchService.LANGUAGE_LUCENE);
            sp.setSkipCount(startIndex);
            sp.setLimit(pageSize);
            sp.setQuery(query);
            if (sortDefinition != null) {
                sp.addSort(sortDefinition);
            }

            ResultSet results = null;
            List<NodeRef> nodeRefs = new LinkedList<>();
            try {
                results = searchService.query(sp);
                nodeRefs = results.getNodeRefs();
            } finally {
                if (results != null) {
                    results.close();
                }
            }

            JSONArray nodes = new JSONArray();
            for (NodeRef nodeRef : nodeRefs) {
                JSONObject node = nodeToJSON (nodeRef);
                nodes.put(node);
            }

            int resultsEnd = results.length() - results.getStart();
            res.setHeader("Content-Range", "items " + results.getStart() +
                    "-" + resultsEnd + "/" + results.length());

            String jsonString = nodes.toString();
            res.getWriter().write(jsonString);
        } catch (JSONException e) {
            throw new WebScriptException("Unable to serialize JSON");
        }
    }

    protected SearchParameters.SortDefinition getSortDefinition(String sortBy) {
        String sortField = null;
        boolean ascending = false;
        if (sortBy != null) {
            // the 'sort' argument is of the format " column" (originally "+column", but Alfresco converts the + to a space) or "-column"
            ascending = sortBy.charAt(0) == '-' ? false : true;
            sortField = sortBy.substring(1);
        }
        return new SearchParameters.SortDefinition(SearchParameters.SortDefinition.SortType.FIELD, "@" + sortField, ascending);
    }

    protected JSONObject nodeToJSON(NodeRef nodeRef) throws JSONException {
        JSONObject json = new JSONObject();
        // TODO: Don't include ALL properties
        Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
        for (Map.Entry<QName, Serializable> entry : properties.entrySet()) {
            json.put(entry.getKey().toPrefixString(namespaceService), entry.getValue());
        }
        json.put("TYPE", nodeService.getType(nodeRef).toPrefixString(namespaceService));
        json.put("nodeRef", nodeRef.toString());
        return json;
    }

    private class Range {
        public int startIndex;
        public int endIndex;

        public Range(int startIndex, int endIndex) {
            this.startIndex = startIndex;
            this.endIndex = endIndex;
        }
    }

    protected Range parseRangeHeader(String range) {
        final String RANGE_START = "items=";
        if (range != null && range.contains(RANGE_START)) {
            String rest = range.substring(RANGE_START.length());
            int dash = rest.indexOf("-");
            int startIndex = Integer.parseInt(rest.substring(0, dash));
            int endIndex = Integer.parseInt(rest.substring(dash + 1));
            return new Range(startIndex, endIndex);
        } else {
            return null;
        }
    }

    protected String buildQuery(String filtersJSON) throws JSONException {
        List<String> searchTerms = new ArrayList<>();

        if (filtersJSON != null) {
            JSONArray filters = new JSONArray(filtersJSON);
            for (int i = 0; i < filters.length(); i++) {
                JSONObject filter = filters.getJSONObject(i);
                String searchTerm = processFilter(filter);
                if (searchTerm != null) {
                    searchTerms.add(searchTerm);
                }
            }
        }

        searchTerms.add("TYPE:" + quote(baseType));
        String query = StringUtils.join(searchTerms, " AND ");
        return query;
    }

    private String processFilter(JSONObject filter) throws JSONException {
        String name = filter.getString("name");
        String operator = filter.getString("operator");

        // TODO: Handle non-string values
        String value = filter.getString("value");
        System.out.println("Filter " + name + " " + operator + " " + value);

        if (value.equals("")) {
            return null;
        }

        // Escape field name for lucene
        String field = QueryParser.escape(name);
        String prepend = "";
        if (operator.equals("!=")) {
            prepend = "-";
        }

        // Prefix fields with @ symbol
        if (name.contains(":") && !name.startsWith("@")) {
            field = "@" + field;
        }

        return prepend + field + ':' + quote(value);
    }

    /**
     * Surrounds a value with quotes, escaping any quotes inside the value.
     * Escapes backslashes, double- and single-quotes.
     * @param value
     * @return
     */
    protected String quote(String value) {
        return "\"" +
                value.replace("\\", "\\\\"). // Backslash
                      replace("'", "\\'"). // Single quote
                      replace("\"", "\\\"") + // Double quote
                "\"";
    }

    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setNamespaceService(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }

    public void setRepository(Repository repository) {
        this.repository = repository;
    }
}


