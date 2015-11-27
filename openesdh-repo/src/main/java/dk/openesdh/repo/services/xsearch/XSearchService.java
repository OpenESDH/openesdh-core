package dk.openesdh.repo.services.xsearch;

import java.util.Map;

/**
 * Created by flemmingheidepedersen on 15/09/14.
 */
public interface XSearchService {

    String NODE_REF = "nodeRef";

    String CASE_DOCUMENTS = "documents";

    public XResultSet getNodes(Map<String, String> params, int startIndex, int pageSize, String sortField, boolean ascending);

    public XResultSet getNodesJSON(Map<String, String> params, int startIndex, int pageSize, String sortField,
            boolean ascending);
}
