package dk.openesdh.repo.services.tenant;

import java.util.Collection;
import java.util.List;

import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.namespace.QName;

import com.fasterxml.jackson.core.JsonProcessingException;

import dk.openesdh.repo.model.TenantInfo;

public interface TenantOpeneModulesService {

    /**
     * Retrieves list of modules of the specified tenant.
     *
     * @param tenant
     * @return
     */
    List<String> getTenantModules(String tenant);

    /**
     * Saves enabled modules list and UI context for the specified tenant.
     *
     * @param tenant
     * @param modules
     * @throws JsonProcessingException
     * @throws ContentIOException
     */
    void saveTenantInfo(TenantInfo tenant) throws ContentIOException, JsonProcessingException;

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
     * @throws JsonProcessingException
     * @throws ContentIOException
     */
    void deleteTenantModules(String tenant) throws ContentIOException, JsonProcessingException;

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

    /**
     * Creates new tenant.
     * 
     * @param tenant
     * @throws JsonProcessingException
     * @throws ContentIOException
     */
    void createTenant(TenantInfo tenant) throws ContentIOException, JsonProcessingException;

    /**
     * Retrieves a list of all tenants with enabled modules and UI context.
     * 
     * @return
     */
    Collection<TenantInfo> getTenantsInfo();

}
