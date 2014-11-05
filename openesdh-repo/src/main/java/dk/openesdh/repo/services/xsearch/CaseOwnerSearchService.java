package dk.openesdh.repo.services.xsearch;

import java.util.Map;

/**
 * Created by flemmingheidepedersen on 15/09/14.
 */
public interface CaseOwnerSearchService {
    public XResultSet getNodes(Map<String, String> params, int startIndex, int pageSize, String sortField, boolean ascending);
}
