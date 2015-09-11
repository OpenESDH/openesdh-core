package dk.openesdh.repo.webscripts.documents;

import java.io.IOException;

import dk.openesdh.repo.webscripts.utils.WebScriptUtils;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import dk.openesdh.repo.services.documents.DocumentService;

public class MoveToCase  extends AbstractWebScript {

	private static Log logger = LogFactory.getLog(MoveToCase.class);

    private DocumentService documentService;

    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }
    
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res)
			throws IOException {

		JSONObject json = DocumentsWebScriptUtil.retrieveAndCheckParams(req);

		String caseId = (String) json.get("caseId");
		String docRef = (String) json.get("nodeRef");

		try {
			documentService.moveDocumentToCase(new NodeRef(docRef), caseId);
		} catch (Exception e) {
			logError(e);
			throw new WebScriptException(Status.STATUS_CONFLICT,
					"Unable to move document to case. " + e.getMessage());
		}

		WebScriptUtils.respondSuccess(res, "The document has been moved to the case " + caseId);
	}

	private void logError(Exception e) {
		logger.error("Unable to move document to case due to the following exception: "
				+ e.getMessage());
		e.printStackTrace();
	}

}
