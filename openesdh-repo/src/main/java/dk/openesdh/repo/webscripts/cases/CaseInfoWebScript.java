package dk.openesdh.repo.webscripts.cases;

import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.stereotype.Component;

import com.github.dynamicextensionsalfresco.webscripts.annotations.Authentication;
import com.github.dynamicextensionsalfresco.webscripts.annotations.AuthenticationType;
import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.RequestParam;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.UriVariable;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import com.github.dynamicextensionsalfresco.webscripts.resolutions.Resolution;

import dk.openesdh.repo.services.cases.CaseOwnersService;
import dk.openesdh.repo.services.cases.CaseService;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

/**
 * Created by torben on 11/09/14.
 */
@Component
@WebScript(description = "Retrieves case info either by caseId or case nodeRef", defaultFormat = "json", baseUri = "/api/openesdh/caseinfo", families = "Case Tools")
public class CaseInfoWebScript {

    @Autowired
    private CaseService caseService;

    @Autowired
    private CaseOwnersService caseOwnersService;

    @Authentication(AuthenticationType.USER)
    @Uri(value = "/{caseId}", method = HttpMethod.GET)
    public Resolution getCaseInfoById(@UriVariable(WebScriptUtils.CASE_ID) final String caseId) throws JSONException {
        NodeRef caseNodeRef = caseService.getCaseById(caseId);
        if (caseNodeRef == null) {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "CASE_NOT_FOUND");
        }
        return getCaseInfo(caseNodeRef);
    }

    @Authentication(AuthenticationType.USER)
    @Uri(method = HttpMethod.GET)
    public Resolution getCaseInfoByNodeRef(@RequestParam(value = WebScriptUtils.NODE_REF) NodeRef caseNodeRef) throws JSONException {
        return getCaseInfo(caseNodeRef);
    }

    private Resolution getCaseInfo(NodeRef caseNodeRef) throws JSONException {
        JSONObject json = caseService.getCaseInfoJson(caseNodeRef);
        JSONObject properties = (JSONObject) json.get("properties");
        properties.put("owners", caseOwnersService.getCaseOwners(caseNodeRef));
        return WebScriptUtils.jsonResolution(json);
    }
}
