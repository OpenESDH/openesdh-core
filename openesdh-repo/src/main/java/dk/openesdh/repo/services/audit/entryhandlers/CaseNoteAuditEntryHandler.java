package dk.openesdh.repo.services.audit.entryhandlers;

import static dk.openesdh.repo.services.audit.AuditEntryHandler.REC_TYPE.NOTE;
import static dk.openesdh.repo.services.audit.entryhandlers.TransactionPathAuditEntryHandler.TRANSACTION_ACTION;
import static dk.openesdh.repo.services.audit.entryhandlers.TransactionPathAuditEntryHandler.TRANSACTION_ACTION_CREATE;
import static dk.openesdh.repo.services.audit.entryhandlers.TransactionPathAuditEntryHandler.TRANSACTION_ACTION_UPDATE_NODE_PROPERTIES;
import static dk.openesdh.repo.services.audit.entryhandlers.TransactionPathAuditEntryHandler.TRANSACTION_PROPERTIES_ADD;
import static dk.openesdh.repo.services.audit.entryhandlers.TransactionPathAuditEntryHandler.TRANSACTION_PROPERTIES_TO;
import static dk.openesdh.repo.services.audit.entryhandlers.TransactionPathAuditEntryHandler.TRANSACTION_TYPE;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.springframework.extensions.surf.util.I18NUtil;

import dk.openesdh.repo.services.audit.AuditEntryHandler;
import dk.openesdh.repo.services.audit.AuditUtils;

public class CaseNoteAuditEntryHandler extends AuditEntryHandler {

    private static final int MAX_NOTE_TEXT_LENGTH = 40;

    @Override
    public Optional<JSONObject> handleEntry(String user, long time, Map<String, Serializable> values) {
        String transactionAction = (String) values.get(TRANSACTION_ACTION);
        switch (transactionAction) {
            case TRANSACTION_ACTION_CREATE:
                return handleEntry(user, time, values, TRANSACTION_PROPERTIES_ADD, "auditlog.label.note.added");
            case TRANSACTION_ACTION_UPDATE_NODE_PROPERTIES:
                return handleEntry(user, time, values, TRANSACTION_PROPERTIES_TO, "auditlog.label.note.updated");
            default:
                return Optional.empty();
        }
    }

    private Optional<JSONObject> handleEntry(String user, long time, Map<String, Serializable> values, String propertiesField, String actionMessageKey) {
        String trimmedNote = StringUtils.abbreviate(AuditUtils.getTitle(values), MAX_NOTE_TEXT_LENGTH);
        JSONObject auditEntry = createNewAuditEntry(user, time);
        auditEntry.put(ACTION, I18NUtil.getMessage(actionMessageKey, trimmedNote));
        auditEntry.put(TYPE, getTypeMessage(NOTE));
        return Optional.of(auditEntry);
    }

    public static boolean canHandle(Map<String, Serializable> values) {
        String type = (String) values.get(TRANSACTION_TYPE);
        return type.startsWith("note:");
    }
}
