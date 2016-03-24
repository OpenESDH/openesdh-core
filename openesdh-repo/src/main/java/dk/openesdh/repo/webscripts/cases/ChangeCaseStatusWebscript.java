package dk.openesdh.repo.webscripts.cases;

import java.io.IOException;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.github.dynamicextensionsalfresco.webscripts.annotations.Attribute;
import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.UriVariable;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import com.github.dynamicextensionsalfresco.webscripts.resolutions.Resolution;

import dk.openesdh.repo.model.CaseStatus;
import dk.openesdh.repo.services.cases.CaseService;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

@Component
@WebScript(description = "Change case status", families = "Case tools")
public class ChangeCaseStatusWebscript {

    @Autowired
    @Qualifier(CaseService.BEAN_ID)
    private CaseService caseService;

    @Attribute
    protected CaseStatus getStatus(WebScriptRequest req) throws IOException {
        JSONObject json = WebScriptUtils.readJson(req);
        String status = (String) json.get("status");
        return StringUtils.isEmpty(status)
                ? null
                : CaseStatus.valueOf(status.toUpperCase());
    }

    @Uri(value = "/api/openesdh/case/{caseId}/status", method = HttpMethod.POST, defaultFormat = "json")
    public Resolution execute(
            @UriVariable final String caseId,
            @Attribute(required = true) CaseStatus caseStatus) throws Exception {
        caseService.changeNodeStatus(caseService.getCaseById(caseId), caseStatus);
        return WebScriptUtils.respondSuccess("The case status has been changed");
    }

}
