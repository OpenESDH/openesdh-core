package dk.openesdh.repo.services.tenant;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.alfresco.repo.importer.ImporterBootstrap;
import org.alfresco.repo.security.authentication.AuthenticationContext;
import org.alfresco.repo.tenant.MultiTAdminServiceImpl;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.module.ModuleDetails;
import org.alfresco.service.cmr.module.ModuleService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.view.ImporterService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.transaction.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import dk.openesdh.repo.classification.sync.ClassificationSynchronizer;
import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.utils.ClassUtils;

/**
 * This aspect enables us to intercept modules initialization when a new tenant is created.
 * We need this to be able to initialize new tenant with OpenE artifacts (folders, groups, etc).
 *
 * @author rudinjur
 *
 */
@Service("MultiTenantAdminModulesAspect")
public class MultiTenantAdminModulesAspect implements BeanFactoryAware {

    private final Logger logger = LoggerFactory.getLogger(MultiTenantAdminModulesAspect.class);

    private static final String TENANT_ADMIN_SERVICE = "tenantAdminService";

    private static final String OPENE_MODULE_SERVICE = "OpeneMultiTenantModuleService";

    private static final String OPENE_MULTI_TENANT = "opene-multi-tenant";

    private ListableBeanFactory beanFactory;

    private List<String> moduleSpacesImporterBeanIds = Collections.emptyList();
    private List<String> openeModuleIdsWithMtSuffix = Collections.emptyList();

    @Autowired
    @Qualifier("transactionService")
    private TransactionService transactionService;
    @Autowired
    @Qualifier("storeImporterTransactionHelper")
    private RetryingTransactionHelper retryingTransactionHelper;
    @Autowired
    @Qualifier("namespaceService")
    private NamespaceService namespaceService;
    @Autowired
    @Qualifier("nodeService")
    private NodeService nodeService;
    @Autowired
    @Qualifier("importerComponent")
    private ImporterService importerService;
    @Autowired
    @Qualifier("authenticationContext")
    private AuthenticationContext authenticationContext;

    private ImporterBootstrap extraMtImporter;

    @Autowired
    @Qualifier("moduleService")
    private ModuleService moduleService;

    @Autowired
    @Qualifier("kleClassificationSynchronizer")
    private ClassificationSynchronizer classificationSynchronizer;

    /**
     * Initializes the aspect for MultiTAdminService.
     * The aspect is triggered as a new tenant is created by the system.
     */
    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        if (!beanFactory.containsBean(TENANT_ADMIN_SERVICE) || !beanFactory.containsBean(OPENE_MODULE_SERVICE)) {
            return;
        }
        ClassUtils.checkHasMethods(ModuleService.class, "startModules");
        this.beanFactory = (ListableBeanFactory) beanFactory;
        openeModuleIdsWithMtSuffix = getOpeneModuleIdsWithMtSuffix();
        moduleSpacesImporterBeanIds = getImporterBeanIds(ImporterBootstrap.class);
        moduleSpacesImporterBeanIds.sort(this::sortImporters);
        extraMtImporter = getExtraMtImporter();

        MultiTAdminServiceImpl tenantAdminService = (MultiTAdminServiceImpl) beanFactory
                .getBean(TENANT_ADMIN_SERVICE);
        Advised moduleServiceProxy = (Advised) beanFactory.getBean(OPENE_MODULE_SERVICE);
        tenantAdminService.setModuleService((ModuleService) moduleServiceProxy);

        NameMatchMethodPointcutAdvisor afterStartModulesAdvisor = new NameMatchMethodPointcutAdvisor(
                (AfterReturningAdvice) this::afterStartModules);
        afterStartModulesAdvisor.addMethodName("startModules");
        moduleServiceProxy.addAdvisor(afterStartModulesAdvisor);
    }

    /**
     * Performs necessary imports for a newly created tenant environment.
     */
    public void afterStartModules(Object returnValue, Method method, Object[] args, Object target) {
        moduleSpacesImporterBeanIds.stream().forEach(this::runSpacesImporter);
        logger.debug("Running extra mt importer");
        extraMtImporter.bootstrap();
        logger.debug("Done");
        // It will grab domain of the tenant currently being created, since the
        // MultiTAdminServiceImpl is running as tenant system user.
        classificationSynchronizer.synchronizeTenant(TenantUtil.getCurrentDomain());
    }

    private List<String> getOpeneModuleIdsWithMtSuffix() {
        return moduleService.getAllModules()
                .stream()
                .filter(md -> OpenESDHModel.OPENESDH_REPO_MODULE_ID.equals(md.getId()) || isOpenesdhRepoDependant(md))
                .map(ModuleDetails::getId)
                .map(id -> id + "_mt_")
                .collect(Collectors.toList());
    }

    public static boolean isOpenesdhRepoDependant(ModuleDetails module) {
        return module.getDependencies()
                .stream()
                .filter(d -> OpenESDHModel.OPENESDH_REPO_MODULE_ID.equals(d.getDependencyId()))
                .findAny()
                .isPresent();
    }

    public static boolean isOpeneMultitenantModule(ModuleDetails module) {
        return module.getAliases().contains(module.getId() + "-" + OPENE_MULTI_TENANT);
    }

    private List<String> getImporterBeanIds(Class<?> beanClass) {
        return Arrays.asList(BeanFactoryUtils.beanNamesForTypeIncludingAncestors(this.beanFactory, beanClass))
                .stream()
                .filter(this::isOpeneMultiTenantImporterBean)
                .collect(Collectors.toList());
    }

    private int sortImporters(String id1, String id2) {
        if (id1.startsWith(OpenESDHModel.OPENESDH_REPO_MODULE_ID)
                && !id2.startsWith(OpenESDHModel.OPENESDH_REPO_MODULE_ID)) {
            return -1;
        }

        if (id2.startsWith(OpenESDHModel.OPENESDH_REPO_MODULE_ID)
                && !id1.startsWith(OpenESDHModel.OPENESDH_REPO_MODULE_ID)) {
            return 1;
        }

        return 0;
    }

    private boolean isOpeneMultiTenantImporterBean(String beanId) {
        return openeModuleIdsWithMtSuffix.stream()
                .filter(moduleIdMt -> beanId.startsWith(moduleIdMt))
                .findAny()
                .isPresent();
    }

    /**
     * Initializes an extra importer for some data which is imported using patches (e.g. sites).
     * This is due to patches cannot be re-applied for a tenant environment.
     */
    private ImporterBootstrap getExtraMtImporter() {
        ImporterBootstrap importer = new ImporterBootstrap();
        importer.setTransactionService(transactionService);
        importer.setRetryingTransactionHelper(retryingTransactionHelper);
        importer.setNodeService(nodeService);
        importer.setNamespaceService(namespaceService);
        importer.setAuthenticationContext(authenticationContext);
        importer.setImporterService(importerService);
        importer.setStoreUrl(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE.toString());
        importer.setUseExistingStore(true);

        Properties configuration = ((ImporterBootstrap) beanFactory.getBean(moduleSpacesImporterBeanIds.get(0)))
                .getConfiguration();
        importer.setConfiguration(configuration);

        List<Properties> moduleSpacesBootstrapViews = getImporterBeanIds(Properties.class)
                .stream()
                .map(id -> (Properties) beanFactory.getBean(id))
                .collect(Collectors.toList());
        importer.setBootstrapViews(moduleSpacesBootstrapViews);
        return importer;
    }

    private void runSpacesImporter(String beanId) {
        ImporterBootstrap importer = (ImporterBootstrap) beanFactory.getBean(beanId);
        logger.debug("Running importer: " + beanId);
        importer.bootstrap();
        logger.debug("Done");
    }

}
