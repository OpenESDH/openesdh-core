package dk.openesdh.repo.services.xsearch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryParser.QueryParser;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dk.openesdh.repo.services.cases.CasePermission;
import dk.openesdh.repo.services.members.CaseMembersService;

/**
 * Created by flemmingheidepedersen on 12/09/14.
 */
@Service("XSearchService")
public class XSearchServiceImpl extends AbstractXSearchService {

    private static final String IN_VALUE_LIST = "IN";

    protected enum FilterType {
        AND,
        OR
    }

    @Autowired
    private CaseMembersService caseMembersService;

    public XResultSet getNodes(Map<String, String> params, int startIndex, int pageSize, String sortField, boolean ascending) {
        String baseType = params.get("baseType");
        if (baseType == null) {
            throw new AlfrescoRuntimeException("Must specify a baseType parameter");
        }

        String filtersJSON = params.get("filters");
        FilterType filterType;
        switch (params.getOrDefault("filterType", "AND")) {
            case "OR":
                filterType = FilterType.OR;
                break;
            case "AND":
            default:
                filterType = FilterType.AND;
        }
//        = "OR".equals(params.get("filterType");
        try {
            String query = buildQuery(baseType, filtersJSON, filterType);
            return executeQuery(query, startIndex, pageSize, sortField, ascending);
        } catch (JSONException e) {
            throw new AlfrescoRuntimeException("Unable to parse filters JSON");
        }
    }

    protected String buildQuery(String baseType, String filtersJSON) throws JSONException {
        return buildQuery(baseType, filtersJSON, FilterType.AND);
    }

    protected String buildQuery(String baseType, String filtersJSON, FilterType filterType) throws JSONException {
        List<String> searchTerms = new ArrayList<>();

        if (filtersJSON == null) {
            searchTerms.add("TYPE:" + quote(baseType));
            searchTerms.add("ISNOTNULL:\"oe:id\"");
            return StringUtils.join(searchTerms, " AND ");
        }

        Optional<JSONArray> optFilters = parseJsonArray(filtersJSON);
        if (optFilters.isPresent()) {
            JSONArray filters = optFilters.get();
            for (int i = 0; i < filters.length(); i++) {
                processFilter(filters.getJSONObject(i))
                        .ifPresent(searchTerm -> searchTerms.add(searchTerm));
            }
        } else {
            processFilter(new JSONObject(filtersJSON))
                    .ifPresent(searchTerm -> searchTerms.add(searchTerm));
        }

        return String.format(SEARCH_QUERY, quote(baseType)) + queryAND(searchTerms, filterType.name());
    }

    private static final String SEARCH_QUERY = "TYPE:%1$s AND ISNOTNULL:\"oe:id\"";

    private static String queryAND(List<String> searchTerms, String filterTypeName) {
        if (searchTerms.isEmpty()) {
            return "";
        }
        return " AND (" + StringUtils.join(searchTerms, " " + filterTypeName + " ") + ")";
    }

    protected Optional<JSONArray> parseJsonArray(String s) {
        try {
            return Optional.of(new JSONArray(s));
        } catch (JSONException e) {
        }
        return Optional.empty();
    }

    String processFilterValue(JSONObject filter) throws JSONException {

        Optional<JSONObject> optValue = getJSONValueObject(filter);
        if (optValue.isPresent()) {
            JSONObject value = optValue.get();
            if (value.has("dateRange")) {
                return processDateRangeValue(value);
            } else {
                return value.toString();
            }
        }

        Optional<JSONArray> optArrayValue = getJSONValueArray(filter);
        if (optArrayValue.isPresent()) {
            JSONArray value = optArrayValue.get();
            List<String> list = new ArrayList<>();
            for (int i = 0; i < value.length(); i++) {
                list.add(quote(value.getString(i)));
            }
            return "(" + StringUtils.join(list, ",") + ")";
        }

        String value = filter.getString("value");
        if (!value.equals("")) {
            return quote(value);
        }

        return null;
    }

    private Optional<JSONObject> getJSONValueObject(JSONObject obj) {
        try {
            JSONObject value = obj.getJSONObject("value");
            return Optional.of(value);
        } catch (JSONException e) {

        }
        return Optional.empty();
    }

    private Optional<JSONArray> getJSONValueArray(JSONObject obj) {
        try {
            JSONArray value = obj.getJSONArray("value");
            return Optional.of(value);
        } catch (JSONException e) {
        }
        return Optional.empty();
    }

    private String processDateRangeValue(JSONObject value) throws JSONException {
        JSONArray dateRange = value.getJSONArray("dateRange");
        if (dateRange.length() == 0) {
            return value.toString();
        }

        if (dateRange.getString(0).equals("")) {
            dateRange.put(0, "MIN");
        }
        if (dateRange.getString(1).equals("")) {
            dateRange.put(1, "MAX");
        }
        String rangeFrom = quote(stripTimeZoneFromDateTime(dateRange.getString(0)));
        String rangeTo = quote(stripTimeZoneFromDateTime(dateRange.getString(1)));
        return "[" + rangeFrom + " TO " + rangeTo + "]";
    }

    List<Long> getCaseDbIdsWhereAuthoritiesHaveRole(String role, List<NodeRef> authorityNodeRefs) {
        return authorityNodeRefs.stream()
                .flatMap(authorityNodeRef
                        -> caseMembersService.getCaseDbIdsWhereAuthorityHasRole(authorityNodeRef, role).stream())
                .collect(Collectors.toList());
    }

    Optional<String> processFilter(JSONObject filter) throws JSONException {
        String name = filter.getString("name");
        String operator = filter.getString("operator");

        if (IN_VALUE_LIST.equals(operator)) {
            return processValueListFilter(filter);
        }

        String value;

        if (name.equals("case:owners")) {
            // Special handling for search on case:owners
            // TODO: Move this code to a better place; it is too specific to
            // cases
            JSONArray owners = filter.getJSONArray("value");
            List<NodeRef> ownersNodeRefs = new ArrayList<>();
            for (int i = 0; i < owners.length(); i++) {
                ownersNodeRefs.add(new NodeRef((String) owners.get(i)));
            }
            List<Long> dbIds = getCaseDbIdsWhereAuthoritiesHaveRole(CasePermission.OWNER.getRegExp(false), ownersNodeRefs);

            name = "sys:node-dbid";
            JSONArray dbIdsJsonArray = new JSONArray();
            for (Long dbId : dbIds) {
                dbIdsJsonArray.put(dbId.toString());
            }
            if (!dbIds.isEmpty()) {
                filter.put("value", dbIdsJsonArray);
            } else {
                filter.put("value", "undefined");
            }
        }

        value = processFilterValue(filter);

        return createFilter(name, operator, value);
    }

    private Optional<String> processValueListFilter(JSONObject filter) throws JSONException {

        String name = filter.getString("name");

        Optional<JSONArray> optValues = getJSONValueArray(filter);
        if (!optValues.isPresent()) {
            return Optional.empty();
        }
        List<String> searchTerms = new ArrayList<>();
        JSONArray values = optValues.get();
        for (int i = 0; i < values.length(); i++) {
            createFilter(name, "", values.getString(i))
                    .ifPresent(searchTerm -> searchTerms.add(searchTerm));
        }
        if (searchTerms.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of("(" + StringUtils.join(searchTerms, " OR ") + ")");
    }

    private Optional<String> createFilter(String name, String operator, String value) {
        if (value == null) {
            return Optional.empty();
        }

        // Escape field name for lucene
        String field = QueryParser.escape(name);
        String prepend = "";
        if (operator.equals("!=")) {
            prepend = "-";
        }

        // Prefix fields with @ symbol
        if (name.contains(":") && !name.startsWith("@")) {
            field = "@" + field;
        }

        return Optional.of(prepend + field + ':' + value);
    }

}
