package dk.openesdh.repo.webscripts.cases;

import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.dynamicextensionsalfresco.webscripts.annotations.Authentication;
import com.github.dynamicextensionsalfresco.webscripts.annotations.AuthenticationType;
import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.RequestParam;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.UriVariable;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import com.github.dynamicextensionsalfresco.webscripts.resolutions.Resolution;

import dk.openesdh.repo.exceptions.DomainException;
import dk.openesdh.repo.services.cases.CaseService;
import dk.openesdh.repo.webscripts.WebScriptParams;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

/**
 * Created by torben on 11/09/14.
 */
@Component
@WebScript(description = "Retrieves case info either by caseId or case nodeRef", defaultFormat = "json", baseUri = "/api/openesdh/caseinfo", families = "Case Tools")
public class CaseInfoWebScript {

    @Autowired
    private CaseService caseService;

    @Authentication(AuthenticationType.USER)
    @Uri(value = "/{caseId}", method = HttpMethod.GET)
    public Resolution getCaseInfoById(@UriVariable(WebScriptParams.CASE_ID) final String caseId) throws JSONException {
        NodeRef caseNodeRef = caseService.getCaseById(caseId);
        if (caseNodeRef == null) {
            throw new DomainException("CASE.CASE_NOT_FOUND");
        }
        return getCaseInfo(caseNodeRef);
    }

    @Authentication(AuthenticationType.USER)
    @Uri(method = HttpMethod.GET)
    public Resolution getCaseInfoByNodeRef(@RequestParam(value = WebScriptParams.NODE_REF) NodeRef caseNodeRef) throws JSONException {
        return getCaseInfo(caseNodeRef);
    }

    private Resolution getCaseInfo(NodeRef caseNodeRef) throws JSONException {
        JSONObject json = caseService.getCaseInfoJson(caseNodeRef);
        return WebScriptUtils.jsonResolution(json);
    }
}
