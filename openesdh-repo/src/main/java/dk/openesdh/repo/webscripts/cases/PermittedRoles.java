package dk.openesdh.repo.webscripts.cases;

import dk.openesdh.repo.model.OpenESDHModel;
import java.io.IOException;
import java.util.List;
import org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

public class PermittedRoles extends AbstractWebScript {

    private DictionaryService dictionaryService;

    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        List<String> roleConstraints = (List<String>) this.dictionaryService
                .getConstraint(OpenESDHModel.CONSTRAINT_CASE_ALLOWED_PARTY_ROLES)
                .getConstraint()
                .getParameters()
                .get(ListOfValuesConstraint.ALLOWED_VALUES_PARAM);

        try {
            JSONArray json = buildJSON(roleConstraints);
            json.write(res.getWriter());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    JSONArray buildJSON(List<String> roles) throws JSONException {
        return new JSONArray(roles);
    }

}
