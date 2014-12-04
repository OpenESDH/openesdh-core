package dk.openesdh.repo.webscripts.xsearch;

import dk.openesdh.repo.services.xsearch.PartySearchService;
import dk.openesdh.repo.services.xsearch.XResultSet;
import dk.openesdh.repo.services.xsearch.XSearchService;
import dk.openesdh.repo.utils.Utils;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
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
import java.util.List;
import java.util.Map;

public class PartySearch extends AbstractWebScript {
    static Logger log = Logger.getLogger(PartySearch.class);

    protected NodeService nodeService;
    protected NamespaceService namespaceService;
    protected Repository repository;
    protected PartySearchService partySearchService;

    public static int DEFAULT_PAGE_SIZE = 25;

    public void execute(WebScriptRequest req, WebScriptResponse res)
            throws IOException {
        try {
            Map<String, String> params = Utils.parseParameters(req.getURL());

            int startIndex = 0;
            int pageSize = DEFAULT_PAGE_SIZE;

            String paramPageSize = params.get("pageSize");
            if (paramPageSize != null) {
                pageSize = Integer.parseInt(paramPageSize);
            }
            String paramPage = params.get("page");
            if (paramPage != null) {
                startIndex = pageSize * (Integer.parseInt(paramPage) - 1);
            }

            String sortField = params.get("sortField");
            boolean ascending = Boolean.parseBoolean(params.get
                    ("sortAscending"));

            XResultSet results = partySearchService.getNodes(params, startIndex, pageSize, sortField, ascending);
            List<NodeRef> nodeRefs = results.getNodeRefs();
            JSONArray nodes = new JSONArray();
            for (NodeRef nodeRef : nodeRefs) {
                JSONObject node = nodeToJSON(nodeRef);
                nodes.put(node);
            }

            JSONObject response = new JSONObject();
            response.put("totalRecords", results.getNumberFound());
            response.put("startIndex", startIndex);
            response.put("items", nodes);
            res.setContentEncoding("UTF-8");
            res.getWriter().write(response.toString());
        } catch (JSONException e) {
            throw new WebScriptException("Unable to serialize JSON");
        }
    }

    protected JSONObject nodeToJSON(NodeRef nodeRef) throws JSONException {
        JSONObject json = new JSONObject();
        // TODO: Don't include ALL properties
        Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
        for (Map.Entry<QName, Serializable> entry : properties.entrySet()) {
            json.put(entry.getKey().toPrefixString(namespaceService), entry.getValue());
        }
        List<AssociationRef> associations = nodeService.getTargetAssocs
                (nodeRef, RegexQNamePattern.MATCH_ALL);
        for (AssociationRef association : associations) {
            String assocName = association.getTypeQName().toPrefixString
                    (namespaceService);
            if (!json.has(assocName)) {
                JSONArray refs = new JSONArray();
                refs.put(association.getTargetRef());
                json.put(assocName, refs);
            } else {
                JSONArray refs = (JSONArray) json.get(assocName);
                refs.put(association.getTargetRef());
                json.put(association.getTypeQName().toPrefixString
                        (namespaceService), refs);
            }
        }
        json.put("TYPE", nodeService.getType(nodeRef).toPrefixString(namespaceService));
        json.put("nodeRef", nodeRef.toString());
        return json;
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

    public void setPartySearchService(PartySearchService partySearchService) {
        this.partySearchService = partySearchService;
    }
}


