package dk.openesdh.repo.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PersonService.PersonInfo;

public class CaseDocument extends CaseFolderItem {

    private String docNo;
    private DocumentType type;
    private DocumentCategory category;
    private String status;
    private NodeRef mainDocNodeRef;
    private PersonInfo owner;
    private boolean locked;
    private String mimetype;
    private Map<String, Serializable> aspects = new HashMap<>();

    private List<CaseDocumentAttachment> attachments = new ArrayList<>();

    public CaseDocument() {

    }

    public CaseDocument(NodeRef nodeRef) {
        super(nodeRef);
    }

    public String getMimetype() {
        return mimetype;
    }

    public void setMimetype(String mimetype) {
        this.mimetype = mimetype;
    }

    public List<CaseDocumentAttachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<CaseDocumentAttachment> attachments) {
        this.attachments = attachments;
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

    public PersonInfo getOwner() {
        return owner;
    }

    public void setOwner(PersonInfo owner) {
        this.owner = owner;
    }

    public NodeRef getMainDocNodeRef() {
        return mainDocNodeRef;
    }

    public void setMainDocNodeRef(NodeRef mainDocNodeRef) {
        this.mainDocNodeRef = mainDocNodeRef;
    }

    public NodeRef nodeRefObject() {
        return getNodeRef();
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public Map<String, Serializable> getAspects() {
        return aspects;
    }

    public void setAspects(Map<String, Serializable> aspects) {
        this.aspects = aspects;
    }

}
