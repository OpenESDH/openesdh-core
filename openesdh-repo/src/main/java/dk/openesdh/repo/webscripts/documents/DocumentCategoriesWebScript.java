package dk.openesdh.repo.webscripts.documents;

import dk.openesdh.repo.model.DocumentCategory;
import dk.openesdh.repo.services.documents.DocumentCategoryService;
import dk.openesdh.repo.webscripts.AbstractRESTWebscript;
import dk.openesdh.repo.webscripts.ParamUtils;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;
import org.alfresco.service.cmr.repository.NodeRef;
import org.codehaus.plexus.util.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
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
        writeDocumentCategoryToResponse(documentCategory, res);
    }

    @Override
    protected void post(NodeRef nodeRef, WebScriptRequest req, WebScriptResponse res) throws IOException, JSONException {
        String name = ParamUtils.getRequiredParameter(req, "name");
        String displayName = ParamUtils.getRequiredParameter(req, "displayName");
        DocumentCategory saveDocumentCategory = updateDocumentCategory(nodeRef, name, displayName);
        writeDocumentCategoryToResponse(saveDocumentCategory, res);
    }

    @Override
    protected void delete(NodeRef nodeRef, WebScriptRequest req, WebScriptResponse res) throws IOException, JSONException {
        checkRequiredNodeRef(nodeRef);
        DocumentCategory category = new DocumentCategory();
        category.setNodeRef(nodeRef);
        documentCategoryService.deleteDocumentCategory(category);
        res.getWriter().append("Deleted succesfully");
    }

    private void checkRequiredNodeRef(NodeRef nodeRef) throws WebScriptException {
        if (nodeRef == null) {
            throw new WebScriptException("Required parameter is not defined: nodeRef");
        }
    }

    private DocumentCategory updateDocumentCategory(NodeRef nodeRef, String name, String displayName) {
        DocumentCategory category = new DocumentCategory();
        category.setNodeRef(nodeRef);
        category.setName(name);
        category.setDisplayName(displayName);
        return documentCategoryService.saveDocumentCategory(category);
    }

    private void writeDocumentCategoryToResponse(DocumentCategory documentCategory, WebScriptResponse res) throws JSONException, IOException {
        documentCategory.toJSONObject().write(res.getWriter());
    }
}
