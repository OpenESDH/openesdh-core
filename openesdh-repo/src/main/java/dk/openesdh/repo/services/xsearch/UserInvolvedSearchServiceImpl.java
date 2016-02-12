package dk.openesdh.repo.services.xsearch;

import dk.openesdh.repo.model.OpenESDHModel;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by flemmingheidepedersen on 12/09/14.
 */
@Service("UserInvolvedSearchService")
public class UserInvolvedSearchServiceImpl extends AbstractXSearchService implements UserInvolvedSearchService {

    private static final Pattern REGEXP_GROUP_CASE = Pattern.compile("GROUP_case_(\\d+)-(\\d+)_(\\D+)");

    @Autowired
    @Qualifier("AuthorityService")
    private AuthorityService authorityService;

    public XResultSet getNodes(Map<String, String> params, int startIndex, int pageSize, String sortField, boolean ascending) {
        String user = params.get("user");

        Set<String> caseGroupsNodedbid = getCaseGroupsNodedbid(user);

        if (caseGroupsNodedbid.isEmpty()) {
            return new XResultSet();
        } else {
            int collected = 0;
            int limit = 200; // execute the query for every 200 groups

            XResultSet combinedResult = new XResultSet(new LinkedList<>(), 0);
            //NOte that it is the postfix that is now fixed and not the naming prefix as was before
            // i.e. case:base  is now base:case. other case types are in form of <prefix>:case and inherit from base:case
            String baseQuery = "TYPE:\"" + OpenESDHModel.CASE_PREFIX + ":case\"" + " AND NOT ASPECT:\"" + OpenESDHModel.ASPECT_OE_LOCKED + "\"";

            Iterator iterator = caseGroupsNodedbid.iterator();

            String nodedbidsQuery = "";
            while (iterator.hasNext()) {
                String element = ((String) iterator.next());
                nodedbidsQuery += " \"" + element + "\"";

                collected++;
                // no need to check if the user has been involved in the case - just check to see if the lasted modifier property is equal to the user
                if (collected == limit) {
                    XResultSet result = executeQuery(baseQuery + " AND " + "@sys\\:node-dbid:(" + nodedbidsQuery + ")");
                    combinedResult.addAll(result);
                    collected = 0;
                    nodedbidsQuery = "";
                } else if (!iterator.hasNext()) {
                    XResultSet result = executeQuery(baseQuery + " AND " + "@sys\\:node-dbid:(" + nodedbidsQuery + ")");
                    combinedResult.addAll(result);
                }
            }
            return combinedResult;
        }
    }

    Set<String> getCaseGroupsNodedbid(String user) {
        // put nodedbid in hashmap as the user can be a member of two groups that belong to the same case
        Set<String> allGroups = authorityService.getContainingAuthorities(AuthorityType.GROUP, user, true);
        return allGroups.stream()
                .map(REGEXP_GROUP_CASE::matcher)
                .filter(Matcher::matches)
                .map(matcher -> matcher.group(2))
                .collect(Collectors.toSet());
    }

}
