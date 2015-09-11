package dk.openesdh.repo.services;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.DictionaryModelType;
import org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint;
import org.alfresco.service.cmr.dictionary.*;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

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
        
        nodeInfo.properties.put(OpenESDHModel.ASSOC_CASE_OWNERS, getCaseOwnerUserNames(nodeRef));
        
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

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    private JSONObject getNodePropertyValue(NodeInfo nodeInfo, QName propertyQName) throws JSONException {

        if (OpenESDHModel.ASSOC_CASE_OWNERS.equals(propertyQName)) {
            return getCaseOwners(nodeInfo);
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
        } else if (propertyDefinition.getDataType().getName().equals(DataTypeDefinition.CATEGORY)) {
            valueObj = getCategoryValue((NodeRef) value);
        } else {
            valueObj.put("value", value.toString());
            valueObj.put("displayValue", getDisplayLabel(propertyDefinition, value));
            valueObj.put("type", "String");
        }

        valueObj.put("label", propertyDefinition.getTitle(dictionaryService));

        return valueObj;
    }

    private JSONObject getCategoryValue(NodeRef nodeRef) throws JSONException {
        JSONObject valueObj = new JSONObject();
        Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
        // TODO: Use dedicated category type, and render it in the client side
        valueObj.put("type", "String");
        valueObj.put("value", properties.get(ContentModel.PROP_NAME) + " " + properties.get(ContentModel.PROP_TITLE));
        return valueObj;
    }

    private JSONObject getCaseOwners(NodeInfo nodeInfo) throws JSONException {
        Serializable caseOwnerUserNames = nodeInfo.properties.get(OpenESDHModel.ASSOC_CASE_OWNERS);
        if (caseOwnerUserNames == null) {
            return new JSONObject();
        }
        JSONObject caseOwners = getPersonsValue((String[]) caseOwnerUserNames);
        AssociationDefinition assocDef = dictionaryService.getAssociation(OpenESDHModel.ASSOC_CASE_OWNERS);
        caseOwners.put("label", assocDef.getTitle(dictionaryService));
        return caseOwners;
    }

    private JSONObject getPersonValue(String userName) throws JSONException {
        return getPersonsValue(new String[] { userName });
    }

    private JSONObject getPersonsValue(String[] userNames) throws JSONException {
        JSONObject valueObj = new JSONObject();
        String commaDelimitedUserNames = StringUtils.arrayToCommaDelimitedString(userNames);
        valueObj.put("type", "UserName");
        valueObj.put("value", commaDelimitedUserNames);

        ArrayList<String> fullNames = new ArrayList<String>();
        for (String userName : userNames) {
            NodeRef personNodeRef = personService.getPerson(userName);
            String firstName = (String) nodeService.getProperty(personNodeRef, ContentModel.PROP_FIRSTNAME);
            String lastName = (String) nodeService.getProperty(personNodeRef, ContentModel.PROP_LASTNAME);
            String fullName = StringUtils.isEmpty(lastName) ? firstName : firstName + " " + lastName;
            fullNames.add(fullName);
        }
        String commaDelimitedFullNames = StringUtils.collectionToDelimitedString(fullNames, ", ");
        valueObj.put("fullname", commaDelimitedFullNames);

        return valueObj;
    }

    private String[] getCaseOwnerUserNames(NodeRef nodeRef) {

        List<AssociationRef> caseOwnersAssocList = nodeService.getTargetAssocs(nodeRef,
                OpenESDHModel.ASSOC_CASE_OWNERS);

        if (CollectionUtils.isEmpty(caseOwnersAssocList)) {
            return new String[0];
        }

        ArrayList<String> caseOwnerUserNames = new ArrayList<String>();
        for (AssociationRef caseOwnerAssoc : caseOwnersAssocList) {
            String caseOwnerUserName = personService.getPerson(caseOwnerAssoc.getTargetRef()).getUserName();
            caseOwnerUserNames.add(caseOwnerUserName);
        }

        return caseOwnerUserNames.toArray(new String[0]);
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
