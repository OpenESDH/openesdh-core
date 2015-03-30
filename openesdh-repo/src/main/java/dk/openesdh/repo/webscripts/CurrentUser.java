package dk.openesdh.repo.webscripts;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PersonService;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * @author Lanre Abiwon
 */
public class CurrentUser extends AbstractRESTWebscript{
    private PersonService personService;

    @Override
    protected void get(NodeRef nodeRef, WebScriptRequest req, WebScriptResponse res){
        String userName = AuthenticationUtil.getFullyAuthenticatedUser();
        NodeRef personNodeRef = this.personService.getPerson(userName);
        PersonService.PersonInfo personInfo = this.personService.getPerson(personNodeRef);
        try {
            //returning the response in the same format as the api/forms/picker/authority/children
            //Note that some properties are just skipped as they do not make sense in the context of the user
            JSONObject obj = new JSONObject();
            obj.put("type", "cm:person");
            obj.put("nodeRef", personInfo.getNodeRef());
            obj.put("name", personInfo.getFirstName()+" "+personInfo.getLastName() +" ("+ personInfo.getUserName()+")");
            obj.put("userName", personInfo.getUserName());
            obj.put("selectable", true);
            obj.write(res.getWriter());
        }
        catch (Exception joe){
            throw new WebScriptException("THere was an exception getting the current authenticated user.\nReason"+ joe.getLocalizedMessage());
        }
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }
}
