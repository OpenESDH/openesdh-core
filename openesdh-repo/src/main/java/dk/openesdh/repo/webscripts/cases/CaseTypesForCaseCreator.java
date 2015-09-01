package dk.openesdh.repo.webscripts.cases;

import java.io.IOException;
import java.util.Collection;

import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.QName;
import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.security.access.AccessDeniedException;

import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.cases.CaseService;
import dk.openesdh.repo.utils.Utils;

public class CaseTypesForCaseCreator extends AbstractWebScript {

    // Dependencies
    private DictionaryService dictionaryService;
    private CaseService caseService;

    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    public void setCaseService(CaseService caseService) {
        this.caseService = caseService;
    }

    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        Collection<QName> caseTypes = dictionaryService.getSubTypes(OpenESDHModel.TYPE_CASE_BASE, true);

        // build a json object
        JSONArray arr = new JSONArray();

        for (QName caseType : caseTypes) {

            if (!canCreateCaseOfType(caseType)) {
                continue;
            }

            try {
                arr.put(Utils.getCaseTypeJson(caseType, dictionaryService, caseService));
            } catch (JSONException e) {
                throw new WebScriptException("Unable to serialize JSON");
            }
        }
        // build a JSON string and send it back
        String jsonString = arr.toString();
        res.getWriter().write(jsonString);
    }

    public boolean canCreateCaseOfType(QName caseType) {
        // skip the basetype - getSubTypes returns it together with the
        // subtypes
        if (caseType.getLocalName().equals(OpenESDHModel.TYPE_BASE_NAME)) {
            return false;
        }

        try {
            caseService.checkCaseCreatorPermissions(caseType);
        } catch (AccessDeniedException ex) {
            return false;
        }

        return true;
    }

}
