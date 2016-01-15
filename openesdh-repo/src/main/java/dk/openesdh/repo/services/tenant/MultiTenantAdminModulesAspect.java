package dk.openesdh.repo.services.tenant;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.alfresco.repo.importer.ImporterBootstrap;
import org.alfresco.repo.tenant.MultiTAdminServiceImpl;
import org.alfresco.service.cmr.module.ModuleDetails;
import org.alfresco.service.cmr.module.ModuleService;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.NameMatchMethodPointcutAdvisor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import dk.openesdh.repo.model.OpenESDHModel;

/**
 * This aspect enables us to intercept modules initialization when a new tenant is created. 
 * We need this to be able to initialize new tenant with OpenE artifacts (folders, groups, etc).
 * 
 * @author rudinjur
 *
 */
@Service("MultiTenantAdminModulesAspect")
public class MultiTenantAdminModulesAspect implements BeanFactoryAware {
    
    private static final String TENANT_ADMIN_SERVICE = "tenantAdminService";
    
    private static final String OPENE_MODULE_SERVICE = "OpeneMultiTenantModuleService";

    private ListableBeanFactory beanFactory;

    private List<String> moduleSpacesImporterBeanIds = Collections.emptyList();
    private List<String> moduleSpaceBootstrapViewPropIds = Collections.emptyList();
    private List<String> openeModuleIdsWithMtSuffix = Collections.emptyList();
    private boolean spaceViewPropsAppended = false;

    @Autowired
    @Qualifier("moduleService")
    private ModuleService moduleService;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        if (!beanFactory.containsBean(TENANT_ADMIN_SERVICE) || !beanFactory.containsBean(OPENE_MODULE_SERVICE)) {
            return;
        }
        this.beanFactory = (ListableBeanFactory) beanFactory;
        openeModuleIdsWithMtSuffix = getOpeneModuleIdsWithMtSuffix();
        moduleSpacesImporterBeanIds = getImporterBeanIds(ImporterBootstrap.class);
        moduleSpaceBootstrapViewPropIds = getImporterBeanIds(Properties.class);
        
        MultiTAdminServiceImpl tenantAdminService = (MultiTAdminServiceImpl) beanFactory
                .getBean(TENANT_ADMIN_SERVICE);
        Advised moduleServiceProxy = (Advised) beanFactory.getBean(OPENE_MODULE_SERVICE);
        tenantAdminService.setModuleService((ModuleService) moduleServiceProxy);

        NameMatchMethodPointcutAdvisor afterStartModulesAdvisor = new NameMatchMethodPointcutAdvisor(
                (AfterReturningAdvice) this::afterStartModules);
        afterStartModulesAdvisor.addMethodName("startModules");
        moduleServiceProxy.addAdvisor(afterStartModulesAdvisor);
    }
    
    public void afterStartModules(Object returnValue, Method method, Object[] args, Object target) {
        appendSpacesBootstrapViewsToLastImporter();
        moduleSpacesImporterBeanIds.stream().forEach(this::runSpacesImporter);
    }
    
    private List<String> getOpeneModuleIdsWithMtSuffix(){
        return moduleService.getAllModules()
                .stream()
                .filter(md -> OpenESDHModel.OPENESDH_REPO_MODULE_ID.equals(md.getId()) || isOpenesdhRepoDependant(md))
                .map(ModuleDetails::getId)
                .map(id -> id + "_mt_")
                .collect(Collectors.toList());
    }
    
    private boolean isOpenesdhRepoDependant(ModuleDetails module){
        return module.getDependencies()
                .stream()
                .filter(d -> OpenESDHModel.OPENESDH_REPO_MODULE_ID.equals(d.getDependencyId()))
                .findAny()
                .isPresent();
    }

    private List<String> getImporterBeanIds(Class<?> beanClass) {
        return Arrays.asList(BeanFactoryUtils.beanNamesForTypeIncludingAncestors(this.beanFactory, beanClass))
                .stream()
                .filter(this::isOpeneMultiTenantImporterBean)
                .collect(Collectors.toList());
    }
    
    private boolean isOpeneMultiTenantImporterBean(String beanId){
        return openeModuleIdsWithMtSuffix.stream()
                    .filter(moduleIdMt -> beanId.startsWith(moduleIdMt))
                    .findAny()
                    .isPresent();
    }

    private void appendSpacesBootstrapViewsToLastImporter() {
        if (spaceViewPropsAppended) {
            return;
        }
        ImporterBootstrap lastImporter = (ImporterBootstrap) beanFactory.getBean(moduleSpacesImporterBeanIds
                .get(moduleSpacesImporterBeanIds.size() - 1));
        moduleSpaceBootstrapViewPropIds
            .stream()
            .map(id -> Arrays.asList((Properties) beanFactory.getBean(id)))
            .forEach(lastImporter::addBootstrapViews);
        spaceViewPropsAppended = true;
    }

    private void runSpacesImporter(String beanId) {
        ImporterBootstrap importer = (ImporterBootstrap) beanFactory.getBean(beanId);
        System.out.println("running importer: " + beanId);
        importer.bootstrap();
        System.out.println("done");
    }

}
