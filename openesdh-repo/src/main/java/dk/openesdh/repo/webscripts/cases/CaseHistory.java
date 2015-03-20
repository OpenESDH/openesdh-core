package dk.openesdh.repo.webscripts.cases;

import dk.openesdh.repo.services.audit.AuditSearchService;
import dk.openesdh.repo.services.NodeInfoService;
import dk.openesdh.repo.services.cases.CaseService;
import dk.openesdh.repo.webscripts.xsearch.XSearchWebscript;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.JSONArray;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import org.springframework.extensions.surf.util.I18NUtil;


import java.io.IOException;
import java.util.List;

/**
 * Created by flemming on 19/09/14.
 */
public class CaseHistory extends XSearchWebscript {

    private AuditSearchService auditSearchService;

    public void setAuditSearchService(AuditSearchService auditSearchService) {
        this.auditSearchService = auditSearchService;
    }


    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        NodeRef caseNodeRef = new NodeRef(req.getParameter("nodeRef"));

        int startIndex = 0;
        int pageSize = defaultPageSize;

        String rangeHeader = req.getHeader("x-range");
        int[] range = parseRangeHeader(rangeHeader);
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

        res.setContentEncoding("UTF-8");
        res.setHeader("Content-Range", "items " + startIndex +
                "-" + resultsEnd + "/" + totalResults);
        res.getWriter().write(JSONArray.toJSONString(results));
    }


}
