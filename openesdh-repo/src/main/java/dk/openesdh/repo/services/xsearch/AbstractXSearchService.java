package dk.openesdh.repo.services.xsearch;

import dk.openesdh.repo.services.cases.CaseService;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;

/**
 * Created by flemmingheidepedersen on 12/09/14.
 */
public abstract class AbstractXSearchService implements XSearchService {
    protected CaseService caseService;

    protected Repository repositoryHelper;
    protected SearchService searchService;
    @Override
    public abstract XResultSet getNodes(Map<String, String> params, int startIndex, int pageSize, String sortField, boolean ascending);

    protected SearchParameters.SortDefinition getSortDefinition(String sortField, boolean ascending) {
        return new SearchParameters.SortDefinition(SearchParameters.SortDefinition.SortType.FIELD, "@" + sortField, ascending);
    }

    /**
     * Empty. Provided to allow overriding of search parameters.
     * @param sp
     */
    protected void setSearchParameters(SearchParameters sp) {

    }

    protected XResultSet executeQuery(String query, int startIndex, int pageSize, String sortField, boolean ascending) {
        SearchParameters.SortDefinition sortDefinition =
                getSortDefinition(sortField, ascending);

        SearchParameters sp = new SearchParameters();
        sp.addStore(repositoryHelper.getCompanyHome().getStoreRef());
        sp.setSkipCount(startIndex);
        sp.setLimit(pageSize);
        sp.setLimitBy(LimitBy.FINAL_SIZE);
        sp.setQuery(query);
        if (sortDefinition != null) {
            sp.addSort(sortDefinition);
        }
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        setSearchParameters(sp);

        ResultSet results;
        List<NodeRef> nodeRefs = new LinkedList<>();
        results = searchService.query(sp);
        if (results != null) {
            nodeRefs = results.getNodeRefs();
            results.close();
            return new XResultSet(nodeRefs, results.getNumberFound());
        } else {
            results.close();
            return new XResultSet(nodeRefs);
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
        return getNodes(params, 0, -1, "cm:name", true);
    }

    public void setCaseService(CaseService caseService) {
        this.caseService = caseService;
    }
}
