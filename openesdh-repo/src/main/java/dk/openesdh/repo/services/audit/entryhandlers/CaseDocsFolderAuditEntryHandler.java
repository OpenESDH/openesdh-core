package dk.openesdh.repo.services.audit.entryhandlers;

import static dk.openesdh.repo.services.audit.AuditEntryHandler.REC_TYPE.FOLDER;
import static dk.openesdh.repo.services.audit.entryhandlers.TransactionPathAuditEntryHandler.*;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import dk.openesdh.repo.model.CaseFolderItem;
import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.audit.AuditEntry;
import dk.openesdh.repo.services.audit.AuditEntryHandler;

public class CaseDocsFolderAuditEntryHandler extends AuditEntryHandler {

    @Override
    public Optional<AuditEntry> handleEntry(AuditEntry auditEntry, Map<String, Serializable> values) {
        auditEntry.setType(FOLDER);

        String transactionAction = (String) values.get(TRANSACTION_ACTION);
        Optional<String> folderPath = getFolderPath(values);
        if (!folderPath.isPresent()) {
            return Optional.empty();
        }

        auditEntry.addData("title", folderPath.get());
        switch (transactionAction) {
            case TRANSACTION_ACTION_CREATE:
                auditEntry.setAction("auditlog.label.folder.created");
                break;
            case TRANSACTION_ACTION_DELETE:
                auditEntry.setAction("auditlog.label.folder.deleted");
                break;
            default:
                String subActions = (String) values.get(TRANSACTION_SUB_ACTIONS);
                if (Objects.nonNull(subActions) && subActions.contains(TRANSACTION_ACTION_UPDATE_NODE_PROPERTIES)) {
                    auditEntry.setAction("auditlog.label.folder.updated");
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
