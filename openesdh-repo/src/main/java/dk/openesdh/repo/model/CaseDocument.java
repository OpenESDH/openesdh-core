package dk.openesdh.repo.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.alfresco.service.cmr.security.PersonService.PersonInfo;

public class CaseDocument {

    private String tite;
    private String docNo;
    private String type;
    private String category;
    private String state;
    private Date created;
    private Date modified;
    private String nodeRef;
    private String mainDocNodeRef;
    private PersonInfo owner;
    private String fileType;

    private List<CaseDocumentAttachment> attachments = new ArrayList<>();

    public List<CaseDocumentAttachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<CaseDocumentAttachment> attachments) {
        this.attachments = attachments;
    }

    public String getTite() {
        return tite;
    }

    public void setTite(String tite) {
        this.tite = tite;
    }

    public String getDocNo() {
        return docNo;
    }

    public void setDocNo(String docNo) {
        this.docNo = docNo;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
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

    public String getNodeRef() {
        return nodeRef;
    }

    public void setNodeRef(String nodeRef) {
        this.nodeRef = nodeRef;
    }

    public String getMainDocNodeRef() {
        return mainDocNodeRef;
    }

    public void setMainDocNodeRef(String mainDocNodeRef) {
        this.mainDocNodeRef = mainDocNodeRef;
    }

    public PersonInfo getOwner() {
        return owner;
    }

    public void setOwner(PersonInfo owner) {
        this.owner = owner;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

}
