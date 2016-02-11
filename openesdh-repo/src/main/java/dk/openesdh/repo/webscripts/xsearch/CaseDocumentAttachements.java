package dk.openesdh.repo.webscripts.xsearch;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import dk.openesdh.repo.services.xsearch.CaseDocumentsAttachmentsSearchService;
import dk.openesdh.repo.services.xsearch.XResultSet;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

/**
 * Extends XSearchWebscript to change the data retriever to retrieve just the attachments.
 */
public class CaseDocumentAttachements extends XSearchWebscript {

    private static final Logger log = Logger.getLogger(CaseDocumentAttachements.class);

    private CaseDocumentsAttachmentsSearchService caseDocumentsSearchService;

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
        if (StringUtils.isBlank(caseDocNodeRefStr) || caseDocNodeRefStr.equalsIgnoreCase("undefined") || caseDocNodeRefStr.equalsIgnoreCase("null")) {
            return;
        }
        int startIndex = 0;
        XResultSet results = caseDocumentsSearchService.getAttachments(params);
        int resultsEnd = results.getLength() + startIndex;
        res.setHeader("Content-Range", "items " + startIndex + "-" + resultsEnd + "/" + results.getNumberFound());
        String jsonString = results.getNodes().toString();
        res.setContentEncoding(WebScriptUtils.CONTENT_ENCODING_UTF_8);
        res.getWriter().write(jsonString);
    }

    public void setCaseDocumentsSearchService(CaseDocumentsAttachmentsSearchService caseDocumentsSearchService) {
        this.caseDocumentsSearchService = caseDocumentsSearchService;
    }
}
