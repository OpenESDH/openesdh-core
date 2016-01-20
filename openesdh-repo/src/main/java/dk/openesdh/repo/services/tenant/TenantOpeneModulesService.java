package dk.openesdh.repo.services.tenant;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.ContentIOException;
import org.json.JSONException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public interface TenantOpeneModulesService {

    /**
     * Retrieves a map of all tenants with enabled modules. The map doesn't
     * contain tenants with no modules enabled.
     */
    Map<String, List<String>> getAllTenantsModules() throws JsonParseException, JsonMappingException, IOException;

    /**
     * Retrieves list of modules of the specified tenant.
     */
    List<String> getTenantModules(String tenant) throws JsonParseException, JsonMappingException, IOException;

    /**
     * Saves enabled modules list for the specified tenant.
     */
    void saveTenantModules(String tenant, List<String> modules) throws JsonParseException, JsonMappingException,
            IOException, ContentIOException, JSONException;

    /**
     * Retrieves a list of OpenE modules which can be enabled for tenants.
     */
    List<String> getOpeneModules();

    /**
     * Removes enabled modules configuration for the specified tenant.
     */
    void deleteTenantModules(String tenant) throws JsonParseException, JsonMappingException, IOException;
}
