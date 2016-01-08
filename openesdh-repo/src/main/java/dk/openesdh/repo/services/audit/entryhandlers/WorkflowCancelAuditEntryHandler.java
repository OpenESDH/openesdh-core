package dk.openesdh.repo.services.audit.entryhandlers;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

import org.json.simple.JSONObject;
import org.springframework.extensions.surf.util.I18NUtil;

import dk.openesdh.repo.services.audit.AuditEntryHandler;

public class WorkflowCancelAuditEntryHandler extends AuditEntryHandler {

    public static final String WORKFLOW_CANCEL_CASE = "/esdh/workflow/cancelWorkflow/case";
    private static final String WORKFLOW_CANCEL_DESCRIPTION = "/esdh/workflow/cancelWorkflow/description";

    @Override
    public Optional<JSONObject> handleEntry(String user, long time, Map<String, Serializable> values) {
        JSONObject auditEntry = createNewAuditEntry(user, time);
        auditEntry.put(TYPE, getTypeMessage("workflow"));
        auditEntry.put(ACTION,
                I18NUtil.getMessage("auditlog.label.workflow.canceled", values.get(WORKFLOW_CANCEL_DESCRIPTION)));
        return Optional.of(auditEntry);
    }

}
