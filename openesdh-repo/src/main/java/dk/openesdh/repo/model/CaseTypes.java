package dk.openesdh.repo.model;

import java.io.IOException;
import java.util.Collection;

import dk.openesdh.repo.services.cases.CaseService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import dk.openesdh.repo.utils.Utils;

/**
 * Created by flemming on 8/19/14.
 */
public class CaseTypes extends AbstractWebScript {

    // Dependencies
    private CaseService caseService;
    private DictionaryService dictionaryService;

    public void setCaseService(CaseService caseService) {
        this.caseService = caseService;
    }
    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    public void execute(WebScriptRequest req, WebScriptResponse res)
            throws IOException {
        Collection<QName> caseTypes = dictionaryService.getSubTypes(OpenESDHModel.TYPE_CASE_BASE, true);

        // build a json object
        JSONArray arr = new JSONArray();

        for (QName caseType : caseTypes) {
            // skip the basetype - getSubTypes returns it together with the subtypes
            if (caseType.getLocalName().equals(OpenESDHModel.TYPE_BASE_NAME)) {
                continue;
            }
            try {
                JSONObject c = Utils.getCaseTypeJson(caseType, dictionaryService);
                c.put("createFormWidgets", caseService.getCaseCreateFormWidgets(type));
                arr.put(c);
            } catch (JSONException e) {
                throw new WebScriptException("Unable to serialize JSON");
            }
        }
        // build a JSON string and send it back
        String jsonString = arr.toString();
        res.getWriter().write(jsonString);
    }
}


