package dk.openesdh.repo.services.audit.entryhandlers;

import static dk.openesdh.repo.services.audit.entryhandlers.TransactionPathAuditEntryHandler.TRANSACTION_ACTION;
import static dk.openesdh.repo.services.audit.entryhandlers.TransactionPathAuditEntryHandler.TRANSACTION_ACTION_CREATE;
import static dk.openesdh.repo.services.audit.entryhandlers.TransactionPathAuditEntryHandler.TRANSACTION_ACTION_UPDATE_NODE_PROPERTIES;
import static dk.openesdh.repo.services.audit.entryhandlers.TransactionPathAuditEntryHandler.TRANSACTION_PATH;
import static dk.openesdh.repo.services.audit.entryhandlers.TransactionPathAuditEntryHandler.TRANSACTION_SUB_ACTIONS;
import static dk.openesdh.repo.services.audit.entryhandlers.TransactionPathAuditEntryHandler.*;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.json.simple.JSONObject;
import org.springframework.extensions.surf.util.I18NUtil;

import dk.openesdh.repo.model.CaseFolderItem;
import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.audit.AuditEntryHandler;

public class CaseDocsFolderAuditEntryHandler extends AuditEntryHandler {

    @Override
    public Optional<JSONObject> handleEntry(String user, long time, Map<String, Serializable> values) {
        JSONObject auditEntry = createNewAuditEntry(user, time);
        auditEntry.put(TYPE, getTypeMessage(REC_TYPE.FOLDER));
        String transactionAction = (String) values.get(TRANSACTION_ACTION);
        Optional<String> folderPath = getFolderPath(values);
        if (!folderPath.isPresent()) {
            return Optional.empty();
        }
        switch (transactionAction) {
            case TRANSACTION_ACTION_CREATE:
                auditEntry.put(ACTION, I18NUtil.getMessage("auditlog.label.folder.created", folderPath.get()));
                break;
            case TRANSACTION_ACTION_DELETE:
                auditEntry.put(ACTION, I18NUtil.getMessage("auditlog.label.folder.deleted", folderPath.get()));
                break;
            default:
                String subActions = (String) values.get(TRANSACTION_SUB_ACTIONS);
                if (Objects.nonNull(subActions) && subActions.contains(TRANSACTION_ACTION_UPDATE_NODE_PROPERTIES)) {
                    auditEntry.put(ACTION, I18NUtil.getMessage("auditlog.label.folder.updated", folderPath.get()));
                    break;
                } else {
                    return Optional.empty();
                }
        }
        return Optional.of(auditEntry);
    }

    private Optional<String> getFolderPath(Map<String, Serializable> values) {
        String path = (String) values.get(TRANSACTION_PATH);
        if (Objects.isNull(path)) {
            return Optional.empty();
        }
        int baseDocumentsIndex = path.indexOf(OpenESDHModel.PREFIXED_DOCUMENTS_FOLDER_NAME);
        int folderPathStart = baseDocumentsIndex + OpenESDHModel.PREFIXED_DOCUMENTS_FOLDER_NAME.length() + 1;
        if (baseDocumentsIndex == -1 || folderPathStart > path.length()) {
            return Optional.empty();
        }
        return Optional.of(path.substring(folderPathStart).replace("cm:", ""));
    }

    public static boolean canHandle(Map<String, Serializable> values) {
        String type = (String) values.get(TRANSACTION_TYPE);
        return type.equals(CaseFolderItem.ITEM_TYPE_FOLDER);
    }
}
