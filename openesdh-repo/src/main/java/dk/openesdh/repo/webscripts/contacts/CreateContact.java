package dk.openesdh.repo.webscripts.contacts;

import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.contacts.ContactService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

/**
 * @author Lanre
 */
public class CreateContact extends AbstractWebScript {
    NodeService nodeService;
    ContactService contactService;

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        JSONObject parsedRequest;
        try {
            //Get the information from the JSON structure from the request
            parsedRequest = new JSONObject(req.getContent().getContent());
            HashMap<QName, Serializable> typeProps = new HashMap<QName, Serializable>();

            String email = parsedRequest.getString("email");
            String contactType = parsedRequest.getString("contactType");

            NodeRef createdContact = contactService.createContact(email, contactType);

            JSONObject obj = new JSONObject();

            if (createdContact != null) {
                obj.put("contactNodeRef", createdContact.toString());
                obj.put("type", this.nodeService.getProperty(createdContact, OpenESDHModel.PROP_CONTACT_TYPE));
            } else
                obj.put("message", "uncreated");

            obj.write(res.getWriter());
        } catch (JSONException jse) {

        }
    }

    /**
     * Grabbed from the org.alfresco.repo.web.scripts.discussion.AbstractDiscussionWebScript
     * @param json
     * @param key
     * @return
     */
    public String getOrNull(org.json.simple.JSONObject json, String key) {
        if (json.containsKey(key)) {
            return (String) json.get(key);
        }
        return null;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setContactService(ContactService contactService) {
        this.contactService = contactService;
    }
}
