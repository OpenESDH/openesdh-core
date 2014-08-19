package dk.openesdh.repo.model;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.ModelDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by flemming on 8/19/14.
 */
public class CaseTypes extends AbstractWebScript {

    // Dependencies
    private DictionaryService dictionaryService;


    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    public void execute(WebScriptRequest req, WebScriptResponse res)
            throws IOException
    {
        Collection<QName> caseTypes = dictionaryService.getSubTypes(OpenESDHModel.TYPE_CASE_BASE,true);

        ModelDefinition modelDefinition = dictionaryService.getModel(QName.createQName(OpenESDHModel.CASE_URI, "caseModel"));

        Collection<QName> properties = dictionaryService.getProperties(QName.createQName(OpenESDHModel.CASE_URI,"caseModel"));

        TypeDefinition simpleCase = dictionaryService.getType(OpenESDHModel.TYPE_CASE_SIMPLE);


        try
        {


            Iterator iterator = simpleCase.getProperties().entrySet().iterator();
            Iterator it = simpleCase.getProperties().entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pairs = (Map.Entry) it.next();
                 System.out.println(pairs.getKey() + " = " + pairs.getValue());
            }



            // build a json object
            JSONObject obj = new JSONObject();

            for (QName c : properties) {

                // System.out.println(c.getPrefixString());

                // put some data on it
                PropertyDefinition def = dictionaryService.getProperty(c);
                obj.put("name", "property:" + c + "  " + "type:" + def);
                // build a JSON string and send it back
                String jsonString = obj.toString();
                res.getWriter().write(jsonString);
            }
        }
        catch(JSONException e)
        {
            throw new WebScriptException("Unable to serialize JSON");
        }
    }
}


