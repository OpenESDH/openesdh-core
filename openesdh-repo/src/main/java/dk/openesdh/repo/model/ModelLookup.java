package dk.openesdh.repo.model;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by flemming on 8/19/14.
 */
public class ModelLookup extends AbstractWebScript {

    // Dependencies
    private DictionaryService dictionaryService;
    private NamespaceService namespaceService;

    public void setNamespaceService(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }

    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    public void execute(WebScriptRequest req, WebScriptResponse res)
            throws IOException  {
        String typeParameter = req.getParameter("type");

        if (typeParameter == null) {
            throw new AlfrescoRuntimeException("Must specify a type " +
                    "parameter");
        }

        TypeDefinition modelType = getTypeDefinition(typeParameter);

        JSONObject mainObject = new JSONObject();

        Map<String, JSONObject> properties = getProperties(modelType);

        try {
            mainObject.put("properties", properties);
        } catch (JSONException e) {
            throw new WebScriptException("Unable to serialize JSON");
        }

        Map<String, JSONObject> associations = getAssociations(modelType);

        try {
            mainObject.put("associations", associations);
        } catch (JSONException e) {
            throw new WebScriptException("Unable to serialize JSON");
        }


        String jsonString = mainObject.toString();
        res.getWriter().write(jsonString);
    }

    TypeDefinition getTypeDefinition(String typeParameter) {
        return dictionaryService.getType(QName.createQName(typeParameter,
                namespaceService));
    }

    Map<String, JSONObject> getAssociations(TypeDefinition modelType) {
        Map<String, JSONObject> associations = new HashMap<String, JSONObject>();
        Iterator<Map.Entry<QName, AssociationDefinition>> it = modelType.getAssociations().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<QName, AssociationDefinition> pairs = it.next();
            JSONObject name = new JSONObject();
            try {
                name.put("isSourceMany", (pairs.getValue()).isSourceMany());
                name.put("isTargetMany", (pairs.getValue()).isTargetMany());
                name.put("title", pairs.getValue()
                        .getTitle(dictionaryService));

            } catch (JSONException e) {
                throw new WebScriptException("Unable to serialize JSON");
            }
            associations.put(pairs.getKey().toPrefixString(namespaceService), name);

        }
        return associations;
    }

    Map<String, JSONObject> getProperties(TypeDefinition modelType) {
        Map<String, JSONObject> properties = new HashMap<String, JSONObject>();
        Iterator<Map.Entry<QName, PropertyDefinition>> it = modelType.getProperties().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<QName, PropertyDefinition> pairs = it.next();
            JSONObject dataType = new JSONObject();
            try {
                dataType.put("type", pairs.getValue()
                        .getDataType().getName().toPrefixString(namespaceService));
                dataType.put("title", pairs.getValue()
                        .getTitle(dictionaryService));
                dataType.put("isMultiValued", pairs.getValue().isMultiValued());
            } catch (JSONException e) {
                throw new WebScriptException("Unable to serialize JSON");
            }
            properties.put(pairs.getKey().toPrefixString(namespaceService),
                    dataType);
        }
        return properties;
    }
}