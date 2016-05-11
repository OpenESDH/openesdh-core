package dk.openesdh.repo.services.audit.entryhandlers;

import static dk.openesdh.repo.services.audit.AuditEntryHandler.REC_TYPE.NOTE;
import static dk.openesdh.repo.services.audit.entryhandlers.TransactionPathAuditEntryHandler.TRANSACTION_ACTION;
import static dk.openesdh.repo.services.audit.entryhandlers.TransactionPathAuditEntryHandler.TRANSACTION_ACTION_CREATE;
import static dk.openesdh.repo.services.audit.entryhandlers.TransactionPathAuditEntryHandler.TRANSACTION_ACTION_UPDATE_NODE_PROPERTIES;
import static dk.openesdh.repo.services.audit.entryhandlers.TransactionPathAuditEntryHandler.TRANSACTION_TYPE;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;

import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.audit.AuditEntry;
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
    public Optional<AuditEntry> handleEntry(String user, long time, Map<String, Serializable> values) {
        String trimmedNote = StringUtils.abbreviate(AuditUtils.getTitle(values), MAX_NOTE_TEXT_LENGTH);
        AuditEntry auditEntry = new AuditEntry(user, time);
        auditEntry.setType(NOTE);

        String transactionAction = (String) values.get(TRANSACTION_ACTION);
        switch (transactionAction) {
            case TRANSACTION_ACTION_CREATE:
                auditEntry.setAction("auditlog.label.note.added");
                auditEntry.addData("note", trimmedNote);
                break;
            case TRANSACTION_ACTION_UPDATE_NODE_PROPERTIES:
                auditEntry.setAction("auditlog.label.note.updated");
                auditEntry.addData("note", trimmedNote);

                JSONArray changes = nodePropertiesChangeHandler.getChangedProperties(values);
                if (changes.isEmpty()) {
                    return Optional.empty();
                }
                auditEntry.addData("props", changes);
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
