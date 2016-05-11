package dk.openesdh.repo.services.audit.entryhandlers;

import static dk.openesdh.repo.services.audit.AuditEntryHandler.REC_TYPE.WORKFLOW;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

import dk.openesdh.repo.services.audit.AuditEntry;
import dk.openesdh.repo.services.audit.AuditEntryHandler;

public class WorkflowStartAuditEntryHandler extends AuditEntryHandler {

    public static final String WORKFLOW_START_CASE = "/esdh/workflow/start/case";
    private static final String WORKFLOW_START_DESCRIPTION = "/esdh/workflow/start/description";

    @Override
    public Optional<AuditEntry> handleEntry(String user, long time, Map<String, Serializable> values) {
        AuditEntry auditEntry = new AuditEntry(user, time);
        auditEntry.setType(WORKFLOW);
        auditEntry.setAction("auditlog.label.workflow.started");
        auditEntry.addData("description", values.get(WORKFLOW_START_DESCRIPTION));
        return Optional.of(auditEntry);
    }

}
