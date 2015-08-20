package dk.openesdh.repo.model;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lanre.
 */
public class CaseInfoImpl implements CaseInfo {
    /**
     * Case node reference
     */
    private NodeRef nodeRef;

    /**
     * Case preset
     */
    private String caseId;

    /**
     * Case title
     */
    private String title;

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
     * Case start date
     */
    private Date startDate;

    /**
     * Case end date
     */
    private Date endDate;

    /**
     * Set of custom properties that have been defined for case
     */
    private Map<QName, Serializable> allProperties = new HashMap<QName, Serializable>(1);

//Set of contacts??

    public CaseInfoImpl(NodeRef nodeRef, String caseId, String title, String description, Map<QName, Serializable> allProperties) {
        this.nodeRef = nodeRef;
        this.caseId = caseId;
        this.title = title;
        this.description = description;
        this.allProperties = allProperties;
    }

    //<editor-fold desc="Getters, setters and other generated function">
    @Override
    public NodeRef getNodeRef() {
        return nodeRef;
    }

    public void setNodeRef(NodeRef nodeRef) {
        this.nodeRef = nodeRef;
    }

    @Override
    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
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

    @Override
    public Date getStartDate() {
        return startDate;
    }

    @Override
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    @Override
    public Date getEndDate() {
        return endDate;
    }

    @Override
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CaseInfoImpl caseInfo = (CaseInfoImpl) o;

        return this.caseId.equals(caseInfo.caseId);

    }

    @Override
    public int hashCode() {
        return caseId.hashCode();
    }

    @Override
    public String toString() {
        return this.title + " (" + caseId + ")";
    }
    //</editor-fold>

}
