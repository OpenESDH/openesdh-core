package dk.openesdh.repo.webscripts.tenant;

import java.io.IOException;

import org.alfresco.service.cmr.repository.ContentIOException;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Authentication;
import com.github.dynamicextensionsalfresco.webscripts.annotations.AuthenticationType;
import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.UriVariable;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import com.github.dynamicextensionsalfresco.webscripts.resolutions.Resolution;

import dk.openesdh.repo.model.TenantInfo;
import dk.openesdh.repo.services.tenant.TenantOpeneModulesService;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

@Component
@WebScript(description = "Manages tenant OpenE modules configuration", families = "Tenant")
public class TenantOpeneModulesWebScript {

    @Autowired
    @Qualifier("TenantOpeneModulesService")
    private TenantOpeneModulesService tenantModulesService;

    @Authentication(value = AuthenticationType.ADMIN)
    @Uri(value = "/api/openesdh/tenants", method = HttpMethod.GET, defaultFormat = WebScriptUtils.JSON)
    public Resolution getTenantsInfo() throws JsonParseException, JsonMappingException, IOException {
        return WebScriptUtils.jsonResolution(tenantModulesService.getTenantsInfo());
    }

    @Authentication(value = AuthenticationType.ADMIN)
    @Uri(value = "/api/openesdh/modules", method = HttpMethod.GET, defaultFormat = WebScriptUtils.JSON)
    public Resolution getOpeneModules() {
        return WebScriptUtils.jsonResolution(tenantModulesService.getOpeneModules());
    }

    @Authentication(value = AuthenticationType.ADMIN)
    @Uri(value = "/api/openesdh/tenant/update", method = HttpMethod.POST, defaultFormat = WebScriptUtils.JSON)
    public void saveTenantModules(WebScriptRequest req)
            throws JsonParseException, JsonMappingException,
            IOException, ContentIOException, JSONException {
        TenantInfo tenant = (TenantInfo) WebScriptUtils.readJson(TenantInfo.class, req);
        tenantModulesService.saveTenantInfo(tenant);
    }

    @Authentication(value = AuthenticationType.ADMIN)
    @Uri(value = "/api/openesdh/tenant/{tenant}/modules", method = HttpMethod.DELETE, defaultFormat = WebScriptUtils.JSON)
    public void deleteTenantModules(@UriVariable("tenant") String tenant) throws JsonParseException,
            JsonMappingException, IOException, ContentIOException, JSONException {

        tenantModulesService.deleteTenantModules(tenant);
    }

    @Authentication(value = AuthenticationType.ADMIN)
    @Uri(value = "/api/openesdh/tenant", method = HttpMethod.POST, defaultFormat = WebScriptUtils.JSON)
    public void createTenant(WebScriptRequest req) throws IOException {
        TenantInfo tenant = (TenantInfo) WebScriptUtils.readJson(TenantInfo.class, req);
        tenantModulesService.createTenant(tenant);
    }
}
