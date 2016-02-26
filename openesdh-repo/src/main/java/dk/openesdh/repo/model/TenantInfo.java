package dk.openesdh.repo.model;

import java.util.Collections;
import java.util.List;

public class TenantInfo {

    private String tenantDomain;
    private String tenantAdminPassword;
    private String tenantContentStoreRoot;
    private String tenantUIContext;
    private List<String> modules = Collections.emptyList();

    public String getTenantDomain() {
        return tenantDomain;
    }

    public void setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
    }

    public String getTenantAdminPassword() {
        return tenantAdminPassword;
    }

    public void setTenantAdminPassword(String tenantAdminPassword) {
        this.tenantAdminPassword = tenantAdminPassword;
    }

    public String getTenantContentStoreRoot() {
        return tenantContentStoreRoot;
    }

    public void setTenantContentStoreRoot(String tenantContentStoreRoot) {
        this.tenantContentStoreRoot = tenantContentStoreRoot;
    }

    public String getTenantUIContext() {
        return tenantUIContext;
    }

    public void setTenantUIContext(String tenantUIContext) {
        this.tenantUIContext = tenantUIContext;
    }

    public List<String> getModules() {
        return modules;
    }

    public void setModules(List<String> modules) {
        this.modules = modules;
    }

}
