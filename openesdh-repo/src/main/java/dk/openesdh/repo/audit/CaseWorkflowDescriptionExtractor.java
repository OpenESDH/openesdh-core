package dk.openesdh.repo.audit;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("audit.dk.openesdh.CaseWorkflowDescriptionExtractor")
public class CaseWorkflowDescriptionExtractor extends AbstractAnnotatedDataExtractor {

    @Autowired
    @Qualifier("WorkflowService")
    private WorkflowService workflowService;

    @SuppressWarnings("unchecked")
    @Override
    public Serializable extractData(Serializable value) throws Throwable {
        if (value instanceof String) {
            return getWorkflowDescriptionByTaskId(value.toString());
        }
        Map<QName, Serializable> params = (Map<QName, Serializable>) value;
        return params.get(WorkflowModel.PROP_WORKFLOW_DESCRIPTION);
    }

    private Serializable getWorkflowDescriptionByTaskId(String taskId) {
        return workflowService.getTaskById(taskId).getPath().getInstance().getDescription();
    }

}
