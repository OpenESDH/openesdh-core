package dk.openesdh.doctemplates.tenant;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import dk.openesdh.repo.services.tenant.TenantModulesSecurityAutoProxyCreator;

@SuppressWarnings("serial")
@Component
public class OfficeTemplatesTenantServicesSecurity extends TenantModulesSecurityAutoProxyCreator {

    @PostConstruct
    public void init() {
        setOpeneModuleId("openesdh-doc-templates");
        setBeanPackageNames("dk.openesdh.doctemplates.services*");
    }

}
