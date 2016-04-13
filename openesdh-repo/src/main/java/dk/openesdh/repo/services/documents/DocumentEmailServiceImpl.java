package dk.openesdh.repo.services.documents;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.cases.CaseService;

@Service(DocumentEmailService.BEAN_ID)
public class DocumentEmailServiceImpl implements DocumentEmailService {

    @Autowired
    private CaseService caseService;
    @Autowired
    private NodeService nodeService;
    @Autowired
    private DocumentService documentService;
    @Autowired
    private ContentService contentService;
    @Autowired
    private JavaMailSender mailService;
    @Value("${mail.from.default}")
    private String defaultFromEmail;

    @Override
    public void send(String caseId, Collection<NodeRef> recipients, String subject, String text, Collection<NodeRef> attachments) {
        NodeRef caseNode = caseService.getCaseById(caseId);
        if (caseNode == null) {
            throw new AlfrescoRuntimeException("Case not found" + caseId);
        }
        MimeMessagePreparator preparator = mimeMessage -> {
            MimeMessageHelper message = new MimeMessageHelper(mimeMessage, attachments.size() > 0, "UTF-8");
            recipients.stream().map(this::getEmailAddress).forEach(email -> {
                try {
                    message.addTo(new InternetAddress(email));
                } catch (MessagingException e) {
                    throw new AlfrescoRuntimeException(e.getMessage());
                }
            });
            message.setSubject(subject);
            message.setText(text);
            message.setFrom(new InternetAddress(defaultFromEmail));

            attachments.stream().map(documentService::getMainDocument).forEach(attachment -> {
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

    private String getEmailAddress(NodeRef nodeRef) {
        return (String) nodeService.getProperty(nodeRef, OpenESDHModel.PROP_CONTACT_EMAIL);
    }
}
