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


    protected static String stripTimeZoneFromDateTime(String str) {
        return str.replaceAll("((\\d{4})-(\\d{2})-(\\d{2})T(\\d{2}):(\\d{2}):(\\d{2}))[+-](\\d{2}):(\\d{2})", "$1");
    }


    /**
     * Surrounds a value with quotes, escaping any quotes inside the value.
     * Escapes backslashes, double- and single-quotes.
     * @param value
     * @return
     */
    protected static String quote(String value) {
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

    public XResultSet getNodes(Map<String, String> params) {
        return getNodes(params, 0, -1, "@cm:name", true);
    }
}