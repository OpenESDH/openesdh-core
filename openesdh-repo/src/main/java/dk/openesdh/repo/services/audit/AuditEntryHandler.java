package dk.openesdh.repo.services.audit;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

public abstract class AuditEntryHandler implements IAuditEntryHandler {

    public enum REC_TYPE {
        CASE,
        DOCUMENT,
        ATTACHMENT,
        SYSTEM,
        MEMBER,
        PARTY,
        NOTE,
        FOLDER,
        WORKFLOW
    }
    public static final String TIME = "time";
    public static final String USER = "user";
    public static final String TYPE = "type";
    public static final String ACTION = "action";

    public Optional<AuditEntry> createAuditEntry(AuditEntry auditEntry, Map<String, Serializable> values) {
        return handleEntry(auditEntry, values);
    }
}
