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
import org.json.JSONArray;
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

        try
        {
            // build a json object
            JSONArray arr = new JSONArray();


            for (QName caseType : caseTypes) {
                JSONObject c = new JSONObject();
                c.put("NamespaceURI", caseType.getNamespaceURI());
                c.put("Prefix", caseType.getPrefixString());
                c.put("Name", caseType.getLocalName());

                arr.put(c);
            }
            // build a JSON string and send it back
            String jsonString = arr.toString();
            res.getWriter().write(jsonString);
        }
        catch(JSONException e)
        {
            throw new WebScriptException("Unable to serialize JSON");
        }
    }
}


