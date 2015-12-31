package dk.openesdh.repo.webscripts;

import java.io.IOException;

import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import com.github.dynamicextensionsalfresco.webscripts.AnnotationWebScriptRequest;
import com.github.dynamicextensionsalfresco.webscripts.AnnotationWebscriptResponse;
import com.github.dynamicextensionsalfresco.webscripts.resolutions.Resolution;
import com.github.dynamicextensionsalfresco.webscripts.resolutions.ResolutionParameters;

import dk.openesdh.repo.model.ResultSet;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

public interface PageableWebScript<T> {
    
    int DEFAULT_PAGE_SIZE = 25;

    /**
     * Parse a HTTP Range header and return an array containing 2 elemens: the
     * start and end index of the range.
     *
     * @param range
     * @return
     */
    static int[] parseRangeHeader(String range) {
        final String RANGE_START = "items=";
        if (range != null && range.contains(RANGE_START)) {
            String rest = range.substring(RANGE_START.length());
            int dash = rest.indexOf("-");
            int startIndex = Integer.parseInt(rest.substring(0, dash));
            int endIndex = Integer.parseInt(rest.substring(dash + 1));
            return new int[] { startIndex, endIndex };
        } else {
            return null;
        }
    }

    static void getItemsPage(WebScriptRequest req, WebScriptResponse res, PageableWebScript<?> ws)
            throws IOException {
        int startIndex = 0;
        int pageSize = PageableWebScript.DEFAULT_PAGE_SIZE;

        String rangeHeader = req.getHeader("x-range");
        int[] range = parseRangeHeader(rangeHeader);

        if (range != null) {
            startIndex = range[0];
            pageSize = range[1] - range[0];
        }

        ResultSet<?> items = ws.getItems(startIndex, pageSize);

        int resultsEnd = items.getResultList().size() + startIndex;
        res.setHeader("Content-Range", "items " + startIndex + "-" + resultsEnd + "/" + items.getTotalItems());
        res.setContentEncoding(WebScriptUtils.CONTENT_ENCODING_UTF_8);
        WebScriptUtils.writeJson(items.getResultList(), res);
    }

    static Resolution getItemsPage(PageableWebScript<?> ws) {
        return (AnnotationWebScriptRequest req, AnnotationWebscriptResponse res, ResolutionParameters params) -> getItemsPage(
                req, res, ws);
    }

    ResultSet<T> getItems(int startIndex, int pageSize);
}
