package dk.openesdh.repo.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.alfresco.repo.security.permissions.PermissionCheckValue;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * @author Lanre Abiwon.
 */

public interface CaseInfo extends PermissionCheckValue {
    /**
     * Get the case node reference
     *
     * @return  NodeRef     case node reference, null if not set
     */
    public abstract NodeRef getNodeRef();

    /**
     * Get the case id
     *
     * @return  String  case id
     */
    public abstract String getCaseId();

    /**
     * Get the title
     *
     * @return  String  case title
     */
    public abstract String getTitle();

    /**
     * Set the title
     *
     * @param title case title
     */
    public abstract void setTitle(String title);

    /**
     * Get the description
     *
     * @return  String  case description
     */
    public abstract String getDescription();

    /**
     * Set the description
     *
     * @param description   case description
     */
    public abstract void setDescription(String description);

    /**
     * Get the custom property values
     *
     * @return  Map<QName, Serializable>    map of custom property names and values
     */
    public abstract Map<QName, Serializable> getAllProperties();

    /**
     * Get the value of a custom property
     *
     * @param  name             name of custom property
     * @return Serializable     value of the property, null if not set or doesn't exist
     */
    public abstract Serializable getCustomProperty(QName name);

    /**
     * Get the case start date
     *
     * @return <code>Date</code> case start date
     */
    public abstract Date getStartDate();

    /**
     * Get the case end date
     *
     * @return <code>Date</code> case start date
     */
    public abstract Date getEndDate();

    /**
     * Get the case created date
     *
     * @return <code>Date</code> case created date
     */
    public abstract Date getCreatedDate();

    /**
     * Set the case created date
     *
     * @param createdDate case created date
     */
    public abstract void setStartDate(Date createdDate);

    /**
     * Set the case created date
     *
     * @param createdDate case created date
     */
    public abstract void setEndDate(Date createdDate);

    /**
     * Set the case created date
     *
     * @param createdDate case created date
     */
    public abstract void setCreatedDate(Date createdDate);

    /**
     * Get the case last modified date
     *
     * @return <code>Date</code> case last modified date
     */
    public abstract Date getLastModifiedDate();

    /**
     * Set the case last modified date
     *
     * @param lastModifiedDate case last modified date
     */
    public abstract void setLastModifiedDate(Date lastModifiedDate);

}