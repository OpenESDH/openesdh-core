package dk.openesdh.repo.model;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.namespace.QName;
import org.json.JSONArray;
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

    private String typeParameter;
    private String uriParameter;


    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    public void execute(WebScriptRequest req, WebScriptResponse res)
            throws IOException  {

        typeParameter = req.getParameter("type");
        uriParameter = req.getParameter("uri");

        if (typeParameter == null || uriParameter == null) {
            throw new AlfrescoRuntimeException("Must specify a type and uri parameter - ex. type=simpel and uri=http://openesdh.dk/model/case/1.0/");
        }

        TypeDefinition modelType = dictionaryService.getType(QName.createQName(uriParameter, typeParameter));

        Map properties = new HashMap();
        JSONObject mainObject = new JSONObject();

        Iterator it = modelType.getProperties().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            JSONObject dataType = new JSONObject();
            try {
                dataType.put("type", ((PropertyDefinition) pairs.getValue()).getDataType().toString());
                dataType.put("isMultiValued", ((PropertyDefinition) pairs.getValue()).isMultiValued());
            } catch (JSONException e) {
                throw new WebScriptException("Unable to serialize JSON");
            }
            properties.put(pairs.getKey().toString(), dataType);
        }

        try {
            mainObject.put("properties", properties);
        } catch (JSONException e) {
            throw new WebScriptException("Unable to serialize JSON");
        }

        Map associations = new HashMap();
        it = modelType.getAssociations().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            JSONObject name = new JSONObject();
            try {
                name.put("isSourceMany", ((AssociationDefinition)pairs.getValue()).isSourceMany());
                name.put("isTargetMany", ((AssociationDefinition)pairs.getValue()).isTargetMany());

            } catch (JSONException e) {
                throw new WebScriptException("Unable to serialize JSON");
            }
            associations.put(pairs.getKey().toString(), name);

        }

        try {
            mainObject.put("associations", associations);
        } catch (JSONException e) {
            throw new WebScriptException("Unable to serialize JSON");
        }


        String jsonString = mainObject.toString();
        res.getWriter().write(jsonString);
    }
}