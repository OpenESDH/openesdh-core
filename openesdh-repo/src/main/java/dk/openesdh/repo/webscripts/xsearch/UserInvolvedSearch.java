package dk.openesdh.repo.webscripts.xsearch;

import java.util.Map;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Extends XSearchWebscript to pass the currently authenticated user to the
 * xSearchService.
 */
public class UserInvolvedSearch extends XSearchWebscript {

    @Override
    protected Map<String, String> getParams(WebScriptRequest req) {
        Map<String, String> params = super.getParams(req);
        params.put("user", AuthenticationUtil.getFullyAuthenticatedUser());
        return params;
    }
}
