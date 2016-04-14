package dk.openesdh.repo.webscripts.cases;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import com.github.dynamicextensionsalfresco.webscripts.resolutions.Resolution;

import dk.openesdh.repo.services.contacts.PartyRoleService;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

@Component
@WebScript(description = "Provides API for party roles")
public class PartyRolesWebScript {

    @Autowired
    @Qualifier("PartyRoleService")
    private PartyRoleService partyRoleService;

    @Uri("/api/openesdh/party/roles")
    public Resolution getRoles() {
        return WebScriptUtils.jsonResolution(partyRoleService.getClassifValues());
    }

}
