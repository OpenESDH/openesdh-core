package dk.openesdh.repo.webscripts.documents;

import dk.openesdh.repo.model.DocumentType;
import dk.openesdh.repo.services.documents.DocumentTypeService;
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
        writeDocumentTypeToResponse(documentType, res);
    }

    @Override
    protected void post(NodeRef nodeRef, WebScriptRequest req, WebScriptResponse res) throws IOException, JSONException {
        String name = ParamUtils.getRequiredParameter(req, "name");
        String displayName = ParamUtils.getRequiredParameter(req, "displayName");
        DocumentType saveDocumentType = updateDocumentType(nodeRef, name, displayName);
        writeDocumentTypeToResponse(saveDocumentType, res);
    }

    @Override
    protected void delete(NodeRef nodeRef, WebScriptRequest req, WebScriptResponse res) throws IOException, JSONException {
        checkRequiredNodeRef(nodeRef);
        DocumentType type = new DocumentType();
        type.setNodeRef(nodeRef);
        documentTypeService.deleteDocumentType(type);
        res.getWriter().append("Deleted succesfully");
    }

    private void checkRequiredNodeRef(NodeRef nodeRef) throws WebScriptException {
        if (nodeRef == null) {
            throw new WebScriptException("Required parameter is not defined: nodeRef");
        }
    }

    private DocumentType updateDocumentType(NodeRef nodeRef, String name, String displayName) {
        DocumentType type = new DocumentType();
        type.setNodeRef(nodeRef);
        type.setName(name);
        type.setDisplayName(displayName);
        return documentTypeService.saveDocumentType(type);
    }

    private void writeDocumentTypeToResponse(DocumentType documentType, WebScriptResponse res) throws JSONException, IOException {
        documentType.toJSONObject().write(res.getWriter());
    }
}
