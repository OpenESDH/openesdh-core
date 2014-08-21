package dk.openesdh.repo.model;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.namespace.QName;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by flemming on 8/19/14.
 */
public class CaseModel extends AbstractWebScript {

    // Dependencies
    private DictionaryService dictionaryService;

    private String caseTypeParameter;


    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    public void execute(WebScriptRequest req, WebScriptResponse res)
            throws IOException
    {

        caseTypeParameter = req.getParameter("caseType");
        if (caseTypeParameter == null) {
            throw new AlfrescoRuntimeException("Must specify a caseType parameter");
        }

        TypeDefinition caseType = dictionaryService.getType(QName.createQName(OpenESDHModel.CASE_URI, caseTypeParameter));

        JSONObject obj = new JSONObject();

        Iterator it = caseType.getProperties().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            try {
                obj.put(pairs.getKey().toString(), ((PropertyDefinition)pairs.getValue()).getDataType().toString());
            } catch (JSONException e) {
                throw new WebScriptException("Unable to serialize JSON");
            }
        }
        String jsonString = obj.toString();
        res.getWriter().write(jsonString);
    }
}