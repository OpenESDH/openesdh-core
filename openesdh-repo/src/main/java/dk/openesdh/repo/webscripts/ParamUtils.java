package dk.openesdh.repo.webscripts;

import org.apache.commons.lang.StringUtils;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class ParamUtils {

    public static String getRequiredParameter(WebScriptRequest req, String paramKey) {
        String result = getOptionalParameter(req, paramKey);
        if (StringUtils.isEmpty(result)) {
            throw new WebScriptException("Required parameter is not defined: " + paramKey);
        }
        return result;
    }

    public static String[] getRequiredParameters(WebScriptRequest req, String paramKey) {
        String[] result = req.getParameterValues("authorityNodeRefs");
        if (result == null) {
            throw new WebScriptException("Required parameter is not defined: " + paramKey);
        }
        return result;
    }

    public static String getOptionalParameter(WebScriptRequest req, String paramKey) {
        return req.getParameter(paramKey);
    }

}
