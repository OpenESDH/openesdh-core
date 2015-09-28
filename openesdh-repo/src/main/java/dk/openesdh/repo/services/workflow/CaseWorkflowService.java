package dk.openesdh.repo.services.workflow;

import org.alfresco.service.cmr.workflow.WorkflowPath;

import dk.openesdh.repo.model.WorkflowInfo;

public interface CaseWorkflowService {

    String WORKFLOW_PRIORITY_HIGH = "1";
    String WORKFLOW_PRIORITY_MEDIUM = "2";
    String WORKFLOW_PRIORITY_LOW = "3";

    WorkflowPath startWorkflow(WorkflowInfo workflow);
}