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

    NodeInfo getNodeInfo(NodeRef nodeRef);

    JSONObject buildJSON(NodeInfo nodeInfo);

    JSONObject getSelectedProperties(NodeInfo nodeInfo, Collection<QName> objectProps);

    org.json.simple.JSONObject getNodeParametersJSON(NodeRef nodeRef);

    Object formatValue(QName qname, Serializable value);

    /**
     * convert namespaced properties json to properties map
     *
     * @param json must match format of <pre>{ 'namespace_prefix' : { 'property_name' : 'value'}}</pre>
     * Example:
     * <pre>
     * {
     *   cm: {
     *          title: 'this is title',
     *          name: 'this is name'
     *       },
     *   oe: {
     *          locked: true
     *       }
     * }
     * </pre>
     *
     * @return
     */
    Map<QName, Serializable> getNodePropertiesFromJSON(JSONObject json);

}
