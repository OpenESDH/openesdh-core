package dk.openesdh.repo.audit;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("audit.dk.openesdh.CaseWorkflowReviewOutcomeExtractor")
public class CaseWorkflowReviewOutcomeExtractor extends AbstractAnnotatedDataExtractor {

    @Autowired
    @Qualifier("WorkflowService")
    private WorkflowService workflowService;

    @Override
    public Serializable extractData(Serializable value) throws Throwable {
        return getTaskOutcome(workflowService.getTaskById(value.toString())).orElse(null);
    }

    public static Optional<Serializable> getTaskOutcome(WorkflowTask task) {
        Map<QName, Serializable> taskProps = task.getProperties();
        return Optional.ofNullable(taskProps.get(WorkflowModel.PROP_OUTCOME_PROPERTY_NAME))
                .map(outcomePropName -> taskProps.get((QName) outcomePropName));
    }

}
