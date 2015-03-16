package dk.openesdh.repo.services;

import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.webscripts.cases.CaseInfo;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
        return nodeInfo;
    }

    @Override
    public JSONObject buildJSON(NodeInfo nodeInfo, CaseInfo caseInfo) {
        JSONObject result = new JSONObject();
        JSONObject properties = new JSONObject();
        try {
            for (Map.Entry<QName, Serializable> entry : nodeInfo
                    .properties.entrySet()) {
                Serializable value = entry.getValue();
                QName key = entry.getKey();
                JSONObject valueObj = new JSONObject();
                if (value != null) {
                    if (Date.class.equals(value.getClass())) {
                        valueObj.put("type", "Date");
                        valueObj.put("value", ((Date) value).getTime());
                    }
                    else if(key.getPrefixString().equals("modifier") || key.getPrefixString().equals("creator")) {
                        valueObj.put("type", "UserName");
                        valueObj.put("value", value);
                        NodeRef personNodeRef = personService.getPerson((String) value);
                        String firstName = (String) nodeService.getProperty(personNodeRef, ContentModel.PROP_FIRSTNAME);
                        String lastName = (String) nodeService.getProperty(personNodeRef, ContentModel.PROP_LASTNAME);
                        valueObj.put("fullname", firstName + " " + lastName);
                    } else {
                        valueObj.put("value", value);
                        valueObj.put("type", "String");
                    }

                    valueObj.put("label", dictionaryService.getProperty(key).getTitle(dictionaryService));
                    properties.put(entry.getKey().toPrefixString(namespaceService), valueObj);
                }
            }
            result.put("properties", properties);
            JSONObject aspectsObj = new JSONObject();
            for (QName aspect : nodeInfo.aspects) {
                aspectsObj.put(aspect.toPrefixString(namespaceService), true);
            }
            result.put("aspects", aspectsObj);
            result.put("isJournalized", nodeInfo.aspects.contains(OpenESDHModel.ASPECT_OE_JOURNALIZED));
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

            for(QName reqObject : objectProps){
                JSONObject valueObj = new JSONObject();
                Serializable value = nodeInfo.properties.get(reqObject);
                if (value != null) {
                    if (Date.class.equals(value.getClass())) {
                        valueObj.put("type", "Date");
                        valueObj.put("value", ((Date) value).getTime());
                    }
                    else if(reqObject.getPrefixString().equals("modifier") || reqObject.getPrefixString().equals("creator")) {
                        valueObj.put("type", "UserName");
                        valueObj.put("value", value);
                        NodeRef personNodeRef = personService.getPerson((String) value);
                        String firstName = (String) nodeService.getProperty(personNodeRef, ContentModel.PROP_FIRSTNAME);
                        String lastName = (String) nodeService.getProperty(personNodeRef, ContentModel.PROP_LASTNAME);
                        valueObj.put("fullname", firstName + " " + lastName);
                    } else {
                        valueObj.put("value", value);
                        valueObj.put("type", "String");
                    }

                    valueObj.put("label", dictionaryService.getProperty(reqObject).getTitle(dictionaryService));
                    properties.put(reqObject.toPrefixString(namespaceService), valueObj);
                }
                properties.put(reqObject.toPrefixString(namespaceService), valueObj);
            }
            result.put("properties", properties);

            result.put("isJournalized", nodeInfo.aspects.contains(OpenESDHModel.ASPECT_OE_JOURNALIZED));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }
}
