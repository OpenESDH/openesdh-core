package dk.openesdh.repo.services.classification;

import dk.openesdh.repo.classification.sync.ClassificationSynchronizer;
import dk.openesdh.repo.classification.sync.kle.KLEClassificationSynchronizer;
import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.xsearch.AbstractXSearchService;
import dk.openesdh.repo.services.xsearch.XResultSet;
import dk.openesdh.repo.services.xsearch.XSearchService;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO9075;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by syastrov on 9/16/15.
 */
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
        String query = "+PATH:\"" + queryPath + "\" AND +(@cm\\:name:\"" +
                term + "*\" OR @cm\\:title:\"*" + term + "*\")";

        return executeQuery(query, startIndex, pageSize, sortField,
                ascending);
    }

    /**
     * Get the path as an ISO9075 encoded path
     */
    protected String encodePath(String path) {
        return Arrays.asList(path.split("/")).stream().map((String p) -> "cm:" + ISO9075.encode(p)).collect(Collectors.joining("/"));
    }
}
