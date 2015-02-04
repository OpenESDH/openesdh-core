package dk.openesdh.share.evaluator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.RequestContext;
import org.springframework.extensions.surf.extensibility.ExtensionModuleEvaluator;

import java.util.Map;

/**
 * @author Lanre Abiwon.
 */
public class OpenESDHCaseModuleEvaluator implements ExtensionModuleEvaluator {
    private static Log logger = LogFactory.getLog(OpenESDHCaseModuleEvaluator.class);
    private final String CASE_STATUS = "caseStatus";
    private final String CASE_TYPE = "caseType";
    private CaseEvaluatorUtil caseEvaluatorUtil;

    @Override
    public String[] getRequiredProperties(){
        return new String[] {CASE_STATUS, CASE_TYPE};
    }

    @Override
    public boolean applyModule(RequestContext context, Map<String, String> evaluationProperties) {
        JSONObject caseDetails  = caseEvaluatorUtil.getCaseDetails(context);

        try {
            // Meaning we got the case details back
            if (caseDetails != null) {
                String caseType = (String) caseDetails.get("caseType");
                String caseStatus = (String) caseDetails.get("caseStatus");

                // Test case types filter
                if (!caseType.matches(caseEvaluatorUtil.getEvaluatorParam(evaluationProperties, CASE_TYPE, ".*"))) {
                    return false;
                }

                // Test case status filter
                if (caseStatus == null || !caseStatus.matches(caseEvaluatorUtil.getEvaluatorParam(evaluationProperties, CASE_STATUS, ".*"))) {
                    return false;
                }
                return true;
            }
        }
        catch (JSONException jse){
            logger.error("Problem with populating the json object: " + jse.getMessage());
        }

        return false;
    }

    public void setCaseEvaluatorUtil(CaseEvaluatorUtil caseEvaluatorUtil) {
        this.caseEvaluatorUtil = caseEvaluatorUtil;
    }
}
