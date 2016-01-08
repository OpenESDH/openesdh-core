package dk.openesdh.repo.services.audit.entryhandlers;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

import org.json.simple.JSONObject;
import org.springframework.extensions.surf.util.I18NUtil;

import dk.openesdh.repo.services.audit.AuditEntryHandler;

public class WorkflowTaskEndAuditEntryHandler extends AuditEntryHandler {

    public static final String WORKFLOW_END_TASK_CASE = "/esdh/workflow/endTask/case";
    private static final String WORKFLOW_END_TASK_DESCRIPTION = "/esdh/workflow/endTask/description";
    private static final String WORKFLOW_END_TASK_REVIEW_OUTCOME = "/esdh/workflow/endTask/reviewOutcome";

    @Override
    public Optional<JSONObject> handleEntry(String user, long time, Map<String, Serializable> values) {
        JSONObject auditEntry = createNewAuditEntry(user, time);
        auditEntry.put(TYPE, getTypeMessage("workflow"));

        String taskOutcome = values.getOrDefault(WORKFLOW_END_TASK_REVIEW_OUTCOME, "ended").toString();
        auditEntry.put(ACTION,
                I18NUtil.getMessage("auditlog.label.workflow.task." + taskOutcome,
                        values.get(WORKFLOW_END_TASK_DESCRIPTION)));
        return Optional.of(auditEntry);
    }

}
