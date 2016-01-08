package dk.openesdh.repo.services.audit.entryhandlers;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

import org.json.simple.JSONObject;
import org.springframework.extensions.surf.util.I18NUtil;

public class MemberRemoveAuditEntryHandler extends MemberAddAuditEntryHandler {

    public static final String MEMBER_REMOVE_PATH = "/esdh/security/removeAuthority/args/parentName/value";
    private static final String MEMBER_REMOVE_CHILD = "/esdh/security/removeAuthority/args/childName/value";

    @Override
    public Optional<JSONObject> handleEntry(String user, long time, Map<String, Serializable> values) {
        String parent = (String) values.get(MEMBER_REMOVE_PATH);
        String role = getRoleFromCaseGroupName(parent);
        if (role == null) {
            return Optional.empty();
        }
        String authority = (String) values.get(MEMBER_REMOVE_CHILD);
        JSONObject auditEntry = createNewAuditEntry(user, time);
        auditEntry.put(ACTION, I18NUtil.getMessage("auditlog.label.member.removed", authority, role));
        auditEntry.put(TYPE, getTypeMessage("member"));
        return Optional.of(auditEntry);
    }

}
