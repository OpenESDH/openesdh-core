package dk.openesdh.repo.services.xsearch;

import org.alfresco.error.AlfrescoRuntimeException;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryParser.QueryParser;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by flemmingheidepedersen on 12/09/14.
 */
public class XSearchServiceImpl extends AbstractXSearchService {

    @Override
    public XResultSet getNodes(Map<String, String> params, int startIndex, int pageSize, String sortField, boolean ascending) {
        baseType = params.get("baseType");
        if (baseType == null) {
            throw new AlfrescoRuntimeException("Must specify a baseType parameter");
        }

        String filtersJSON = params.get("filters");
        try {
            String query = buildQuery(filtersJSON);
            return executeQuery(query);
        } catch (JSONException e) {
            throw new AlfrescoRuntimeException("Unable to parse filters JSON");
        }
    }


}
