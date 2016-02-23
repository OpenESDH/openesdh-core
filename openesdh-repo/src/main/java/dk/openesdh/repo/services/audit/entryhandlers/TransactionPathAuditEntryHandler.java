package dk.openesdh.repo.services.audit.entryhandlers;

import static dk.openesdh.repo.services.audit.AuditEntryHandler.REC_TYPE.ATTACHMENT;
import static dk.openesdh.repo.services.audit.AuditEntryHandler.REC_TYPE.CASE;
import static dk.openesdh.repo.services.audit.AuditEntryHandler.REC_TYPE.DOCUMENT;
import static dk.openesdh.repo.services.audit.AuditEntryHandler.REC_TYPE.NOTE;
import static dk.openesdh.repo.services.audit.AuditEntryHandler.REC_TYPE.SYSTEM;
import static dk.openesdh.repo.services.audit.AuditUtils.getLastPathElement;
import static dk.openesdh.repo.services.audit.AuditUtils.getTitle;
import static dk.openesdh.repo.services.system.OpenESDHFoldersService.FILES_ROOT_PATH;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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

import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.audit.AuditEntryHandler;
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

    private static final int MAX_NOTE_TEXT_LENGTH = 40;

    private final DictionaryService dictionaryService;
    private final List<QName> ignoredProperties;
    private Map<Predicate<Map<String, Serializable>>, IAuditEntryHandler> trPathEntryHandlers = new HashMap<>();

    public TransactionPathAuditEntryHandler(DictionaryService dictionaryService, List<QName> ignoredProperties,
            Map<Predicate<Map<String, Serializable>>, IAuditEntryHandler> trPathEntryHandlers) {
        this.dictionaryService = dictionaryService;
        this.ignoredProperties = ignoredProperties;
        this.trPathEntryHandlers = trPathEntryHandlers;
    }

    public void addTransactionPathEntryHandler(Predicate<Map<String, Serializable>> predicate,
            IAuditEntryHandler handler) {
        this.trPathEntryHandlers.put(predicate, handler);
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
                return getEntryTransactionUpdateProperties(user, time, values);

            default:
                if (values.containsKey(TRANSACTION_SUB_ACTIONS)) {
                    String subActions = (String) values.get(TRANSACTION_SUB_ACTIONS);
                    if (subActions.contains("updateNodeProperties")) {
                        return getEntryTransactionUpdateProperties(user, time, values);
                    }
                }
        }
        return Optional.empty();
    }

    private Optional<JSONObject> getEntryTransactionCreate(String user, long time, Map<String, Serializable> values) {
        String type = (String) values.get(TRANSACTION_TYPE);
        String path = (String) values.get(TRANSACTION_PATH);
        Set<QName> aspectsAdd = (Set<QName>) values.get(TRANSACTION_ASPECT_ADD);

        Map<QName, Serializable> properties = (Map<QName, Serializable>) values
                .get("/esdh/transaction/properties/add");
        JSONObject auditEntry = createNewAuditEntry(user, time);
        if (path.contains(OpenESDHModel.DOCUMENTS_FOLDER_NAME)) {
            // TODO: These checks should check for subtypes using
            // dictionaryService
            if (type.equals("cm:content")) {
                boolean isMainFile = aspectsAdd != null && aspectsAdd.contains(OpenESDHModel.ASPECT_DOC_IS_MAIN_FILE);
                if (!isMainFile) {
                    Optional<String> title = localizedProperty(properties, ContentModel.PROP_TITLE);
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
                Optional<String> title = localizedProperty(properties, ContentModel.PROP_TITLE);
                if (!title.isPresent()) {
                    return Optional.empty();
                }
                auditEntry.put(ACTION, I18NUtil.getMessage("auditlog.label.document.added", title.get()));
                auditEntry.put(TYPE, getTypeMessage(DOCUMENT));
            } else {
                return Optional.empty();
            }
        } else if (type.startsWith("note:")) {
            String trimmedNote = StringUtils.abbreviate((String) properties.get(OpenESDHModel.PROP_NOTE_CONTENT), MAX_NOTE_TEXT_LENGTH);
            auditEntry.put(ACTION, I18NUtil.getMessage("auditlog.label.note.added", trimmedNote));
            auditEntry.put(TYPE, getTypeMessage(NOTE));
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
        String newVersion = (String) getFromPropertyMap(values, "/esdh/transaction/properties/to", ContentModel.PROP_VERSION_LABEL);
        auditEntry.put(ACTION, I18NUtil.getMessage("auditlog.label.checkedin", title, newVersion));
        auditEntry.put(TYPE, getTypeMessage(SYSTEM));
        return Optional.of(auditEntry);
    }

    private Optional<JSONObject> getEntryTransactionUpdateVersion(String user, long time, Map<String, Serializable> values) {
        String oldVersion = (String) getFromPropertyMap(
                values, "/esdh/transaction/properties/from", ContentModel.PROP_VERSION_LABEL);
        String newVersion = (String) getFromPropertyMap(
                values, "/esdh/transaction/properties/to", ContentModel.PROP_VERSION_LABEL);
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

        Map<QName, Serializable> fromMap = (Map<QName, Serializable>) values.get("/esdh/transaction/properties/from");
        Map<QName, Serializable> toMap = (Map<QName, Serializable>) values.get("/esdh/transaction/properties/to");
        if (fromMap == null || toMap == null) {
            return Optional.empty();
        }
        fromMap = filterUndesirableProps(fromMap);
        toMap = filterUndesirableProps(toMap);
        List<String> changes = new ArrayList<>();
        final Map<QName, Serializable> finalToMap = toMap;
        fromMap.forEach((qName, value) -> {
            String nodeTitle = getTitle(values);
            if (StringUtils.isEmpty(nodeTitle)
                    //do not add case title in case history
                    || StringUtils.endsWith((String) values.get("/esdh/transaction/type"), ":case")) {
                nodeTitle = "";
            } else {
                nodeTitle = " (" + nodeTitle + ")";
            }
            changes.add(I18NUtil.getMessage("auditlog.label.property.update",
                    getPropertyTitle(qName),
                    value.toString(),
                    finalToMap.get(qName).toString()) + nodeTitle);
        });
        if (changes.isEmpty()) {
            return Optional.empty();
        }

        JSONObject auditEntry = createNewAuditEntry(user, time);
        auditEntry.put(TYPE, getTypeMessage(type));
        auditEntry.put(ACTION,
                I18NUtil.getMessage("auditlog.label.properties.updated", StringUtils.join(changes, ";\n")));
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

    private Optional<String> localizedProperty(Map<QName, Serializable> properties, QName propQName) {
        HashMap<Locale, String> property = (HashMap) properties.get(propQName);
        if (property == null) {
            return Optional.empty();
        }
        String value = property.get(I18NUtil.getContentLocale());
        if (value == null) {
            value = property.get(Locale.ENGLISH);
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
