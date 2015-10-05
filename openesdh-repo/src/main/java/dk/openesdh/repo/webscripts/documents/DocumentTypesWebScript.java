package dk.openesdh.repo.webscripts.documents;

import dk.openesdh.repo.model.DocumentType;
import dk.openesdh.repo.services.documents.DocumentTypeService;
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

public class DocumentTypesWebScript extends AbstractRESTWebscript {

    private DocumentTypeService documentTypeService;

    public void setDocumentTypeService(DocumentTypeService documentTypeService) {
        this.documentTypeService = documentTypeService;
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
            new JSONArray(documentTypeService.getDocumentTypes().stream()
                    .map(DocumentType::toJSONObject)
                    .collect(Collectors.toList())
            ).write(res.getWriter());
            return;
        }
        DocumentType documentType = documentTypeService.getDocumentType(nodeRef);
        if (documentType == null) {
            throw new WebScriptException("Document type not found");
        }
        createJSONObjectWithMultilanguage(documentType)
                .write(res.getWriter());
    }

    private JSONObject createJSONObjectWithMultilanguage(DocumentType type) throws JSONException {
        JSONObject json = type.toJSONObject();
        json.put("mlDisplayNames", documentTypeService.getMultiLanguageDisplayNames(type.getNodeRef()).toJSONArray());
        return json;
    }

    @Override
    protected void post(NodeRef nodeRef, WebScriptRequest req, WebScriptResponse res) throws IOException, JSONException {
        String name = ParamUtils.getRequiredParameter(req, "name");
        String mlDisplayNames = ParamUtils.getRequiredParameter(req, "mlDisplayNames");
        DocumentType saveDocumentType = createOrUpdateDocumentType(nodeRef, name, mlDisplayNames);
        res.setContentEncoding(WebScriptUtils.CONTENT_ENCODING_UTF_8);
        saveDocumentType.toJSONObject()
                .write(res.getWriter());
    }

    @Override
    protected void delete(NodeRef nodeRef, WebScriptRequest req, WebScriptResponse res) throws IOException, JSONException {
        checkRequiredParam(nodeRef, "nodeRef");
        DocumentType type = new DocumentType();
        type.setNodeRef(nodeRef);
        documentTypeService.deleteDocumentType(type);
        res.getWriter().append("Deleted succesfully");
    }

    private DocumentType createOrUpdateDocumentType(NodeRef nodeRef, String name, String mlDisplayNames) throws JSONException {
        DocumentType type = new DocumentType();
        type.setNodeRef(nodeRef);
        type.setName(name);
        return documentTypeService.createOrUpdateDocumentType(
                type,
                MultiLanguageValue.createFromJSONString(mlDisplayNames));
    }
}
