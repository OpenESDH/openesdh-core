package dk.openesdh.repo.services.workflow;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

public interface WorkflowTaskService {

    public static final String TASK_PACKAGE_ITEMS = "packageItems";
    public static final String TASK_CASE_ID = "caseId";
    public static final String PACKAGE_ITEM_NAME = "name";
    public static final String PACKAGE_ITEM_MODIFIED = "modified";
    public static final String PACKAGE_ITEM_DESCRIPTION = "description";
    public static final String PACKAGE_ITEM_CREATEDBY = "createdBy";
    public static final String PACKAGE_ITEM_MAIN_DOC_NODE_REF = "mainDocNodeRef";
    public static final String PACKAGE_ITEM_DOC_RECORD_NODE_REF = "docRecordNodeRef";
    public static final String WORKFLOW_ASSIGNEES = "workflowAssignees";

    public static final String NODE_REF = "nodeRef";

    /**
     * Retrieves workflow task with package contents
     * 
     * @param taskId
     *            Id of the task to retrieve
     * @return a map with task properties and package items
     */
    Map<String, Object> getWorkflowTask(String taskId);

    /**
     * Retrieves case id for the provided workflow or task
     * 
     * @param workflowOrTaskId
     *            Id of the workflow or task to retrieve case id for
     * @return case id
     */
    Optional<String> getWorkflowCaseId(String workflowOrTaskId);

    /**
     * Retrieves caseId for the provided workflow task
     * 
     * @param taskId
     * @return
     */
    Optional<Serializable> getCaseIdByTaskId(String taskId);

}
