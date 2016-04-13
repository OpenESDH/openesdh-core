package dk.openesdh.repo.services.documents;

import java.util.Collection;

import org.alfresco.service.Auditable;
import org.alfresco.service.cmr.repository.NodeRef;

public interface DocumentEmailService {

    String BEAN_ID = "DocumentEmailService";

    /**
     * Sends email to recipients with attached document
     *
     * @param caseId
     * @param recipients
     * @param subject
     * @param message
     * @param attachments
     */
    @Auditable(
            parameters = {"caseId", "recipients", "subject", "message", "attachments"},
            recordable = {true, true, false, false, true})
    public void send(String caseId, Collection<NodeRef> recipients, String subject, String message, Collection<NodeRef> attachments);

}
