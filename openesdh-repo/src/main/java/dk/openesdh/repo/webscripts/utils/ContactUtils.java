package dk.openesdh.repo.webscripts.utils;

import java.io.Serializable;
import java.util.Map;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.json.simple.JSONObject;

public class ContactUtils {

    public static JSONObject createContactJson(NodeRef contactNode, Map<QName, Serializable> props) {
        JSONObject result = new JSONObject();
        result.put("nodeRefId", contactNode.toString());
        result.put("storeType", contactNode.getStoreRef().getProtocol());
        result.put("storeId", contactNode.getStoreRef().getIdentifier());
        result.put("id", contactNode.getId());
        props.entrySet().stream()
                .filter((Map.Entry<QName, Serializable> t)
                        -> t.getValue() != null && !isKeyOfSystemModelNamepace(t.getKey()))
                .forEach((entry)
                        -> result.put(entry.getKey().getLocalName(), entry.getValue()));
//        result.put("associations", getAssociations(contactNode));
        return result;
    }

    private static boolean isKeyOfSystemModelNamepace(QName key) {
        return key.getNamespaceURI().equalsIgnoreCase(NamespaceService.SYSTEM_MODEL_1_0_URI);
    }
}
