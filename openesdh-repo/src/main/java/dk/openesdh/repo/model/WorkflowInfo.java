package dk.openesdh.repo.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;

public class WorkflowInfo {

    private String workflowType;
    private List<NodeRef> items = new ArrayList<>();
    private List<String> assignees = new ArrayList<>();
    private List<String> groupAssignees = new ArrayList<>();
    private String assignTo;
    private String assignToGroup;
    private Date dueDate;
    private String priority;
    private String message;
    private boolean sendEmailNotifications;
    private Map<String, Object> properties = new HashMap<>();

    public String getWorkflowType() {
        return workflowType;
    }

    public void setWorkflowType(String workflowType) {
        this.workflowType = workflowType;
    }

    public List<NodeRef> getItems() {
        return items;
    }

    public void setItems(List<NodeRef> items) {
        this.items = items;
    }

    public String getAssignTo() {
        return assignTo;
    }

    public void setAssignTo(String assignTo) {
        this.assignTo = assignTo;
    }

    public String getAssignToGroup() {
        return assignToGroup;
    }

    public void setAssignToGroup(String assignToGroup) {
        this.assignToGroup = assignToGroup;
    }

    public List<String> getGroupAssignees() {
        return groupAssignees;
    }

    public void setGroupAssignees(List<String> groupAssignees) {
        this.groupAssignees = groupAssignees;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSendEmailNotifications() {
        return sendEmailNotifications;
    }

    public void setSendEmailNotifications(boolean sendEmailNotifications) {
        this.sendEmailNotifications = sendEmailNotifications;
    }

    public List<String> getAssignees() {
        return assignees;
    }

    public void setAssignees(List<String> assignees) {
        this.assignees = assignees;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

}
