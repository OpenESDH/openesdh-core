package dk.openesdh.repo.services.audit;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.audit.AuditService;
import org.json.simple.JSONArray;

public class OpenESDHAuditQueryCallBack implements AuditService.AuditQueryCallback {

    private final Map<String, AuditEntryHandler> auditEntryHandlers;

    private final JSONArray result = new JSONArray();

    public OpenESDHAuditQueryCallBack(Map<String, AuditEntryHandler> auditEntryHandlers) {
        this.auditEntryHandlers = auditEntryHandlers;
    }

    public JSONArray getResult() {
        return result;
    }

    @Override
    public boolean valuesRequired() {
        return true;
    }

    @Override
    public boolean handleAuditEntry(Long entryId, String applicationName, String user, long time, Map<String, Serializable> values) {
        getAuditEntryHandler(values.keySet())
                .flatMap(handler -> handler.createAuditEntry(user, time, values))
                .ifPresent(auditEntry -> result.add(auditEntry));
        return true;
    }

    private Optional<AuditEntryHandler> getAuditEntryHandler(Set<String> auditValuesEntryKeys) {
        return auditEntryHandlers.entrySet()
                .stream()
                .filter(handler -> auditValuesEntryKeys.contains(handler.getKey()))
                .findFirst()
                .map(handler -> handler.getValue());
    }

    @Override
    public boolean handleAuditEntryError(Long entryId, String errorMsg, Throwable error) {
        throw new AlfrescoRuntimeException(errorMsg, error);
    }
}
