package dk.openesdh.repo.services.audit.entryhandlers;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

import org.json.simple.JSONObject;
import org.springframework.extensions.surf.util.I18NUtil;

import dk.openesdh.repo.services.audit.AuditEntryHandler;

public class WorkflowStartAuditEntryHandler extends AuditEntryHandler {

    public static final String WORKFLOW_START_CASE = "/esdh/workflow/start/case";
    private static final String WORKFLOW_START_DESCRIPTION = "/esdh/workflow/start/description";

    @Override
    public Optional<JSONObject> handleEntry(String user, long time, Map<String, Serializable> values) {
        JSONObject auditEntry = createNewAuditEntry(user, time);
        auditEntry.put(TYPE, getTypeMessage("workflow"));
        auditEntry.put(ACTION,
                I18NUtil.getMessage("auditlog.label.workflow.started", values.get(WORKFLOW_START_DESCRIPTION)));
        return Optional.of(auditEntry);
    }

}
