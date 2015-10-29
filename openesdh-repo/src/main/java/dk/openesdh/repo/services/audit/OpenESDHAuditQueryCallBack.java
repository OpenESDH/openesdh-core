package dk.openesdh.repo.services.audit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.BlogIntegrationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.model.ImapModel;
import org.alfresco.service.cmr.audit.AuditService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.JSONArray;
import org.springframework.extensions.surf.util.I18NUtil;

import dk.openesdh.repo.model.OpenESDHModel;

/**
 * Created by flemmingheidepedersen on 18/11/14.
 */
public class OpenESDHAuditQueryCallBack implements AuditService.AuditQueryCallback {

    private static final String MEMBER_ADD_PATH = "/esdh/security/addAuthority/args/parentName/value";
    private static final String MEMBER_ADD_CHILD = "/esdh/security/addAuthority/args/childName/value";
    private static final String MEMBER_REMOVE_PATH = "/esdh/security/removeAuthority/args/parentName/value";
    private static final String MEMBER_REMOVE_CHILD = "/esdh/security/removeAuthority/args/childName/value";
    private static final String TRANSACTION_PATH = "/esdh/transaction/user";
    private static final String TRANSACTION_ACTION = "/esdh/transaction/action";
    private static final String TRANSACTION_SUB_ACTIONS = "/esdh/transaction/sub-actions";
    private static final String PARTY_ADD_NAME = "/esdh/child/add/args/contactName";
    private static final String PARTY_ADD_GROUP_NAME = "/esdh/child/add/args/groupName";
    private static final String PARTY_REMOVE_NAME = "/esdh/child/remove/args/contactName";
    private static final String PARTY_REMOVE_GROUP_NAME = "/esdh/child/remove/args/groupName";
    private static final String WORKFLOW_START_CASE = "/esdh/workflow/start/case";
    private static final String WORKFLOW_START_DESCRIPTION = "/esdh/workflow/start/description";
    private static final String WORKFLOW_END_TASK_CASE = "/esdh/workflow/endTask/case";
    private static final String WORKFLOW_END_TASK_DESCRIPTION = "/esdh/workflow/endTask/description";
    private static final String WORKFLOW_END_TASK_REVIEW_OUTCOME = "/esdh/workflow/endTask/reviewOutcome";

    private static final String WORKFLOW_CANCEL_CASE = "/esdh/workflow/cancelWorkflow/case";
    private static final String WORKFLOW_CANCEL_DESCRIPTION = "/esdh/workflow/cancelWorkflow/description";

    private static final int MAX_NOTE_TEXT_LENGTH = 40;

    private static final List<QName> undesiredProps = Arrays.asList(
            ContentModel.PROP_DEAD_PROPERTIES,
            ContentModel.PROP_NODE_REF,
            ContentModel.PROP_MODIFIED,
            ContentModel.PROP_VERSION_LABEL,
            ForumModel.PROP_COMMENT_COUNT,
            ImapModel.PROP_CHANGE_TOKEN, ImapModel.PROP_UIDVALIDITY,
            ImapModel.PROP_MAXUID, BlogIntegrationModel.PROP_LINK);

    private JSONArray result = new JSONArray();

    private DictionaryService dictionaryService;

    public OpenESDHAuditQueryCallBack(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    public JSONArray getResult() {
        return result;
    }

    @Override
    public boolean valuesRequired() {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean handleAuditEntry(Long entryId, String applicationName, String user, long time, Map<String, Serializable> values) {

        getAuditEntryHandler(values.keySet())
            .flatMap(handler -> handler.createAuditEntry(user, time, values))
            .ifPresent(auditEntry -> result.add(auditEntry));
        return true;
    }

    private Optional<AuditEntryHandler> getAuditEntryHandler(Set<String> auditValuesEntryKeys) {
        return getAuditEntryHandlers().entrySet()
                .stream()
               .filter(handler -> auditValuesEntryKeys.contains(handler.getKey()))
               .findAny()
               .map(handler -> handler.getValue());
    }

    private Map<String, AuditEntryHandler> getAuditEntryHandlers() {
        Map<String, AuditEntryHandler> handlers = new HashMap<String, AuditEntryHandler>();
        handlers.put(PARTY_REMOVE_NAME, this::getEntryPartyRemove);
        handlers.put(PARTY_ADD_NAME, this::getEntryPartyAdd);
        handlers.put(MEMBER_ADD_PATH, this::getEntryMemberAdd);
        handlers.put(MEMBER_REMOVE_PATH, this::getEntryMemberRemove);
        handlers.put(TRANSACTION_PATH, this::getEntryTransactionPath);
        handlers.put(WORKFLOW_START_CASE, this::getEntryWorkflowStart);
        handlers.put(WORKFLOW_END_TASK_CASE, this::getEntryWorkflowTaskEnd);
        handlers.put(WORKFLOW_CANCEL_CASE, this::getEntryWorkflowCancel);

        return handlers;
    }

    private Optional<JSONObject> getEntryTransactionPath(String user, long time, Map<String, Serializable> values) throws JSONException{
        
        switch ((String) values.get(TRANSACTION_ACTION)) {
        case "CREATE":
            return getEntryTransactionCreate(user, time, values);
        case "DELETE":
            return getEntryTransactionDelete(user, time, values);
        case "CHECK IN":
            return getEntryTransactionCheckIn(user, time, values);
        case "updateNodeProperties":
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

    @SuppressWarnings("unchecked")
    private Optional<JSONObject> getEntryTransactionUpdateProperties(String user, long time,
            Map<String, Serializable> values) throws JSONException {
        QName nodeType = (QName) values.get("/esdh/transaction/nodeType");
        String type;
        if (dictionaryService.isSubClass(nodeType, OpenESDHModel.TYPE_CASE_BASE)) {
            type = "case";
        } else if (dictionaryService.isSubClass(nodeType, OpenESDHModel.TYPE_DOC_BASE)) {
            type = "document";
        } else if (dictionaryService.isSubClass(nodeType, OpenESDHModel.TYPE_DOC_FILE)) {
            // TODO: Distinguish between main file and attachments
            type = "attachment";
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
            changes.add(I18NUtil.getMessage("auditlog.label.property.update",
                    getPropertyTitle(qName), value.toString(), finalToMap
                            .get(qName).toString()));
        });
        if (changes.isEmpty()) {
            return Optional.empty();
        }
        JSONObject auditEntry = createNewAuditEntry(user, time);
        auditEntry.put("type", getTypeMessage(type));
        auditEntry.put("action",
                I18NUtil.getMessage("auditlog.label.properties.updated", StringUtils.join(changes, ";\n")));
        return Optional.of(auditEntry);
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
                .filter(p -> !undesiredProps.contains(p.getKey()))
                .collect(Collectors.toMap(p -> p.getKey(), p -> Optional.ofNullable(p.getValue()).orElse("")));
    }

    @SuppressWarnings("unchecked")
    private Optional<JSONObject> getEntryTransactionCreate(String user, long time, Map<String, Serializable> values)
            throws JSONException {
        String type = (String) values.get("/esdh/transaction/type");
        String path = (String) values.get("/esdh/transaction/path");
        String[] pArray = path.split("/");
        Set<QName> aspectsAdd = (Set<QName>) values.get("/esdh/transaction/aspects/add");

        Map<QName, Serializable> properties = (Map<QName, Serializable>) values
                .get("/esdh/transaction/properties/add");
        JSONObject auditEntry = createNewAuditEntry(user, time);
        if (path.contains(OpenESDHModel.DOCUMENTS_FOLDER_NAME)) {
            QName name = ContentModel.PROP_NAME;
            // TODO: These checks should check for subtypes using
            // dictionaryService
            if (type.equals("cm:content")) {
                boolean isMainFile = aspectsAdd != null && aspectsAdd.contains(OpenESDHModel.ASPECT_DOC_IS_MAIN_FILE);
                if (!isMainFile) {
                    auditEntry.put("action", I18NUtil.getMessage("auditlog.label.attachment.added") + " " + properties.get(name));
                    auditEntry.put("type", getTypeMessage("attachment"));
                } else {
                    return Optional.empty();
                    // Adding main doc, don't log an entry because you would
                    // get two entries when adding a document: one for the record
                    // and one for the main file
                }
            } else if (type.contains("doc:")) {
                auditEntry.put("action", I18NUtil.getMessage("auditlog.label.document.added") + " " + properties.get(name));
                auditEntry.put("type", getTypeMessage("document"));
            } else {
                return Optional.empty();
            }
        } else if (type.startsWith("note:")) {
            String trimmedNote = StringUtils.abbreviate((String) properties.get(OpenESDHModel.PROP_NOTE_CONTENT), MAX_NOTE_TEXT_LENGTH);
            auditEntry.put("action", I18NUtil.getMessage("auditlog.label.note.added") + " " + trimmedNote);
            auditEntry.put("type", getTypeMessage("note"));
        } else {
            auditEntry.put("action", I18NUtil.getMessage("auditlog.label.case.created") + " " + pArray[pArray.length - 1].split(":")[1]);
            auditEntry.put("type", getTypeMessage("case"));
        }
        return Optional.of(auditEntry);
    }

    @SuppressWarnings("unchecked")
    private Optional<JSONObject> getEntryTransactionDelete(String user, long time, Map<String, Serializable> values)
            throws JSONException {
        HashSet<String> aspects = (HashSet<String>) values.get("/esdh/transaction/aspects/delete");
        String path = (String) values.get("/esdh/transaction/path");
        String[] pArray = path.split("/");
        JSONObject auditEntry = createNewAuditEntry(user, time);
        if (aspects != null && aspects.contains(ContentModel.ASPECT_COPIEDFROM.toString())) {
            auditEntry.put("action", I18NUtil.getMessage("auditlog.label.finished.editing") + " " + pArray[pArray.length - 1].split(":")[1]);
            auditEntry.put("type", getTypeMessage("system"));
        } else {
            auditEntry.put("action", I18NUtil.getMessage("auditlog.label.deleted.document") + " " + pArray[pArray.length - 1].split(":")[1]);
            auditEntry.put("type", getTypeMessage("system"));
        }
        return Optional.of(auditEntry);
    }

    private Optional<JSONObject> getEntryTransactionCheckIn(String user, long time, Map<String, Serializable> values)
            throws JSONException {
        String path = (String) values.get("/esdh/transaction/path");
        String[] pArray = path.split("/");
        JSONObject auditEntry = createNewAuditEntry(user, time);
        auditEntry.put("action", I18NUtil.getMessage("auditlog.label.checkedin") + " " + pArray[pArray.length - 1].split(":")[1]);
        auditEntry.put("type", getTypeMessage("system"));
        return Optional.of(auditEntry);
    }

    private Optional<JSONObject> getEntryMemberAdd(String user, long time, Map<String, Serializable> values)
            throws JSONException {
        String parent = (String) values.get(MEMBER_ADD_PATH);
        String role = getRoleFromCaseGroupName(parent);
        if (role == null) {
            return Optional.empty();
        }
        String authority = (String) values.get(MEMBER_ADD_CHILD);
        JSONObject auditEntry = createNewAuditEntry(user, time);
        auditEntry.put("action", I18NUtil.getMessage("auditlog.label.member.added", authority, role));
        auditEntry.put("type", getTypeMessage("member"));
        return Optional.of(auditEntry);
    }

    private Optional<JSONObject> getEntryMemberRemove(String user, long time, Map<String, Serializable> values)
            throws JSONException {
        String parent = (String) values.get(MEMBER_REMOVE_PATH);
        String role = getRoleFromCaseGroupName(parent);
        if (role == null) {
            return Optional.empty();
        }
        String authority = (String) values.get(MEMBER_REMOVE_CHILD);
        JSONObject auditEntry = createNewAuditEntry(user, time);
        auditEntry.put("action", I18NUtil.getMessage("auditlog.label.member.removed", authority, role));
        auditEntry.put("type", getTypeMessage("member"));
        return Optional.of(auditEntry);
    }

    private Optional<JSONObject> getEntryPartyAdd(String user, long time, Map<String, Serializable> values)
            throws JSONException {
        String contactName = (String) values.get(PARTY_ADD_NAME);
        if (StringUtils.isEmpty(contactName)) {
            return Optional.empty();
        }
        JSONObject auditEntry = createNewAuditEntry(user, time);
        String groupName = (String) values.get(PARTY_ADD_GROUP_NAME);
        auditEntry.put("action", I18NUtil.getMessage("auditlog.label.party.added", contactName, groupName));
        auditEntry.put("type", getTypeMessage("party"));
        return Optional.of(auditEntry);
    }

    private Optional<JSONObject> getEntryPartyRemove(String user, long time, Map<String, Serializable> values)
            throws JSONException {
        String contactName = (String) values.get(PARTY_REMOVE_NAME);
        if (StringUtils.isEmpty(contactName)) {
            return Optional.empty();
        }
        JSONObject auditEntry = createNewAuditEntry(user, time);
        String groupName = (String) values.get(PARTY_REMOVE_GROUP_NAME);
        auditEntry.put("action", I18NUtil.getMessage("auditlog.label.party.removed", contactName, groupName));
        auditEntry.put("type", getTypeMessage("party"));
        return Optional.of(auditEntry);
    }

    private Optional<JSONObject> getEntryWorkflowStart(String user, long time, Map<String, Serializable> values)
            throws JSONException {
        JSONObject auditEntry = createNewAuditEntry(user, time);
        putAuditEntryType(auditEntry, "workflow");
        auditEntry.put("action",
                I18NUtil.getMessage("auditlog.label.workflow.started", values.get(WORKFLOW_START_DESCRIPTION)));
        return Optional.of(auditEntry);
    }

    private Optional<JSONObject> getEntryWorkflowTaskEnd(String user, long time, Map<String, Serializable> values)
            throws JSONException {
        JSONObject auditEntry = createNewAuditEntry(user, time);
        putAuditEntryType(auditEntry, "workflow");

        String taskOutcome = Optional.ofNullable(values.get(WORKFLOW_END_TASK_REVIEW_OUTCOME)).orElse("ended")
                .toString();

        auditEntry.put("action",
                I18NUtil.getMessage("auditlog.label.workflow.task." + taskOutcome,
                        values.get(WORKFLOW_END_TASK_DESCRIPTION)));
        return Optional.of(auditEntry);
    }

    private Optional<JSONObject> getEntryWorkflowCancel(String user, long time, Map<String, Serializable> values)
            throws JSONException {
        JSONObject auditEntry = createNewAuditEntry(user, time);
        putAuditEntryType(auditEntry, "workflow");
        auditEntry.put("action",
                I18NUtil.getMessage("auditlog.label.workflow.canceled", values.get(WORKFLOW_CANCEL_DESCRIPTION)));
        return Optional.of(auditEntry);
    }

    private JSONObject createNewAuditEntry(String user, long time) throws JSONException {
        JSONObject auditEntry = new JSONObject();
        auditEntry.put("user", user);
        auditEntry.put("time", time);
        return auditEntry;
    }

    private void putAuditEntryType(JSONObject auditEntry, String type) throws JSONException {
        auditEntry.put("type", getTypeMessage(type));
    }

    private String getTypeMessage(String type) {
        return I18NUtil.getMessage("auditlog.label.type." + type);
    }

    /**
     * Return the role given a case group name. Returns null if the group name does not belong to a case.
     *
     * @param groupName
     * @return
     */
    private String getRoleFromCaseGroupName(String groupName) {
        Pattern pattern = Pattern.compile("GROUP_case_([\\d\\-]+)_(.+)");
        Matcher matcher = pattern.matcher(groupName);
        if (matcher.matches()) {
            return matcher.group(2);
        } else {
            return null;
        }
    }

    @Override
    public boolean handleAuditEntryError(Long entryId, String errorMsg, Throwable error) {
        throw new AlfrescoRuntimeException(errorMsg, error);
    }

    interface AuditEntryHandler {

        Optional<JSONObject> handleEntry(String user, long time, Map<String, Serializable> values)
                throws JSONException;

        default Optional<JSONObject> createAuditEntry(String user, long time, Map<String, Serializable> values) {
            try {
                return handleEntry(user, time, values);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return Optional.empty();
        }
    }
}
