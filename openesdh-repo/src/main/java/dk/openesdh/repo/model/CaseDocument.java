package dk.openesdh.repo.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.alfresco.service.cmr.security.PersonService.PersonInfo;

public class CaseDocument {

    private String title;
    private String docNo;
    private DocumentType type;
    private DocumentCategory category;
    private String status;
    private Date created;
    private Date modified;
    private String nodeRef;
    private String mainDocNodeRef;
    private PersonInfo owner;

    private List<CaseDocumentAttachment> attachments = new ArrayList<>();

    public List<CaseDocumentAttachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<CaseDocumentAttachment> attachments) {
        this.attachments = attachments;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String tite) {
        this.title = tite;
    }

    public String getDocNo() {
        return docNo;
    }

    public void setDocNo(String docNo) {
        this.docNo = docNo;
    }

    public DocumentType getType() {
        return type;
    }

    public void setType(DocumentType type) {
        this.type = type;
    }

    public DocumentCategory getCategory() {
        return category;
    }

    public void setCategory(DocumentCategory category) {
        this.category = category;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

}
