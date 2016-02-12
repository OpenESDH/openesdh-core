package dk.openesdh.doctemplates.services.officetemplate;

import java.util.List;

/**
 * Created by syastrov on 9/23/15.
 */
public class OfficeTemplate {

    private String nodeRef;
    private String name;
    private String title;
    private String description;
    private List<OfficeTemplateField> fields;

    public String getNodeRef() {
        return nodeRef;
    }

    public void setNodeRef(String nodeRef) {
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
