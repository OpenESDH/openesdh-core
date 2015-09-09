package dk.openesdh.repo.webscripts.cases;

import dk.openesdh.repo.services.cases.CaseService;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.springframework.extensions.webscripts.*;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Map;

public class ChangeCaseStatus extends AbstractWebScript {

	private static Log logger = LogFactory.getLog(ChangeCaseStatus.class);

	private CaseService caseService;

	public void setCaseService(CaseService caseService) {
		this.caseService = caseService;
	}

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res)
			throws IOException {

		Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
		String caseId = templateArgs.get("caseId");

		if (StringUtils.isEmpty(caseId)) {
			throw new WebScriptException(Status.STATUS_BAD_REQUEST,
					"No case id provided");
		}

		JSONObject json = WebScriptUtils.readJson(req);
		String status = (String) json.get("status");

		if (StringUtils.isEmpty(status)) {
			throw new WebScriptException(Status.STATUS_BAD_REQUEST,
					"No status provided");
		}

		try {
			caseService.changeNodeStatus(caseService.getCaseById(caseId), status);
		} catch (Exception e) {
			throw new WebScriptException(Status.STATUS_FORBIDDEN,
					"Unable to switch case status: " + e.getMessage());
		}

		WebScriptUtils.respondSuccess(res, "The case status has been changed");
	}

}
