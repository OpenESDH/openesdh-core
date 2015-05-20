package dk.openesdh.repo.services.audit;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.audit.AuditService;
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

    private static final int MAX_NOTE_TEXT_LENGTH = 40;
    private Map<String, Boolean> validKeys;

    private JSONArray result = new JSONArray();

    public JSONArray getResult() {
        return result;
    }


    private void DefaultValidKeysSetup() {

        validKeys = new HashMap<String, Boolean>();

        validKeys.put("/esdh/transaction/action=CREATE", true);
        validKeys.put("/esdh/transaction/action=DELETE", true);
        validKeys.put("/esdh/transaction/action=CHECK IN", true);
    }

    public OpenESDHAuditQueryCallBack(Map<String, Boolean> validKeys)  {
        super();

        if (validKeys != null) {
            this.validKeys = validKeys;
        }
        else {
            this.DefaultValidKeysSetup();
        }
    }

    public OpenESDHAuditQueryCallBack() {
        super();
//        try {
//            result.put("entries", new JSONArray());
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
        this.DefaultValidKeysSetup();
    }

    private String getFullUserName(String user) {
        return "";
    }


    /**
     * Return the role given a case group name.
     * Returns null if the group name does not belong to a case.
     * @param groupName
     * @return
     */
    public static String getRoleFromCaseGroupName(String groupName) {
        Pattern pattern = Pattern.compile("GROUP_case_([\\d\\-]+)_(.+)");
        Matcher matcher = pattern.matcher(groupName);
        if (matcher.matches()) {
            return matcher.group(2);
        } else {
            return null;
        }
    }

    @Override
    public boolean valuesRequired() {
        return true;
    }

    @Override
    public boolean handleAuditEntry(Long entryId, String applicationName, String user, long time, Map<String, Serializable> values) {
        try {
            JSONObject auditEntry = new JSONObject();
            auditEntry.put("user",user);
            auditEntry.put("time", time);

            for (Map.Entry<String, Serializable> entry : values.entrySet()) {
                String key = entry.getKey();
                Serializable value = entry.getValue();

                if (key == null || value == null) {
                    continue;
                }
                switch (key) {
                    // Added member to role
                    case "/esdh/security/addAuthority/args/parentName/value": {
                        String parent = (String) values.get("/esdh/security/addAuthority/args/parentName/value");
                        String role = getRoleFromCaseGroupName(parent);
                        if (role != null) {
                            String authority = (String) values.get("/esdh/security/addAuthority/args/childName/value");
                            auditEntry.put("action", I18NUtil.getMessage("auditlog.label.member.added", authority, role));
                            auditEntry.put("type", I18NUtil.getMessage("auditlog.label.type.member"));
                            result.add(auditEntry);
                        }
                        break;
                    }

                    // Removed member from role
                    case "/esdh/security/removeAuthority/args/parentName/value": {
                        String parent = (String) values.get("/esdh/security/removeAuthority/args/parentName/value");
                        String role = getRoleFromCaseGroupName(parent);
                        if (role != null) {
                            String authority = (String) values.get("/esdh/security/removeAuthority/args/childName/value");
                            auditEntry.put("action", I18NUtil.getMessage("auditlog.label.member.removed", authority, role));
                            auditEntry.put("type", I18NUtil.getMessage("auditlog.label.type.member"));
                            result.add(auditEntry);
                        }
                        break;
                    }

                    // file/folder CRUD transaction
                    case "/esdh/transaction/user":  {
                        if (values.get("/esdh/transaction/action").equals("CREATE")) {
                            if (validKeys.get("/esdh/transaction/action=CREATE")) {
                                String type = (String)values.get("/esdh/transaction/type");
                                String path = (String)values.get("/esdh/transaction/path");
                                String[] pArray = path.split("/");

                                Map properties = (Map) values.get("/esdh/transaction/properties/add");

                                if (path.indexOf(OpenESDHModel.DOCUMENTS_FOLDER_NAME) != -1) {
                                    QName name = QName.createQName("http://www.alfresco.org/model/content/1.0", "name");
                                    if (type.equals("cm:content")) {
                                        auditEntry.put("action", I18NUtil.getMessage("auditlog.label.attachment.added") + " " + properties.get(name));
                                        auditEntry.put("type", I18NUtil.getMessage("auditlog.label.type.attachment"));
                                        result.add(auditEntry);
                                    }
                                    else if (type.contains("doc:")) {
                                        auditEntry.put("action", I18NUtil.getMessage("auditlog.label.document.added") + " " + properties.get(name));
                                        auditEntry.put("type", I18NUtil.getMessage("auditlog.label.type.document"));
                                        result.add(auditEntry);
                                    }
                                    else if (type.contains("cm:folder")) {
                                        auditEntry.put("action", I18NUtil.getMessage("auditlog.label.folder.added") + " " + properties.get(name));
                                        auditEntry.put("type", I18NUtil.getMessage("auditlog.label.type.folder"));
                                        result.add(auditEntry);
                                    }
                                } else if (type.startsWith("note:")) {
                                    String trimmedNote = StringUtils
                                            .abbreviate((String) properties
                                                    .get(OpenESDHModel.PROP_NOTE_CONTENT), MAX_NOTE_TEXT_LENGTH);
                                    auditEntry.put("action", I18NUtil
                                            .getMessage("auditlog.label.note" +
                                                    ".added") + " " + trimmedNote);
                                    result.add(auditEntry);
                                    auditEntry.put("type", I18NUtil
                                            .getMessage("auditlog.label.type.note"));
                                } else {
                                    auditEntry.put("action", I18NUtil.getMessage("auditlog.label.case.created") + " " + pArray[6].split(":")[1]);
                                    auditEntry.put("type", getTypeMessageSystem());
                                    result.add(auditEntry);
                                }
                            }
                        }
                        else if (values.get("/esdh/transaction/action").equals("DELETE")) {
                            if (validKeys.get("/esdh/transaction/action=DELETE")) {

                                HashSet<String> aspects = (HashSet)values.get("/esdh/transaction/aspects/delete");
                                QName name = QName.createQName("http://www.alfresco.org/model/content/1.0", "copiedfrom");

                                String path = (String)values.get("/esdh/transaction/path");
                                String[] pArray = path.split("/");


                                if (aspects != null && aspects.contains(name)) {
                                    auditEntry.put("action", I18NUtil.getMessage("auditlog.label.finished.editing") + " " + pArray[pArray.length-1].split(":")[1]);
                                    auditEntry.put("type", getTypeMessageSystem());
                                    result.add(auditEntry);
                                }
                                else {
                                    auditEntry.put("action", I18NUtil.getMessage("auditlog.label.deleted.document") + " " + pArray[pArray.length-1].split(":")[1]);
                                    auditEntry.put("type", getTypeMessageSystem());
                                    result.add(auditEntry);
                                }
                            }
                        }
                        else if (values.get("/esdh/transaction/action").equals("CHECK IN")) {
                            if (validKeys.get("/esdh/transaction/action=CHECK IN")) {

                                String path = (String)values.get("/esdh/transaction/path");
                                String[] pArray = path.split("/");

                                auditEntry.put("action", I18NUtil.getMessage("auditlog.label.checkedin") + " " + pArray[pArray.length-1].split(":")[1]);
                                auditEntry.put("type", getTypeMessageSystem());
                                result.add(auditEntry);
                            }
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return true;
    }
    
    private String getTypeMessageSystem(){
        return I18NUtil.getMessage("auditlog.label.type.system");
    }

    @Override
    public boolean handleAuditEntryError(Long entryId, String errorMsg, Throwable error) {

        throw new AlfrescoRuntimeException(errorMsg,error);
    }
}




