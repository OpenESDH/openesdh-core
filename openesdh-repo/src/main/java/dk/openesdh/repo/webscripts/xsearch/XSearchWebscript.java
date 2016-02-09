package dk.openesdh.repo.webscripts.xsearch;

import java.io.IOException;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import dk.openesdh.repo.services.xsearch.XResultSet;
import dk.openesdh.repo.services.xsearch.XSearchService;
import dk.openesdh.repo.utils.Utils;
import dk.openesdh.repo.webscripts.PageableWebScript;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

public class XSearchWebscript extends AbstractWebScript {

    private static final Logger logger = Logger.getLogger(XSearchWebscript.class);

    protected XSearchService xSearchService;

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
        Map<String, String> params = getParams(req);

        int startIndex = 0;
        int pageSize = PageableWebScript.DEFAULT_PAGE_SIZE;

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
            // the 'sort' argument is of the format " column" (originally
            // "+column", but Alfresco converts the + to a space) or "-column"
            ascending = sortBy.charAt(0) != '-';
            sortField = sortBy.substring(1);
        }

        XResultSet results = xSearchService.getNodesJSON(params, startIndex, pageSize, sortField, ascending);

        int resultsEnd = results.getLength() + startIndex;
        res.setHeader("Content-Range", "items " + startIndex + "-" + resultsEnd + "/" + results.getNumberFound());

        String jsonString = results.getNodes().toString();
        res.setContentEncoding(WebScriptUtils.CONTENT_ENCODING_UTF_8);
        res.getWriter().write(jsonString);

    }

    protected Map<String, String> getParams(WebScriptRequest req) {
        return Utils.parseParameters(req.getURL());
    }

    public void setxSearchService(XSearchService xSearchService) {
        this.xSearchService = xSearchService;
    }
}
