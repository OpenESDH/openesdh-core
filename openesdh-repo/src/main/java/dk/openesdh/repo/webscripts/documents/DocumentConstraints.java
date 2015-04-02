package dk.openesdh.repo.webscripts.documents;

import dk.openesdh.repo.model.OpenESDHModel;
import org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.util.*;

public class DocumentConstraints extends AbstractWebScript {

    private DictionaryService dictionaryService;

    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        Collection<ConstraintDefinition> typeConstraints = this.dictionaryService.getConstraints(OpenESDHModel.DOCUMENT_MODEL);
        List<String> constraintValues;
        int i = 0;
        JSONObject jsonResponse = new JSONObject();

        try {
            for(ConstraintDefinition constraint : typeConstraints){
                //This only works if we stick to the rigorous naming convention of adding "Constraint" to the end
                //of our constraints.
                if(constraint.getName().getLocalName().contains("Constraint")) {
                    constraintValues = (List<String>)constraint.getConstraint().getParameters().get(ListOfValuesConstraint.ALLOWED_VALUES_PARAM);
                    JSONArray values = buildJSON(constraintValues);
                    jsonResponse.put(constraint.getName().getLocalName(),values);
                   // System.out.println(++i + " -> " + constraint.getName().getLocalName());
                }
            }
            jsonResponse.write(res.getWriter());
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
