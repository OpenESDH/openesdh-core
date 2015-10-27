package dk.openesdh.repo.audit;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowInstanceQuery;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("audit.dk.openesdh.CaseWorkflowInitiatorExtractor")
public class CaseWorkflowInitiatorExtractor extends AbstractAnnotatedDataExtractor {

    @Autowired
    @Qualifier("WorkflowService")
    private WorkflowService workflowService;
    @Autowired
    private PersonService personService;

    @SuppressWarnings("unchecked")
    @Override
    public Serializable extractData(Serializable value) throws Throwable {
        Map<QName, Serializable> params = (Map<QName, Serializable>) value;
        return Optional.ofNullable((NodeRef) params.get(WorkflowModel.ASSOC_PACKAGE))
                .flatMap(bpmPackage -> getWorkflowByBpmPackage(bpmPackage))
                .map(WorkflowInstance::getInitiator)
                .map(personRef -> personService.getPerson(personRef).getUserName())
                .orElse(null);
    }

    private Optional<WorkflowInstance> getWorkflowByBpmPackage(NodeRef bpmPackage) {
        Map<QName, Object> props = new HashMap<QName, Object>();
        props.put(WorkflowModel.ASSOC_PACKAGE, bpmPackage);

        WorkflowInstanceQuery query = new WorkflowInstanceQuery();
        query.setActive(true);
        query.setCustomProps(props);
        List<WorkflowInstance> workflows = workflowService.getWorkflows(query);
        return workflows.isEmpty() ? Optional.empty() : Optional.of(workflows.get(0));
    }

}
