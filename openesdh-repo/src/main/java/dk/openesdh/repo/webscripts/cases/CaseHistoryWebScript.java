package dk.openesdh.repo.webscripts.cases;

import java.io.IOException;

import org.alfresco.service.cmr.repository.NodeRef;
import org.json.simple.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

import com.github.dynamicextensionsalfresco.webscripts.annotations.Header;
import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.RequestParam;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.UriVariable;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import com.github.dynamicextensionsalfresco.webscripts.resolutions.Resolution;

import dk.openesdh.repo.services.audit.AuditSearchService;
import dk.openesdh.repo.services.cases.CaseService;
import dk.openesdh.repo.webscripts.PageableWebScript;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

@Component
@WebScript(description = "Retrieve history about a case", defaultFormat = "json", families = "Case Tools")
public class CaseHistoryWebScript {

    private final Logger logger = LoggerFactory.getLogger(CaseHistoryWebScript.class);

    @Autowired
    private AuditSearchService auditSearchService;
    @Autowired
    @Qualifier("CaseService")
    private CaseService caseService;

    @Uri(value = "/api/openesdh/case/{caseId}/history", method = HttpMethod.GET, defaultFormat = "json")
    public Resolution getHistoryByCaseId(
            @UriVariable(WebScriptUtils.CASE_ID) final String caseId,
            @Header("x-range") final String rangeHeader,
            WebScriptResponse res
    ) throws IOException {
        NodeRef caseNodeRef = caseService.getCaseById(caseId);
        return getHistoryByCaseNodeRef(caseNodeRef, rangeHeader, res);
    }

    @Uri(value = "/api/openesdh/casehistory", method = HttpMethod.GET, defaultFormat = "json")
    public Resolution getHistoryByCaseNodeRef(
            @RequestParam(value = WebScriptUtils.NODE_REF) NodeRef caseNodeRef,
            @Header("x-range") final String rangeHeader,
            WebScriptResponse res
    ) throws IOException {
        int startIndex = 0;
        int pageSize = PageableWebScript.DEFAULT_PAGE_SIZE;

        int[] range = PageableWebScript.parseRangeHeader(rangeHeader);
        if (range != null) {
            logger.debug("Range: {} - {}", range[0], range[1]);
            startIndex = range[0];
            pageSize = range[1] - range[0];
        }

        JSONArray allResults = auditSearchService.getAuditLogByCaseNodeRef(caseNodeRef, 1000);
        int totalResults = allResults.size();

        int resultsEnd = startIndex + pageSize;
        if (resultsEnd > totalResults) {
            resultsEnd = totalResults;
        }

        JSONArray results = new JSONArray();
        results.addAll(allResults.subList(startIndex, resultsEnd));
        res.setHeader("Content-Range", "items " + startIndex + "-" + resultsEnd + "/" + totalResults);
        return WebScriptUtils.jsonResolution((results));

    }

}
