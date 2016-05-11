package dk.openesdh.repo.services.audit.entryhandlers;

import static dk.openesdh.repo.services.audit.AuditEntryHandler.REC_TYPE.MEMBER;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;

import dk.openesdh.repo.services.audit.AuditEntry;
import dk.openesdh.repo.services.audit.AuditEntryHandler;
import dk.openesdh.repo.services.cases.CaseService;

public class MemberAddAuditEntryHandler extends AuditEntryHandler {

    public static final String MEMBER_ADD_PATH = "/esdh/security/addAuthority/args/parentName/value";
    private static final String MEMBER_ADD_CHILD = "/esdh/security/addAuthority/args/childName/value";

    @Override
    public Optional<AuditEntry> handleEntry(String user, long time, Map<String, Serializable> values) {
        String parent = (String) values.get(MEMBER_ADD_PATH);
        String role = getRoleFromCaseGroupName(parent);
        if (role == null) {
            return Optional.empty();
        }
        String authority = (String) values.get(MEMBER_ADD_CHILD);
        AuditEntry auditEntry = new AuditEntry(user, time);
        auditEntry.setType(MEMBER);
        auditEntry.setAction("auditlog.label.member.added");
        auditEntry.addData("authority", authority);
        auditEntry.addData("role", role);
        return Optional.of(auditEntry);
    }

    /**
     * Return the role given a case group name. Returns null if the group name does not belong to a case.
     *
     * @param groupName
     * @return
     */
    protected String getRoleFromCaseGroupName(String groupName) {
        Matcher matcher = CaseService.CASE_ROLE_GROUP_NAME_PATTERN.matcher(groupName);
        if (matcher.matches()) {
            return matcher.group(2);
        } else {
            return null;
        }
    }

}
