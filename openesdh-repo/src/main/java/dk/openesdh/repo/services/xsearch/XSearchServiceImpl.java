package dk.openesdh.repo.services.xsearch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryParser.QueryParser;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by flemmingheidepedersen on 12/09/14.
 */
public class XSearchServiceImpl extends AbstractXSearchService {
    public XResultSet getNodes(Map<String, String> params, int startIndex, int pageSize, String sortField, boolean ascending) {
        String baseType = params.get("baseType");
        if (baseType == null) {
            throw new AlfrescoRuntimeException("Must specify a baseType parameter");
        }

        String filtersJSON = params.get("filters");
        try {
            String query = buildQuery(baseType, filtersJSON);
            return executeQuery(query, startIndex, pageSize, sortField, ascending);
        } catch (JSONException e) {
            throw new AlfrescoRuntimeException("Unable to parse filters JSON");
        }
    }


    protected String buildQuery(String baseType, String filtersJSON) throws JSONException {
        List<String> searchTerms = new ArrayList<>();

        if (filtersJSON == null) {
            searchTerms.add("TYPE:" + quote(baseType));
            return StringUtils.join(searchTerms, " AND ");
        }

        JSONArray filters = getJsonArray(filtersJSON);
        if (filters != null) {
            for (int i = 0; i < filters.length(); i++) {
                processFilter(filters.getJSONObject(i))
                    .ifPresent(searchTerm -> searchTerms.add(searchTerm));
            }
        } else {
            processFilter(new JSONObject(filtersJSON))
                .ifPresent(searchTerm -> searchTerms.add(searchTerm));
        }

        searchTerms.add("TYPE:" + quote(baseType));
        return StringUtils.join(searchTerms, " AND ");
    }

    protected JSONArray getJsonArray(String s) {
        try {
            return new JSONArray(s);
        } catch (JSONException e) {
            return null;
        }
    }

    String processFilterValue(JSONObject filter) throws JSONException {
        try {
            JSONObject value = filter.getJSONObject("value");
            if (value.has("dateRange")) {
                JSONArray dateRange = value.getJSONArray("dateRange");
                if (dateRange.length() > 0) {
                    if (dateRange.getString(0).equals("")) {
                        dateRange.put(0, "MIN");
                    }
                    if (dateRange.getString(1).equals("")) {
                        dateRange.put(1, "MAX");
                    }
                    String rangeFrom = quote(
                            stripTimeZoneFromDateTime(dateRange.getString(0)));
                    String rangeTo = quote(
                            stripTimeZoneFromDateTime(dateRange.getString(1)));
                    return "[" + rangeFrom + " TO " + rangeTo + "]";
                } else {
                    return value.toString();
                }
            } else {
                return value.toString();
            }
        } catch (JSONException e) {
            try {
                JSONArray value = filter.getJSONArray("value");
                List<String> list = new ArrayList<>();
                for (int i = 0; i < value.length(); i++) {
                    list.add(quote(value.getString(i)));
                }
                return "(" + StringUtils.join(list, ",") + ")";
            } catch (JSONException e2) {
                String value = filter.getString("value");
                if (!value.equals("")) {
                    return quote(value);
                } else {
                    return null;
                }
            }
        }
    }

    List<Long> getCaseDbIdsWhereAuthoritiesHaveRole(String role, List<NodeRef> authorityNodeRefs) {
        List<Long> dbIds = new ArrayList<>();
        for (NodeRef authorityNodeRef : authorityNodeRefs) {
            dbIds.addAll(caseService.getCaseDbIdsWhereAuthorityHasRole(authorityNodeRef, role));
        }
        return dbIds;
    }

    Optional<String> processFilter(JSONObject filter) throws JSONException {
        String name = filter.getString("name");
        String operator = filter.getString("operator");

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
            List<Long> dbIds = getCaseDbIdsWhereAuthoritiesHaveRole("CaseOwners", ownersNodeRefs);

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
        //System.out.println("Filter " + name + " " + operator + " " + value);

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
