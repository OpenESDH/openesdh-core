package dk.openesdh.repo.audit;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.namespace.QName;
import org.springframework.stereotype.Service;

@Service("audit.dk.openesdh.CaseWorkflowDescriptionExtractor")
public class CaseWorkflowDescriptionExtractor extends AbstractAnnotatedDataExtractor {

    @SuppressWarnings("unchecked")
    @Override
    public Serializable extractData(Serializable value) throws Throwable {
        Map<QName, Serializable> params = (Map<QName, Serializable>) value;
        return params.get(WorkflowModel.PROP_WORKFLOW_DESCRIPTION);
    }

}
