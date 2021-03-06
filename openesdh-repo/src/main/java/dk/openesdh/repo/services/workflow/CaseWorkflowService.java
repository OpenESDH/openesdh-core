package dk.openesdh.repo.services.workflow;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.namespace.QName;

import dk.openesdh.repo.model.WorkflowInfo;

public interface CaseWorkflowService {

    String NAME = "CaseWorkflowService";

    String WORKFLOW_PRIORITY_HIGH = "1";
    String WORKFLOW_PRIORITY_MEDIUM = "2";
    String WORKFLOW_PRIORITY_LOW = "3";

    String WORKFLOW_START_TASK_ID = "activiti$start";

    WorkflowPath startWorkflow(WorkflowInfo workflow);

    List<Map<String, Object>> getWorkflowAssignees(String pathId);

    void grantCaseAccessToWorkflowAssignees(Map<QName, Serializable> workflowParams);
}
