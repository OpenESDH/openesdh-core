package dk.openesdh.repo.services.audit;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

import org.json.simple.JSONObject;
import org.springframework.extensions.surf.util.I18NUtil;

public abstract class AuditEntryHandler {

    public enum REC_TYPE {
        CASE,
        DOCUMENT,
        ATTACHMENT,
        SYSTEM,
        MEMBER,
        PARTY,
        NOTE
    }
    public static final String TIME = "time";
    public static final String USER = "user";
    public static final String TYPE = "type";
    public static final String ACTION = "action";

    protected abstract Optional<JSONObject> handleEntry(String user, long time, Map<String, Serializable> values);

    public Optional<JSONObject> createAuditEntry(String user, long time, Map<String, Serializable> values) {
        return handleEntry(user, time, values);
    }

    protected JSONObject createNewAuditEntry(String user, long time) {
        JSONObject auditEntry = new JSONObject();
        auditEntry.put(USER, user);
        auditEntry.put(TIME, time);
        return auditEntry;
    }

    protected String getTypeMessage(String type) {
        return I18NUtil.getMessage("auditlog.label.type." + type);
    }
    protected String getTypeMessage(REC_TYPE type) {
        return getTypeMessage(type.name().toLowerCase());
    }
}
