package dk.openesdh.repo.model;

import java.io.Serializable;

import org.alfresco.service.cmr.repository.NodeRef;
import org.json.simple.JSONObject;

public class OEParameter {

    private NodeRef nodeRef;
    private String name;
    private Serializable value;

    public JSONObject toJSONObject() {
        JSONObject json = new JSONObject();
        if (nodeRef != null) {
            json.put("nodeRef", nodeRef.toString());
        }
        json.put("name", name);
        json.put("value", value);
        return json;
    }

    public NodeRef getNodeRef() {
        return nodeRef;
    }

    public void setNodeRef(NodeRef nodeRef) {
        this.nodeRef = nodeRef;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Serializable getValue() {
        return value;
    }

    public void setValue(Serializable value) {
        this.value = value;
    }
}
