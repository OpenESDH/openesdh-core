package dk.openesdh.repo.services.audit.entryhandlers;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.springframework.extensions.surf.util.I18NUtil;

import dk.openesdh.repo.services.audit.AuditEntryHandler;

public class PartyAddAuditEntryHandler extends AuditEntryHandler {

    public static final String PARTY_ADD_NAME = "/esdh/child/add/args/contactName";
    private static final String PARTY_ADD_GROUP_NAME = "/esdh/child/add/args/groupName";

    @Override
    public Optional<JSONObject> handleEntry(String user, long time, Map<String, Serializable> values) {
        String contactName = (String) values.get(PARTY_ADD_NAME);
        if (StringUtils.isEmpty(contactName)) {
            return Optional.empty();
        }
        JSONObject auditEntry = createNewAuditEntry(user, time);
        String groupName = (String) values.get(PARTY_ADD_GROUP_NAME);
        auditEntry.put(ACTION, I18NUtil.getMessage("auditlog.label.party.added", contactName, groupName));
        auditEntry.put(TYPE, getTypeMessage("party"));
        return Optional.of(auditEntry);
    }

}
