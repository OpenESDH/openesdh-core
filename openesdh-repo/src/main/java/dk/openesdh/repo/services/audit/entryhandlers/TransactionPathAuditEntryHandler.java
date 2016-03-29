package dk.openesdh.repo.services.audit.entryhandlers;

import static dk.openesdh.repo.services.audit.AuditEntryHandler.REC_TYPE.ATTACHMENT;
import static dk.openesdh.repo.services.audit.AuditEntryHandler.REC_TYPE.CASE;
import static dk.openesdh.repo.services.audit.AuditEntryHandler.REC_TYPE.DOCUMENT;
import static dk.openesdh.repo.services.audit.AuditEntryHandler.REC_TYPE.SYSTEM;
import static dk.openesdh.repo.services.audit.AuditUtils.getLastPathElement;
import static dk.openesdh.repo.services.audit.AuditUtils.getTitle;
import static dk.openesdh.repo.services.system.OpenESDHFoldersService.FILES_ROOT_PATH;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.util.CollectionUtils;

import com.google.common.base.Strings;

import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.audit.AuditEntryHandler;
import dk.openesdh.repo.services.audit.AuditSearchService;
import dk.openesdh.repo.services.audit.IAuditEntryHandler;

public class TransactionPathAuditEntryHandler extends AuditEntryHandler {

    public static final String TRANSACTION_USER = "/esdh/transaction/user";
    public static final String TRANSACTION_PATH = "/esdh/transaction/path";
    public static final String TRANSACTION_TYPE = "/esdh/transaction/type";
    public static final String TRANSACTION_ACTION = "/esdh/transaction/action";
    private static final String TRANSACTION_SUB_ACTIONS = "/esdh/transaction/sub-actions";
    private static final String TRANSACTION_ASPECT_ADD = "/esdh/transaction/aspects/add";
    private static final String TRANSACTION_DOC_FROM_FILES = "/esdh/transaction/move/from/path";

    public static final String TRANSACTION_ACTION_CREATE = "CREATE";
    public static final String TRANSACTION_ACTION_DELETE = "DELETE";
    public static final String TRANSACTION_ACTION_CHECK_IN = "CHECK IN";
    public static final String TRANSACTION_ACTION_CREATE_VERSION = "CREATE VERSION";
    public static final String TRANSACTION_ACTION_UPDATE_NODE_PROPERTIES = "updateNodeProperties";

    public static final String TRANSACTION_PROPERTIES_ADD = "/esdh/transaction/properties/add";
    public static final String TRANSACTION_PROPERTIES_FROM = "/esdh/transaction/properties/from";
    public static final String TRANSACTION_PROPERTIES_TO = "/esdh/transaction/properties/to";

    private final DictionaryService dictionaryService;
    private final Set<QName> ignoredProperties;
    private final Set<QName> ignoredAspects;
    private Map<Predicate<Map<String, Serializable>>, IAuditEntryHandler> trPathEntryHandlers = new HashMap<>();

    public TransactionPathAuditEntryHandler(DictionaryService dictionaryService, Set<QName> ignoredProperties,
            Set<QName> ignoredAspects, Map<Predicate<Map<String, Serializable>>, IAuditEntryHandler> trPathEntryHandlers) {
        this.dictionaryService = dictionaryService;
        this.ignoredProperties = ignoredProperties;
        this.ignoredAspects = ignoredAspects;
        this.trPathEntryHandlers = trPathEntryHandlers;
    }

    @Override
    public Optional<JSONObject> handleEntry(String user, long time, Map<String, Serializable> values) {
        return trPathEntryHandlers.entrySet()
                .stream()
                .filter(entry -> entry.getKey().test(values))
                .findAny()
                .map(entry -> entry.getValue())
                .orElse(this::defaultHandleEntry)
                .handleEntry(user, time, values);
    }

    private Optional<JSONObject> defaultHandleEntry(String user, long time, Map<String, Serializable> values) {
        switch ((String) values.get(TRANSACTION_ACTION)) {
            case TRANSACTION_ACTION_CREATE:
                return getEntryTransactionCreate(user, time, values);
            case TRANSACTION_ACTION_DELETE:
                return getEntryTransactionDelete(user, time, values);
            case TRANSACTION_ACTION_CHECK_IN:
                return getEntryTransactionCheckIn(user, time, values);
            case TRANSACTION_ACTION_CREATE_VERSION:
                if (isMovedFromOeFile(values)) {
                    //skip version if moved from oe:files (duplicates records)
                    return Optional.empty();
                }
                return getEntryTransactionUpdateVersion(user, time, values);
            case TRANSACTION_ACTION_UPDATE_NODE_PROPERTIES:
                if (isIgnoredAspectAddTransaction(values)) {
                    return Optional.empty();
                }
                return getEntryTransactionUpdateProperties(user, time, values);

            default:
                if (values.containsKey(TRANSACTION_SUB_ACTIONS)) {
                    String subActions = (String) values.get(TRANSACTION_SUB_ACTIONS);
                    if (subActions.contains(TRANSACTION_ACTION_UPDATE_NODE_PROPERTIES)) {
                        if (isIgnoredAspectAddTransaction(values)) {
                            return Optional.empty();
                        }
                        return getEntryTransactionUpdateProperties(user, time, values);
                    }
                }
        }
        return Optional.empty();
    }

    private boolean isIgnoredAspectAddTransaction(Map<String, Serializable> values) {
        if (values.containsKey(TRANSACTION_ASPECT_ADD)) {
            Set<QName> aspectsAdd = (Set<QName>) values.get(TRANSACTION_ASPECT_ADD);
            //is locked in alfresco but not in OpenE - then ignore.
            if (aspectsAdd.contains(ContentModel.ASPECT_LOCKABLE) && !aspectsAdd.contains(OpenESDHModel.ASPECT_OE_LOCKED)) {
                return true;
            }
            //if contains any other ignored aspects
            return CollectionUtils.containsAny(aspectsAdd, ignoredAspects);
        }
        return false;
    }

    private Optional<JSONObject> getEntryTransactionCreate(String user, long time, Map<String, Serializable> values) {
        String type = (String) values.get(TRANSACTION_TYPE);
        String path = (String) values.get(TRANSACTION_PATH);
        Set<QName> aspectsAdd = (Set<QName>) values.get(TRANSACTION_ASPECT_ADD);

        Map<QName, Serializable> properties = (Map<QName, Serializable>) values.get(TRANSACTION_PROPERTIES_ADD);
        JSONObject auditEntry = createNewAuditEntry(user, time);
        if (path.contains(OpenESDHModel.DOCUMENTS_FOLDER_NAME)) {
            // TODO: These checks should check for subtypes using
            // dictionaryService
            if (type.equals("cm:content")) {
                boolean isMainFile = aspectsAdd != null && aspectsAdd.contains(OpenESDHModel.ASPECT_DOC_IS_MAIN_FILE);
                if (!isMainFile) {
                    Optional<String> title = getLocalizedProperty(properties, ContentModel.PROP_TITLE);
                    if (!title.isPresent()) {
                        return Optional.empty();
                    }
                    auditEntry.put(ACTION, I18NUtil.getMessage("auditlog.label.attachment.added", title.get()));
                    auditEntry.put(TYPE, getTypeMessage(ATTACHMENT));
                } else {
                    return Optional.empty();
                    // Adding main doc, don't log an entry because you would
                    // get two entries when adding a document: one for the record
                    // and one for the main file
                }
            } else if (type.contains("doc:")) {
                Optional<String> title = getLocalizedProperty(properties, ContentModel.PROP_TITLE);
                if (!title.isPresent()) {
                    return Optional.empty();
                }
                auditEntry.put(ACTION, I18NUtil.getMessage("auditlog.label.document.added", title.get()));
                auditEntry.put(TYPE, getTypeMessage(DOCUMENT));
            } else {
                return Optional.empty();
            }
        } else {
            auditEntry.put(ACTION, I18NUtil.getMessage("auditlog.label.case.created", getLastPathElement(values)[1]));
            auditEntry.put(TYPE, getTypeMessage(CASE));
        }
        return Optional.of(auditEntry);
    }

    private Optional<JSONObject> getEntryTransactionDelete(String user, long time, Map<String, Serializable> values) {
        HashSet<String> aspects = (HashSet<String>) values.get("/esdh/transaction/aspects/delete");
        JSONObject auditEntry = createNewAuditEntry(user, time);
        String[] lastPathElement = getLastPathElement(values);
        if (aspects != null && aspects.contains(ContentModel.ASPECT_COPIEDFROM.toString())) {
            auditEntry.put(ACTION, I18NUtil.getMessage("auditlog.label.finished.editing", lastPathElement[1]));
            auditEntry.put(TYPE, getTypeMessage(SYSTEM));
        } else {
            switch (values.get(TRANSACTION_TYPE).toString()) {
                case "doc:digitalFile":
                    if (isContent(lastPathElement)) {
                        //delete action on content of document folder is not shown to prevent duplicate records
                        return Optional.empty();
                    }
                    auditEntry.put(TYPE, getTypeMessage(ATTACHMENT));
                    break;
                case "doc:simple":
                    auditEntry.put(TYPE, getTypeMessage(DOCUMENT));
                    break;
                case "cm:content":
                    //delete action on content of document folder is not shown to prevent duplicate records
                    return Optional.empty();
                default:
                    auditEntry.put(TYPE, getTypeMessage(SYSTEM));
            }
            auditEntry.put(ACTION, I18NUtil.getMessage("auditlog.label.deleted.document", lastPathElement[1]));
        }
        return Optional.of(auditEntry);
    }

    private boolean isContent(String[] lastPathElement) {
        return lastPathElement[0].equals("doc") && lastPathElement[1].startsWith("content_");
    }

    private Optional<JSONObject> getEntryTransactionCheckIn(String user, long time, Map<String, Serializable> values) {
        JSONObject auditEntry = createNewAuditEntry(user, time);
        String title = getTitle(values);
        String newVersion = (String) getFromPropertyMap(values, TRANSACTION_PROPERTIES_TO, ContentModel.PROP_VERSION_LABEL);
        auditEntry.put(ACTION, I18NUtil.getMessage("auditlog.label.checkedin", title, newVersion));
        auditEntry.put(TYPE, getTypeMessage(SYSTEM));
        return Optional.of(auditEntry);
    }

    private Optional<JSONObject> getEntryTransactionUpdateVersion(String user, long time, Map<String, Serializable> values) {
        String oldVersion = (String) getFromPropertyMap(
                values, TRANSACTION_PROPERTIES_FROM, ContentModel.PROP_VERSION_LABEL);
        String newVersion = (String) getFromPropertyMap(
                values, TRANSACTION_PROPERTIES_TO, ContentModel.PROP_VERSION_LABEL);
        JSONObject auditEntry = createNewAuditEntry(user, time);
        auditEntry.put(ACTION, I18NUtil.getMessage("auditlog.label.office.edit",
                getTitle(values),
                oldVersion,
                newVersion));
        auditEntry.put(TYPE, getTypeMessage(DOCUMENT));
        return Optional.of(auditEntry);
    }

    private Optional<JSONObject> getEntryTransactionUpdateProperties(String user, long time,
            Map<String, Serializable> values) {
        QName nodeType = (QName) values.get("/esdh/transaction/nodeType");
        REC_TYPE type;
        if (dictionaryService.isSubClass(nodeType, OpenESDHModel.TYPE_CASE_BASE)) {
            type = CASE;
        } else if (dictionaryService.isSubClass(nodeType, OpenESDHModel.TYPE_DOC_BASE)) {
            type = DOCUMENT;
        } else if (dictionaryService.isSubClass(nodeType, OpenESDHModel.TYPE_DOC_FILE)) {
            // TODO: Distinguish between main file and attachments
            type = ATTACHMENT;
        } else {
            return Optional.empty();
        }

        Map<QName, Serializable> toMap = (Map<QName, Serializable>) values.get(TRANSACTION_PROPERTIES_TO);
        Map<QName, Serializable> addMap = (Map<QName, Serializable>) values.get(TRANSACTION_PROPERTIES_ADD);
        if (addMap == null && toMap == null) {
            return Optional.empty();
        }
        toMap = filterUndesirableProps(toMap);
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
        if (changes.isEmpty()) {
            return Optional.empty();
        }

        Collections.sort(changes);

        String nodeTitle = "";
        //do not add case title in case history
        if (!StringUtils.endsWith((String) values.get("/esdh/transaction/type"), ":case")) {
            nodeTitle = getTitle(values);
            nodeTitle = StringUtils.isNotEmpty(nodeTitle) ? " (" + nodeTitle + ")" : "";
        }

        JSONObject auditEntry = createNewAuditEntry(user, time);
        auditEntry.put(TYPE, getTypeMessage(type));
        auditEntry.put(ACTION, I18NUtil.getMessage("auditlog.label.properties.updated", nodeTitle, StringUtils.join(changes, ";\n")));
        return Optional.of(auditEntry);
    }

    private Map<QName, Serializable> filterUndesirableProps(Map<QName, Serializable> map) {
        return map.entrySet()
                .stream()
                .filter(p -> !ignoredProperties.contains(p.getKey()))
                .collect(Collectors.toMap(p -> p.getKey(), p -> Optional.ofNullable(p.getValue()).orElse("")));
    }

    private Serializable getFromPropertyMap(Map<String, Serializable> values, String mapProperty, QName name) {
        return values.containsKey(mapProperty)
                ? ((Map<QName, Serializable>) values.get(mapProperty)).get(name)
                : null;
    }

    private Optional<String> getLocalizedProperty(Map<QName, Serializable> properties, QName propQName) {
        return getLocalizedPropertyValue(properties.get(propQName));
    }

    private Optional<String> getLocalizedPropertyValue(Serializable property) {
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

    private static boolean isMovedFromOeFile(Map<String, Serializable> values) {
        return values.containsKey(TRANSACTION_DOC_FROM_FILES)
                && StringUtils.startsWith((String) values.get(TRANSACTION_DOC_FROM_FILES), FILES_ROOT_PATH);
    }
}
