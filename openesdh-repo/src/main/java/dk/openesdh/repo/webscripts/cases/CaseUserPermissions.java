package dk.openesdh.repo.webscripts.cases;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import dk.openesdh.repo.services.cases.CaseService;

public class CaseUserPermissions extends AbstractWebScript {

    private CaseService caseService;

    public void setCaseService(CaseService caseService) {
        this.caseService = caseService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
        String caseId = templateArgs.get("caseId");
        List<String> permissionSet = caseService.getCaseUserPermissions(caseId);
        JSONArray json = new JSONArray();
        json.addAll(permissionSet);
        json.writeJSONString(res.getWriter());
    }

}
