package dk.openesdh.repo.services.audit.entryhandlers;

import static dk.openesdh.repo.services.audit.AuditEntryHandler.REC_TYPE.ATTACHMENT;
import static dk.openesdh.repo.services.audit.AuditEntryHandler.REC_TYPE.CASE;
import static dk.openesdh.repo.services.audit.AuditEntryHandler.REC_TYPE.DOCUMENT;
import static dk.openesdh.repo.services.audit.AuditEntryHandler.REC_TYPE.SYSTEM;
import static dk.openesdh.repo.services.audit.AuditUtils.getDocumentPath;
import static dk.openesdh.repo.services.audit.AuditUtils.getLastPathElement;
import static dk.openesdh.repo.services.audit.AuditUtils.getLocalizedProperty;
import static dk.openesdh.repo.services.audit.AuditUtils.getTitle;
import static dk.openesdh.repo.services.system.OpenESDHFoldersService.FILES_ROOT_PATH;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.audit.AuditEntry;
import dk.openesdh.repo.services.audit.AuditEntryHandler;
import dk.openesdh.repo.services.audit.IAuditEntryHandler;

public class TransactionPathAuditEntryHandler extends AuditEntryHandler {

    public static final String TRANSACTION_USER = "/esdh/transaction/user";
    public static final String TRANSACTION_PATH = "/esdh/transaction/path";
    public static final String TRANSACTION_TYPE = "/esdh/transaction/type";
    public static final String TRANSACTION_ACTION = "/esdh/transaction/action";
    public static final String TRANSACTION_SUB_ACTIONS = "/esdh/transaction/sub-actions";
    public static final String TRANSACTION_ASPECT_ADD = "/esdh/transaction/aspects/add";
    private static final String TRANSACTION_DOC_FROM_FILES = "/esdh/transaction/move/from/path";

    public static final String TRANSACTION_ACTION_CREATE = "CREATE";
    public static final String TRANSACTION_ACTION_DELETE = "DELETE";
    public static final String TRANSACTION_ACTION_CHECK_IN = "CHECK IN";
    public static final String TRANSACTION_ACTION_CREATE_VERSION = "CREATE VERSION";
    public static final String TRANSACTION_ACTION_UPDATE_NODE_PROPERTIES = "updateNodeProperties";
    public static final String TRANSACTION_ACTION_COPY = "COPY";

    public static final String TRANSACTION_PROPERTIES_ADD = "/esdh/transaction/properties/add";
    public static final String TRANSACTION_PROPERTIES_FROM = "/esdh/transaction/properties/from";
    public static final String TRANSACTION_PROPERTIES_TO = "/esdh/transaction/properties/to";
    public static final String TRANSACTION_PROPERTIES_TITLE = "/esdh/transaction/properties/title";

    private final DictionaryService dictionaryService;
    private final Set<QName> ignoredAspects;
    private final NodePropertyChangesAuditEntrySubHandler nodePropertyChangesHandler;
    private Map<Predicate<Map<String, Serializable>>, IAuditEntryHandler> trPathEntryHandlers = new HashMap<>();

    public TransactionPathAuditEntryHandler(DictionaryService dictionaryService,
            NodePropertyChangesAuditEntrySubHandler nodePropertyChangesHandler,
            Set<QName> ignoredAspects,
            Map<Predicate<Map<String, Serializable>>, IAuditEntryHandler> trPathEntryHandlers) {
        this.dictionaryService = dictionaryService;
        this.nodePropertyChangesHandler = nodePropertyChangesHandler;
        this.ignoredAspects = ignoredAspects;
        this.trPathEntryHandlers = trPathEntryHandlers;
    }

    @Override
    public Optional<AuditEntry> handleEntry(String user, long time, Map<String, Serializable> values) {
        return trPathEntryHandlers.entrySet()
                .stream()
                .filter(entry -> entry.getKey().test(values))
                .findAny()
                .map(entry -> entry.getValue())
                .orElse(this::defaultHandleEntry)
                .handleEntry(user, time, values);
    }

    private Optional<AuditEntry> defaultHandleEntry(String user, long time, Map<String, Serializable> values) {
        String transactionAction = (String) values.get(TRANSACTION_ACTION);
        switch (transactionAction) {
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
                    if (subActions.contains(TRANSACTION_ACTION_UPDATE_NODE_PROPERTIES)
                            && !transactionAction.equals(TRANSACTION_ACTION_COPY)) {
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

    private Optional<AuditEntry> getEntryTransactionCreate(String user, long time, Map<String, Serializable> values) {
        String type = (String) values.get(TRANSACTION_TYPE);
        String path = (String) values.get(TRANSACTION_PATH);
        Set<QName> aspectsAdd = (Set<QName>) values.get(TRANSACTION_ASPECT_ADD);

        Map<QName, Serializable> properties = (Map<QName, Serializable>) values.get(TRANSACTION_PROPERTIES_ADD);
        AuditEntry auditEntry = new AuditEntry(user, time);
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
                    auditEntry.setAction("auditlog.label.attachment.added");
                    auditEntry.setType(ATTACHMENT);
                    auditEntry.addData("title", title.get());
                    auditEntry.addData("path", getDocumentPath(values));
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
                auditEntry.setAction("auditlog.label.document.added");
                auditEntry.setType(DOCUMENT);
                auditEntry.addData("title", title.get());
                auditEntry.addData("path", getDocumentPath(values));
            } else {
                return Optional.empty();
            }
        } else {
            auditEntry.setAction("auditlog.label.case.created");
            auditEntry.setType(CASE);
            auditEntry.addData("title", getLastPathElement(values)[1]);
        }
        return Optional.of(auditEntry);
    }

    private Optional<AuditEntry> getEntryTransactionDelete(String user, long time, Map<String, Serializable> values) {
        HashSet<String> aspects = (HashSet<String>) values.get("/esdh/transaction/aspects/delete");
        AuditEntry auditEntry = new AuditEntry(user, time);
        String[] lastPathElement = getLastPathElement(values);
        if (aspects != null && aspects.contains(ContentModel.ASPECT_COPIEDFROM.toString())) {
            auditEntry.setAction("auditlog.label.document.editing_finished");
            auditEntry.setType(SYSTEM);
            auditEntry.addData("title", getDocumentPath(values) + lastPathElement[1]);
        } else {
            switch (values.get(TRANSACTION_TYPE).toString()) {
                case "doc:digitalFile":
                    if (isContent(lastPathElement)) {
                        //delete action on content of document folder is not shown to prevent duplicate records
                        return Optional.empty();
                    }
                    auditEntry.setType(ATTACHMENT);
                    break;
                case "doc:simple":
                    auditEntry.setType(DOCUMENT);
                    break;
                case "cm:content":
                    //delete action on content of document folder is not shown to prevent duplicate records
                    return Optional.empty();
                default:
                    auditEntry.setType(SYSTEM);
            }
            auditEntry.setAction("auditlog.label.document.deleted");
            auditEntry.addData("title", getDocumentPath(values) + lastPathElement[1]);
        }
        return Optional.of(auditEntry);
    }

    private boolean isContent(String[] lastPathElement) {
        return lastPathElement[0].equals("doc") && lastPathElement[1].startsWith("content_");
    }

    private Optional<AuditEntry> getEntryTransactionCheckIn(String user, long time, Map<String, Serializable> values) {
        AuditEntry auditEntry = new AuditEntry(user, time);
        auditEntry.setAction("auditlog.label.checkedin");

        String[] lastPathElement = getLastPathElement(values);
        if (isContent(lastPathElement)) {
            auditEntry.setType(DOCUMENT);
        } else if (lastPathElement[0].equals("cm")) {
            auditEntry.setType(ATTACHMENT);
        } else {
            auditEntry.setType(SYSTEM);
        }

        String newVersion = (String) getFromPropertyMap(values, TRANSACTION_PROPERTIES_TO, ContentModel.PROP_VERSION_LABEL);
        auditEntry.addData("title", getTitle(values));
        auditEntry.addData("path", getDocumentPath(values));
        auditEntry.addData("newVersion", newVersion);
        return Optional.of(auditEntry);
    }

    private Optional<AuditEntry> getEntryTransactionUpdateVersion(String user, long time, Map<String, Serializable> values) {
        AuditEntry auditEntry = new AuditEntry(user, time);
        auditEntry.setAction("auditlog.label.office.edit");
        auditEntry.setType(DOCUMENT);

        String oldVersion = (String) getFromPropertyMap(values, TRANSACTION_PROPERTIES_FROM, ContentModel.PROP_VERSION_LABEL);
        String newVersion = (String) getFromPropertyMap(values, TRANSACTION_PROPERTIES_TO, ContentModel.PROP_VERSION_LABEL);
        auditEntry.addData("title", getTitle(values));
        auditEntry.addData("path", getDocumentPath(values));
        auditEntry.addData("oldVersion", oldVersion);
        auditEntry.addData("newVersion", newVersion);
        return Optional.of(auditEntry);
    }

    private Optional<AuditEntry> getEntryTransactionUpdateProperties(String user, long time,
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

        List<String> changes = nodePropertyChangesHandler.getChangedProperties(values);
        if (changes.isEmpty()) {
            return Optional.empty();
        }

        String nodeTitle = "";
        //do not add case title in case history
        if (!StringUtils.endsWith((String) values.get("/esdh/transaction/type"), ":case")) {
            nodeTitle = getTitle(values);
            nodeTitle = StringUtils.isNotEmpty(nodeTitle) ? " (" + nodeTitle + ")" : "";
        }

        AuditEntry auditEntry = new AuditEntry(user, time);
        auditEntry.setAction("auditlog.label.properties.updated");
        auditEntry.setType(type);

        auditEntry.addData("title", nodeTitle);
        auditEntry.addData("props", changes);
        return Optional.of(auditEntry);
    }

    private Serializable getFromPropertyMap(Map<String, Serializable> values, String mapProperty, QName name) {
        return values.containsKey(mapProperty)
                ? ((Map<QName, Serializable>) values.get(mapProperty)).get(name)
                : null;
    }

    private static boolean isMovedFromOeFile(Map<String, Serializable> values) {
        return values.containsKey(TRANSACTION_DOC_FROM_FILES)
                && StringUtils.startsWith((String) values.get(TRANSACTION_DOC_FROM_FILES), FILES_ROOT_PATH);
    }
}
