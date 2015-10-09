package dk.openesdh.repo.services.workflow;

import java.util.Map;

public interface WorkflowTaskService {

    public static final String TASK_PACKAGE_ITEMS = "packageItems";
    public static final String PACKAGE_ITEM_NAME = "name";
    public static final String PACKAGE_ITEM_MODIFIED = "modified";
    public static final String PACKAGE_ITEM_DESCRIPTION = "description";
    public static final String PACKAGE_ITEM_CREATEDBY = "createdBy";
    public static final String PACKAGE_ITEM_MAIN_DOC_NODE_REF = "mainDocNodeRef";

    public static final String NODE_REF = "nodeRef";

    /**
     * Retrieves workflow task with package contents
     * 
     * @param taskId
     *            Id of the task to retreive
     * @return a map with task properties and package items
     */
    Map<String, Object> getWorkflowTask(String taskId);

}
