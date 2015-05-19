package dk.openesdh.repo.webscripts.documents;

import java.io.IOException;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.NodeRef;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.util.StringUtils;

public class DocumentsWebScriptUtil {

	public static JSONObject retrieveAndCheckParams(WebScriptRequest req) {
	
		String contentType = req.getContentType();
		if (contentType != null && contentType.indexOf(';') != -1) {
			contentType = contentType.substring(0, contentType.indexOf(';'));
		}
	
		if (!MimetypeMap.MIMETYPE_JSON.equals(contentType)) {
			throw new WebScriptException(Status.STATUS_UNSUPPORTED_MEDIA_TYPE,
					"Wrong Content-Type");
		}
	
		JSONObject json = null;
		JSONParser parser = new JSONParser();
		try {
			String content = req.getContent().getContent();
			json = (JSONObject) parser.parse(content);
		} catch (IOException | ParseException e) {
			throw new WebScriptException(Status.STATUS_BAD_REQUEST,
					"Invalid JSON: " + e.getMessage());
		}
	
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

	public static void respondSuccess(WebScriptResponse res, String message)
			throws IOException {
		DocumentsWebScriptUtil.respondWithMessage(res, Status.STATUS_OK, message);
	}

	public static void respondWithMessage(WebScriptResponse res, int status,
			String message) throws IOException {
		JSONObject json = new JSONObject();
		res.setStatus(status);
		json.put("message", message);
		json.writeJSONString(res.getWriter());
		res.getWriter().flush();
	}

}
