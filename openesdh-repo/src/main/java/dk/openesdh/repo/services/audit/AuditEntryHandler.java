package dk.openesdh.repo.services.audit;

import java.io.Serializable;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.alfresco.service.namespace.QName;
import org.json.simple.JSONObject;
import org.springframework.extensions.surf.util.I18NUtil;

import com.google.common.base.Strings;

public abstract class AuditEntryHandler implements IAuditEntryHandler {

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

    protected Optional<String> getLocalizedProperty(Map<QName, Serializable> properties, QName propQName) {
        return getLocalizedPropertyValue(properties.get(propQName));
    }

    protected Optional<String> getLocalizedPropertyValue(Serializable property) {
        if (property == null) {
            return Optional.empty();
        }
        if ((property instanceof Date)) {
            return Optional.of(AuditSearchService.AUDIT_DATE_FORMAT.format(property));
        }
        if (!(property instanceof Map)) {
            return Optional.ofNullable(Strings.emptyToNull(Objects.toString(property, null)));
        }

        String value = ((Map<Locale, String>) property).get(I18NUtil.getContentLocale());
        if (value == null) {
            value = ((Map<Locale, String>) property).get(Locale.ENGLISH);
        }
        return Optional.ofNullable(value);
    }

}
