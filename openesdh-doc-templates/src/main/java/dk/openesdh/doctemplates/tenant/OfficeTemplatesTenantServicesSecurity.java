package dk.openesdh.doctemplates.tenant;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import dk.openesdh.repo.services.tenant.TenantModulesSecurityAutoProxyCreator;

@Component
public class OfficeTemplatesTenantServicesSecurity extends TenantModulesSecurityAutoProxyCreator {

    private static final long serialVersionUID = 1L;

    @PostConstruct
    public void init() {
        setOpeneModuleId("openesdh-doc-templates");
        setBeanPackageNames("dk.openesdh.doctemplates.services*");
    }

}
