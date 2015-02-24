package dk.openesdh.repo.webscripts.xsearch;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.apache.log4j.Logger;
import org.springframework.extensions.webscripts.WebScriptRequest;

import java.util.Map;

/**
 * Extends XSearchWebscript to pass the currently authenticated user to the
 * xSearchService.
 */
public class UserInvolvedSearch extends XSearchWebscript {
    protected static Logger log = Logger.getLogger(UserInvolvedSearch.class);

    @Override
    protected Map<String, String> getParams(WebScriptRequest req) {
        Map<String, String> params = super.getParams(req);
        params.put("user", AuthenticationUtil.getFullyAuthenticatedUser());
        return params;
    }
}
