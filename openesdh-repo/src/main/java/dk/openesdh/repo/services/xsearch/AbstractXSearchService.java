package dk.openesdh.repo.services.xsearch;

import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryParser.QueryParser;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by flemmingheidepedersen on 12/09/14.
 */
public abstract class AbstractXSearchService implements XSearchService {

    String baseType;
    private Repository repositoryHelper;
    private SearchService searchService;

    @Override
    public abstract XResultSet getNodes(Map<String, String> params, int startIndex, int pageSize, String sortField, boolean ascending);

    private SearchParameters.SortDefinition getSortDefinition(String sortField, boolean ascending) {
        return new SearchParameters.SortDefinition(SearchParameters.SortDefinition.SortType.FIELD, "@" + sortField, ascending);
    }

    protected XResultSet executeQuery(String query, int startIndex, int pageSize, String sortField, boolean ascending) {
        SearchParameters.SortDefinition sortDefinition =
                getSortDefinition(sortField, ascending);

        SearchParameters sp = new SearchParameters();
        sp.addStore(repositoryHelper.getCompanyHome().getStoreRef());
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setSkipCount(startIndex);
        sp.setLimit(pageSize);
        sp.setQuery(query);
        if (sortDefinition != null) {
            sp.addSort(sortDefinition);
        }

        ResultSet results;
        List<NodeRef> nodeRefs = new LinkedList<>();
        results = searchService.query(sp);
        if (results != null) {
            nodeRefs = results.getNodeRefs();
            results.close();
            return new XResultSet(nodeRefs, results.length());
        } else {
            results.close();
            return new XResultSet(nodeRefs, 0);
        }
    }

    protected XResultSet executeQuery(String query) {
        return executeQuery(query, 0, -1, "cm:name", true);
    }


    protected String stripTimeZoneFromDateTime(String str) {
        return str.replaceAll("((\\d{4})-(\\d{2})-(\\d{2})T(\\d{2}):(\\d{2}):(\\d{2}))[+-](\\d{2}):(\\d{2})", "$1");
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


    public void setRepositoryHelper(Repository repositoryHelper) {
        this.repositoryHelper = repositoryHelper;
    }

    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
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
        return StringUtils.join(searchTerms, " AND ");
    }

    protected String processFilter(JSONObject filter) throws JSONException {
        String name = filter.getString("name");
        String operator = filter.getString("operator");

        String value = processFilterValue(filter);
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

        return prepend + field + ':' + value;
    }

    private String processFilterValue(JSONObject filter) throws JSONException {
        try {
            JSONObject value = filter.getJSONObject("value");
            if (value.has("dateRange")) {
                JSONArray dateRange = value.getJSONArray("dateRange");
                if (dateRange.length() > 0) {
                    if (dateRange.getString(0).equals("")) {
                        dateRange.put(0, "MIN");
                    }
                    if (dateRange.getString(1).equals("")) {
                        dateRange.put(1, "MAX");
                    }
                    String rangeFrom = quote(
                            stripTimeZoneFromDateTime(dateRange.getString(0)));
                    String rangeTo = quote(
                            stripTimeZoneFromDateTime(dateRange.getString(1)));
                    return "[" + rangeFrom + " TO " + rangeTo + "]";
                } else {
                    return value.toString();
                }
            } else {
                return value.toString();
            }
        } catch (JSONException e) {
            try {
                JSONArray value = filter.getJSONArray("value");
                List<String> list = new ArrayList<>();
                for (int i = 0; i < value.length(); i++) {
                    list.add(quote(value.getString(i)));
                }
                return "(" + StringUtils.join(list, ",") + ")";
            } catch (JSONException e2) {
                return quote(filter.getString("value"));
            }
        }
    }
}
