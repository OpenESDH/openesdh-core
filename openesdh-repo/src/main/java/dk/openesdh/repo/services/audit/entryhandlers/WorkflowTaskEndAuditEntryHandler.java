package dk.openesdh.repo.services.audit.entryhandlers;

import static dk.openesdh.repo.services.audit.AuditEntryHandler.REC_TYPE.WORKFLOW;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

import dk.openesdh.repo.services.audit.AuditEntry;
import dk.openesdh.repo.services.audit.AuditEntryHandler;

public class WorkflowTaskEndAuditEntryHandler extends AuditEntryHandler {

    public static final String WORKFLOW_END_TASK_CASE = "/esdh/workflow/endTask/case";
    private static final String WORKFLOW_END_TASK_DESCRIPTION = "/esdh/workflow/endTask/description";
    private static final String WORKFLOW_END_TASK_REVIEW_OUTCOME = "/esdh/workflow/endTask/reviewOutcome";

    @Override
    public Optional<AuditEntry> handleEntry(String user, long time, Map<String, Serializable> values) {
        AuditEntry auditEntry = new AuditEntry(user, time);
        auditEntry.setType(WORKFLOW);

        String taskOutcome = values.getOrDefault(WORKFLOW_END_TASK_REVIEW_OUTCOME, "ended").toString();
        auditEntry.setAction("auditlog.label.workflow.task." + taskOutcome);
        auditEntry.addData("description", values.get(WORKFLOW_END_TASK_DESCRIPTION));
        return Optional.of(auditEntry);
    }

}
