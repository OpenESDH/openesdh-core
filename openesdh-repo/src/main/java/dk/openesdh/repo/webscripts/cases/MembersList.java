package dk.openesdh.repo.webscripts.cases;

import dk.openesdh.repo.services.cases.CaseService;
import dk.openesdh.repo.utils.Utils;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PersonService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * @author Lanre Abiwon.
 * @deprecated used only in Share. Please use {@link dk.openesdh.repo.webscripts.cases.CaseMembersWebScript}
 */
@Deprecated
public class MembersList extends DeclarativeWebScript {

    private static Log logger = LogFactory.getLog(MembersList.class);
    private CaseService caseService;
    private AuthorityService authorityService;
    private PersonService personService;
    private NodeService nodeService;
    private DictionaryService dictionaryService;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache){
//        NodeRef caseNodeRef = new NodeRef(req.getParameter("nodeRef"));
        Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();

        Map<String, Object> model = new HashMap<String, Object>();

        try{
            String storeType = templateArgs.get("store_type");
            String storeId = templateArgs.get("store_id");
            String id = templateArgs.get("id");
            String caseId = templateArgs.get("caseId");
            String caseNodeRefStr;
            //reconstruct the nodeRef
            NodeRef caseNode;
            if(StringUtils.isNotEmpty(storeType)&&StringUtils.isNotEmpty(storeId)&&StringUtils.isNotEmpty(id)) {
                caseNodeRefStr = storeType + "://" + storeId + "/" + id;
                caseNode = new NodeRef(caseNodeRefStr);
            }
            else
                caseNode = this.caseService.getCaseById(caseId);

            Map<String, Set<String>> membersByRole = caseService.getMembersByRole(caseNode, true, true);
            JSONArray json = buildJSON(membersByRole);

            // construct model for response template to render
            model.put("memberslist", json);
        }
        catch (JSONException je){
            logger.error(je.getMessage());
        }

        return model;
    }

    JSONArray buildJSON(Map<String, Set<String>> membersByRole) throws JSONException {
        JSONArray result = new JSONArray();

        for (Map.Entry<String, Set<String>> entry : membersByRole.entrySet()) {
            Set<String> value = entry.getValue();
            for (String authority : value) {
                JSONObject memberObj = new JSONObject();
                boolean isGroup = authority.startsWith("GROUP_");
                memberObj.put("authorityType", isGroup ? "GROUP" : "USER");
                memberObj.put("authority", authority);

                NodeRef authorityNodeRef = authorityService.getAuthorityNodeRef(authority);
                String displayName;
                if(!isGroup) {
                    memberObj.put("isGroup", false);
                    PersonService.PersonInfo personInfo = personService.getPerson(authorityNodeRef);
                    displayName = personInfo.getFirstName() + " " + personInfo.getLastName();
                    try {
                        NodeRef avatarNodeRef = this.nodeService.getTargetAssocs(personInfo.getNodeRef(), ContentModel.ASSOC_AVATAR).get(0).getTargetRef();
                        //Inject avatar node if it exists.
                        if (avatarNodeRef != null) {
                            memberObj.put("avatarNodeRef", avatarNodeRef);
                            memberObj.put("avatar", "api/node/" + avatarNodeRef.toString().replace("://", "/") + "/content/thumbnails/avatar");
                        }
                    } catch (IndexOutOfBoundsException iobe) {
                        logger.warn("\n\n ===>(MemberList.java 92) This is a Warning. " + displayName + " has no avatar <===\n\n");
                    }
                }
                else{
                    displayName = authorityService.getAuthorityDisplayName(authority);
                    memberObj.put("avatar", "components/images/group-16.png");
                    memberObj.put("isGroup", true);
                }

                String displayRoleName = Utils.getRoleDisplayLabel(entry.getKey(), dictionaryService);
                memberObj.put("role", displayRoleName);

                memberObj.put("displayName", displayName);
                memberObj.put("nodeRef", authorityNodeRef);
                result.put(memberObj);
            }
        }
        return result;
    }

    JSONArray buildCaseMembersJSON(Map<String, Set<String>> membersByRole) throws JSONException {
        JSONArray result = new JSONArray();

        for (Map.Entry<String, Set<String>> entry : membersByRole.entrySet()) {
            Set<String> value = entry.getValue();
            for (String authority : value) {
                JSONObject memberObj = new JSONObject();
                boolean isGroup = authority.startsWith("GROUP_");
                memberObj.put("authorityType", isGroup ? "group" : "user");
                memberObj.put("authority", authority);

                NodeRef authorityNodeRef = authorityService.getAuthorityNodeRef
                        (authority);

                String displayName;
                if (isGroup) {
                    displayName = authorityService.getAuthorityDisplayName(authority);
                } else {
                    PersonService.PersonInfo personInfo = personService.getPerson(authorityNodeRef);
                    displayName = personInfo.getFirstName() + " " + personInfo.getLastName();
                }
                memberObj.put("displayName", displayName);
                memberObj.put("role", entry.getKey());
                memberObj.put("nodeRef", authorityNodeRef);
                result.put(memberObj);
            }

        }

        return result;
    }

    //region Service Setters
    public void setCaseService(CaseService caseService) {
        this.caseService = caseService;
    }

    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    //endregion

}
