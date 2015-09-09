package dk.openesdh.repo.webscripts.documents;

import dk.openesdh.repo.webscripts.utils.WebScriptUtils;
import org.alfresco.service.cmr.repository.NodeRef;
import org.json.simple.JSONObject;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.util.StringUtils;

public class DocumentsWebScriptUtil {

	public static JSONObject retrieveAndCheckParams(WebScriptRequest req) {
		JSONObject json = WebScriptUtils.readJson(req);

		String caseId = (String) json.get("caseId");
		String docRef = (String) json.get("nodeRef");
	
		if (StringUtils.isEmpty(docRef)) {
			throw new WebScriptException(Status.STATUS_BAD_REQUEST,
					"No document node ref provided");
		}
	
		if (StringUtils.isEmpty(caseId)) {
			throw new WebScriptException(Status.STATUS_BAD_REQUEST,
					"No target case id provided");
		}
	
		if (!NodeRef.isNodeRef(docRef)) {
			throw new WebScriptException(Status.STATUS_BAD_REQUEST,
					"Invalid document NodeRef provided: " + docRef);
		}
	
		return json;
	}

}
