package dk.openesdh.repo.services.tenant;

import org.alfresco.service.cmr.module.ModuleService;
import org.junit.Test;

import dk.openesdh.repo.utils.ClassUtils;

public class MultiTenantAdminModulesAspectIT {

    @Test
    public void checkMandatoryMethods() {
        ClassUtils.checkHasMethods(ModuleService.class, "startModules");
    }

}
