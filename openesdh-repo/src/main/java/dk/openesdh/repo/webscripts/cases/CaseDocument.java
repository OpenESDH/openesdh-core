package dk.openesdh.repo.webscripts.cases;

import dk.openesdh.repo.services.cases.CaseService;
import dk.openesdh.repo.services.documents.DocumentService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.*;

import java.io.IOException;
import java.util.Map;

/**
 * Created by rasmutor on 2/6/15.
 */
public class CaseDocument extends AbstractWebScript {

    private static final Log LOG = LogFactory.getLog(CaseDocument.class);

    private CaseService caseService;
    private DocumentService documentService;

    @Override
    public void execute(WebScriptRequest request, WebScriptResponse response) throws IOException {
        Map<String, String> templateArgs = request.getServiceMatch().getTemplateVars();
        String caseId = templateArgs.get("caseId");
        LOG.info("CaseDocument: caseId = " + caseId);

        String documentName = request.getParameter("name");

        NodeRef theCase = caseService.getCaseById(caseId);
        if (theCase == null) {
            LOG.error("Case not found: " + caseId);
            throw new WebScriptException(Status.STATUS_NOT_FOUND, "Case not found: " + caseId);
        }
        LOG.debug(theCase.toString());

        NodeRef documentsFolder = caseService.getDocumentsFolder(theCase);
        LOG.debug("documentsFolder: " + documentsFolder.toString());
        ChildAssociationRef documentFolder;
        try {
            documentFolder = documentService.createDocumentFolder(documentsFolder, documentName);
        } catch (RuntimeException e) {
            throw new WebScriptException(Status.STATUS_CONFLICT, e.getMessage());
        }
        NodeRef childRef = documentFolder.getChildRef();

        try {
            new JSONObject().put("nodeRef", childRef.toString()).write(response.getWriter());
        } catch (JSONException e) {
            throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public void setCaseService(CaseService caseService) {
        this.caseService = caseService;
    }

    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }
}

