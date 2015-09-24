package dk.openesdh.repo.model;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONException;
import org.json.JSONObject;

public class DocumentType {

    private NodeRef nodeRef;
    private String name;

    public DocumentType() {
    }

    public DocumentType(String typeNodeRefId) {
        this.nodeRef = new NodeRef(typeNodeRefId);
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

    public JSONObject toJSONObject() {
        try {
            JSONObject json = new JSONObject();
            json.put("nodeRef", nodeRef.toString());
            json.put("name", name);
            return json;
        } catch (JSONException ex) {
            throw new AlfrescoRuntimeException("Json error", ex);
        }
    }

    @Override
    public String toString() {
        return "[" + nodeRef + ", " + name + "]";
    }
}
