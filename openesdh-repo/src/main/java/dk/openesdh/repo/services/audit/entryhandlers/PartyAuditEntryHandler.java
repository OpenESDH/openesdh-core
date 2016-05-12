package dk.openesdh.repo.services.audit.entryhandlers;

import static dk.openesdh.repo.services.audit.AuditEntryHandler.REC_TYPE.PARTY;
import static dk.openesdh.repo.services.audit.entryhandlers.TransactionPathAuditEntryHandler.TRANSACTION_ACTION;
import static dk.openesdh.repo.services.audit.entryhandlers.TransactionPathAuditEntryHandler.TRANSACTION_ACTION_CREATE;
import static dk.openesdh.repo.services.audit.entryhandlers.TransactionPathAuditEntryHandler.TRANSACTION_ACTION_UPDATE_NODE_PROPERTIES;
import static dk.openesdh.repo.services.audit.entryhandlers.TransactionPathAuditEntryHandler.TRANSACTION_TYPE;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.audit.AuditEntry;
import dk.openesdh.repo.services.audit.AuditEntryHandler;

public class PartyAuditEntryHandler extends AuditEntryHandler {

    public static final String CASE_PARTIES_REMOVE = "/esdh/case-parties/remove/contactName";

    private static final String TYPE_PARTY = OpenESDHModel.CONTACT_PREFIX + ":" + OpenESDHModel.STYPE_PARTY;

    private static final String TRANSACTION_PROPERTIES_ADD_CONTACT_NAME = "/esdh/transaction/properties/addContactName";
    private static final String TRANSACTION_PROPERTIES_UPDATE_CONTACT_NAME = "/esdh/transaction/properties/updateContactName";
    private static final String TRANSACTION_PROPERTIES_PARTY_ROLE_FROM = "/esdh/transaction/properties/partyRoleFrom";
    private static final String TRANSACTION_PROPERTIES_PARTY_ROLE_TO = "/esdh/transaction/properties/partyRoleTo";

    @Override
    public Optional<AuditEntry> handleEntry(AuditEntry auditEntry, Map<String, Serializable> values) {
        auditEntry.setType(PARTY);

        if (values.keySet().contains(CASE_PARTIES_REMOVE)) {
            handleOnPartyDelete(values, auditEntry);
        } else {
            String transactionAction = (String) values.get(TRANSACTION_ACTION);
            switch (transactionAction) {
                case TRANSACTION_ACTION_CREATE:
                    handleOnPartyCreate(values, auditEntry);
                    break;
                case TRANSACTION_ACTION_UPDATE_NODE_PROPERTIES:
                    handleOnUpdateRole(values, auditEntry);
                    break;
                default:
                    return Optional.empty();
            }
        }
        return Optional.of(auditEntry);
    }

    public static boolean canHandleTransactionEntry(Map<String, Serializable> values) {
        String type = (String) values.get(TRANSACTION_TYPE);
        return type.equals(TYPE_PARTY);
    }

    private void handleOnPartyCreate(Map<String, Serializable> values, AuditEntry auditEntry) {
        String contactName = (String) values.get(TRANSACTION_PROPERTIES_ADD_CONTACT_NAME);
        auditEntry.setAction("auditlog.label.party.added");
        auditEntry.addData("contactName", contactName);
    }

    private void handleOnPartyDelete(Map<String, Serializable> values, AuditEntry auditEntry) {
        String contactName = (String) values.get(CASE_PARTIES_REMOVE);
        auditEntry.setAction("auditlog.label.party.removed");
        auditEntry.addData("contactName", contactName);
    }

    private void handleOnUpdateRole(Map<String, Serializable> values, AuditEntry auditEntry) {
        String contactName = (String) values.get(TRANSACTION_PROPERTIES_UPDATE_CONTACT_NAME);
        String roleBeforeName = (String) values.get(TRANSACTION_PROPERTIES_PARTY_ROLE_FROM);
        String roleAfterName = (String) values.get(TRANSACTION_PROPERTIES_PARTY_ROLE_TO);

        auditEntry.setAction("auditlog.label.party.role_changed");
        auditEntry.addData("contactName", contactName);
        auditEntry.addData("roleBeforeName", roleBeforeName);
        auditEntry.addData("roleAfterName", roleAfterName);
    }
}
