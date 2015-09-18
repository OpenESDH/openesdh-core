package dk.openesdh.repo.services.workflow;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import dk.openesdh.repo.model.WorkflowInfo;

public class CaseWorkflowServiceImpl implements CaseWorkflowService {

    private WorkflowService workflowService;
    private NodeService nodeService;

    public void setWorkflowService(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    @Override
    public WorkflowPath startWorkflow(WorkflowInfo workflow) {

        NodeRef workflowPackage = workflowService.createPackage(null);
        addItemsToWorkflowPackage(workflowPackage, workflow);

        Map<QName, Serializable> params = getWorkflowParams(workflow);
        params.put(WorkflowModel.ASSOC_PACKAGE, workflowPackage);
        
        return workflowService.startWorkflow(workflow.getWorkflowType(), params);
    }

    protected Map<QName, Serializable> getWorkflowParams(WorkflowInfo workflow) {
        Map<QName, Serializable> params = new HashMap<QName, Serializable>();

        setNodeRefProp(params, WorkflowModel.ASSOC_ASSIGNEE, workflow.getAssignTo());
        setNodeRefProp(params, WorkflowModel.ASSOC_GROUP_ASSIGNEE, workflow.getAssignToGroup());
        setNodeRefListProp(params, WorkflowModel.ASSOC_ASSIGNEES, workflow.getAssignees());
        setNodeRefListProp(params, WorkflowModel.ASSOC_GROUP_ASSIGNEES, workflow.getGroupAssignees());
        params.put(WorkflowModel.PROP_WORKFLOW_DUE_DATE, workflow.getDueDate());
        params.put(WorkflowModel.PROP_WORKFLOW_DESCRIPTION, workflow.getMessage());
        params.put(WorkflowModel.PROP_SEND_EMAIL_NOTIFICATIONS, workflow.isSendEmailNotifications());
        params.put(WorkflowModel.PROP_PRIORITY, workflow.getPriority());
        params.put(WorkflowModel.PROP_PERCENT_COMPLETE, workflow.getRequiredApprovalPercentage());

        return params;
    }

    protected void setNodeRefProp(Map<QName, Serializable> params, QName prop, String value) {
        if (StringUtils.isEmpty(value)) {
            return;
        }
        params.put(prop, new NodeRef(value));
    }
    
    protected void setNodeRefListProp(Map<QName, Serializable> params, QName prop, List<String> values) {
        if (CollectionUtils.isEmpty(values)) {
            return;
        }

        List<NodeRef> nodeRefList = values.stream()
                .map(value -> new NodeRef(value))
                .collect(Collectors.toList());

        params.put(prop, (Serializable) nodeRefList);
    }

    protected void addItemsToWorkflowPackage(NodeRef workflowPackage, WorkflowInfo workflow) {
        workflow.getItems()
                .stream()
                .forEach(item -> {
                    NodeRef itemNodeRef = new NodeRef(item);
                    String itemName = nodeService.getProperty(itemNodeRef, ContentModel.PROP_NAME).toString();
                    nodeService.addChild(workflowPackage, new NodeRef(item),
                        ContentModel.ASSOC_CONTAINS,
                        QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
                                QName.createValidLocalName(itemName)));
                });
    }

}
