package dk.openesdh.repo.services;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.Constraint;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.util.CollectionUtils;

import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.webscripts.cases.CaseInfo;

/**
 * @author Torben Lauritzen.
 */
public class NodeInfoServiceImpl implements NodeInfoService {

    private NodeService nodeService;
    private DictionaryService dictionaryService;
    private PersonService personService;

    private NamespaceService namespaceService;
    public void setNamespaceService(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }

    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    @Override
    public NodeInfo getNodeInfo(NodeRef nodeRef) {
        NodeInfo nodeInfo = new NodeInfo();
        nodeInfo.properties = nodeService.getProperties(nodeRef);
        nodeInfo.aspects = nodeService.getAspects(nodeRef);
        nodeInfo.nodeClassName = nodeService.getType(nodeRef);
        
        nodeInfo.properties.put(OpenESDHModel.ASSOC_CASE_OWNERS, getCaseOwnerUserName(nodeRef));
        
        return nodeInfo;
    }

    @Override
    public JSONObject buildJSON(NodeInfo nodeInfo, CaseInfo caseInfo) {
        JSONObject result = new JSONObject();
        try {

            ArrayList<QName> propertiesToRetrieve = new ArrayList<QName>(nodeInfo.properties.keySet());
            propertiesToRetrieve.add(OpenESDHModel.ASSOC_CASE_OWNERS);

            result = getSelectedProperties(nodeInfo, caseInfo, propertiesToRetrieve);

            JSONObject aspectsObj = new JSONObject();
            for (QName aspect : nodeInfo.aspects) {
                aspectsObj.put(aspect.toPrefixString(namespaceService), true);
            }
            result.put("aspects", aspectsObj);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public JSONObject getSelectedProperties(NodeInfo nodeInfo, CaseInfo caseInfo, List<QName>objectProps){
        JSONObject result = new JSONObject();
        JSONObject properties = new JSONObject();

        try {

            for(QName propertyQName : objectProps){
                JSONObject valueObj = getNodePropertyValue(nodeInfo, propertyQName);
                properties.put(propertyQName.toPrefixString(namespaceService), valueObj);
            }
            result.put("properties", properties);
            result.put("isJournalized", nodeInfo.aspects.contains(OpenESDHModel.ASPECT_OE_JOURNALIZED));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    private JSONObject getNodePropertyValue(NodeInfo nodeInfo, QName propertyQName) throws JSONException {

        if (OpenESDHModel.ASSOC_CASE_OWNERS.equals(propertyQName)) {
            return getCaseOwner(nodeInfo);
        }

        JSONObject valueObj = new JSONObject();
        Serializable value = nodeInfo.properties.get(propertyQName);

        if (value == null) {
            return valueObj;
        }

        PropertyDefinition propertyDefinition = getPropertyDefinition(nodeInfo, propertyQName);

        if (Date.class.equals(value.getClass())) {
            valueObj.put("type", "Date");
            valueObj.put("value", ((Date) value).getTime());
        } else if (propertyQName.getPrefixString().equals("modifier") || propertyQName.getPrefixString().equals("creator")) {
            valueObj = getPersonValue((String) value);
        } else {
            valueObj.put("value", getDisplayLabel(propertyDefinition, value));
            valueObj.put("type", "String");
        }

        valueObj.put("label", propertyDefinition.getTitle(dictionaryService));

        return valueObj;
    }

    private JSONObject getCaseOwner(NodeInfo nodeInfo) throws JSONException {
        Serializable caseOwnerUserName = nodeInfo.properties.get(OpenESDHModel.ASSOC_CASE_OWNERS);
        if (caseOwnerUserName == null) {
            return new JSONObject();
        }
        JSONObject caseOwner = getPersonValue((String) caseOwnerUserName);
        AssociationDefinition assocDef = dictionaryService.getAssociation(OpenESDHModel.ASSOC_CASE_OWNERS);
        caseOwner.put("label", assocDef.getTitle(dictionaryService));
        return caseOwner;
    }

    private JSONObject getPersonValue(String userName) throws JSONException {
        JSONObject valueObj = new JSONObject();
        valueObj.put("type", "UserName");
        valueObj.put("value", userName);
        NodeRef personNodeRef = personService.getPerson(userName);
        String firstName = (String) nodeService.getProperty(personNodeRef, ContentModel.PROP_FIRSTNAME);
        String lastName = (String) nodeService.getProperty(personNodeRef, ContentModel.PROP_LASTNAME);
        valueObj.put("fullname", firstName + " " + lastName);
        return valueObj;
    }

    private String getCaseOwnerUserName(NodeRef nodeRef) {
        List<AssociationRef> caseOwnersAssocList = nodeService.getTargetAssocs(nodeRef,
                OpenESDHModel.ASSOC_CASE_OWNERS);
        if (CollectionUtils.isEmpty(caseOwnersAssocList)) {
            return null;
        }
        NodeRef personNodeRef = caseOwnersAssocList.get(0).getTargetRef();
        return personService.getPerson(personNodeRef).getUserName();
    }

    private PropertyDefinition getPropertyDefinition(NodeInfo nodeInfo, QName propertyQName) {

        PropertyDefinition propertyDefinition = dictionaryService
                .getProperty(nodeInfo.nodeClassName, propertyQName);
        if (propertyDefinition == null) {
            propertyDefinition = dictionaryService.getProperty(propertyQName);
        }

        return propertyDefinition;
    }

    private Serializable getDisplayLabel(PropertyDefinition propertyDefinition, Serializable value) {

        List<ConstraintDefinition> constraints = propertyDefinition.getConstraints();
        if (CollectionUtils.isEmpty(constraints)) {
            return value;
        }

        for (ConstraintDefinition constraintDef : constraints) {
            Constraint constraint = constraintDef.getConstraint();
            if (constraint instanceof ListOfValuesConstraint) {
                return ((ListOfValuesConstraint) constraint).getDisplayLabel((String) value, dictionaryService);
            }
        }

        return value;
    }
}
