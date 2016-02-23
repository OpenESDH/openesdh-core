package dk.openesdh.repo.services.audit;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

import org.json.simple.JSONObject;

public interface IAuditEntryHandler {

    Optional<JSONObject> handleEntry(String user, long time, Map<String, Serializable> values);
}
