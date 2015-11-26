package dk.openesdh.repo.webscripts.cases;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.util.StringUtils;

import dk.openesdh.repo.services.audit.AuditSearchService;
import dk.openesdh.repo.services.cases.CaseService;
import dk.openesdh.repo.webscripts.PageableWebScript;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;
import dk.openesdh.repo.webscripts.xsearch.XSearchWebscript;

/**
 * Created by flemming on 19/09/14.
 */
public class CaseHistoryWebScript extends AbstractWebScript {

    private static Logger logger = Logger.getLogger(XSearchWebscript.class);

    private AuditSearchService auditSearchService;

    private CaseService caseService;

    public void setAuditSearchService(AuditSearchService auditSearchService) {
        this.auditSearchService = auditSearchService;
    }

    public void setCaseService(CaseService caseService) {
        this.caseService = caseService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        NodeRef caseNodeRef = null;
        String caseNodeRefParam = req.getParameter(WebScriptUtils.NODE_REF);
        if (!StringUtils.isEmpty(caseNodeRefParam)) {
            caseNodeRef = new NodeRef(caseNodeRefParam);
        } else {
            Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
            String caseId = templateArgs.get(WebScriptUtils.CASE_ID);
            caseNodeRef = caseService.getCaseById(caseId);
        }

        int startIndex = 0;
        int pageSize = PageableWebScript.DEFAULT_PAGE_SIZE;

        String rangeHeader = req.getHeader("x-range");
        int[] range = PageableWebScript.parseRangeHeader(rangeHeader);
        if (range != null) {
            logger.debug("Range: " + range[0] + " - " + range[1]);
            startIndex = range[0];
            pageSize = range[1] - range[0];
        }

        JSONArray allResults = auditSearchService.getAuditLogByCaseNodeRef(caseNodeRef, 1000);
        int totalResults = allResults.size();

        int resultsEnd = startIndex + pageSize;
        if (resultsEnd > totalResults) {
            resultsEnd = totalResults;
        }

        List results = allResults.subList(startIndex, resultsEnd);

        res.setContentEncoding(WebScriptUtils.CONTENT_ENCODING_UTF_8);
        res.setHeader("Content-Range", "items " + startIndex +
                "-" + resultsEnd + "/" + totalResults);
        res.getWriter().write(JSONArray.toJSONString(results));
    }


}
