package dk.openesdh.repo.services.audit.entryhandlers;

import static dk.openesdh.repo.services.audit.entryhandlers.TransactionPathAuditEntryHandler.TRANSACTION_PROPERTIES_ADD;
import static dk.openesdh.repo.services.audit.entryhandlers.TransactionPathAuditEntryHandler.TRANSACTION_PROPERTIES_TO;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.QName;
import org.json.simple.JSONObject;
import org.springframework.extensions.surf.util.I18NUtil;

import dk.openesdh.repo.services.audit.AuditEntryHandler;

/**
 * not a real entry handler, used in other handlers to display changed properties on any node type
 *
 * @author petraarn
 */
public class NodePropertyChangesAuditEntrySubHandler extends AuditEntryHandler {

    private final DictionaryService dictionaryService;
    private final Set<QName> ignoredProperties;

    public NodePropertyChangesAuditEntrySubHandler(DictionaryService dictionaryService, Set<QName> ignoredProperties) {
        this.dictionaryService = dictionaryService;
        this.ignoredProperties = ignoredProperties;
    }

    @Override
    public Optional<JSONObject> handleEntry(String user, long time, Map<String, Serializable> values) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public List<String> getChangedProperties(Map<String, Serializable> values) {
        Map<QName, Serializable> toMap = (Map<QName, Serializable>) values.get(TRANSACTION_PROPERTIES_TO);
        Map<QName, Serializable> addMap = (Map<QName, Serializable>) values.get(TRANSACTION_PROPERTIES_ADD);
        if (addMap == null && toMap == null) {
            return Collections.emptyList();
        }
        toMap = toMap == null ? new HashMap<>() : filterUndesirableProps(toMap);
        if (addMap != null) {
            addMap = filterUndesirableProps(addMap);
            toMap.putAll(addMap);
        }

        List<String> changes = new ArrayList<>();
        toMap.forEach((qName, value) -> {
            Optional<String> to = getLocalizedPropertyValue(value);
            if (!to.isPresent()) {
                changes.add(I18NUtil.getMessage("auditlog.label.property.removed",
                        getPropertyTitle(qName)));
            } else {
                changes.add(I18NUtil.getMessage("auditlog.label.property.changed",
                        getPropertyTitle(qName),
                        to.orElse("")));
            }
        });
        Collections.sort(changes);
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
