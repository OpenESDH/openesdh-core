package dk.openesdh.repo.webscripts.xsearch;

import dk.openesdh.repo.model.OpenESDHModel;
import org.alfresco.model.ContentModel;
import dk.openesdh.repo.services.xsearch.UserInvolvedSearchService;
import dk.openesdh.repo.services.xsearch.XResultSet;
import dk.openesdh.repo.services.xsearch.XSearchService;
import dk.openesdh.repo.utils.Utils;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.*;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class UserInvolvedSearch extends AbstractWebScript {
    static Logger log = Logger.getLogger(UserInvolvedSearch.class);

    protected SearchService searchService;
    protected NodeService nodeService;
    protected NamespaceService namespaceService;
    protected Repository repository;
    protected AuthenticationService authenticationService;
    protected UserInvolvedSearchService userInvolvedSearchService;

    public static int DEFAULT_PAGE_SIZE = 25;

    public void execute(WebScriptRequest req, WebScriptResponse res)
            throws IOException {
        try {
            Map<String, String> params = Utils.parseParameters(req.getURL());

            int startIndex = 0;
            int pageSize = DEFAULT_PAGE_SIZE;

            params.put("user", authenticationService.getCurrentUserName());
            params.put("filter", "");
            params.put("baseType", "");


            String rangeHeader = req.getHeader("x-range");
            Range range = parseRangeHeader(rangeHeader);
            if (range != null) {
                log.debug("Range: " + range.startIndex + " - " + range.endIndex);
                startIndex = range.startIndex;
                pageSize = range.endIndex - range.startIndex;
            }

            String sortField = null;
            boolean ascending = false;
            String sortBy = req.getParameter("sortBy");
            if (sortBy != null) {
                // the 'sort' argument is of the format " column" (originally "+column", but Alfresco converts the + to a space) or "-column"
                ascending = sortBy.charAt(0) == '-' ? false : true;
                sortField = sortBy.substring(1);
            }

            XResultSet results = userInvolvedSearchService.getNodes(params, startIndex, pageSize, sortField, ascending);
            List<NodeRef> nodeRefs = results.getNodeRefs();
            JSONArray nodes = new JSONArray();
            for (NodeRef nodeRef : nodeRefs) {
                JSONObject node = nodeToJSON (nodeRef);
                nodes.put(node);
            }

            int resultsEnd = results.getLength() - startIndex;
            res.setHeader("Content-Range", "items " + startIndex +
                    "-" + resultsEnd + "/" + results.getLength());

            String jsonString = nodes.toString();
            res.getWriter().write(jsonString);
        } catch (JSONException e) {
            throw new WebScriptException("Unable to serialize JSON");
        }
    }

    protected JSONObject nodeToJSON(NodeRef nodeRef) throws JSONException {
        JSONObject json = new JSONObject();
        Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
        json.put ("oe:id", nodeService.getProperty(nodeRef, OpenESDHModel.PROP_OE_ID));
        json.put ("oe:status", nodeService.getProperty(nodeRef, OpenESDHModel.PROP_OE_STATUS));
        json.put ("cm:modified", nodeService.getProperty(nodeRef, ContentModel.PROP_MODIFIED));
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

    public void setUserInvolvedSearchService(UserInvolvedSearchService userInvolvedSearchService) {
        this.userInvolvedSearchService = userInvolvedSearchService;
    }
    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }
}


