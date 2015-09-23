package dk.openesdh.repo.services.workflow;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import dk.openesdh.repo.model.WorkflowInfo;

public class CaseWorkflowServiceImpl implements CaseWorkflowService {

    private WorkflowService workflowService;
    private NodeService nodeService;
    private NamespaceService namespaceService;
    private DictionaryService dictionaryService;

    public void setWorkflowService(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setNamespaceService(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }

    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    @Override
    public WorkflowPath startWorkflow(WorkflowInfo workflow) {

        NodeRef workflowPackage = workflowService.createPackage(null);
        addItemsToWorkflowPackage(workflowPackage, workflow);

        Map<QName, Serializable> params = getWorkflowParams(workflow);
        params.put(WorkflowModel.ASSOC_PACKAGE, workflowPackage);
        
        WorkflowPath wfPath = workflowService.startWorkflow(workflow.getWorkflowType(), params);
        signalStartTask(wfPath);
        return wfPath;
    }

    protected void signalStartTask(WorkflowPath path) {
        WorkflowTask startTask = workflowService.getStartTask(path.getInstance().getId());
        if (startTask != null) {
            workflowService.endTask(startTask.getId(), null);
        }
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

        params.putAll(parseProperties(workflow.getProperties()));

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

    protected Map<QName, Serializable> parseProperties(Map<String, Object> properties) {
        Map<QName, Serializable> props = new HashMap<QName, Serializable>();
        if (properties.keySet().isEmpty()) {
            return props;
        }

        for (String name : properties.keySet()) {
            QName key = QName.createQName(name.replaceFirst("_", ":"), namespaceService);
            Serializable value = parsePropertyValue(key, properties.get(name));
            props.put(key, value);
        }
        return props;
    }

    protected Serializable parsePropertyValue(QName name, Object value) {
        if (value == null) {
            return null;
        }

        Serializable result = parsePropertyValueByDictionaryService(name, value);
        if (result != null) {
            return result;
        }
        
        result = parseArrayOrCollection(name, value);
        if (result != null) {
            return result;
        }

        return (Serializable) value;
    }

    protected Serializable parsePropertyValueByDictionaryService(QName name, Object value){
        PropertyDefinition prop = dictionaryService.getProperty(name);
        if(prop == null){
            return null;
        }
        
        if (!prop.isMultiValued() || (!value.getClass().isArray() && !(value instanceof Collection<?>))) {
            return (Serializable) DefaultTypeConverter.INSTANCE.convert(prop.getDataType(), value);
        }
        
        Collection<?> values = toCollection(value);

        return (Serializable) values.stream()
                .map(val -> (Serializable) DefaultTypeConverter.INSTANCE.convert(prop.getDataType(), val))
                .collect(Collectors.toList());
    }
    
    protected Serializable parseArrayOrCollection(QName name, Object value){
        if(!value.getClass().isArray() && !(value instanceof Collection<?>)){
            return null;
        }

        Collection<?> values = toCollection(value);

        return (Serializable) values.stream()
                .map(val -> val.toString())
                .collect(Collectors.toList());
    }

    protected Collection<?> toCollection(Object value) {
        if (value instanceof Collection<?>) {
            return (Collection<?>) value;
        }
        return Arrays.asList((Object[]) value);
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
