package dk.openesdh.repo.webscripts.cases;

import dk.openesdh.repo.model.OpenESDHModel;
import org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.util.List;

public class PermittedStates extends AbstractWebScript {

    private DictionaryService dictionaryService;

    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        List<String> roleConstraints = (List<String>) this.dictionaryService.getConstraint(OpenESDHModel.CONSTRAINT_CASE_SIMPLE_STATUS).getConstraint().getParameters().get(ListOfValuesConstraint.ALLOWED_VALUES_PARAM);

        try {
            JSONArray json = buildJSON(roleConstraints);
            json.write(res.getWriter());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    JSONArray buildJSON(List<String> roles) throws
            JSONException {
        JSONArray result = new JSONArray();
        for (String role : roles) {
            result.put(role);
        }
        return result;
    }

}
