package dk.openesdh.repo.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PersonService;

public class Note {

    private NodeRef nodeRef;

    private NodeRef parent;

    private String title;

    private String content;

    private String author;

    private List<NodeRef> concernedParties = new ArrayList<>();

    private List<ContactInfo> concernedPartiesInfo = new ArrayList<>();

    private PersonService.PersonInfo authorInfo;

    private String creator;

    private Date created;

    private Date modified;

    public Note() {

    }

    public NodeRef getNodeRef() {
        return nodeRef;
    }

    public void setNodeRef(NodeRef nodeRef) {
        this.nodeRef = nodeRef;
    }

    public NodeRef getParent() {
        return parent;
    }

    public void setParent(NodeRef parent) {
        this.parent = parent;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public PersonService.PersonInfo getAuthorInfo() {
        return authorInfo;
    }

    public void setAuthorInfo(PersonService.PersonInfo authorInfo) {
        this.authorInfo = authorInfo;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
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

    public List<ContactInfo> getConcernedPartiesInfo() {
        return concernedPartiesInfo;
    }

    public void setConcernedPartiesInfo(List<ContactInfo> concernedPartiesInfo) {
        this.concernedPartiesInfo = concernedPartiesInfo;
    }

    public List<NodeRef> getConcernedParties() {
        return concernedParties;
    }

    public void setConcernedParties(List<NodeRef> concernedParties) {
        this.concernedParties = concernedParties;
    }

}
