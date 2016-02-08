package dk.openesdh.repo.webscripts.documents;

import java.io.IOException;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import dk.openesdh.repo.services.documents.CaseDocumentCopyService;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

public class CopyToCase extends AbstractWebScript {

	private static Log logger = LogFactory.getLog(CopyToCase.class);

    @Autowired
    @Qualifier("CaseDocumentCopyService")
    private CaseDocumentCopyService copyService;

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res)
			throws IOException {

		JSONObject json = DocumentsWebScriptUtil.retrieveAndCheckParams(req);

		String caseId = (String) json.get("caseId");
		String docRef = (String) json.get("nodeRef");

		WebScriptUtils.respondSuccess(res, "The document has been copied to the case "
				+ caseId);

		try {
            copyService.copyDocumentToCase(new NodeRef(docRef), caseId);
		} catch (Exception e) {
			logError(e);
			throw new WebScriptException(Status.STATUS_CONFLICT, "Unable to copy document to case. " + e.getMessage());
		}
	}

	private void logError(Exception e) {
		logger.error("Unable to copy document to case due to the following exception: " + e.getMessage());
		e.printStackTrace();
	}
}
