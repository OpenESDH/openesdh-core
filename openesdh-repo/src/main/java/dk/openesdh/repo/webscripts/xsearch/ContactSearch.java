package dk.openesdh.repo.webscripts.xsearch;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.xsearch.ContactSearchService;
import dk.openesdh.repo.services.xsearch.XResultSet;
import dk.openesdh.repo.utils.Utils;
import dk.openesdh.repo.webscripts.contacts.ContactUtils;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

public class ContactSearch extends AbstractWebScript {

    private NodeService nodeService;
    private ContactSearchService contactSearchService;

    public static final int DEFAULT_PAGE_SIZE = 25;

    @SuppressWarnings("unchecked")
    @Override
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
            boolean ascending = params.get("sortAscending") == null || Boolean.parseBoolean(params.get("sortAscending"));

            XResultSet results = contactSearchService.getNodes(params, startIndex, pageSize, sortField, ascending);
            List<NodeRef> nodeRefs = results.getNodeRefs();
            JSONArray nodes = new JSONArray();
            for (NodeRef nodeRef : nodeRefs) {
                JSONObject node = nodeToJSON(nodeRef);
                nodes.add(node);
            }

            JSONObject response = new JSONObject();
            response.put("totalRecords", results.getNumberFound());
            response.put("startIndex", startIndex);
            response.put("items", nodes);
            res.setContentEncoding(WebScriptUtils.CONTENT_ENCODING_UTF_8);
            response.writeJSONString(res.getWriter());
        } catch (JSONException e) {
            throw new WebScriptException("Unable to serialize JSON");
        }
    }

    private JSONObject nodeToJSON(NodeRef nodeRef) throws JSONException {
        Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
        if (!properties.containsKey(OpenESDHModel.PROP_CONTACT_DEPARTMENT)) {
            properties.put(OpenESDHModel.PROP_CONTACT_DEPARTMENT, getParentsDepartment(nodeRef));
        }
        return ContactUtils.createContactJson(nodeRef, properties);
    }

    private Serializable getParentsDepartment(NodeRef nodeRef) {
        for (AssociationRef assoc : nodeService.getSourceAssocs(nodeRef, OpenESDHModel.ASSOC_CONTACT_MEMBERS)) {
            return nodeService.getProperty(assoc.getSourceRef(), OpenESDHModel.PROP_CONTACT_DEPARTMENT);
        }
        return null;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setContactSearchService(ContactSearchService contactSearchService) {
        this.contactSearchService = contactSearchService;
    }
}
