package dk.openesdh.repo.services.audit.entryhandlers;

import static dk.openesdh.repo.services.audit.AuditEntryHandler.REC_TYPE.NOTE;
import static dk.openesdh.repo.services.audit.entryhandlers.TransactionPathAuditEntryHandler.TRANSACTION_ACTION;
import static dk.openesdh.repo.services.audit.entryhandlers.TransactionPathAuditEntryHandler.TRANSACTION_ACTION_CREATE;
import static dk.openesdh.repo.services.audit.entryhandlers.TransactionPathAuditEntryHandler.TRANSACTION_ACTION_UPDATE_NODE_PROPERTIES;
import static dk.openesdh.repo.services.audit.entryhandlers.TransactionPathAuditEntryHandler.TRANSACTION_TYPE;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.springframework.extensions.surf.util.I18NUtil;

import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.audit.AuditEntryHandler;
import dk.openesdh.repo.services.audit.AuditUtils;

public class CaseNoteAuditEntryHandler extends AuditEntryHandler {

    private static final int MAX_NOTE_TEXT_LENGTH = 40;
    private static final String TYPE_PREFIX_TO_HANDLE = OpenESDHModel.NOTE_PREFIX + ":";

    private final NodePropertyChangesAuditEntrySubHandler nodePropertiesChangeHandler;

    public CaseNoteAuditEntryHandler(NodePropertyChangesAuditEntrySubHandler nodePropertiesChangeHandler) {
        this.nodePropertiesChangeHandler = nodePropertiesChangeHandler;
    }

    @Override
    public Optional<JSONObject> handleEntry(String user, long time, Map<String, Serializable> values) {
        String trimmedNote = StringUtils.abbreviate(AuditUtils.getTitle(values), MAX_NOTE_TEXT_LENGTH);
        JSONObject auditEntry = createNewAuditEntry(user, time);
        auditEntry.put(TYPE, getTypeMessage(NOTE));
        String transactionAction = (String) values.get(TRANSACTION_ACTION);
        switch (transactionAction) {
            case TRANSACTION_ACTION_CREATE:
                auditEntry.put(ACTION, I18NUtil.getMessage("auditlog.label.note.added", trimmedNote));
                break;
            case TRANSACTION_ACTION_UPDATE_NODE_PROPERTIES:
                List<String> changes = nodePropertiesChangeHandler.getChangedProperties(values);
                if (changes.isEmpty()) {
                    return Optional.empty();
                }
                auditEntry.put(ACTION, I18NUtil.getMessage("auditlog.label.note.updated", trimmedNote, StringUtils.join(changes, ";\n")));
                break;
            default:
                return Optional.empty();
        }
        return Optional.of(auditEntry);
    }

    public static boolean canHandle(Map<String, Serializable> values) {
        String type = (String) values.get(TRANSACTION_TYPE);
        return type.startsWith(TYPE_PREFIX_TO_HANDLE);
    }
}
