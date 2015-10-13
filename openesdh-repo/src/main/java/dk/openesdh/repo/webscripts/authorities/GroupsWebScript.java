package dk.openesdh.repo.webscripts.authorities;

import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import com.github.dynamicextensionsalfresco.webscripts.resolutions.Resolution;
import dk.openesdh.repo.model.OpenESDHModel;
import static dk.openesdh.repo.webscripts.ParamUtils.getOptionalParameter;
import static dk.openesdh.repo.webscripts.ParamUtils.getRequiredParameter;
import static dk.openesdh.repo.webscripts.ParamUtils.getRequiredTemplateParam;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.security.authority.AuthorityInfo;
import org.alfresco.service.cmr.dictionary.InvalidAspectException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

@Component
@WebScript(description = "Manage groups", families = {"Authorities"})
public class GroupsWebScript {

    private static final String CREATED_ON_OPEN_E = "OPENE";
    private static final String PARAM_SHORT_NAME_KEY = "shortName";
    private static final String PARAM_DISPLAY_NAME_KEY = "displayName";
    private static final int MAX_ITEMS = 10000;

    @Autowired
    private AuthorityService authorityService;
    @Autowired
    private NodeService nodeService;

    @Uri(value = "/api/groups/{shortName}/create", method = HttpMethod.POST, defaultFormat = "json")
    public Resolution createGroup(WebScriptRequest req, WebScriptResponse res) throws JSONException {
        String shortName = getRequiredTemplateParam(req, PARAM_SHORT_NAME_KEY);
        String displayName = getRequiredParameter(req, PARAM_DISPLAY_NAME_KEY);
        String fullName = authorityService.createAuthority(AuthorityType.GROUP, shortName, displayName, authorityService.getDefaultZones());
        addAspectTypeOPENE(fullName);
        return WebScriptUtils.jsonResolution(toGroupJSON(new AuthorityInfo(null, displayName, fullName)));
    }

    @Uri(value = "/api/groups/list/{type}", method = HttpMethod.GET, defaultFormat = "json")
    public Resolution getAuthorities(WebScriptRequest req, WebScriptResponse res) throws JSONException {
        String type = getRequiredTemplateParam(req, "type");
        String filter = getOptionalParameter(req, "filter");
        String sortBy = getOptionalParameter(req, "sortBy", "displayName");
        boolean sortAsc = BooleanUtils.toBoolean(getOptionalParameter(req, "sortAsc", "true"));
        int skipCount = NumberUtils.toInt(getOptionalParameter(req, "skipCount"));
        int maxItems = NumberUtils.toInt(getOptionalParameter(req, "maxItems"), MAX_ITEMS);
        Stream<AuthorityInfo> stream;
        JSONObject pagingJson = new JSONObject();
        pagingJson.put("skipCount", skipCount);
        pagingJson.put("maxItems", maxItems);
        switch (type) {
            case "ALL": {
                String zone = getOptionalParameter(req, "zone");
                PagingRequest paging = new PagingRequest(skipCount, maxItems);
                paging.setRequestTotalCountMax(MAX_ITEMS);
                PagingResults<AuthorityInfo> groups = authorityService.getAuthoritiesInfo(AuthorityType.GROUP, zone, filter, sortBy, sortAsc, paging);
                pagingJson.put("totalItems", groups.getTotalResultCount().getFirst());
                stream = groups.getPage().stream();
                break;
            }
            case "SYS": {
                String zone = getOptionalParameter(req, "zone");
                stream = getFilteredAndPagedGroups(info -> !hasAspectTypeOPENE(info.getAuthorityName()),
                        zone, filter, sortBy, sortAsc, type, pagingJson, skipCount, maxItems);
                break;
            }
            case "OE": {
                String zone = getOptionalParameter(req, "zone", AuthorityService.ZONE_APP_DEFAULT);
                stream = getFilteredAndPagedGroups(info -> hasAspectTypeOPENE(info.getAuthorityName()),
                        zone, filter, sortBy, sortAsc, type, pagingJson, skipCount, maxItems);
                break;
            }
            default: {
                String zone = getOptionalParameter(req, "zone", AuthorityService.ZONE_APP_DEFAULT);
                stream = getFilteredAndPagedGroups(info -> typeEqualsOpenEType(type, info.getAuthorityName()),
                        zone, filter, sortBy, sortAsc, type, pagingJson, skipCount, maxItems);
                break;
            }
        }
        List<JSONObject> jsonAuthorities = stream.map(info -> toGroupJSON(info)).collect(Collectors.toList());
        JSONObject json = new JSONObject()
                .put("data", new JSONArray(jsonAuthorities))
                .put("paging", pagingJson);
        return WebScriptUtils.jsonResolution(json);
    }

    private Stream<AuthorityInfo> getFilteredAndPagedGroups(Predicate<AuthorityInfo> streamFilter,
            String zone, String filter, String sortBy, boolean sortAsc, String type, JSONObject pagingJson,
            int skipCount, int maxItems) throws JSONException {
        Stream<AuthorityInfo> stream;
        PagingRequest paging = new PagingRequest(MAX_ITEMS);
        PagingResults<AuthorityInfo> groups = authorityService.getAuthoritiesInfo(AuthorityType.GROUP, zone, filter, sortBy, sortAsc, paging);
        List<AuthorityInfo> collected = groups.getPage()
                .stream()
                .filter(streamFilter)
                .collect(Collectors.toList());
        pagingJson.put("totalItems", collected.size());
        stream = collected.stream()
                .skip(skipCount)
                .limit(maxItems);
        return stream;
    }

    private JSONObject toGroupJSON(AuthorityInfo info) {
        try {
            JSONObject json = new JSONObject();
            json.put("authorityType", AuthorityType.GROUP);
            json.put("shortName", info.getShortName());
            json.put("fullName", info.getAuthorityName());
            json.put("displayName", info.getShortName());
            json.put("url", "/api/groups/" + info.getShortName());
            //uncoment if needed (properties exists in original /alfresco/service/api/groups
            //json.put("zones", "");
            return json;
        } catch (JSONException ex) {
            throw new WebScriptException(ex.getMessage(), ex);
        }
    }

    private boolean typeEqualsOpenEType(String type, String authorityName) throws InvalidNodeRefException {
        return type.equals(nodeService.getProperty(authorityService.getAuthorityNodeRef(authorityName), OpenESDHModel.PROP_OE_OPENE_TYPE));
    }

    private boolean hasAspectTypeOPENE(String authorityName) {
        return nodeService.hasAspect(authorityService.getAuthorityNodeRef(authorityName), OpenESDHModel.ASPECT_OE_OPENE_TYPE);
    }

    private void addAspectTypeOPENE(String fullName) throws InvalidNodeRefException, InvalidAspectException {
        NodeRef nodeRef = authorityService.getAuthorityNodeRef(fullName);
        Map<QName, Serializable> aspectProps = new HashMap<>();
        aspectProps.put(OpenESDHModel.PROP_OE_OPENE_TYPE, CREATED_ON_OPEN_E);
        nodeService.addAspect(nodeRef, OpenESDHModel.ASPECT_OE_OPENE_TYPE, aspectProps);
    }
}
