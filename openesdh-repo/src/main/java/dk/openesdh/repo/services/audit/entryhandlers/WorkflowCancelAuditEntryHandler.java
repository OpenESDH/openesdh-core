package dk.openesdh.repo.services.audit.entryhandlers;

import static dk.openesdh.repo.services.audit.AuditEntryHandler.REC_TYPE.WORKFLOW;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

import dk.openesdh.repo.services.audit.AuditEntry;
import dk.openesdh.repo.services.audit.AuditEntryHandler;

public class WorkflowCancelAuditEntryHandler extends AuditEntryHandler {

    public static final String WORKFLOW_CANCEL_CASE = "/esdh/workflow/cancelWorkflow/case";
    private static final String WORKFLOW_CANCEL_DESCRIPTION = "/esdh/workflow/cancelWorkflow/description";

    @Override
    public Optional<AuditEntry> handleEntry(AuditEntry auditEntry, Map<String, Serializable> values) {
        auditEntry.setType(WORKFLOW);
        auditEntry.setAction("auditlog.label.workflow.canceled");
        auditEntry.addData("description", values.get(WORKFLOW_CANCEL_DESCRIPTION));
        return Optional.of(auditEntry);
    }

}
