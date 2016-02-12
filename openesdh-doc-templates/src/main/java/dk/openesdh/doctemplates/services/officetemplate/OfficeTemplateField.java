package dk.openesdh.doctemplates.services.officetemplate;

/**
 * Created by syastrov on 9/23/15.
 */
public class OfficeTemplateField {
    private String name;
    private String value;
    private String type;
    private String mappedFieldName;

    // TODO: isMandatory, etc

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMappedFieldName() {
        return mappedFieldName;
    }

    public void setMappedFieldName(String mappedFieldName) {
        this.mappedFieldName = mappedFieldName;
    }
}
