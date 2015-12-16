package dk.openesdh.repo.webscripts.authorities;

import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.security.authority.AuthorityInfo;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.servlet.FormData.FormField;
import org.springframework.stereotype.Component;

import com.github.dynamicextensionsalfresco.webscripts.annotations.FileField;
import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.RequestParam;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.UriVariable;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import com.github.dynamicextensionsalfresco.webscripts.resolutions.Resolution;

import dk.openesdh.repo.services.authorities.GroupsService;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

@Component
@WebScript(description = "Manage groups", families = {"Authorities"})
public class GroupsWebScript {

    private static final String CREATED_ON_OPEN_E = "OPENE";
    private static final int MAX_ITEMS = 10000;
    private static final String CSV_HEADER = "Group name,Display name,Member of groups,Simple case,Staff case\n";
    
    @Autowired
    private AuthorityService authorityService;
    @Autowired
    private NodeService nodeService;
    @Autowired
    @Qualifier("GroupsService")
    private GroupsService groupsService;

    @Uri(value = "/api/groups/{shortName}/create", method = HttpMethod.POST, defaultFormat = "json")
    public Resolution createGroup(
            @UriVariable final String shortName,
            @RequestParam(required = true) final String displayName
    ) throws JSONException {
        String fullName = authorityService.createAuthority(AuthorityType.GROUP, shortName, displayName, authorityService.getDefaultZones());
        groupsService.addAspectTypeOPENE(fullName);
        return WebScriptUtils.jsonResolution(toGroupJSON(new AuthorityInfo(null, displayName, fullName)));
    }

    @Uri(value = "/api/groups/list/{type}", method = HttpMethod.GET, defaultFormat = "json")
    public Resolution getAuthorities(
            @UriVariable final String type,
            @RequestParam(required = false, defaultValue = AuthorityService.ZONE_APP_DEFAULT) final String zone,
            @RequestParam(required = false) final String filter,
            @RequestParam(required = false, defaultValue = "displayName") final String sortBy,
            @RequestParam(required = false, defaultValue = "true") final Boolean sortAsc,
            @RequestParam(required = false, defaultValue = "0") final Integer skipCount,
            @RequestParam(required = false, defaultValue = "" + MAX_ITEMS) final Integer maxItems
    ) throws JSONException {
        Stream<AuthorityInfo> stream;
        JSONObject pagingJson = new JSONObject();
        pagingJson.put("skipCount", skipCount);
        pagingJson.put("maxItems", maxItems);
        switch (type) {
            case "ALL": {
                PagingRequest paging = new PagingRequest(skipCount, maxItems);
                paging.setRequestTotalCountMax(MAX_ITEMS);
                PagingResults<AuthorityInfo> groups = authorityService.getAuthoritiesInfo(AuthorityType.GROUP, zone, filter, sortBy, sortAsc, paging);
                pagingJson.put("totalItems", groups.getTotalResultCount().getFirst());
                stream = groups.getPage().stream();
                break;
            }
            case "SYS": {
                stream = getFilteredAndPagedGroups(info -> !groupsService.hasAspectTypeOPENE(info.getAuthorityName()),
                        zone, filter, sortBy, sortAsc, type, pagingJson, skipCount, maxItems);
                break;
            }
            case "OE": {
                stream = getFilteredAndPagedGroups(info -> groupsService.hasAspectTypeOPENE(info.getAuthorityName()),
                        zone, filter, sortBy, sortAsc, type, pagingJson, skipCount, maxItems);
                break;
            }
            default: {
                stream = getFilteredAndPagedGroups(info -> groupsService.typeEqualsOpenEType(type, info.getAuthorityName()),
                        zone, filter, sortBy, sortAsc, type, pagingJson, skipCount, maxItems);
                break;
            }
        }
        List<JSONObject> jsonAuthorities = stream.map(this::toGroupJSON).collect(Collectors.toList());
        JSONObject json = new JSONObject()
                .put("data", new JSONArray(jsonAuthorities))
                .put("paging", pagingJson);
        return WebScriptUtils.jsonResolution(json);
    }

    @Uri(value = "/api/openesdh/groups/upload", multipartProcessing = true, method = HttpMethod.POST)
    public Resolution uploadGroupsCSVFile(@FileField("filedata") FormField fileField) throws IOException,
            JSONException {
        JSONObject json = new JSONObject();
        try {
            groupsService.uploadGroupsCSV(fileField.getInputStream());
            json.put("STATUS", "SUCCESS");
        } catch (Exception ex) {
            json.put("STATUS", "FAILED");
            json.put("message", ex.getMessage());
            return WebScriptUtils.jsonResolution(json);
        }
        json.put("message", "Successfully uploaded");
        return WebScriptUtils.jsonResolution(json);
    }

    @Uri(value = "/api/openesdh/groups/upload/sample", method = HttpMethod.GET)
    public void downloadCsvFileSample(WebScriptResponse res) throws IOException {
        res.addHeader("Content-Disposition", "attachment; filename=ExampleGroupUpload.csv");
        res.getWriter().append(CSV_HEADER);
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
}
