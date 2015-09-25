package dk.openesdh.repo.model;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.json.JSONException;
import org.json.JSONObject;

public class DocumentType {

    private NodeRef nodeRef;
    private String name;
    private String displayName;
    private Boolean systemType;

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

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Boolean getSystemType() {
        return systemType;
    }

    public void setSystemType(Boolean systemType) {
        this.systemType = systemType;
    }

    public JSONObject toJSONObject() {
        try {
            JSONObject json = new JSONObject();
            json.put("nodeRef", nodeRef.toString());
            json.put("name", name);
            json.put("displayName", displayName);
            json.put("systemType", systemType);
            return json;
        } catch (JSONException ex) {
            throw new AlfrescoRuntimeException("Json error", ex);
        }
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
