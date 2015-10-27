package dk.openesdh.repo.services.workflow;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.security.PersonService.PersonInfo;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.model.WorkflowInfo;
import dk.openesdh.repo.services.cases.CaseService;

@Service
public class CaseWorkflowServiceImpl implements CaseWorkflowService {

    @Autowired
    @Qualifier("WorkflowService")
    private WorkflowService workflowService;
    @Autowired
    private NodeService nodeService;
    @Autowired
    private NamespaceService namespaceService;
    @Autowired
    private DictionaryService dictionaryService;
    @Autowired
    private PersonService personService;
    @Autowired
    private AuthorityService authorityService;
    @Autowired
    private CaseService caseService;

    @Autowired
    @Qualifier("retryingTransactionHelper")
    protected RetryingTransactionHelper retryingTransactionHelper;

    @Override
    public WorkflowPath startWorkflow(WorkflowInfo workflow) {

        Map<QName, Serializable> workflowParams = getWorkflowParams(workflow);

        grantCaseAccessToWorkflowAssignees(workflowParams);

        return retryingTransactionHelper.doInTransaction(() -> {

            NodeRef workflowPackage = workflowService.createPackage(null);
            addItemsToWorkflowPackage(workflowPackage, workflow);
            workflowParams.put(WorkflowModel.ASSOC_PACKAGE, workflowPackage);

            WorkflowPath wfPath = workflowService.startWorkflow(workflow.getWorkflowType(), workflowParams);
            signalStartTask(wfPath);
            return wfPath;
        });
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
        params.put(WorkflowModel.PROP_WORKFLOW_PRIORITY, workflow.getPriority());

        params.putAll(parseProperties(workflow.getProperties()));

        return params;
    }

    protected boolean grantCaseAccessToWorkflowAssignees(Map<QName, Serializable> workflowParams) {
        return retryingTransactionHelper.doInTransaction(() -> {
            return grantCaseAccessToAssignee(workflowParams)
                || grantCaseAccessToAssignees(workflowParams)
                || grantCaseAccessToGroupAssignee(workflowParams) 
                || grantCaseAccessToGroupAssignees(workflowParams);
        });
    }

    protected boolean grantCaseAccessToAssignee(Map<QName, Serializable> workflowParams) {
        if (!workflowParams.containsKey(WorkflowModel.ASSOC_ASSIGNEE)) {
            return false;
        }

        NodeRef assigneeNodRef = (NodeRef) workflowParams.get(WorkflowModel.ASSOC_ASSIGNEE);
        PersonInfo assignee = personService.getPerson(assigneeNodRef);
        
        NodeRef caseNodeRef = getCaseNodeRef(workflowParams);

        if (!getCaseMembers(caseNodeRef).contains(assignee.getUserName())) {
            caseService.addAuthorityToRole(assigneeNodRef, getCaseReaderRole(caseNodeRef), caseNodeRef);
        }

        return true;
    }
    
    @SuppressWarnings("unchecked")
    protected boolean grantCaseAccessToAssignees(Map<QName, Serializable> workflowParams) {
        if (!workflowParams.containsKey(WorkflowModel.ASSOC_ASSIGNEES)) {
            return false;
        }

        NodeRef caseNodeRef = getCaseNodeRef(workflowParams);
        List<String> caseMembers = getCaseMembers(caseNodeRef);
        
        List<NodeRef> assigneeNodeRefs = (List<NodeRef>) workflowParams.get(WorkflowModel.ASSOC_ASSIGNEES);
        
        List<NodeRef> nonMembersAssignees = assigneeNodeRefs
                .stream()
                .filter(nodeRef -> !caseMembers.contains(personService.getPerson(nodeRef).getUserName()))
                .collect(Collectors.toList());
        
        if (!nonMembersAssignees.isEmpty()) {
            caseService.addAuthoritiesToRole(nonMembersAssignees, getCaseReaderRole(caseNodeRef), caseNodeRef);
        }

        return true;
    }

    protected boolean grantCaseAccessToGroupAssignee(Map<QName, Serializable> workflowParams) {
        if (!workflowParams.containsKey(WorkflowModel.ASSOC_GROUP_ASSIGNEE)) {
            return false;
        }

        NodeRef groupAssigneeNodeRef = (NodeRef) workflowParams.get(WorkflowModel.ASSOC_GROUP_ASSIGNEE);
        grantCaseAccessToPeople(getPeopleFromGroup(groupAssigneeNodeRef), workflowParams);

        return true;
    }

    @SuppressWarnings("unchecked")
    protected boolean grantCaseAccessToGroupAssignees(Map<QName, Serializable> workflowParams) {
        if (!workflowParams.containsKey(WorkflowModel.ASSOC_GROUP_ASSIGNEES)) {
            return false;
        }

        List<NodeRef> groupNodeRefs = (List<NodeRef>) workflowParams
                .get(WorkflowModel.ASSOC_GROUP_ASSIGNEES);

        Set<String> peopleNames = groupNodeRefs
                .stream()
                .flatMap(groupNodeRef -> getPeopleFromGroup(groupNodeRef).stream())
                .collect(Collectors.toSet());

        grantCaseAccessToPeople(peopleNames, workflowParams);
        return true;
    }

    protected void grantCaseAccessToPeople(Set<String> peopleNames, Map<QName, Serializable> workflowParams) {
        if (peopleNames.isEmpty()) {
            return;
        }

        NodeRef caseNodeRef = getCaseNodeRef(workflowParams);
        List<String> caseMembers = getCaseMembers(caseNodeRef);

        List<NodeRef> nonMembersAssignees = peopleNames
                .stream()
                .filter(name -> !caseMembers.contains(name))
                .map(name -> personService.getPerson(name))
                .collect(Collectors.toList());

        if (!nonMembersAssignees.isEmpty()) {
            caseService.addAuthoritiesToRole(nonMembersAssignees, getCaseReaderRole(caseNodeRef), caseNodeRef);
        }
    }

    protected Set<String> getPeopleFromGroup(NodeRef groupNodeRef) {
        String groupName = nodeService.getProperty(groupNodeRef, ContentModel.PROP_AUTHORITY_NAME).toString();
        return authorityService.getContainedAuthorities(AuthorityType.USER, groupName, true);
    }

    protected NodeRef getCaseNodeRef(Map<QName, Serializable> workflowParams) {
        String caseId = workflowParams.get(OpenESDHModel.PROP_OE_CASE_ID).toString();
        return caseService.getCaseById(caseId);
    }

    protected List<String> getCaseMembers(NodeRef caseNodeRef) {

        Map<String, Set<String>> caseMembers = caseService.getMembersByRole(caseNodeRef, true, true); 
        return caseMembers.keySet()
                .stream()
                .map(key -> caseMembers.get(key))
                .flatMap(members -> members.stream())
                .collect(Collectors.toList());
    }
    
    protected String getCaseReaderRole(NodeRef caseNodeRef) {
        return caseService.getRoles(caseNodeRef)
                .stream()
                .filter(role -> role.contains(CaseService.READER))
                .findAny()
                .get();
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
