package dk.openesdh.repo.webscripts.cases;

import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.UriVariable;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import com.github.dynamicextensionsalfresco.webscripts.resolutions.Resolution;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import dk.openesdh.repo.services.cases.CaseService;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

@Component
@WebScript(description = "Retrieve current user permissions for the case", families = {"Case Tools"})
public class CaseUserPermissionWebScript {

    @Autowired
    private CaseService caseService;

    @Uri(value = "/api/openesdh/case/{caseId}/user/permissions", method = HttpMethod.GET, defaultFormat = "json")
    public Resolution execute(@UriVariable final String caseId) throws IOException {
        return WebScriptUtils.jsonResolution(
                caseService.getCaseUserPermissions(caseId));
    }
}
