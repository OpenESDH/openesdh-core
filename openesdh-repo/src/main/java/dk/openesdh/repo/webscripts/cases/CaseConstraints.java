package dk.openesdh.repo.webscripts.cases;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import dk.openesdh.repo.services.cases.CaseService;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

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
        final String caseModelName = "caseModel";
        res.setContentEncoding(WebScriptUtils.CONTENT_ENCODING_UTF_8);
        JSONObject jsonResponse = new JSONObject();

        Collection<QName> allModels = dictionaryService.getAllModels();
        List<QName> oeCaseModels = new ArrayList<>();

        //Search the list of models for subtypes of the base case (base:case)
        for(QName model : allModels){
            String modelName = model.getLocalName();
            if(modelName.equals(caseModelName))
                oeCaseModels.add(model);
        }

        for(QName oeModel : oeCaseModels){
            String modelPrefix = StringUtils.substringBefore(oeModel.getPrefixString(), ":");
            Collection<ConstraintDefinition> typeConstraints = this.dictionaryService.getConstraints(oeModel);
            try {
                for(ConstraintDefinition constraint : typeConstraints){
                    //This only works if we stick to the rigorous naming convention of adding "Constraint" to the end
                    //of our constraints.
                    if(constraint.getName().getLocalName().contains("Constraint")) {
                        JSONArray values = caseService.buildConstraintsJSON(constraint);
                        JSONObject tmp = new JSONObject().put(constraint.getName().getLocalName(),values);
                        jsonResponse.put(modelPrefix, tmp);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        try {
            jsonResponse.write(res.getWriter());
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

    }

}
