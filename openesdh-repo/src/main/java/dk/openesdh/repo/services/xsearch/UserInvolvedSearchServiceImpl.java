package dk.openesdh.repo.services.xsearch;

import dk.openesdh.repo.model.OpenESDHModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by flemmingheidepedersen on 12/09/14.
 */
public class UserInvolvedSearchServiceImpl extends AbstractXSearchService implements UserInvolvedSearchService {

    protected AuthorityService authorityService;

    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    public XResultSet getNodes(Map<String, String> params, int startIndex, int pageSize, String sortField, boolean ascending) {
        String user = params.get("user");

        Set<String> caseGroupsNodedbid = getCaseGroupsNodedbid(user);

        if (caseGroupsNodedbid.size() == 0) {
            return new XResultSet();
        } else {
            int collected = 0;
            int limit = 200; // execute the query for every 200 groups


            XResultSet combinedResult = new XResultSet(new LinkedList<NodeRef>(), 0);
            String baseQuery = "TYPE:\"" + OpenESDHModel.CASE_PREFIX + ":" + OpenESDHModel.TYPE_BASE_NAME + "\"" + " AND NOT ASPECT:\"" + OpenESDHModel.ASPECT_OE_JOURNALIZED + "\"";

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


    protected Set<String> getCaseGroupsNodedbid(String user) {

        // put nodedbid in hashmap as the user can be a member of two groups that belong to the same case
        HashSet<String> caseGroupsNodedbid = new HashSet<>();

        Set<String> allGroups = authorityService.getContainingAuthorities(AuthorityType.GROUP, user, true);
        Iterator iterator = allGroups.iterator();

        while (iterator.hasNext()) {
            String groupName = (String) iterator.next();
            Pattern pattern = Pattern.compile("GROUP_case_(\\d+)-(\\d+)_(\\D+)");
            Matcher matcher = pattern.matcher(groupName);

            if (matcher.matches()) {
                caseGroupsNodedbid.add(matcher.group(2));
            }
        }
        return caseGroupsNodedbid;
    }


}