package dk.openesdh.repo.services.audit;

import dk.openesdh.repo.model.OpenESDHModel;

import java.io.Serializable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.audit.AuditService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.JSONArray;
import org.springframework.extensions.surf.util.I18NUtil;

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
    private static final String PARTY_ADD_NAME = "/esdh/child/add/args/contactName";
    private static final String PARTY_ADD_GROUP_NAME = "/esdh/child/add/args/groupName";
    private static final String PARTY_REMOVE_NAME = "/esdh/child/remove/args/contactName";
    private static final String PARTY_REMOVE_GROUP_NAME = "/esdh/child/remove/args/groupName";

    private static final int MAX_NOTE_TEXT_LENGTH = 40;

    private static final List<String> undesiredProps = Arrays.asList
            ("deadproperties", "noderef", "modified", "commentcount",
                    "changetoken", "uidvalidity", "maxuid", "link");

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

    @Override
    public boolean handleAuditEntry(Long entryId, String applicationName, String user, long time, Map<String, Serializable> values) {
        try {
            JSONObject auditEntry = new JSONObject();
            auditEntry.put("user", user);
            auditEntry.put("time", time);

            // Added member to role
            if (values.containsKey(MEMBER_ADD_PATH)) {
                addEntryMemberAdd(values, auditEntry);
                return true;
            }

            // Removed member from role
            if (values.containsKey(MEMBER_REMOVE_PATH)) {
                addEntryMemberRemove(values, auditEntry);
                return true;
            }

            // file/folder CRUD transaction
            if (values.containsKey(TRANSACTION_PATH)) {
                switch ((String) values.get(TRANSACTION_ACTION)) {
                    case "CREATE":
                        addEntryTransactionCreate(values, auditEntry);
                        break;
                    case "DELETE":
                        addEntryTransactionDelete(values, auditEntry);
                        break;
                    case "CHECK IN":
                        addEntryTransactionCheckIn(values, auditEntry);
                        break;
                    case "updateNodeProperties":
                        addEntryTransactionUpdateProperties(values, auditEntry);
                }
                return true;
            }

            // Added party
            if (values.containsKey(PARTY_ADD_NAME)) {
                addEntryPartyAdd(values, auditEntry);
                return true;
            }

            // Removed party
            if (values.containsKey(PARTY_REMOVE_NAME)) {
                addEntryPartyRemove(values, auditEntry);
                return true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return true;
    }

    private void addEntryTransactionUpdateProperties(Map<String, Serializable> values, JSONObject auditEntry) throws JSONException {
        QName className = (QName) values.get("/esdh/transaction/type");
        String type;
        if (dictionaryService.isSubClass(className, OpenESDHModel.TYPE_CASE_BASE)) {
            type = "case";
        } else if (dictionaryService.isSubClass(className, OpenESDHModel.TYPE_DOC_BASE)) {
            type = "document";
        } else if (dictionaryService.isSubClass(className, OpenESDHModel.TYPE_DOC_FILE)) {
            // TODO: Distinguish between main file and attachments
            type = "attachment";
        } else {
            return;
        }
        auditEntry.put("type", type);
        Map<QName, Serializable> fromMap = filterUndesirableProps((Map<QName, Serializable>) values.get("/esdh/transaction/properties/from"));
        Map<QName, Serializable> toMap = filterUndesirableProps((Map<QName, Serializable>) values.get("/esdh/transaction/properties/to"));
        List<String> changes = new ArrayList<>();
        fromMap.forEach((qName, value) -> {
            changes.add(I18NUtil.getMessage("auditlog.label.property.update",
                    getPropertyTitle(qName), value.toString(), toMap.get(qName).toString()));
        });
        auditEntry.put("action", I18NUtil.getMessage("auditlog.label.properties.updated", StringUtils.join(changes, "\n")));
        result.add(auditEntry);
    }

    private String getPropertyTitle(QName qName) {
        PropertyDefinition property = dictionaryService.getProperty(qName);
        if (property == null) {
            return qName.getLocalName();
        } else {
            return property.getTitle(dictionaryService);
        }
    }


    private Map<QName, Serializable> filterUndesirableProps(Map<QName, Serializable> map) {
        return map.entrySet().stream().filter(
                p -> !undesiredProps.contains(p.getKey().getLocalName().toLowerCase()))
                .collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
    }

    private void addEntryMemberAdd(Map<String, Serializable> values, JSONObject auditEntry) throws JSONException {
        String parent = (String) values.get(MEMBER_ADD_PATH);
        String role = getRoleFromCaseGroupName(parent);
        if (role != null) {
            String authority = (String) values.get(MEMBER_ADD_CHILD);
            auditEntry.put("action", I18NUtil.getMessage("auditlog.label.member.added", authority, role));
            auditEntry.put("type", getTypeMessage("member"));
            result.add(auditEntry);
        }
    }

    private void addEntryMemberRemove(Map<String, Serializable> values, JSONObject auditEntry) throws JSONException {
        String parent = (String) values.get(MEMBER_REMOVE_PATH);
        String role = getRoleFromCaseGroupName(parent);
        if (role != null) {
            String authority = (String) values.get(MEMBER_REMOVE_CHILD);
            auditEntry.put("action", I18NUtil.getMessage("auditlog.label.member.removed", authority, role));
            auditEntry.put("type", getTypeMessage("member"));
            result.add(auditEntry);
        }
    }

    private void addEntryTransactionCreate(Map<String, Serializable> values, JSONObject auditEntry) throws JSONException {
        String type = (String) values.get("/esdh/transaction/type");
        String path = (String) values.get("/esdh/transaction/path");
        String[] pArray = path.split("/");

        Map properties = (Map) values.get("/esdh/transaction/properties/add");

        if (path.contains(OpenESDHModel.DOCUMENTS_FOLDER_NAME)) {
            QName name = ContentModel.PROP_NAME;
            if (type.equals("cm:content")) {
                auditEntry.put("action", I18NUtil.getMessage("auditlog.label.attachment.added") + " " + properties.get(name));
                auditEntry.put("type", getTypeMessage("attachment"));
                result.add(auditEntry);
            } else if (type.contains("doc:")) {
                auditEntry.put("action", I18NUtil.getMessage("auditlog.label.document.added") + " " + properties.get(name));
                auditEntry.put("type", getTypeMessage("document"));
                result.add(auditEntry);
            } else if (type.contains("cm:folder")) {
                auditEntry.put("action", I18NUtil.getMessage("auditlog.label.folder.added") + " " + properties.get(name));
                auditEntry.put("type", getTypeMessage("folder"));
                result.add(auditEntry);
            }
        } else if (type.startsWith("note:")) {
            String trimmedNote = StringUtils.abbreviate((String) properties.get(OpenESDHModel.PROP_NOTE_CONTENT), MAX_NOTE_TEXT_LENGTH);
            auditEntry.put("action", I18NUtil.getMessage("auditlog.label.note.added") + " " + trimmedNote);
            auditEntry.put("type", getTypeMessage("note"));
            result.add(auditEntry);
        } else {
            auditEntry.put("action", I18NUtil.getMessage("auditlog.label.case.created") + " " + pArray[pArray.length - 1].split(":")[1]);
            auditEntry.put("type", getTypeMessage("system"));
            result.add(auditEntry);
        }
    }

    private void addEntryTransactionDelete(Map<String, Serializable> values, JSONObject auditEntry) throws JSONException {
        HashSet<String> aspects = (HashSet) values.get("/esdh/transaction/aspects/delete");
        String path = (String) values.get("/esdh/transaction/path");
        String[] pArray = path.split("/");

        if (aspects != null && aspects.contains(ContentModel.ASPECT_COPIEDFROM.toString())) {
            auditEntry.put("action", I18NUtil.getMessage("auditlog.label.finished.editing") + " " + pArray[pArray.length - 1].split(":")[1]);
            auditEntry.put("type", getTypeMessage("system"));
            result.add(auditEntry);
        } else {
            auditEntry.put("action", I18NUtil.getMessage("auditlog.label.deleted.document") + " " + pArray[pArray.length - 1].split(":")[1]);
            auditEntry.put("type", getTypeMessage("system"));
            result.add(auditEntry);
        }
    }

    private void addEntryTransactionCheckIn(Map<String, Serializable> values, JSONObject auditEntry) throws JSONException {
        String path = (String) values.get("/esdh/transaction/path");
        String[] pArray = path.split("/");

        auditEntry.put("action", I18NUtil.getMessage("auditlog.label.checkedin") + " " + pArray[pArray.length - 1].split(":")[1]);
        auditEntry.put("type", getTypeMessage("system"));
        result.add(auditEntry);
    }

    private void addEntryPartyAdd(Map<String, Serializable> values, JSONObject auditEntry) throws JSONException {
        String contactName = (String) values.get(PARTY_ADD_NAME);
        if (StringUtils.isNotEmpty(contactName)) {
            String groupName = (String) values.get(PARTY_ADD_GROUP_NAME);
            auditEntry.put("action", I18NUtil.getMessage("auditlog.label.party.added", contactName, groupName));
            auditEntry.put("type", getTypeMessage("party"));
            result.add(auditEntry);
        }
    }

    private void addEntryPartyRemove(Map<String, Serializable> values, JSONObject auditEntry) throws JSONException {
        String contactName = (String) values.get(PARTY_REMOVE_NAME);
        if (StringUtils.isNotEmpty(contactName)) {
            String groupName = (String) values.get(PARTY_REMOVE_GROUP_NAME);
            auditEntry.put("action", I18NUtil.getMessage("auditlog.label.party.removed", contactName, groupName));
            auditEntry.put("type", getTypeMessage("party"));
            result.add(auditEntry);
        }
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
}
