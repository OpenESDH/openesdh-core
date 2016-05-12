package dk.openesdh.repo.services.audit;

import static dk.openesdh.repo.services.audit.AuditEntryHandler.ACTION;
import static dk.openesdh.repo.services.audit.AuditEntryHandler.TIME;
import static dk.openesdh.repo.services.audit.AuditEntryHandler.TYPE;
import static dk.openesdh.repo.services.audit.AuditEntryHandler.USER;

import org.json.simple.JSONObject;

public class AuditEntry {

    private final String user;
    private final long time;
    private final String fullName;
    private String action;
    private String type;
    private final JSONObject data = new JSONObject();

    AuditEntry(String user, long time, String fullName) {
        this.user = user;
        this.time = time;
        this.fullName = fullName;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * sets type by enum name with prefix "auditlog.type."
     *
     * @param type
     */
    public void setType(Enum type) {
        this.type = "auditlog.type." + type.name();
    }

    public void addData(String key, Object value) {
        data.put(key, value);
    }

    public JSONObject toJSON() {
        JSONObject auditEntry = new JSONObject();
        auditEntry.put(USER, user);
        auditEntry.put(TIME, time);
        auditEntry.put(ACTION, action);
        auditEntry.put(TYPE, type);
        auditEntry.put("data", data);
        auditEntry.put("fullName", fullName);
        return auditEntry;
    }
}
