package dk.openesdh.doctemplates.api.model;


import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Created by syastrov on 9/23/15.
 */
public class OfficeTemplate {

    private NodeRef nodeRef;
    private String name;
    private String title;
    private NodeRef docType;
    private NodeRef docCategory;
    private String description;
    private List<OfficeTemplateField> fields;

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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public NodeRef getDocType() {
        return docType;
    }

    public void setDocType(NodeRef docType) {
        this.docType = docType;
    }

    public NodeRef getDocCategory() {
        return docCategory;
    }

    public void setDocCategory(NodeRef docCategory) {
        this.docCategory = docCategory;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<OfficeTemplateField> getFields() {
        return fields;
    }

    public void setFields(List<OfficeTemplateField> fields) {
        this.fields = fields;
    }

}
