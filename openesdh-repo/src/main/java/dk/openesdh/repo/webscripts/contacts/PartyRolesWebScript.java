package dk.openesdh.repo.webscripts.contacts;

import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.github.dynamicextensionsalfresco.webscripts.annotations.Attribute;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Authentication;
import com.github.dynamicextensionsalfresco.webscripts.annotations.AuthenticationType;
import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.UriVariable;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import com.github.dynamicextensionsalfresco.webscripts.resolutions.Resolution;

import dk.openesdh.repo.model.ClassifValue;
import dk.openesdh.repo.services.classification.ClassificatorManagementService;
import dk.openesdh.repo.webscripts.WebScriptParams;
import dk.openesdh.repo.webscripts.classification.ClassificatorValuesWebScript;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

@Component
@WebScript(description = "Provides API for party roles", families = "Classifications")
public class PartyRolesWebScript extends ClassificatorValuesWebScript {

    @Autowired
    @Qualifier("PartyRoleService")
    private ClassificatorManagementService partyRoleService;

    @Uri("/api/openesdh/party/roles")
    public Resolution getRoles() {
        return WebScriptUtils.jsonResolution(partyRoleService.getClassifValues());
    }

    @Uri("/api/openesdh/party/roles/enabled")
    public Resolution getEnabledRoles() {
        return WebScriptUtils.jsonResolution(partyRoleService.getEnabledClassifValues());
    }

    @Authentication(AuthenticationType.ADMIN)
    @Uri(value = "/api/openesdh/party/roles", method = HttpMethod.POST)
    public Resolution postRole(@Attribute("classifValue") ClassifValue role) throws JSONException {
        return WebScriptUtils.jsonResolution(partyRoleService.createOrUpdateClassifValue(role));
    }

    @Authentication(AuthenticationType.ADMIN)
    @Uri(value = "/api/openesdh/party/roles/{storeType}/{storeId}/{id}", method = HttpMethod.DELETE)
    public void deleteRole(@UriVariable(WebScriptParams.STORE_TYPE) String storeType,
            @UriVariable(WebScriptParams.STORE_ID) String storeId, @UriVariable(WebScriptParams.ID) String id) {
        NodeRef roleRef = new NodeRef(storeType, storeId, id);
        partyRoleService.deleteClassifValue(roleRef);
    }
}
