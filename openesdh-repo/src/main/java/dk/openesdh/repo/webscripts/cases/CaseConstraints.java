package dk.openesdh.repo.webscripts.cases;

import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.cases.CaseService;
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

public class CaseConstraints extends AbstractWebScript {

    //<editor-fold desc="Injected services and setters">
    private DictionaryService dictionaryService;
    private CaseService caseService;

    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    public void setCaseService(CaseService caseService) {
        this.caseService = caseService;
    }
    //</editor-fold>

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        res.setContentEncoding("UTF-8");
        Collection<ConstraintDefinition> typeConstraints = this.dictionaryService.getConstraints(OpenESDHModel.CASE_MODEL);
        JSONObject jsonResponse = new JSONObject();

        try {
            for(ConstraintDefinition constraint : typeConstraints){
                //This only works if we stick to the rigorous naming convention of adding "Constraint" to the end
                //of our constraints.
                if(constraint.getName().getLocalName().contains("Constraint")) {
                    JSONArray values = caseService.buildConstraintsJSON(constraint);
                    jsonResponse.put(constraint.getName().getLocalName(),values);
                }
            }
            jsonResponse.write(res.getWriter());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
