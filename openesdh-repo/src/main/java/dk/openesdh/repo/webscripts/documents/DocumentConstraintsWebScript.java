package dk.openesdh.repo.webscripts.documents;

import java.util.Collection;
import java.util.stream.Collectors;

import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import com.github.dynamicextensionsalfresco.webscripts.resolutions.Resolution;

import dk.openesdh.repo.model.DocumentCategory;
import dk.openesdh.repo.model.DocumentType;
import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.cases.CaseService;
import dk.openesdh.repo.services.documents.DocumentCategoryService;
import dk.openesdh.repo.services.documents.DocumentTypeService;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

@Component
@WebScript(description = "Returns all document constraints separated into different array within the structure", families = {"Case Document Tools"})
public class DocumentConstraintsWebScript {

    @Autowired
    private DictionaryService dictionaryService;
    @Autowired
    private CaseService caseService;
    @Autowired
    @Qualifier("DocumentTypeService")
    private DocumentTypeService documentTypeService;
    @Autowired
    @Qualifier("DocumentCategoryService")
    private DocumentCategoryService documentCategoryService;

    @Uri(value = "/api/openesdh/case/document/constraints", method = HttpMethod.GET, defaultFormat = "json")
    public Resolution get() {
        Collection<ConstraintDefinition> typeConstraints = this.dictionaryService.getConstraints(OpenESDHModel.DOCUMENT_MODEL);
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

            //documentTypes
            jsonResponse.put("documentTypes", new JSONArray(documentTypeService.getClassifValues()
                    .stream()
                    .map(DocumentType::toJSONObject)
                    .collect(Collectors.toList())));

            //documentCategories
            jsonResponse.put("documentCategories", new JSONArray(documentCategoryService.getClassifValues()
                    .stream()
                    .map(DocumentCategory::toJSONObject)
                    .collect(Collectors.toList())));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return WebScriptUtils.jsonResolution(jsonResponse);
    }

}
