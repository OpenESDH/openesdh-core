package dk.openesdh.repo.services.audit.entryhandlers;

import com.google.common.base.Joiner;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang.BooleanUtils;
import org.json.simple.JSONObject;
import org.springframework.extensions.surf.util.I18NUtil;

import dk.openesdh.repo.services.audit.AuditEntryHandler;

public class CaseEmailSentAuditEntryHandler extends AuditEntryHandler {

    public static final String CASE_EMAIL_RECIPIENTS = "/esdh/action/case-email/recipients";
    private static final String CASE_EMAIL_ATTACHMENTS = "/esdh/action/case-email/attachments";

    @Override
    public Optional<JSONObject> handleEntry(String user, long time, Map<String, Serializable> values) {
        JSONObject auditEntry = createNewAuditEntry(user, time);
        List<String> participants = (List<String>) values.get(CASE_EMAIL_RECIPIENTS);
        List<String> attachments = (List<String>) values.get(CASE_EMAIL_ATTACHMENTS);
        auditEntry.put(ACTION, I18NUtil.getMessage("auditlog.label.email.sent."
                + BooleanUtils.toString(attachments.size() == 1, "1", "n"),
                attachments.stream().collect(Collectors.joining("\", \"", "\"", "\"")),
                Joiner.on(", ").join(participants)));
        auditEntry.put(TYPE, getTypeMessage("document"));
        return Optional.of(auditEntry);
    }

}
