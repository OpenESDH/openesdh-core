package dk.openesdh.repo.model;

import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by flemming on 8/19/14.
 */
public class CaseModel extends AbstractWebScript {

    // Dependencies
    private DictionaryService dictionaryService;


    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    public void execute(WebScriptRequest req, WebScriptResponse res)
            throws IOException
    {

        TypeDefinition simpleCase = dictionaryService.getType(OpenESDHModel.TYPE_CASE_SIMPLE);

        Iterator it = simpleCase.getProperties().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            System.out.println(pairs.getKey() + " = " + ((PropertyDefinition)pairs.getValue()).getDataType());
        }
    }
}
