package dk.openesdh.repo.model;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.json.JSONException;
import org.json.JSONObject;

import dk.openesdh.repo.services.system.MultiLanguageValue;

public class ClassifValue {
    private NodeRef nodeRef;
    private String name;
    private String displayName;
    private Boolean disabled = false;
    private Boolean isSystem = false;
    private MultiLanguageValue mlDisplayNames;

    public ClassifValue() {
    }

    public ClassifValue(String nodeRefId) {
        this.nodeRef = new NodeRef(nodeRefId);
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

    public Boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

    public Boolean getIsSystem() {
        return isSystem;
    }

    public void setIsSystem(Boolean isSystem) {
        this.isSystem = isSystem;
    }

    public MultiLanguageValue getMlDisplayNames() {
        return mlDisplayNames;
    }

    public void setMlDisplayNames(MultiLanguageValue mlDisplayNames) {
        this.mlDisplayNames = mlDisplayNames;
    }

    public JSONObject toJSONObject() {
        try {
            return getJsonObject();
        } catch (JSONException ex) {
            throw new AlfrescoRuntimeException("Json error", ex);
        }
    }

    protected JSONObject getJsonObject() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("nodeRef", nodeRef.toString());
        json.put("name", getName());
        json.put("displayName", getDisplayName());
        json.put("disabled", getDisabled());
        json.put("mlDisplayNames", this.mlDisplayNames == null ? null : this.mlDisplayNames.toJSONArray());
        return json;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
