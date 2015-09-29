package dk.openesdh.repo.webscripts;

import org.apache.commons.lang.StringUtils;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class ParamUtils {

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

    public static void checkRequiredParam(Object param, String paramName) throws WebScriptException {
        if (param == null || StringUtils.isEmpty(param.toString())) {
            throw new WebScriptException("Required parameter is not defined: " + paramName);
        }
    }

}
