package dk.openesdh.repo.webscripts.documents;

import dk.openesdh.repo.services.cases.CaseService;
import dk.openesdh.repo.services.documents.DocumentService;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.springframework.extensions.webscripts.*;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Map;

public class ChangeDocumentStatus extends AbstractWebScript {

	private static Log logger = LogFactory.getLog(ChangeDocumentStatus.class);

	private DocumentService documentService;

	public void setDocumentService(DocumentService documentService) {
		this.documentService = documentService;
	}

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res)
			throws IOException {

		Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
		String storeType = templateArgs.get("store_type");
		String storeId = templateArgs.get("store_id");
		String id = templateArgs.get("id");
		String docNodeRefStr = storeType +"://"+storeId+"/"+id;
		NodeRef nodeRef = new NodeRef (docNodeRefStr);

		JSONObject json = WebScriptUtils.readJson(req);
		String status = (String) json.get("status");

		if (StringUtils.isEmpty(status)) {
			throw new WebScriptException(Status.STATUS_BAD_REQUEST,
					"No status provided");
		}

		try {
			documentService.changeNodeStatus(nodeRef, status);
		} catch (Exception e) {
			throw new WebScriptException(Status.STATUS_FORBIDDEN,
					"Unable to switch document status: " + e.getMessage(), e);
		}

		WebScriptUtils.respondSuccess(res, "The document status has been changed");
	}

}
