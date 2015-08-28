package dk.openesdh.repo.webscripts.xsearch;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.xsearch.XResultSet;
import dk.openesdh.repo.services.xsearch.XSearchService;
import dk.openesdh.repo.utils.Utils;
import dk.openesdh.repo.webscripts.PageableWebScript;

public class XSearchWebscript extends AbstractWebScript {
    protected static Logger logger = Logger.getLogger(XSearchWebscript.class);

    protected NodeService nodeService;
    protected NamespaceService namespaceService;
    protected XSearchService xSearchService;
    protected PersonService personService;

    /**
     * The page size to use if none is specified in the request.
     * This is settable from the bean definition.
     */
    protected int defaultPageSize = PageableWebScript.DEFAULT_PAGE_SIZE;

    /**
     * Handles a typical request from a dojo/store/JsonRest store.
     * See http://dojotoolkit.org/reference-guide/1.10/dojo/store/JsonRest.html#implementing-a-rest-server
     * <p/>
     * Paging and sorting information is passed to the xSearchService, the
     * result nodes are converted to JSON and returned in the response along
     * with the number of items found and the start/end index.
     *
     * @param req
     * @param res
     * @throws IOException
     */
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        try {
            Map<String, String> params = getParams(req);

            int startIndex = 0;
            int pageSize = defaultPageSize;

            String rangeHeader = req.getHeader("x-range");
            int[] range = PageableWebScript.parseRangeHeader(rangeHeader);
            if (range != null) {
                logger.debug("Range: " + range[0] + " - " + range[1]);
                startIndex = range[0];
                pageSize = range[1] - range[0];
            }

            String sortField = null;
            boolean ascending = false;
            String sortBy = req.getParameter("sortBy");
            if (sortBy != null) {
                // the 'sort' argument is of the format " column" (originally "+column", but Alfresco converts the + to a space) or "-column"
                ascending = sortBy.charAt(0) != '-';
                sortField = sortBy.substring(1);
            }

            XResultSet results = xSearchService.getNodes(params, startIndex, pageSize, sortField, ascending);
            List<NodeRef> nodeRefs = results.getNodeRefs();
            JSONArray nodes = new JSONArray();
            for (NodeRef nodeRef : nodeRefs) {
                JSONObject node = nodeToJSON(nodeRef);
                nodes.put(node);
            }

            int resultsEnd = results.getLength() + startIndex;
            res.setHeader("Content-Range", "items " + startIndex +
                    "-" + resultsEnd + "/" + results.getNumberFound());

            String jsonString = nodes.toString();
            res.setContentEncoding("UTF-8");
            res.getWriter().write(jsonString);

        } catch (JSONException e) {
            throw new WebScriptException("Unable to serialize JSON");
        }
    }

    protected Map<String, String> getParams(WebScriptRequest req) {
        return Utils.parseParameters(req.getURL());
    }

    /**
     * Serializes the node to JSON.
     * The default implementation outputs all properties and associations.
     * This can be overridden to output less (or more) information.
     *
     * @param nodeRef
     * @return
     * @throws JSONException
     */
    protected JSONObject nodeToJSON(NodeRef nodeRef) throws JSONException {
        JSONObject json = new JSONObject();
        Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
        for (Map.Entry<QName, Serializable> entry : properties.entrySet()) {
            if (entry.getValue() instanceof Date) {
                json.put(entry.getKey().toPrefixString(namespaceService), ((Date) entry.getValue()).getTime());
            } else {
                json.put(entry.getKey().toPrefixString(namespaceService), entry.getValue());
            }
        }
        List<AssociationRef> associations = nodeService.getTargetAssocs(nodeRef, RegexQNamePattern.MATCH_ALL);
        for (AssociationRef association : associations) {
            String assocName = association.getTypeQName().toPrefixString(namespaceService);
            if (!json.has(assocName)) {
                JSONArray refs = new JSONArray();
                addAssocToArray(association, refs);
                json.put(assocName, refs);
            } else {
                JSONArray refs = (JSONArray) json.get(assocName);
                addAssocToArray(association, refs);
                json.put(association.getTypeQName().toPrefixString(namespaceService), refs);
            }
        }
        json.put("TYPE", nodeService.getType(nodeRef).toPrefixString(namespaceService));
        json.put("nodeRef", nodeRef.toString());

        if(nodeService.getType(nodeRef).equals(OpenESDHModel.TYPE_DOC_DIGITAL_FILE)) {
            //also return the filename extension
            String fileName = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
            String extension = FilenameUtils.getExtension(fileName);
            json.put("fileType", extension);
        }
        return json;
    }

    private void addAssocToArray(AssociationRef association, JSONArray refs) throws JSONException{
        if (nodeService.getType(association.getTargetRef()).equals( ContentModel.TYPE_PERSON)) {
            PersonService.PersonInfo info = personService.getPerson(association.getTargetRef());
            JSONObject json = new JSONObject();
            json.put("value", info.getUserName());
            json.put("fullname", info.getFirstName() + " " + info.getLastName());
            refs.put(json);
        }
        else {
            refs.put(association.getTargetRef());
        }
    }

    public void setDefaultPageSize(int defaultPageSize) {
        this.defaultPageSize = defaultPageSize;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setNamespaceService(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }

    public void setxSearchService(XSearchService xSearchService) {
        this.xSearchService = xSearchService;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

}


