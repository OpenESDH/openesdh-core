package dk.openesdh.repo.services.classification;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO9075;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import dk.openesdh.repo.classification.sync.kle.KLEClassificationSynchronizer;
import dk.openesdh.repo.services.xsearch.AbstractXSearchService;
import dk.openesdh.repo.services.xsearch.XResultSet;

/**
 * Created by syastrov on 9/16/15.
 */
@Service("ClassificationSearchService")
public class ClassificationSearchServiceImpl extends AbstractXSearchService {
    @Override
    public XResultSet getNodes(Map<String, String> params, int startIndex, int pageSize, String sortField, boolean ascending) {
        String term = params.get("term");
        if (term == null) {
            throw new AlfrescoRuntimeException("Must specify a term parameter");
        }

        String field = params.get("field");
        if (field == null) {
            throw new AlfrescoRuntimeException("Must specify a " +
                    "field parameter");
        }

        // TODO: Highly KLE-specific. Make generic
        String path;
        String queryPath;
        if (field.equals("oe:journalKey")) {
            path = KLEClassificationSynchronizer.EmneplanLoader.ROOT_CATEGORY_NAME;
            // 3 levels depth
            queryPath = "/cm:generalclassifiable/" + encodePath(path) + "//*/*/*";
        } else if (field.equals("oe:journalFacet")) {
            path = KLEClassificationSynchronizer.FacetterLoader.ROOT_CATEGORY_NAME;
            // 2 levels depth
            queryPath = "/cm:generalclassifiable/" + encodePath(path) + "//*/*";
        } else {
            throw new AlfrescoRuntimeException("Unsupported " +
                    "classification field name: " + field);
        }

        // Search on name OR title
        // Note the use of = prefix on the name field: this is to force it
        // NOT to use tokenisation of the name field.

        // Note: This is FTS search syntax
        String query = "PATH:\"" + queryPath + "\" AND (=name:\"" +
                term + "*\" OR title:\"*" + term + "*\")";

        // Quick hack for demo
        // TODO: The discontinued date should be stored as a category
        // property when being synced by the ClassificationSynchronizer
        // The search should then query for categories that have not been
        // discontinued as of the current date
        query += " AND -title:udgået";

        if (sortField == null) {
            sortField = "cm:name";
            ascending = true;
        }

        return executeQuery(query, startIndex, pageSize, sortField,
                ascending);
    }

    @Override
    protected JSONObject nodeToJSON(NodeRef nodeRef) throws JSONException {
        JSONObject json = new JSONObject();
        Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
        json.put("name", properties.get(ContentModel.PROP_NAME));
        json.put("title", properties.getOrDefault(ContentModel.PROP_TITLE, ""));
        json.put("description", properties.getOrDefault(ContentModel.PROP_DESCRIPTION, ""));
        json.put("nodeRef", nodeRef.toString());
        return json;
    }

    @Override
    protected void setSearchParameters(SearchParameters sp) {
        sp.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
    }

    /**
     * Get the path as an ISO9075 encoded path
     */
    protected String encodePath(String path) {
        return Arrays.asList(path.split("/")).stream().map((String p) -> "cm:" + ISO9075.encode(p)).collect(Collectors.joining("/"));
    }
}
