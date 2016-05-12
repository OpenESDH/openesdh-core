package dk.openesdh.repo.services.audit.entryhandlers;

import static dk.openesdh.repo.services.audit.AuditEntryHandler.REC_TYPE.MEMBER;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

import dk.openesdh.repo.services.audit.AuditEntry;

public class MemberRemoveAuditEntryHandler extends MemberAddAuditEntryHandler {

    public static final String MEMBER_REMOVE_PATH = "/esdh/security/removeAuthority/args/parentName/value";
    private static final String MEMBER_REMOVE_CHILD = "/esdh/security/removeAuthority/args/childName/value";

    @Override
    public Optional<AuditEntry> handleEntry(AuditEntry auditEntry, Map<String, Serializable> values) {
        String parent = (String) values.get(MEMBER_REMOVE_PATH);
        String role = getRoleFromCaseGroupName(parent);
        if (role == null) {
            return Optional.empty();
        }
        String authority = (String) values.get(MEMBER_REMOVE_CHILD);
        auditEntry.setType(MEMBER);
        auditEntry.setAction("auditlog.label.member.removed");
        auditEntry.addData("authority", authority);
        auditEntry.addData("role", role);
        return Optional.of(auditEntry);
    }

}
