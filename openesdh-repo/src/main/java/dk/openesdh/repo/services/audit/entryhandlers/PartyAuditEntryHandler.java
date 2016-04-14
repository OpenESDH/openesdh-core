package dk.openesdh.repo.services.audit.entryhandlers;

import static dk.openesdh.repo.services.audit.AuditEntryHandler.REC_TYPE.PARTY;
import static dk.openesdh.repo.services.audit.entryhandlers.TransactionPathAuditEntryHandler.TRANSACTION_ACTION;
import static dk.openesdh.repo.services.audit.entryhandlers.TransactionPathAuditEntryHandler.TRANSACTION_ACTION_CREATE;
import static dk.openesdh.repo.services.audit.entryhandlers.TransactionPathAuditEntryHandler.TRANSACTION_ACTION_UPDATE_NODE_PROPERTIES;
import static dk.openesdh.repo.services.audit.entryhandlers.TransactionPathAuditEntryHandler.TRANSACTION_TYPE;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

import org.json.simple.JSONObject;
import org.springframework.extensions.surf.util.I18NUtil;

import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.audit.AuditEntryHandler;

public class PartyAuditEntryHandler extends AuditEntryHandler {

    public static final String CASE_PARTIES_REMOVE = "/esdh/case-parties/remove/contactName";

    private static final String TYPE_PARTY = OpenESDHModel.CONTACT_PREFIX + ":" + OpenESDHModel.STYPE_PARTY;

    private static final String TRANSACTION_PROPERTIES_ADD_CONTACT_NAME = "/esdh/transaction/properties/addContactName";
    private static final String TRANSACTION_PROPERTIES_UPDATE_CONTACT_NAME = "/esdh/transaction/properties/updateContactName";
    private static final String TRANSACTION_PROPERTIES_PARTY_ROLE_FROM = "/esdh/transaction/properties/partyRoleFrom";
    private static final String TRANSACTION_PROPERTIES_PARTY_ROLE_TO = "/esdh/transaction/properties/partyRoleTo";

    @Override
    public Optional<JSONObject> handleEntry(String user, long time, Map<String, Serializable> values) {
        JSONObject auditEntry = createNewAuditEntry(user, time);
        auditEntry.put(TYPE, getTypeMessage(PARTY));

        if (values.keySet().contains(CASE_PARTIES_REMOVE)) {
            handleOnPartyDelete(values, auditEntry);
        }else{
            String transactionAction = (String) values.get(TRANSACTION_ACTION);
            switch (transactionAction) {
                case TRANSACTION_ACTION_CREATE:
                    handleOnPartyCreate(values, auditEntry);
                    break;
                case TRANSACTION_ACTION_UPDATE_NODE_PROPERTIES:
                    return Optional.ofNullable(handleOnUpdateRole(values, auditEntry));
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

    private void handleOnPartyCreate(Map<String, Serializable> values, JSONObject auditEntry) {
        String contactName = (String) values.get(TRANSACTION_PROPERTIES_ADD_CONTACT_NAME);
        auditEntry.put(ACTION, I18NUtil.getMessage("auditlog.label.party.added", contactName));
    }

    private void handleOnPartyDelete(Map<String, Serializable> values, JSONObject auditEntry) {
        String contactName = (String) values.get(CASE_PARTIES_REMOVE);
        auditEntry.put(ACTION, I18NUtil.getMessage("auditlog.label.party.removed", contactName));
    }

    private JSONObject handleOnUpdateRole(Map<String, Serializable> values, JSONObject auditEntry) {
        String contactName = (String) values.get(TRANSACTION_PROPERTIES_UPDATE_CONTACT_NAME);
        String roleBeforeName = (String) values.get(TRANSACTION_PROPERTIES_PARTY_ROLE_FROM);
        String roleAfterName = (String) values.get(TRANSACTION_PROPERTIES_PARTY_ROLE_TO);
        auditEntry.put(ACTION, I18NUtil.getMessage("auditlog.label.party.role.changed", contactName, roleBeforeName,
                roleAfterName));
        return auditEntry;
    }
}
