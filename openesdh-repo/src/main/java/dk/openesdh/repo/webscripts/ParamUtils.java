package dk.openesdh.repo.webscripts;

import java.util.Map;
import java.util.Objects;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class ParamUtils {

    private static final String NODE_ID = "id";
    private static final String STORE_ID = "store_id";
    private static final String STORE_TYPE = "store_type";

    public static String getRequiredParameter(WebScriptRequest req, String paramKey) {
        String result = getOptionalParameter(req, paramKey);
        checkRequiredParam(result, paramKey);
        return result;
    }

    public static String[] getRequiredParameters(WebScriptRequest req, String paramKey) {
        String[] result = req.getParameterValues("authorityNodeRefs");
        checkRequiredParam(result, paramKey);
        return result;
    }

    public static String getOptionalParameter(WebScriptRequest req, String paramKey) {
        return req.getParameter(paramKey);
    }

    public static String getOptionalParameter(WebScriptRequest req, String paramKey, String defaultValue) {
        return StringUtils.defaultIfBlank(getOptionalParameter(req, paramKey), defaultValue);
    }

    public static void checkRequiredParam(Object param, String paramName) throws WebScriptException {
        if (param == null || StringUtils.isEmpty(param.toString())) {
            throw new WebScriptException("Required parameter is not defined: " + paramName);
        }
    }

    public static String getRequiredTemplateParam(WebScriptRequest req, String paramKey) {
        Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
        String value = templateArgs.get(paramKey);
        ParamUtils.checkRequiredParam(value, paramKey);
        return value;
    }

    public static NodeRef getNodeRef(WebScriptRequest req) {
        Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
        NodeRef nodeRef = null;
        String storeType = templateArgs.get(STORE_TYPE);
        String storeId = templateArgs.get(STORE_ID);
        String nodeId = templateArgs.get(NODE_ID);
        if (storeType != null && storeId != null && nodeId != null) {
            nodeRef = new NodeRef(storeType, storeId, nodeId);
        }
        return nodeRef;
    }

    public static String getOrNull(JSONObject json, String key) {
        if (json.containsKey(key)) {
            return StringUtils.defaultIfEmpty(Objects.toString(json.get(key)), null);
        }
        return null;
    }
}
