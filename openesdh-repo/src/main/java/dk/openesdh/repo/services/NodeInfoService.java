package dk.openesdh.repo.services;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.json.JSONObject;

/**
 * Created by torben on 11/09/14.
 */
public interface NodeInfoService {

    String NODE_TYPE_PROPERTY = "type";

    class NodeInfo {
        public Map<QName, Serializable> properties;
        public Set<QName> aspects;
        public QName nodeClassName;
    }

    public NodeInfo getNodeInfo(NodeRef nodeRef);

    JSONObject buildJSON(NodeInfo nodeInfo);

    JSONObject getSelectedProperties(NodeInfo nodeInfo, Collection<QName> objectProps);

}
