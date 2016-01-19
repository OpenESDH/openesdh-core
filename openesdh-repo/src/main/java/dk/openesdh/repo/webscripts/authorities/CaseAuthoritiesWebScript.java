package dk.openesdh.repo.webscripts.authorities;

import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.RequestParam;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.UriVariable;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import com.github.dynamicextensionsalfresco.webscripts.resolutions.Resolution;

import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.stereotype.Component;

import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

@Component
@WebScript(description = "Get groups/users for case by case type", families = {"Authorities"})
public class CaseAuthoritiesWebScript extends AuthoritiesWebScript {

    private static final String CREATED_ON_OPEN_E = "OPENE";

    @Uri(value = "/api/openesdh/{caseType}/authorities", method = HttpMethod.GET, defaultFormat = "json")
    public Resolution getCaseAuthorities(
            @UriVariable final String caseType,
            @RequestParam(required = false) final String filter) throws JSONException {
        List<JSONObject> allGroups = getGroups(filter);
        //groups for case type
        List<JSONObject> jsonAuthorities = filterAuthoritiesByType(allGroups, caseType);
        //other groups created in openE
        jsonAuthorities.addAll(filterAuthoritiesByType(allGroups, null));
        //users
        jsonAuthorities.addAll(getPeople(filter));
        return WebScriptUtils.jsonResolution(formatJson(jsonAuthorities));
    }

    private List<JSONObject> filterAuthoritiesByType(List<JSONObject> allGroups, String caseType) {
        return allGroups.stream()
                .filter(json -> isGroupCreatedInOpenEAndFilteredByCaseType(json, caseType))
                .collect(Collectors.toList());
    }

    private boolean isGroupCreatedInOpenEAndFilteredByCaseType(JSONObject json, String caseType) {
        try {
            return caseType == null && CREATED_ON_OPEN_E.equals(json.get(OpenESDHModel.PROP_OE_OPENE_TYPE.getLocalName()))
                    || (caseType != null && caseType.equals(json.get(OpenESDHModel.PROP_OE_OPENE_TYPE.getLocalName())));
        } catch (JSONException ex) {
            throw new WebScriptException(ex.getMessage(), ex);
        }
    }

}
