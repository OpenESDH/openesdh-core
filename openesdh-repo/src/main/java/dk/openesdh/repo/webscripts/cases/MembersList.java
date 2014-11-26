package dk.openesdh.repo.webscripts.cases;

import dk.openesdh.repo.services.cases.CaseService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PersonService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.*;

import javax.swing.text.html.parser.ContentModel;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by lanre on 18/11/2014.
 */
public class MembersList extends DeclarativeWebScript {

    private static Log logger = LogFactory.getLog(MembersList.class);
    private CaseService caseService;
    private AuthorityService authorityService;
    private PersonService personService;
    private NodeService nodeService;



    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache){
//        NodeRef caseNodeRef = new NodeRef(req.getParameter("nodeRef"));
        Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();

        Map<String, Object> model = new HashMap<String, Object>();

        try{
            String storeType = templateArgs.get("store_type");
            String storeId = templateArgs.get("store_id");
            String id = templateArgs.get("id");
            //reconstruct the nodeRef
            String caseNodeRefStr = storeType+"://"+storeId+"/"+id;
            NodeRef caseNode = new NodeRef(caseNodeRefStr);

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
                memberObj.put("authorityType", isGroup ? "group" : "user");
                memberObj.put("authorityType", "user");
                memberObj.put("authority", authority);

                NodeRef authorityNodeRef = authorityService.getAuthorityNodeRef(authority);
                String displayName;
                if(!isGroup) {
                    memberObj.put("isGroup", false);
                    PersonService.PersonInfo personInfo = personService.getPerson(authorityNodeRef);
                    displayName = personInfo.getFirstName() + " " + personInfo.getLastName();
                    try {
                        NodeRef avatarNodeRef = this.nodeService.getTargetAssocs(personInfo.getNodeRef(), org.alfresco.model.ContentModel.ASSOC_AVATAR).get(0).getTargetRef();
                        //Inject avatar node if it exists.
                        if (avatarNodeRef != null) {
                            memberObj.put("avatarNodeRef", avatarNodeRef);
                            memberObj.put("avatar", "api/node/" + avatarNodeRef.toString().replace("://", "/") + "/content/thumbnails/avatar");
                        }
                    } catch (IndexOutOfBoundsException iobe) {
                        logger.warn("\n\n ===>(88) " + displayName + " has no avatar <===\n\n");
                    }
                }
                else{
                    displayName = authorityService.getAuthorityDisplayName(authority);
                    memberObj.put("avatar", "components/images/group-16.png");
                    memberObj.put("isGroup", true);

                }
                memberObj.put("displayName", displayName);
                memberObj.put("role", entry.getKey());
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

    //endregion

}
