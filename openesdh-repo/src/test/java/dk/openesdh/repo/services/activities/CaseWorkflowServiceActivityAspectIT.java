package dk.openesdh.repo.services.activities;

import org.alfresco.service.cmr.workflow.WorkflowService;
import org.junit.Test;

import dk.openesdh.repo.services.workflow.CaseWorkflowService;
import dk.openesdh.repo.utils.ClassUtils;

public class CaseWorkflowServiceActivityAspectIT {

    @Test
    public void checkMandatoryMethods() {
        ClassUtils.checkHasMethods(WorkflowService.class, "endTask", "cancelWorkflow");
        ClassUtils.checkHasMethods(CaseWorkflowService.class, "startWorkflow");
    }

}
