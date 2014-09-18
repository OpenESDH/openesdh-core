package dk.openesdh.repo.webscripts.cases;

import dk.openesdh.repo.services.NodeInfoService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * Created by torben on 11/09/14.
 */
public class CaseInfo extends AbstractWebScript {

    private NodeInfoService nodeInfoService;
    private NamespaceService namespaceService;
    private DictionaryService dictionaryService;

    public void setNodeInfoService(NodeInfoService nodeInfoService) {
        this.nodeInfoService = nodeInfoService;
    }

    public void setNamespaceService(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }

    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        NodeRef caseNodeRef = new NodeRef(req.getParameter("nodeRef"));
        Map<QName, Serializable> caseInfo = nodeInfoService.getNodeInfo(caseNodeRef);
        JSONObject json = buildJSON(caseInfo);

        try {
            json.write(res.getWriter());
        } catch (JSONException e) {
            e.printStackTrace();
        }
//        res.getWriter().write(json.toString());
    }

    JSONObject buildJSON(Map<QName, Serializable> caseInfo) {
        JSONObject result = new JSONObject();
        try {
            for (Map.Entry<QName, Serializable> entry : caseInfo.entrySet()) {
                Serializable value = entry.getValue();
                JSONObject valueObj = new JSONObject();
                if(value != null) {
                    if(Date.class.equals(value.getClass())) {
                        valueObj.put("type", "Date");
                        valueObj.put("value", ((Date)value).getTime());

                        result.put(entry.getKey().toPrefixString(namespaceService), valueObj);
                    }
                    else {
                        valueObj.put("value", value);
                        valueObj.put("type", "String");
                        result.put(entry.getKey().toPrefixString(namespaceService), valueObj);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }


}
