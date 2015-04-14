package dk.openesdh.repo.webscripts.cases;

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
import java.util.Collection;
import java.util.List;

public class CaseConstraints extends AbstractWebScript {

    private DictionaryService dictionaryService;

    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        Collection<ConstraintDefinition> typeConstraints = this.dictionaryService.getConstraints(OpenESDHModel.CASE_MODEL);
        List<String> constraintValues;
        int i = 0;
        JSONObject jsonResponse = new JSONObject();

        res.setContentEncoding("UTF-8");
        try {
            for(ConstraintDefinition constraint : typeConstraints){
                //This only works if we stick to the rigorous naming convention of adding "Constraint" to the end
                //of our constraints.
                if(constraint.getName().getLocalName().contains("Constraint")) {
                    JSONArray values = buildConstraintsJSON(constraint);
                    jsonResponse.put(constraint.getName().getLocalName(),values);
                   // System.out.println(++i + " -> " + constraint.getName().getLocalName());
                }
            }
            jsonResponse.write(res.getWriter());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    JSONArray buildConstraintsJSON(ConstraintDefinition constraint) throws
            JSONException {
        JSONArray result = new JSONArray();
        JSONObject lvPair;

        List<String> constraintValues = (List<String>)constraint.getConstraint().getParameters().get(ListOfValuesConstraint.ALLOWED_VALUES_PARAM);
        for (String constraintValue : constraintValues) {
            lvPair = new JSONObject();
            lvPair.put("label", ((ListOfValuesConstraint)constraint.getConstraint()).getDisplayLabel(constraintValue, dictionaryService));
            lvPair.put("value", constraintValue);
            result.put(lvPair);
        }
        return result;
    }

}
