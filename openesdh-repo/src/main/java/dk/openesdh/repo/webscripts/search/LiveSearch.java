package dk.openesdh.repo.webscripts.search;

import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.stereotype.Component;

import com.github.dynamicextensionsalfresco.webscripts.annotations.*;
import com.github.dynamicextensionsalfresco.webscripts.resolutions.Resolution;

import dk.openesdh.repo.services.search.LiveSearchService;
import dk.openesdh.repo.utils.Utils;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

/**
 * IMPORTANT
 * Please note that this isn't the function/method responsible for returning the results on the search page.
 * For that refer to openesdh-repo/src/main/amp/config/alfresco/extension/templates/webscripts/org/alfresco/slingshot/search/search.lib.js#L139
 * or on github https://github.com/OpenESDH/openesdh-core/blob/develop/openesdh-repo/src/main/amp/config/alfresco/extension/templates/webscripts/org/alfresco/slingshot/search/search.lib.js#L139
 */
@Component
@WebScript(families = {"OpenESDH search"}, defaultFormat = "json", description = "Contextual Live Search Webscripts")
public class LiveSearch {

    private final Logger logger = LoggerFactory.getLogger(LiveSearch.class);

    @Autowired
    @Qualifier("LiveSearchService")
    private LiveSearchService liveSearchService;

    @Uri(value = "/api/openesdh/live-search/{context}?t={term}", method = HttpMethod.GET, defaultFormat = "json")
    public Resolution execute(WebScriptRequest req, @UriVariable final String context, @RequestParam(required = false) final String filter) throws JSONException {
        Map<String, String> params = Utils.parseParameters(req.getURL());
        int maxResults = 3;
        try {
            maxResults = Integer.parseInt(params.get("maxResults"));
        } catch (NumberFormatException nfe) {
            if (logger.isDebugEnabled()) {
                logger.warn("\n\n-----> Max results parameter was unreadable from the webscript request parameter:\n\t\t\t{}", nfe.getLocalizedMessage());
            }
        }

        JSONObject response = liveSearchService.search(context, params.get("t"), maxResults);

        return WebScriptUtils.jsonResolution(response);
    }

}
