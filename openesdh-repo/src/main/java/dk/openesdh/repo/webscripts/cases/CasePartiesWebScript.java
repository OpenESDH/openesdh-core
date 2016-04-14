package dk.openesdh.repo.webscripts.cases;

import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.stereotype.Component;

import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.UriVariable;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import com.github.dynamicextensionsalfresco.webscripts.resolutions.Resolution;

import dk.openesdh.repo.services.cases.PartyService;
import dk.openesdh.repo.webscripts.WebScriptParams;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

@Component
@WebScript(description = "Manage case parties", families = {"Case Tools"})
public class CasePartiesWebScript {

    @Autowired
    @Qualifier(PartyService.BEAN_ID)
    private PartyService partyService;

    @Uri(value = "/api/openesdh/case/{caseId}/parties", method = HttpMethod.GET, defaultFormat = "json")
    public Resolution get(@UriVariable final String caseId) throws JSONException {
        return WebScriptUtils.jsonResolution(partyService.getCasePartiesJson(caseId));
    }

    @Uri(value = "/api/openesdh/case/{caseId}/party", method = HttpMethod.POST, defaultFormat = "json")
    public void post(
            @UriVariable final String caseId,
            WebScriptRequest req) {
        JSONObject json = WebScriptUtils.readJson(req);
        JSONArray contacts = (JSONArray) json.get(PartyService.FIELD_CONTACT_IDS);
        NodeRef roleRef = new NodeRef((String) json.get(PartyService.FIELD_ROLE_REF));
        partyService.addCaseParty(caseId, roleRef, contacts);
    }

    @Uri(value = "/api/openesdh/case/{caseId}/party", method = HttpMethod.PUT, defaultFormat = "json")
    public void put(
            @UriVariable final String caseId,
            WebScriptRequest req) {
        JSONObject json = WebScriptUtils.readJson(req);
        NodeRef roleRef = new NodeRef((String) json.get(PartyService.FIELD_ROLE_REF));
        NodeRef casePartyRef = new NodeRef((String) json.get(PartyService.FIELD_NODE_REF));
        partyService.updateCaseParty(casePartyRef, roleRef);
    }

    @Uri(value = "/api/openesdh/case/{caseId}/party/{storeType}/{storeId}/{id}", method = HttpMethod.DELETE, defaultFormat = "json")
    public void delete(
            @UriVariable final String caseId,
            @UriVariable(WebScriptParams.STORE_TYPE) String storeType,
            @UriVariable(WebScriptParams.STORE_ID) String storeId, 
            @UriVariable(WebScriptParams.ID) String id) {
        NodeRef partyRef = new NodeRef(storeType, storeId, id);
        partyService.removeCaseParty(caseId, partyRef);
    }
}
