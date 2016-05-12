package dk.openesdh.repo.services.audit;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

public interface IAuditEntryHandler {

    Optional<AuditEntry> handleEntry(AuditEntry auditEntry, Map<String, Serializable> values);
}
