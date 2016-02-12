package dk.openesdh.repo.webscripts.search;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.util.PropertyCheck;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import dk.openesdh.repo.model.CaseInfo;
import dk.openesdh.repo.services.cases.CaseService;
import dk.openesdh.repo.utils.Utils;

@Deprecated
public class LiveSearchCases extends DeclarativeWebScript {

    private final Logger logger = LoggerFactory.getLogger(LiveSearchCases.class);

    private CaseService caseService;

    public void setCaseService(CaseService caseService) {
        this.caseService = caseService;
    }

    public void init() {
        PropertyCheck.mandatory(this, "CaseService", caseService);
    }

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
        Map<String, String> params = Utils.parseParameters(req.getURL());
        Map<String, Object> model = new HashMap<>();
        int maxResults = 3;
        try {
            maxResults = Integer.parseInt(params.get("maxResults"));
        } catch (NumberFormatException nfe) {
            if (logger.isDebugEnabled()) {
                logger.warn("\n\n-----> Max results parameter was unreadable from the webscript request parameter:\n\t\t\t" + nfe.getLocalizedMessage());
            }
        }

        try {
            List<CaseInfo> foundCases = this.caseService.findCases(params.get("t"), maxResults);
            JSONArray jsonArray = buildJSON(foundCases);
            model.put("caseList", jsonArray);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return model;
    }

    JSONArray buildJSON(List<CaseInfo> cases) throws JSONException {
        JSONArray result = new JSONArray();
        for (CaseInfo caseItem : cases) {
            JSONObject caseObj = new JSONObject();
            caseObj.put("caseNodeRef", caseItem.getNodeRef());
            caseObj.put("caseId", caseItem.getCaseId());
            caseObj.put("caseTitle", caseItem.getTitle());
            caseObj.put("caseEndDate", caseItem.getEndDate());
            caseObj.put("caseStartDate", caseItem.getStartDate());
            caseObj.put("caseCreatedDate", caseItem.getCreatedDate());
            caseObj.put("caseDescription", caseItem.getDescription());
            result.put(caseObj);
        }
        return result;
    }
}
