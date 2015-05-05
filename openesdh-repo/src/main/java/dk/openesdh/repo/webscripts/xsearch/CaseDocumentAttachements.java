package dk.openesdh.repo.webscripts.xsearch;

import dk.openesdh.repo.services.xsearch.CaseDocumentsSearchService;
import dk.openesdh.repo.services.xsearch.XResultSet;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Extends XSearchWebscript to change the data retriever to retrieve just the attachments.
 */
public class CaseDocumentAttachements extends XSearchWebscript {
    protected static Logger log = Logger.getLogger(CaseDocumentAttachements.class);

    private CaseDocumentsSearchService caseDocumentsSearchService;

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
     * @throws java.io.IOException
     */
    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        Map<String, String> params = getParams(req);
        String caseDocNodeRefStr = params.get("nodeRef");
        if(StringUtils.isBlank(caseDocNodeRefStr) || caseDocNodeRefStr.equalsIgnoreCase("undefined") || caseDocNodeRefStr.equalsIgnoreCase("null"))
            return;
        try {
            NodeRef documentsNodeRef = new NodeRef(params.get("nodeRef"));
            int startIndex = 0;
            int pageSize = defaultPageSize;

            XResultSet results = caseDocumentsSearchService.getAttachments(params);
            List<NodeRef> nodeRefs = results.getNodeRefs();
            JSONArray nodes = new JSONArray();
            for (NodeRef nodeRef : nodeRefs) {
                JSONObject node = nodeToJSON(nodeRef);
                //also return the filename extension
                String fileName = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
                String extension = FilenameUtils.getExtension(fileName);
                node.put("fileType", extension);
                nodes.put(node);
            }

            int resultsEnd = results.getLength() + startIndex;
            res.setHeader("Content-Range", "items " + startIndex + "-" + resultsEnd + "/" + results.getNumberFound());

            String jsonString = nodes.toString();
            res.setContentEncoding("UTF-8");
            res.getWriter().write(jsonString);

        } catch (JSONException e) {
            throw new WebScriptException("Unable to serialize JSON");
        }
    }

    public void setCaseDocumentsSearchService(CaseDocumentsSearchService caseDocumentsSearchService) {
        this.caseDocumentsSearchService = caseDocumentsSearchService;
    }
}
