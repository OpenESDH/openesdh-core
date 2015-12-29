package dk.openesdh.repo.webscripts.cases;

import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import com.github.dynamicextensionsalfresco.webscripts.resolutions.Resolution;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.cases.CaseService;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

@Component
@WebScript(description = "Returns all case constraints separated into different array within the structure", families = {"Case Tools"})
public class CaseConstraintsWebScript {

    @Autowired
    private DictionaryService dictionaryService;
    @Autowired
    private CaseService caseService;

    @Uri(value = "/api/openesdh/case/constraints", method = HttpMethod.GET, defaultFormat = "json")
    public Resolution get() throws IOException {
        JSONObject jsonResponse = new JSONObject();

        //Search the list of models for subtypes of the base case (base:case)
        List<QName> oeCaseModels = dictionaryService.getAllModels()
                .stream()
                .filter(model -> model.getLocalName().equals(OpenESDHModel.CASE_MODEL_NAME))
                .collect(Collectors.toList());

        for (QName oeModel : oeCaseModels) {
            String modelPrefix = StringUtils.substringBefore(oeModel.getPrefixString(), ":");
            Collection<ConstraintDefinition> typeConstraints = this.dictionaryService.getConstraints(oeModel);
            try {
                for (ConstraintDefinition constraint : typeConstraints) {
                    //This only works if we stick to the rigorous naming convention of adding "Constraint" to the end
                    //of our constraints.
                    if (constraint.getName().getLocalName().contains("Constraint")) {
                        JSONArray values = caseService.buildConstraintsJSON(constraint);
                        JSONObject tmp = new JSONObject().put(constraint.getName().getLocalName(), values);
                        jsonResponse.put(modelPrefix, tmp);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return WebScriptUtils.jsonResolution(jsonResponse);
    }
}
