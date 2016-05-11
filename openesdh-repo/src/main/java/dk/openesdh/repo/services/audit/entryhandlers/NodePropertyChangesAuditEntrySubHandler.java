package dk.openesdh.repo.services.audit.entryhandlers;

import static dk.openesdh.repo.services.audit.entryhandlers.TransactionPathAuditEntryHandler.TRANSACTION_PROPERTIES_ADD;
import static dk.openesdh.repo.services.audit.entryhandlers.TransactionPathAuditEntryHandler.TRANSACTION_PROPERTIES_TO;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.QName;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import dk.openesdh.repo.services.audit.AuditUtils;

/**
 * not a real entry handler, used in other handlers to display changed properties on any node type
 *
 * @author petraarn
 */
public class NodePropertyChangesAuditEntrySubHandler {

    private final DictionaryService dictionaryService;
    private final Set<QName> ignoredProperties;

    public NodePropertyChangesAuditEntrySubHandler(DictionaryService dictionaryService, Set<QName> ignoredProperties) {
        this.dictionaryService = dictionaryService;
        this.ignoredProperties = ignoredProperties;
    }

    public JSONArray getChangedProperties(Map<String, Serializable> values) {
        Map<QName, Serializable> toMap = (Map<QName, Serializable>) values.get(TRANSACTION_PROPERTIES_TO);
        Map<QName, Serializable> addMap = (Map<QName, Serializable>) values.get(TRANSACTION_PROPERTIES_ADD);
        if (addMap == null && toMap == null) {
            return new JSONArray();
        }
        toMap = toMap == null ? new HashMap<>() : filterUndesirableProps(toMap);
        if (addMap != null) {
            addMap = filterUndesirableProps(addMap);
            toMap.putAll(addMap);
        }

        JSONArray changes = new JSONArray();
        toMap.forEach((qName, value) -> {
            JSONObject item = new JSONObject();
            JSONObject itemData = new JSONObject();
            item.put("data", itemData);
            changes.add(item);

            Optional<String> to = AuditUtils.getLocalizedPropertyValue(value);
            itemData.put("title", getPropertyTitle(qName));
            if (!to.isPresent()) {
                item.put("code", "auditlog.label.property.removed");
            } else {
                item.put("code", "auditlog.label.property.changed");
                itemData.put("toValue", to.get());
            }
        });
        return changes;
    }

    private String getPropertyTitle(QName qName) {
        PropertyDefinition property = dictionaryService.getProperty(qName);
        if (property != null) {
            String title = property.getTitle(dictionaryService);
            if (title != null) {
                return title;
            }
        }
        return qName.getLocalName();
    }

    private Map<QName, Serializable> filterUndesirableProps(Map<QName, Serializable> map) {
        return map.entrySet()
                .stream()
                .filter(p -> !ignoredProperties.contains(p.getKey()))
                .collect(Collectors.toMap(p -> p.getKey(), p -> Optional.ofNullable(p.getValue()).orElse("")));
    }
}
