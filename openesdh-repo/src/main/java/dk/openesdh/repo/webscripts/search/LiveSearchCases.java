package dk.openesdh.repo.webscripts.search;

import dk.openesdh.repo.model.CaseInfo;
import dk.openesdh.repo.services.cases.CaseService;
import dk.openesdh.repo.utils.Utils;
import org.alfresco.util.PropertyCheck;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class LiveSearchCases extends DeclarativeWebScript {

    //<editor-fold desc="injected services and initialised properties">
    private CaseService caseService;

    public void setCaseService(CaseService caseService) {
        this.caseService = caseService;
    }
    //</editor-fold>

    private static Logger logger = Logger.getLogger(LiveSearchCases.class);

    public void init() {
        PropertyCheck.mandatory(this, "CaseService", caseService);
    }

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req,  Status status, Cache cache) {
        Map<String, String> params = Utils.parseParameters(req.getURL());
        Map<String, Object> model = new HashMap<String, Object>();
        int maxResults = 3;
        try {
            maxResults = Integer.parseInt(params.get("maxResults"));
        }
        catch (NumberFormatException nfe){
            if(logger.isDebugEnabled())
                logger.warn("\n\n-----> Max results parameter was unreadable from the webscript request parameter:\n\t\t\t"+ nfe.getLocalizedMessage());
        }

        try {
            List<CaseInfo> foundCases = this.caseService.findCases(params.get("t"), maxResults );
            JSONArray jsonArray = buildJSON(foundCases);
            model.put("caseList", jsonArray);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return model;
    }

    JSONArray buildJSON(List<CaseInfo> cases) throws JSONException {
        JSONArray result = new JSONArray();
        for(CaseInfo caseItem : cases){
            JSONObject caseObj = new JSONObject();
            caseObj.put("caseId", caseItem.getCaseId());
            caseObj.put("caseTitle",caseItem.getTitle());
            caseObj.put("caseEndDate",caseItem.getEndDate());
            caseObj.put("caseStartDate",caseItem.getStartDate());
            caseObj.put("caseCreatedDate",caseItem.getCreatedDate());
            caseObj.put("caseDescription",caseItem.getDescription());
            result.put(caseObj);
        }
        return result;
    }
}
