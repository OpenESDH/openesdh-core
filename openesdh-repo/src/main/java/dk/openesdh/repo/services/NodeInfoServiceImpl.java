package dk.openesdh.repo.services;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint;
import org.alfresco.service.cmr.dictionary.Constraint;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * @author Torben Lauritzen.
 */
public class NodeInfoServiceImpl implements NodeInfoService {

    private NodeService nodeService;
    private DictionaryService dictionaryService;
    private PersonService personService;
    private NamespaceService namespaceService;

    @Override
    public NodeInfo getNodeInfo(NodeRef nodeRef) {
        NodeInfo nodeInfo = new NodeInfo();
        nodeInfo.properties = nodeService.getProperties(nodeRef);
        nodeInfo.aspects = nodeService.getAspects(nodeRef);
        nodeInfo.nodeClassName = nodeService.getType(nodeRef);
        return nodeInfo;
    }

    @Override
    public JSONObject buildJSON(NodeInfo nodeInfo) {
        JSONObject result = new JSONObject();
        try {
            result = getSelectedProperties(nodeInfo, nodeInfo.properties.keySet());

            JSONObject aspectsObj = new JSONObject();
            for (QName aspect : nodeInfo.aspects) {
                aspectsObj.put(aspect.toPrefixString(namespaceService), true);
            }
            result.put("aspects", aspectsObj);

            result.put(NODE_TYPE_PROPERTY, nodeInfo.nodeClassName.toPrefixString(namespaceService));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public JSONObject getSelectedProperties(NodeInfo nodeInfo, Collection<QName> objectProps) {
        JSONObject result = new JSONObject();
        JSONObject properties = new JSONObject();
        try {
            for (QName propertyQName : objectProps) {
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
        } else if (propertyDefinition == null) {
            valueObj.put("value", value.toString());
            valueObj.put("type", "String");
        } else if (propertyDefinition.getDataType().getName().equals(DataTypeDefinition.CATEGORY)) {
            valueObj = getCategoryValue((NodeRef) value);
        } else {
            valueObj.put("value", value.toString());
            valueObj.put("displayValue", getDisplayLabel(propertyDefinition, value));
            valueObj.put("type", "String");
        }

        if (propertyDefinition != null) {
            valueObj.put("label", propertyDefinition.getTitle(dictionaryService));
        }

        return valueObj;
    }

    private JSONObject getCategoryValue(NodeRef nodeRef) throws JSONException {
        JSONObject valueObj = new JSONObject();
        Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
        // TODO: Use dedicated category type, and render it in the client side
        valueObj.put("type", "String");
        valueObj.put("value", nodeRef);
        valueObj.put("displayValue", properties.get(ContentModel.PROP_NAME) + " " + properties.get(ContentModel.PROP_TITLE));
        return valueObj;
    }

    private JSONObject getPersonValue(String userName) throws JSONException {
        return getPersonsValue(new String[]{userName});
    }

    private JSONObject getPersonsValue(String[] userNames) throws JSONException {
        JSONObject valueObj = new JSONObject();
        String commaDelimitedUserNames = StringUtils.arrayToCommaDelimitedString(userNames);
        valueObj.put("type", "UserName");
        valueObj.put("value", commaDelimitedUserNames);

        ArrayList<String> fullNames = new ArrayList<>();
        List<String> nodeRefs = new ArrayList<>();
        for (String userName : userNames) {
            NodeRef personNodeRef = personService.getPerson(userName);
            nodeRefs.add(personNodeRef.toString());
            String firstName = (String) nodeService.getProperty(personNodeRef, ContentModel.PROP_FIRSTNAME);
            String lastName = (String) nodeService.getProperty(personNodeRef, ContentModel.PROP_LASTNAME);
            String fullName = StringUtils.isEmpty(lastName) ? firstName : firstName + " " + lastName;
            fullNames.add(fullName);
        }
        String commaDelimitedFullNames = StringUtils.collectionToDelimitedString(fullNames, ", ");
        valueObj.put("fullname", commaDelimitedFullNames);
        valueObj.put("nodeRef", new JSONArray(nodeRefs));

        return valueObj;
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
}
