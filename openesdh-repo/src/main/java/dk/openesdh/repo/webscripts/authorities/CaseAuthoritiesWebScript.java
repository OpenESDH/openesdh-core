package dk.openesdh.repo.webscripts.authorities;

import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import com.github.dynamicextensionsalfresco.webscripts.resolutions.Resolution;
import com.google.common.base.Joiner;
import dk.openesdh.repo.model.OpenESDHModel;
import static dk.openesdh.repo.webscripts.ParamUtils.getRequiredTemplateParam;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.security.authority.AuthorityInfo;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

@Component
@WebScript(description = "Get groups/users for case by case type", families = {"Authorities"})
public class CaseAuthoritiesWebScript {

    private static final String CREATED_ON_OPEN_E = "OPENE";
    private static final int MAX_ITEMS = 10000;
    private static final PagingRequest PAGING_REQUEST = new PagingRequest(MAX_ITEMS);
    private static final List SORT_PROPS = Arrays.asList(
            new Pair(ContentModel.PROP_FIRSTNAME, true),
            new Pair(ContentModel.PROP_LASTNAME, true)
    );
    private static final List<QName> FILTER_PROPS = Arrays.asList(
            ContentModel.PROP_FIRSTNAME,
            ContentModel.PROP_LASTNAME);

    @Autowired
    private AuthorityService authorityService;
    @Autowired
    private NodeService nodeService;
    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private PersonService personService;

    @Uri(value = "/api/openesdh/{caseType}/authorities", method = HttpMethod.GET, defaultFormat = "json")
    public Resolution getCaseAuthorities(WebScriptRequest req, WebScriptResponse res) throws JSONException {
        String caseType = getRequiredTemplateParam(req, "caseType");
        //groups for case type
        List<JSONObject> jsonAuthorities = getAuthoritiesByType(req, caseType);
        //other groups created in openE
        jsonAuthorities.addAll(getAuthoritiesByType(req, null));
        //users
        jsonAuthorities.addAll(getPeople(req));
        return WebScriptUtils.jsonResolution(
                new JSONObject().put("data",
                        new JSONObject().put("items",
                                new JSONArray(jsonAuthorities))));
    }

    private List<JSONObject> getAuthoritiesByType(WebScriptRequest req, String caseType) throws JSONException {
        PagingResults<AuthorityInfo> authorities = authorityService.getAuthoritiesInfo(AuthorityType.GROUP,
                null,//zone
                req.getParameter("filter"),
                "displayName", //sortBy
                true, //sortAsc
                PAGING_REQUEST);
        return authorities.getPage()
                .stream()
                .map(info -> createAuthorityListItemJSON(info))
                .filter(json -> isGroupCreatedInOpenEAndFilteredByCaseType(json, caseType))
                .collect(Collectors.toList());
    }

    private List<JSONObject> getPeople(WebScriptRequest req) {
        PagingResults<PersonService.PersonInfo> people = personService.getPeople(
                req.getParameter("filter"), FILTER_PROPS, SORT_PROPS, PAGING_REQUEST);
        return people.getPage()
                .stream()
                .map(info -> createPersonListJSON(info))
                .collect(Collectors.toList());
    }

    private JSONObject createAuthorityListItemJSON(AuthorityInfo info) {
        return createListJSON(
                AuthorityType.GROUP,
                info.getAuthorityName(),
                info.getShortName());
    }

    private JSONObject createPersonListJSON(PersonService.PersonInfo info) {
        return createListJSON(
                AuthorityType.USER,
                info.getUserName(),
                (info.getFirstName() + " " + info.getLastName()).trim());
    }

    private JSONObject createListJSON(AuthorityType type, String shortName, String name) {
        try {
            JSONObject authorityObj = new JSONObject();
            NodeRef nodeRef = authorityService.getAuthorityNodeRef(shortName);
            authorityObj.put("type", getTypeModelName(type));
            authorityObj.put("parentType", "");
            authorityObj.put("isContainer", false);
            authorityObj.put("name", getDisplayName(type, name, shortName, nodeRef));
            authorityObj.put("shortName", shortName);
            authorityObj.put("title", "");
            authorityObj.put("description", "");
            //uncoment if needed (property exists in original /alfresco/service/api/forms/picker/authority/children
            //authorityObj.put("displayPath", nodeService.getPath(nodeRef).toDisplayPath(nodeService, permissionService));
            authorityObj.put("nodeRef", nodeRef.toString());
            authorityObj.put("selectable", type == AuthorityType.GROUP || authenticationService.getAuthenticationEnabled(shortName));
            if (type == AuthorityType.GROUP) {
                authorityObj.put(OpenESDHModel.PROP_OE_OPENE_TYPE.getLocalName(), nodeService.getProperty(nodeRef, OpenESDHModel.PROP_OE_OPENE_TYPE));
            }
            return authorityObj;
        } catch (JSONException ex) {
            throw new WebScriptException(ex.getMessage(), ex);
        }
    }

    private String getDisplayName(AuthorityType type, String name, String shortName, NodeRef nodeRef) throws InvalidNodeRefException {
        if (StringUtils.isEmpty(name)) {
            switch (type) {
                case GROUP:
                    return authorityService.getAuthorityDisplayName(shortName);
                case USER:
                    return Joiner.on(" ").skipNulls().join(
                            nodeService.getProperty(nodeRef, ContentModel.PROP_FIRSTNAME),
                            nodeService.getProperty(nodeRef, ContentModel.PROP_LASTNAME));
                default:
                    throw new WebScriptException("Unexpected authority type: " + type);
            }
        }
        return name;
    }

    private String getTypeModelName(AuthorityType type) {
        switch (type) {
            case GROUP:
                return "cm:" + ContentModel.TYPE_AUTHORITY_CONTAINER.getLocalName();
            case USER:
                return "cm:" + ContentModel.TYPE_PERSON.getLocalName();
            default:
                throw new WebScriptException("Unexpected authority type: " + type);
        }
    }

    private boolean isGroupCreatedInOpenEAndFilteredByCaseType(JSONObject json, String caseType) {
        try {
            return json.has(OpenESDHModel.PROP_OE_OPENE_TYPE.getLocalName())
                    && (caseType == null && CREATED_ON_OPEN_E.equals(json.get(OpenESDHModel.PROP_OE_OPENE_TYPE.getLocalName()))
                    || (caseType != null && caseType.equals(json.get(OpenESDHModel.PROP_OE_OPENE_TYPE.getLocalName()))));
        } catch (JSONException ex) {
            throw new WebScriptException(ex.getMessage(), ex);
        }
    }

}
