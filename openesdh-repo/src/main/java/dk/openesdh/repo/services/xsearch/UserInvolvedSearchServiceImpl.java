package dk.openesdh.repo.services.xsearch;

import dk.openesdh.repo.model.OpenESDHModel;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.domain.permissions.Authority;
import org.alfresco.repo.node.SystemNodeUtils;
import org.alfresco.repo.security.authority.AuthorityDAO;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.apache.solr.common.util.Hash;
import org.json.JSONException;
import org.omg.CORBA.RepositoryIdHelper;

import javax.xml.soap.Node;
import java.security.Permission;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by flemmingheidepedersen on 12/09/14.
 */
public class UserInvolvedSearchServiceImpl extends AbstractXSearchService {

    protected AuthorityService authorityService;


    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    public XResultSet getNodes(Map<String, String> params, int startIndex, int pageSize, String sortField, boolean ascending) {
        baseType = params.get("baseType");
        if (baseType == null) {
            throw new AlfrescoRuntimeException("Must specify a baseType parameter");
        }

        String user = params.get("user");

        HashMap<String, String> caseGroupsNodedbid = getCaseGroupsNodedbid(user);

        if (caseGroupsNodedbid.size() == 0) {
            String query = "TYPE:\"esdh:case\" AND NOT ASPECT:\"case:journalized\" AND @sys\\:node-dbid:( \"-1\" )";
            return executeQuery(query);
        }
        else {
            int collected = 0;
            int limit = 2;


            XResultSet combinedResult = new XResultSet(new LinkedList<NodeRef>(), 0);
            String baseQuery = "TYPE:\"" + OpenESDHModel.CASE_PREFIX + ":" + OpenESDHModel.TYPE_BASE_NAME + "\"" + " AND NOT ASPECT:\"case:journalized\" ";
            Iterator iterator = caseGroupsNodedbid.keySet().iterator();
            System.out.println(caseGroupsNodedbid.size());

            String nodedbidsQuery = "";
            while (iterator.hasNext()) {
                String element = ((String) iterator.next());
                nodedbidsQuery += " \"" + element + "\"";

                collected++;

                if (collected == limit) {
                    XResultSet result = executeQuery(baseQuery + " AND " + "@sys\\:node-dbid:(" + nodedbidsQuery + ")");

                    combinedResult.getNodeRefs().addAll(result.getNodeRefs());
                    collected = 0;
                    nodedbidsQuery = "";
                }
                else if (!iterator.hasNext()) {
                    XResultSet result = executeQuery(baseQuery + " AND " + "@sys\\:node-dbid:(" + nodedbidsQuery + ")");

                    combinedResult.getNodeRefs().addAll(result.getNodeRefs());

                }
            }
            return combinedResult;
        }
    }


    protected HashMap<String, String> getCaseGroupsNodedbid (String user) {

        // put nodedbid in hashmap as the user can be a member of two groups that belong to the same case
        HashMap<String, String> caseGroupsNodedbid = new HashMap<>();

        Set<String> allGroups = authorityService.getContainingAuthorities(AuthorityType.GROUP, user, true);

        Iterator iterator = allGroups.iterator();

        while (iterator.hasNext()) {
            String groupName = (String)iterator.next();
            Pattern pattern = Pattern.compile("GROUP_case_(\\d+)-(\\d+)_(\\D+)");
            Matcher matcher = pattern.matcher(groupName);


            if (matcher.matches()) {
                pattern = Pattern.compile("GROUP_case_(\\d+)-(\\d+)");
                matcher = pattern.matcher(groupName);

                matcher.find();
                caseGroupsNodedbid.put(matcher.group(2), matcher.group(2));

            }
        }
        return caseGroupsNodedbid;
    }
}