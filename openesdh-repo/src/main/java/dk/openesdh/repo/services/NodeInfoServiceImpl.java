package dk.openesdh.repo.services;

import dk.openesdh.repo.webscripts.cases.CaseInfo;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * Created by torben on 11/09/14.
 */
public class NodeInfoServiceImpl implements NodeInfoService {

    private NodeService nodeService;
    private DictionaryService dictionaryService;

    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    @Override
    public Map<QName, Serializable> getNodeInfo(NodeRef nodeRef) {
        Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
        return properties;
    }

    @Override
    public JSONObject buildJSON(Map<QName, Serializable> nodeInfo, CaseInfo caseInfo) {
        JSONObject result = new JSONObject();
        try {
            for (Map.Entry<QName, Serializable> entry : nodeInfo.entrySet()) {
                Serializable value = entry.getValue();
                QName key = entry.getKey();
                JSONObject valueObj = new JSONObject();
                if (value != null) {
                    if (Date.class.equals(value.getClass())) {
                        valueObj.put("type", "Date");
                        valueObj.put("value", ((Date) value).getTime());
                    } else {
                        valueObj.put("value", value);
                        valueObj.put("type", "String");
                    }

                    valueObj.put("label", dictionaryService.getProperty(key).getTitle(dictionaryService));
                    result.put(entry.getKey().toPrefixString(caseInfo.getNamespaceService()), valueObj);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }
}
