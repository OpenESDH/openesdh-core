package dk.openesdh.repo.webscripts.officetemplate;

import dk.openesdh.repo.model.CaseInfo;
import dk.openesdh.repo.model.ContactInfo;
import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.NodeInfoService;
import dk.openesdh.repo.services.cases.CaseService;
import dk.openesdh.repo.services.cases.PartyService;
import dk.openesdh.repo.services.contacts.ContactService;
import dk.openesdh.repo.services.officetemplate.OfficeTemplate;
import dk.openesdh.repo.services.officetemplate.OfficeTemplateService;
import dk.openesdh.repo.webscripts.AbstractRESTWebscript;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.security.NoSuchPersonException;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.*;

public class OfficeTemplateFillPost extends AbstractRESTWebscript {
    private static Logger LOGGER = Logger.getLogger(OfficeTemplateFillPost.class);
    private OfficeTemplateService officeTemplateService;
    private CaseService caseService;
    private NodeInfoService nodeInfoService;
    private NodeService nodeService;
    private PersonService personService;
    private ContactService contactService;
    private PartyService partyService;
    private ContentService contentService;

    public void setCaseService(CaseService caseService) {
        this.caseService = caseService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    public void setContactService(ContactService contactService) {
        this.contactService = contactService;
    }

    public void setPartyService(PartyService partyService) {
        this.partyService = partyService;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    public void setNodeInfoService(NodeInfoService nodeInfoService) {
        this.nodeInfoService = nodeInfoService;
    }

    public void setOfficeTemplateService(OfficeTemplateService officeTemplateService) {
        this.officeTemplateService = officeTemplateService;
    }

    @Override
    protected void post(NodeRef nodeRef, WebScriptRequest req, WebScriptResponse res) throws IOException, JSONException {
        if (nodeRef != null) {
            // Check if the template exists
            try {
                OfficeTemplate template = officeTemplateService.getTemplate(nodeRef);
                if (template == null) {
                    throw new WebScriptException(Status.STATUS_NOT_FOUND, "Template not found");
                }
            } catch (Exception e) {
                throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR, "Error retrieving template", e);
            }

            try {
                Map<String, Serializable> model = new HashMap<>();
                JSONObject json = WebScriptUtils.readJson(req);
                JSONObject modelJson = (JSONObject) json.get("model");
                if (modelJson != null) {
                    // Add the user input to the template model
                    modelJson.forEach((k, v) -> {
                        model.put((String) k, (Serializable) v);
                    });
                    // Anything that is to be filled in automatically by the
                    // system will be overwritten in the following code.
                }

                String caseId = (String) json.get("caseId");
                if (caseId != null) {
                    NodeRef caseNodeRef = caseService.getCaseById(caseId);
                    CaseInfo caseInfo = caseService.getCaseInfo(caseNodeRef);
                    model.put("case.id", caseInfo.getCaseId());
                    model.put("case.title", caseInfo.getTitle());

                    NodeInfoService.NodeInfo nodeInfo = nodeInfoService.getNodeInfo(caseNodeRef);
                    List<QName> requiredProps = Arrays.asList(OpenESDHModel.PROP_OE_ID, ContentModel.PROP_TITLE,
                            OpenESDHModel.ASSOC_CASE_OWNERS, OpenESDHModel.PROP_OE_STATUS,
                            ContentModel.PROP_CREATOR, ContentModel.PROP_CREATED, ContentModel.PROP_MODIFIED,
                            ContentModel.PROP_MODIFIER, ContentModel.PROP_DESCRIPTION,
                            OpenESDHModel.PROP_OE_JOURNALKEY, OpenESDHModel.PROP_OE_JOURNALFACET,
                            OpenESDHModel.PROP_OE_LOCKED_BY, OpenESDHModel.PROP_OE_LOCKED_DATE
                    );

                    org.json.JSONObject infoJson = nodeInfoService.getSelectedProperties(nodeInfo, null, requiredProps);
                    model.put("case.type", getPropertyValue("base:type", infoJson));
                    model.put("case.journalKey", getPropertyValue("oe:journalKey", infoJson));
                }

                String userName = AuthenticationUtil.getFullyAuthenticatedUser();
                NodeRef personNodeRef = personService.getPerson(userName);
                try {
                    PersonService.PersonInfo personInfo = personService.getPerson(personNodeRef);
                    String fullName = personInfo.getFirstName() + " " + personInfo.getLastName();
                    model.put("user.username", userName);
                    model.put("user.name", fullName);
                    Map<QName, Serializable> userProps = nodeService.getProperties(personInfo.getNodeRef());
                    model.put("user.email", userProps.getOrDefault(ContentModel
                            .PROP_EMAIL, ""));
                    model.put("user.telephoneNumber", userProps.getOrDefault(ContentModel.PROP_TELEPHONE, ""));
                    model.put("user.position", userProps.getOrDefault(ContentModel.PROP_JOBTITLE, ""));
                    model.put("user.department", userProps.getOrDefault(ContentModel.PROP_LOCATION, ""));
                } catch (NoSuchPersonException e) {
                    LOGGER.warn("Problem retrieving user's details when filling template", e);
                }

                String contactNodeRefStr = (String) json.get("contactNodeRef");
                if (caseId != null && contactNodeRefStr != null) {
                    NodeRef contactNodeRef = new NodeRef(contactNodeRefStr);
                    ContactInfo contactInfo  = contactService.getContactInfo(contactNodeRef);
                    model.put("receiver.city", contactInfo.getCityName());
                    model.put("receiver.postnumber", contactInfo.getPostCode());
                    model.put("receiver.name", contactInfo.getName());
                    model.put("receiver." + (contactInfo.getType().equalsIgnoreCase("PERSON") ? "cpr" : "cvr"), contactInfo.getIDNumebr());
                }

                ContentReader reader = officeTemplateService.renderTemplate(nodeRef, model);
//                res.setContentEncoding(reader.getEncoding());
                res.setContentType(reader.getMimetype());
                reader.getContent(res.getOutputStream());
            } catch (Exception e) {
                throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR, "Error filling template", e);
            }
        }
    }

    /**
     * Fetch an individual property's value.
     * @param infoJson
     * @param propertyName
     * @return
     */
    private Serializable getPropertyValue(String propertyName, org.json.JSONObject infoJson) {
        try {
            org.json.JSONObject propertyObj = infoJson.getJSONObject(propertyName);
            try {
                Object displayValue = propertyObj.get("displayValue");
                return (Serializable) displayValue;
            } catch (JSONException e) {
                try {
                    return (Serializable) propertyObj.get("value");
                } catch (JSONException e1) {
                    return null;
                }
            }
        } catch (JSONException e) {
            return null;
        }
    }
}
