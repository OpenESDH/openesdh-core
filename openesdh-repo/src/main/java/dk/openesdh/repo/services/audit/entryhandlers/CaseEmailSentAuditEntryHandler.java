package dk.openesdh.repo.services.audit.entryhandlers;

import static dk.openesdh.repo.services.audit.AuditEntryHandler.REC_TYPE.DOCUMENT;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import dk.openesdh.repo.services.audit.AuditEntry;
import dk.openesdh.repo.services.audit.AuditEntryHandler;

public class CaseEmailSentAuditEntryHandler extends AuditEntryHandler {

    public static final String CASE_EMAIL_RECIPIENTS = "/esdh/action/case-email/recipients";
    private static final String CASE_EMAIL_ATTACHMENTS = "/esdh/action/case-email/attachments";

    @Override
    public Optional<AuditEntry> handleEntry(String user, long time, Map<String, Serializable> values) {
        List<String> participants = (List<String>) values.get(CASE_EMAIL_RECIPIENTS);
        List<String> attachments = (List<String>) values.get(CASE_EMAIL_ATTACHMENTS);

        AuditEntry auditEntry = new AuditEntry(user, time);
        auditEntry.setAction("auditlog.label.email.sent_" + BooleanUtils.toString(attachments.size() == 1, "1", "n"));
        auditEntry.setType(DOCUMENT);
        auditEntry.addData("attachments", StringUtils.join(attachments, ", "));
        auditEntry.addData("participants", StringUtils.join(participants, ", "));
        return Optional.of(auditEntry);
    }

}
