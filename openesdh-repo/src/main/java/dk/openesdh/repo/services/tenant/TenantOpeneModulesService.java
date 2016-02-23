package dk.openesdh.repo.services.tenant;

import java.util.List;
import java.util.Map;

import org.alfresco.service.namespace.QName;

public interface TenantOpeneModulesService {

    /**
     * Retrieves a map of all tenants with enabled modules. The map doesn't
     * contain tenants with no modules enabled.
     *
     * @return
     */
    Map<String, List<String>> getAllTenantsModules();

    /**
     * Retrieves list of modules of the specified tenant.
     *
     * @param tenant
     * @return
     */
    List<String> getTenantModules(String tenant);

    /**
     * Saves enabled modules list for the specified tenant.
     *
     * @param tenant
     * @param modules
     */
    void saveTenantModules(String tenant, List<String> modules);

    /**
     * Retrieves a list of OpenE modules which can be enabled for tenants.
     *
     * @return
     */
    List<String> getOpeneModules();

    /**
     * Removes enabled modules configuration for the specified tenant.
     *
     * @param tenant
     */
    void deleteTenantModules(String tenant);

    /**
     * Checks whether the module for the specified case type is enabled for the
     * current tenant.
     *
     * @param caseType
     */
    void checkCaseTypeModuleEnabled(QName caseType);

    /**
     * Checks whether the specified module is enabled for the current tenant.
     *
     * @param module
     */
    void checkCurrentTenantModuleEnabled(String module);
}
