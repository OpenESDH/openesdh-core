package dk.openesdh.repo.model;

import java.io.Serializable;
import java.util.Date;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PersonService.PersonInfo;

public abstract class CaseFolderItem implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String ITEM_TYPE_FOLDER = "cm:folder";

    private String title;
    private Date created;
    private Date modified;
    private PersonInfo creator;
    private String itemType;
    private NodeRef nodeRef;

    public CaseFolderItem() {

    }

    public CaseFolderItem(NodeRef nodeRef) {
        this.nodeRef = nodeRef;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    public PersonInfo getCreator() {
        return creator;
    }

    public void setCreator(PersonInfo creator) {
        this.creator = creator;
    }

    public NodeRef getNodeRef() {
        return nodeRef;
    }

    public void setNodeRef(NodeRef nodeRef) {
        this.nodeRef = nodeRef;
    }

    public boolean isFolder() {
        return ITEM_TYPE_FOLDER.equals(itemType);
    }

    /**
     * This method is intended to prevent deserialization errors
     * 
     * @deprecated
     */
    public void setFolder(boolean folder) {
    }
}
