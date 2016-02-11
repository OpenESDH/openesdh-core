package dk.openesdh.repo.webscripts.cases;

import java.io.IOException;
import java.util.List;

import org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.utils.JSONArrayCollector;

public class PermittedStates extends AbstractWebScript {

    private DictionaryService dictionaryService;

    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        List<String> roleConstraints = (List<String>) this.dictionaryService
                .getConstraint(OpenESDHModel.CONSTRAINT_CASE_BASE_STATUS)
                .getConstraint()
                .getParameters()
                .get(ListOfValuesConstraint.ALLOWED_VALUES_PARAM);
        roleConstraints
                .stream()
                .collect(JSONArrayCollector.simple())
                .writeJSONString(res.getWriter());
    }

}
