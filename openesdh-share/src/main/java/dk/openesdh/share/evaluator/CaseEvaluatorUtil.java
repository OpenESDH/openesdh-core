package dk.openesdh.share.evaluator;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.RequestContext;
import org.springframework.extensions.surf.WebFrameworkServiceRegistry;
import org.springframework.extensions.surf.util.Pair;
import org.springframework.extensions.webscripts.ScriptRemote;
import org.springframework.extensions.webscripts.connector.Response;

import java.util.Map;

/**
 * Utility class for evaluators to pick values from the request and get case information etc.
 * Based on Erik Winlof's SlingshotEvaluatorUtil.java ootb share code
 *
 * @author Lanre Abiwon
 */

public class CaseEvaluatorUtil {
    private static Log logger = LogFactory.getLog(CaseEvaluatorUtil.class);

    private final String CASE_STATUS = "caseStatus";
    private final String  CASE_ID = "caseId";
    //Added this to catch nodeRefs in the pages where caseIDs aren't possible
    //e.g when we need to create content(create-content page) or view document details
    private final String  CASE_NODEREF = "nodeRef";
    private final String  DESTINATION = "destination";
    private final String  TARGET_CASE = "targetCase";

    private WebFrameworkServiceRegistry serviceRegistry;


    /**
     * Helper for getting an evaluator parameter trimmed OR defaultValue if no value has been provided.
     *
     * @param params
     * @param name
     * @param defaultValue
     * @return A trimmed evaluator parameter OR defaultValue if no value has been provided.
     */
    String getEvaluatorParam(Map<String, String> params, String name, String defaultValue) {
        String value = params.get(name);
        if (value != null && StringUtils.isNotEmpty(value)) {
            return value;
        }
        return defaultValue;
    }

    /**
     * Returns the current site id OR null if we aren't in a site
     *
     * @param context
     * @return The current site id OR null if we aren't in a site
     */
    JSONObject getCaseDetails(RequestContext context){
        String caseId  = context.getUriTokens().get(CASE_ID);

        boolean nodeRefExists = nodeRefBoolPair(context).getFirst();

        if(StringUtils.isEmpty(caseId)) {
            caseId = context.getParameter(CASE_ID);

            if(StringUtils.isEmpty(caseId) && nodeRefExists)
                caseId = nodeRefBoolPair(context).getSecond();
        }

        if(StringUtils.isNotEmpty(caseId)) {
            try {
                JSONObject caseDetails = jsonGet("/api/openesdh/case/noderef/" + caseId);
                if (caseDetails != null && StringUtils.isNotEmpty(caseDetails.getString("caseNodeRef")))
                    return caseDetails;
            }
            catch (JSONException jse){
                logger.error("An error occurred in getting a response from the respository: " + jse.getMessage());
            }
        }
        return null;
    }

    Pair<Boolean, String> nodeRefBoolPair(RequestContext context){
        String destination  = context.getParameter(DESTINATION);
        String urlNodeRef = context.getParameter(CASE_NODEREF);
        String workflowCaseNodeRef = context.getParameter(TARGET_CASE);
        Pair<Boolean, String> result = new Pair<>(false,null);

        String node = findNodeInUrl(destination, urlNodeRef, workflowCaseNodeRef);
        if(StringUtils.isEmpty(node))
            return result;

        try {
            JSONObject isCaseDocResult = jsonGet("/api/openesdh/documents/isCaseDoc/" + node.replace("://", "/"));
            if (isCaseDocResult == null || StringUtils.isEmpty(isCaseDocResult.getString("caseId")))
                return result;

            result.setFirst(true);
            result.setSecond(isCaseDocResult.getString("caseId"));
        }
        catch (JSONException jse){
            logger.warn("Error contacting the repository: "+ jse.getMessage());
        }

        return result;
    }

    String findNodeInUrl(String dst, String nde, String cTarget) {
        if(StringUtils.isNotEmpty(dst) ) return dst;
        else if(StringUtils.isNotEmpty(nde) ) return nde;
        else if(StringUtils.isNotEmpty(cTarget) ) return cTarget;
        else return null;
    }

    /**
     * Helper method for making a json get remote call to the default repository.
     *
     * @param uri The uri to get the content for (MUST contain a json response)
     * @return The content of the uri resource parsed into a json object.
     */
    JSONObject jsonGet(String uri) {
        ScriptRemote scriptRemote = serviceRegistry.getScriptRemote();
        Response response  = scriptRemote.connect().get(uri);
        if (response.getStatus().getCode() == 200) {
            try {
                return new JSONObject(response.getResponse());
            }
            catch (JSONException jse){
                logger.error("An error occurred contacting the repository '" + uri + "': " + jse.getMessage());
            }
        }
        return null;
    }

    public void setServiceRegistry(WebFrameworkServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }
}
