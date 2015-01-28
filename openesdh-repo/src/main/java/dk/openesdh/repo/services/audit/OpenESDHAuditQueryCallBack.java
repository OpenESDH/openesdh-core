package dk.openesdh.repo.services.audit;

import com.google.gdata.data.DateTime;
import dk.openesdh.repo.model.OpenESDHModel;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.audit.*;
import org.alfresco.service.cmr.audit.AuditService;
import org.alfresco.service.namespace.QName;
import org.apache.solr.common.util.Hash;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.JSONArray;
import org.springframework.extensions.surf.util.I18NUtil;

import java.io.Serializable;
import java.util.*;

/**
 * Created by flemmingheidepedersen on 18/11/14.
 */
public class OpenESDHAuditQueryCallBack implements AuditService.AuditQueryCallback {

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

//        try {
////            result.put("entries", new JSONArray());
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }

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

    @Override
    public boolean valuesRequired() {
        return true;
    }

    @Override
    public boolean handleAuditEntry(Long entryId, String applicationName, String user, long time, Map<String, Serializable> values) {



        JSONObject auditEntry = new JSONObject();
        try {
            auditEntry.put("user",user);
            auditEntry.put("time", time);
        } catch (JSONException e) {
            e.printStackTrace();
        }



        for (Map.Entry<String, Serializable> entry : values.entrySet()) {

            String key = entry.getKey();
            Serializable value = entry.getValue();
            System.out.println("what is key" + key);
//            System.out.println("values: " + values.get("/esdh/transaction/action"));

            if (key != null && value != null) {

                switch (key) {

                    // file/folder CRUD transaction
                    case "/esdh/transaction/user":  {

                            if (values.get("/esdh/transaction/action").equals("CREATE")) {
                                if (validKeys.get("/esdh/transaction/action=CREATE")) {
                                    String path = (String)values.get("/esdh/transaction/path");
                                    String[] pArray = path.split("/");



                                    if (path.indexOf(OpenESDHModel.DOCUMENTS_FOLDER_NAME) != -1) {

                                        HashMap<String, String> properties = (HashMap)values.get("/esdh/transaction/properties/add");
                                        QName name = QName.createQName("http://www.alfresco.org/model/content/1.0", "name");

                                        try {

                                            auditEntry.put("action", I18NUtil.getMessage("auditlog.label.file.added") + " " + properties.get(name));
                                            result.add(auditEntry);


                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        try {
                                            auditEntry.put("action", I18NUtil.getMessage("auditlog.label.case.created") + " " + pArray[6].split(":")[1]);
                                            result.add(auditEntry);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
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
                                        try {
                                            auditEntry.put("action", I18NUtil.getMessage("auditlog.label.finished.editing") + " " + pArray[pArray.length-1].split(":")[1]);
                                            result.add(auditEntry);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    else {
                                        try {
                                            auditEntry.put("action", I18NUtil.getMessage("auditlog.label.deleted.document") + " " + pArray[pArray.length-1].split(":")[1]);
                                            result.add(auditEntry);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                            else if (values.get("/esdh/transaction/action").equals("CHECK IN")) {
                                if (validKeys.get("/esdh/transaction/action=CHECK IN")) {

                                    String path = (String)values.get("/esdh/transaction/path");
                                    String[] pArray = path.split("/");

                                        try {
                                            auditEntry.put("action", I18NUtil.getMessage("auditlog.label.checkedin") + " " + pArray[pArray.length-1].split(":")[1]);
                                            result.add(auditEntry);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                }
                            }
                        }
                }

            }
        }
//                   System.out.println(entryId + " " + applicationName + " " + user + " " +  new Date(time) + values);
        return true;
    }

    @Override
    public boolean handleAuditEntryError(Long entryId, String errorMsg, Throwable error) {

        throw new AlfrescoRuntimeException(errorMsg,error);
    }
}




