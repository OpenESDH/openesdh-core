package dk.openesdh.repo.webscripts.xsearch;

import dk.openesdh.repo.model.DocumentType;
import dk.openesdh.repo.services.cases.CaseService;
import dk.openesdh.repo.services.documents.DocumentService;
import dk.openesdh.repo.utils.Utils;
import java.util.Map;
import java.util.Set;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.io.FilenameUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class DocumentSearch extends XSearchWebscript {

    private static final String NODE_REF_PARAM_NAME = "nodeRef";
    private static final String CASE_ID_PARAM_NAME = "caseId";

    protected DocumentService documentService;
    protected CaseService caseService;

    /**
     * Adds the main document nodeRef to the results.
     *
     * @param nodeRef
     * @return
     * @throws JSONException
     */
    protected JSONObject nodeToJSON(NodeRef nodeRef) throws JSONException {
        JSONObject json = super.nodeToJSON(nodeRef);
        NodeRef mainDocNodeRef = documentService.getMainDocument(nodeRef);
        if (mainDocNodeRef != null) {
            json.put("mainDocNodeRef", mainDocNodeRef.toString());
            //get document type
            DocumentType documentType = documentService.getDocumentType(nodeRef);
            if (documentType != null) {
                json.put("doc:type", documentType.getName());
            }

            //Get the main document version string
            String mainDocVersion = (String) nodeService.getProperty(mainDocNodeRef, ContentModel.PROP_VERSION_LABEL);
            json.put("mainDocVersion", mainDocVersion);

            //also return the filename extension
            String fileName = (String) nodeService.getProperty(mainDocNodeRef, ContentModel.PROP_NAME);
            String extension = FilenameUtils.getExtension(fileName);
            json.put("fileType", extension);
        }
        return json;
    }

    protected Map<String, String> getParams(WebScriptRequest req) {

        Map<String, String> params = Utils.parseParameters(req.getURL());
        Set<String> paramNames = params.keySet();
        if (paramNames.contains(NODE_REF_PARAM_NAME) || !paramNames.contains(CASE_ID_PARAM_NAME)) {
            return params;
        }

        String caseId = params.get(CASE_ID_PARAM_NAME);
        params.remove(CASE_ID_PARAM_NAME, caseId);
        NodeRef caseNodeRef = caseService.getCaseById(caseId);
        params.put(NODE_REF_PARAM_NAME, caseNodeRef.toString());
        return params;
    }

    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }

    public void setCaseService(CaseService caseService) {
        this.caseService = caseService;
    }
}
