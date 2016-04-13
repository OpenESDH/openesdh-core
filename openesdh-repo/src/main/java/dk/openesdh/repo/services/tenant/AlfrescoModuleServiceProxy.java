package dk.openesdh.repo.services.tenant;

import javax.annotation.PostConstruct;

import org.alfresco.service.cmr.module.ModuleService;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.stereotype.Service;

@SuppressWarnings("serial")
@Service(AlfrescoModuleServiceProxy.MODULE_SERVICE_PROXY)
public class AlfrescoModuleServiceProxy extends ProxyFactoryBean {

    public static final String MODULE_SERVICE_PROXY = "OpeneAlfrescoModuleServiceProxy";

    @PostConstruct
    public void init() {
        setTargetName("moduleService");
        setInterfaces(ModuleService.class);
    }
}
