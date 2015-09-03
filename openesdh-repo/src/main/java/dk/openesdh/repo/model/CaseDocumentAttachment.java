package dk.openesdh.repo.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.alfresco.service.cmr.security.PersonService.PersonInfo;

public class CaseDocumentAttachment {

    private String name;
    private String nodeRef;
    private String versionLabel;
    private PersonInfo creator;
    private PersonInfo modifier;
    private Date created;
    private Date modified;
    private String type;
    private String fileType;
    private String description;
    private List<CaseDocumentAttachment> versions = new ArrayList<CaseDocumentAttachment>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNodeRef() {
        return nodeRef;
    }

    public void setNodeRef(String nodeRef) {
        this.nodeRef = nodeRef;
    }

    public String getVersionLabel() {
        return versionLabel;
    }

    public void setVersionLabel(String versionLabel) {
        this.versionLabel = versionLabel;
    }

    public PersonInfo getCreator() {
        return creator;
    }

    public void setCreator(PersonInfo creator) {
        this.creator = creator;
    }

    public PersonInfo getModifier() {
        return modifier;
    }

    public void setModifier(PersonInfo modifier) {
        this.modifier = modifier;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<CaseDocumentAttachment> getVersions() {
        return versions;
    }

    public void setVersions(List<CaseDocumentAttachment> versions) {
        this.versions = versions;
    }

}
