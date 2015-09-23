package dk.openesdh.repo.webscripts.cases;

import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.cases.CaseService;
import dk.openesdh.repo.services.documents.DocumentService;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.extensions.webscripts.*;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.util.FileCopyUtils;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by rasmutor on 9/16/15.
 */
public class CaseEmail extends AbstractWebScript {

    private static final Log LOG = LogFactory.getLog(CaseEmail.class);

    private CaseService caseService;

    private JavaMailSender mailService;

    private PersonService personService;

    private NodeService nodeService;

    private DocumentService documentService;

    private ContentService contentService;

    @Override
    public void execute(WebScriptRequest webScriptRequest, WebScriptResponse webScriptResponse) throws IOException {

        Map<String, String> templateArgs = webScriptRequest.getServiceMatch().getTemplateVars();
        String caseId = templateArgs.get("caseId");

        NodeRef caseNode = caseService.getCaseById(caseId);
        if (caseNode == null) {
            throw new WebScriptException(Status.STATUS_NOT_FOUND, "Case not found" + caseId);
        }

        Set<String> toSet;
        String subject;
        String text;
        List<NodeRef> attachments;

        try {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(webScriptRequest.getContent().getContent());
            JSONArray to = (JSONArray) json.get("to");
            subject = (String) json.get("subject");
            text = (String) json.get("message");

            toSet = (Set<String>)to.stream().map(this::getEmailAddress).collect(Collectors.toSet());

            JSONArray docs = (JSONArray) json.get("documents");
            attachments = (List<NodeRef>)docs.stream().map(this::getDocumentNodeRef).collect(Collectors.toList());
        } catch (ParseException pe) {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Invalid JSON: " + pe.getMessage());
        }

        if (toSet.size() == 0) {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "No recipients");
        }

        MimeMessagePreparator preparator = mimeMessage -> {
            MimeMessageHelper message = new MimeMessageHelper(mimeMessage, attachments.size() > 0, "UTF-8");
            toSet.stream().forEach(t -> {
                try {
                    message.addTo(new InternetAddress(t));
                } catch (MessagingException e) {
                    throw new WebScriptException(Status.STATUS_BAD_REQUEST, e.getMessage());
                }
            });
            message.setSubject(subject);
            message.setText(text);
            message.setFrom(new InternetAddress("noreply@openesdh.dk"));

            attachments.stream().forEach(attachment -> {
                String name = (String) nodeService.getProperty(attachment, ContentModel.PROP_NAME);
                ContentReader reader = contentService.getReader(attachment, ContentModel.PROP_CONTENT);
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                try {
                    FileCopyUtils.copy(reader.getContentInputStream(), os);
                    message.addAttachment(name, new ByteArrayResource(os.toByteArray()));
                } catch (IOException | MessagingException e) {
                    throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR, e.getMessage());
                }
            });
        };

        mailService.send(preparator);
    }

    private String getEmailAddress(Object o) {
        JSONObject jo = (JSONObject) o;
        String nodeRef = (String) jo.get("nodeRef");
        return (String) nodeService.getProperty(new NodeRef(nodeRef), OpenESDHModel.PROP_CONTACT_EMAIL);
    }

    private NodeRef getDocumentNodeRef(Object o) {
        JSONObject doc = (JSONObject) o;
        String nodeRef = (String) doc.get("nodeRef");
        return documentService.getMainDocument(new NodeRef(nodeRef));
    }

    public void setMailService(JavaMailSender mailService) {
        this.mailService = mailService;
    }

    public void setCaseService(CaseService caseService) {
        this.caseService = caseService;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }
}
