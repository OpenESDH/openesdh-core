package dk.openesdh.doctemplates.model;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * @author lanre.
 */
public class DocumentTemplateInfoImpl implements DocumentTemplateInfo {

    /**
     * Case node reference
     */
    private NodeRef nodeRef;

    /**
     * The case types that the template is assigned to
     */
    private String[] assignedCaseTypes;

    /**
     * Document Template title
     */
    private String title;

    private String name;

    /**
     * Case description
     */
    private String description;

    /**
     * Case created date
     */
    private Date createdDate;

    /**
     * Case last modified date
     */
    private Date lastModifiedDate;

    /**
     * Set of custom properties that have been defined for case
     */
    private Map<QName, Serializable> allProperties = new HashMap<>(1);

    public DocumentTemplateInfoImpl(NodeRef nodeRef, String[] assignedTypes, String title, Map<QName, Serializable> allProperties) {
        this.nodeRef = nodeRef;
        this.assignedCaseTypes = assignedTypes;
        this.title = title;
        this.allProperties = allProperties;
        this.name = allProperties.get(ContentModel.PROP_NAME).toString();
    }

    @Override
    public NodeRef getNodeRef() {
        return nodeRef;
    }

    public void setNodeRef(NodeRef nodeRef) {
        this.nodeRef = nodeRef;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public Date getCreatedDate() {
        return createdDate;
    }

    @Override
    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    @Override
    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    @Override
    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    /**
     * Get the template type (i.e. the document MIME type)
     *
     * @return String case id
     */
    @Override
    public String getTemplateType() {
        return this.getCustomProperty(OpenESDHDocTemplateModel.PROP_TEMPLATE_TYPE).toString();
    }

    @Override
    public void setAssignedCaseTypes(String[] assignedCaseTypes) {
        this.assignedCaseTypes = assignedCaseTypes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the assigned list of case types that this document template applies to
     *
     * @return
     */
    @Override
    public String[] getAssignedCaseTypes() {
        return this.assignedCaseTypes;
    }

    public Serializable getCustomProperty(QName name) {
        Serializable result = null;
        if (this.allProperties != null) {
            result = this.allProperties.get(name);
        }
        return result;
    }

    @Override
    public Map<QName, Serializable> getAllProperties() {
        return allProperties;
    }

    public void setCustomProperties(Map<QName, Serializable> customProperties) {
        this.allProperties = customProperties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DocumentTemplateInfoImpl docTmplInfo = (DocumentTemplateInfoImpl) o;

        return this.nodeRef.equals(docTmplInfo.nodeRef);

    }

    @Override
    public int hashCode() {
        return nodeRef.hashCode();
    }

    @Override
    public String toString() {
        return this.title;
    }
}
