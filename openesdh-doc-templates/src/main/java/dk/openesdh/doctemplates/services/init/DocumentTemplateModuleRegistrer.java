package dk.openesdh.doctemplates.services.init;

import java.util.List;

import javax.annotation.PostConstruct;

import org.alfresco.model.ContentModel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import dk.openesdh.doctemplates.model.DocumentTemplateInfo;
import dk.openesdh.doctemplates.services.documenttemplate.DocumentTemplateService;
import dk.openesdh.repo.services.search.LiveSearchComponent;
import dk.openesdh.repo.services.search.LiveSearchService;

@Component
public class DocumentTemplateModuleRegistrer {

    @Autowired
    @Qualifier("LiveSearchService")
    private LiveSearchService liveSearchService;
    @Autowired
    @Qualifier("DocumentTemplateService")
    private DocumentTemplateService documentTemplateService;

    @PostConstruct
    public void init() {
        liveSearchService.registerComponent("templates", createDocTemplatesSearchComponent());
    }

    private LiveSearchComponent createDocTemplatesSearchComponent() {
        return new LiveSearchComponent() {
            @Override
            public JSONArray search(String query, int size) throws JSONException {
                List<DocumentTemplateInfo> templates = documentTemplateService.findTemplates(query, size);
                return buildDocTemplateJSON(templates);
            }

            JSONArray buildDocTemplateJSON(List<DocumentTemplateInfo> templates) throws JSONException {
                JSONArray result = new JSONArray();
                for (DocumentTemplateInfo template : templates) {
                    JSONObject templateObj = new JSONObject();
                    templateObj.put("title", template.getTitle());
                    templateObj.put("name", template.getName());
                    templateObj.put("nodeRef", template.getNodeRef());
                    templateObj.put("version", template.getCustomProperty(ContentModel.PROP_VERSION_LABEL));
                    templateObj.put("templateType", template.getTemplateType());
                    result.put(templateObj);
                }
                return result;
            }
        };
    }
}
