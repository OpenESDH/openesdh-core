package dk.openesdh.repo.model;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

public class Note {

    private NodeRef parent;

    private String headline;

    private String content;

    private List<String> concernedParties = new ArrayList<String>();

    public Note() {

    }

    public Note(NodeRef parent, String headline, String content) {
        super();
        this.parent = parent;
        this.headline = headline;
        this.content = content;
    }

    public NodeRef getParent() {
        return parent;
    }

    public void setParent(NodeRef parent) {
        this.parent = parent;
    }

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getConcernedParties() {
        return concernedParties;
    }

    public void setConcernedParties(List<String> concernedParties) {
        this.concernedParties = concernedParties;
    }

}
