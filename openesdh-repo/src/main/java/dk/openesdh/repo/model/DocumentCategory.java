package dk.openesdh.repo.model;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.json.JSONException;
import org.json.JSONObject;

public class DocumentCategory {

    private NodeRef nodeRef;
    private String name;
    private String displayName;
    private Boolean systemCategory;

    public DocumentCategory() {
    }

    public DocumentCategory(String typeNodeRefId) {
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

    public Boolean getSystemCategory() {
        return systemCategory;
    }

    public void setSystemCategory(Boolean systemCategory) {
        this.systemCategory = systemCategory;
    }

    public JSONObject toJSONObject() {
        try {
            JSONObject json = new JSONObject();
            json.put("nodeRef", nodeRef.toString());
            json.put("name", name);
            json.put("displayName", displayName);
            json.put("systemType", systemCategory);
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
