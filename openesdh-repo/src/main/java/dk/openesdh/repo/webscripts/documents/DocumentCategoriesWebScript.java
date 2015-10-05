package dk.openesdh.repo.webscripts.documents;

import dk.openesdh.repo.model.DocumentCategory;
import dk.openesdh.repo.services.documents.DocumentCategoryService;
import dk.openesdh.repo.services.system.MultiLanguageValue;
import dk.openesdh.repo.webscripts.AbstractRESTWebscript;
import dk.openesdh.repo.webscripts.ParamUtils;
import static dk.openesdh.repo.webscripts.ParamUtils.checkRequiredParam;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;
import org.alfresco.service.cmr.repository.NodeRef;
import org.codehaus.plexus.util.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

public class DocumentCategoriesWebScript extends AbstractRESTWebscript {

    private DocumentCategoryService documentCategoryService;

    public void setDocumentCategoryService(DocumentCategoryService documentCategoryService) {
        this.documentCategoryService = documentCategoryService;
    }

    @Override
    protected NodeRef getNodeRef(WebScriptRequest req, Map<String, String> templateArgs) {
        String nodeRefId = req.getParameter("nodeRefId");
        if (StringUtils.isNotEmpty(nodeRefId)) {
            return new NodeRef(nodeRefId);
        }
        return super.getNodeRef(req, templateArgs);
    }


    @Override
    protected void get(NodeRef nodeRef, WebScriptRequest req, WebScriptResponse res) throws IOException, JSONException {
        res.setContentEncoding(WebScriptUtils.CONTENT_ENCODING_UTF_8);
        if (nodeRef == null) {
            new JSONArray(documentCategoryService.getDocumentCategories().stream()
                    .map(DocumentCategory::toJSONObject)
                    .collect(Collectors.toList())
            ).write(res.getWriter());
            return;
        }
        DocumentCategory documentCategory = documentCategoryService.getDocumentCategory(nodeRef);
        if (documentCategory == null) {
            throw new WebScriptException("Document category not found");
        }
        createJSONObjectWithMultilanguage(documentCategory)
                .write(res.getWriter());
    }

    private JSONObject createJSONObjectWithMultilanguage(DocumentCategory documentCategory) throws JSONException {
        JSONObject json = documentCategory.toJSONObject();
        json.put("mlDisplayNames", documentCategoryService.getMultiLanguageDisplayNames(documentCategory.getNodeRef()).toJSONArray());
        return json;
    }

    @Override
    protected void post(NodeRef nodeRef, WebScriptRequest req, WebScriptResponse res) throws IOException, JSONException {
        String name = ParamUtils.getRequiredParameter(req, "name");
        String mlDisplayNames = ParamUtils.getRequiredParameter(req, "mlDisplayNames");
        DocumentCategory savedDocumentCategory = createOrUpdateDocumentCategory(nodeRef, name, mlDisplayNames);
        res.setContentEncoding(WebScriptUtils.CONTENT_ENCODING_UTF_8);
        savedDocumentCategory.toJSONObject()
                .write(res.getWriter());
    }

    @Override
    protected void delete(NodeRef nodeRef, WebScriptRequest req, WebScriptResponse res) throws IOException, JSONException {
        checkRequiredParam(nodeRef, "nodeRef");
        DocumentCategory category = new DocumentCategory();
        category.setNodeRef(nodeRef);
        documentCategoryService.deleteDocumentCategory(category);
        res.getWriter().append("Deleted succesfully");
    }

    private DocumentCategory createOrUpdateDocumentCategory(NodeRef nodeRef, String name, String mlDisplayNames) throws JSONException {
        DocumentCategory category = new DocumentCategory();
        category.setNodeRef(nodeRef);
        category.setName(name);
        return documentCategoryService.createOrUpdateDocumentCategory(
                category,
                MultiLanguageValue.createFromJSONString(mlDisplayNames));
    }
}
