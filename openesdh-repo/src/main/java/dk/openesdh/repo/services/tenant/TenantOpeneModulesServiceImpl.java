package dk.openesdh.repo.services.tenant;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.service.cmr.module.ModuleDetails;
import org.alfresco.service.cmr.module.ModuleService;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import dk.openesdh.repo.model.TenantInfo;
import dk.openesdh.repo.services.system.OpenESDHFoldersService;
import dk.openesdh.repo.utils.Utils;

@Service("TenantOpeneModulesService")
public class TenantOpeneModulesServiceImpl implements TenantOpeneModulesService {

    private static final String MSG_TENANT_ENABLED_MODULES_PERMISSION_VIOLATION = "security.permission.err_tenant_enabled_modules_permission_violation";

    @Autowired
    @Qualifier("SearchService")
    private SearchService searchService;
    @Autowired
    @Qualifier("ContentService")
    private ContentService contentService;
    @Autowired
    @Qualifier("ModuleService")
    private ModuleService moduleService;
    @Autowired
    @Qualifier("tenantService")
    private TenantService tenantService;
    @Autowired
    @Qualifier("tenantAdminService")
    private TenantAdminService tenantAdminService;
    @Autowired
    @Qualifier("NamespaceService")
    private NamespaceService namespaceService;

    @Override
    public Collection<TenantInfo> getTenantsInfo() {
        NodeRef mapNodeRef = getTenantModulesMapNodeRef();
        return getTenantInfoMap(mapNodeRef).values();
    }

    @Override
    public List<String> getTenantModules(String tenant) {
        NodeRef mapNodeRef = getTenantModulesMapNodeRef();
        return Optional.ofNullable(getTenantInfoMap(mapNodeRef).get(tenant))
                .map(TenantInfo::getModules)
                .orElse(Collections.emptyList());
    }

    @Override
    public void saveTenantInfo(TenantInfo tenant) throws ContentIOException, JsonProcessingException {
        NodeRef mapNodeRef = getTenantModulesMapNodeRef();
        Map<String, TenantInfo> map = getTenantInfoMap(mapNodeRef);
        TenantInfo tenantInfo = map.get(tenant.getTenantDomain());
        if (Objects.isNull(tenantInfo)) {
            map.put(tenant.getTenantDomain(), tenant);
        } else {
            tenantInfo.setModules(tenant.getModules());
            tenantInfo.setTenantUIContext(tenant.getTenantUIContext());
        }
        saveTenantInfoMap(mapNodeRef, map);
    }

    @Override
    public List<String> getOpeneModules() {
        return moduleService.getAllModules()
                .stream()
                .filter(module -> MultiTenantAdminModulesAspect.isOpeneMultitenantModule(module))
                .map(ModuleDetails::getId)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteTenantModules(String tenant) throws ContentIOException, JsonProcessingException {
        NodeRef mapNodeRef = getTenantModulesMapNodeRef();
        Map<String, TenantInfo> map = getTenantInfoMap(mapNodeRef);
        TenantInfo tenantInfo = map.get(tenant);
        tenantInfo.setModules(Collections.emptyList());
        saveTenantInfoMap(mapNodeRef, map);
    }

    @Override
    public void createTenant(TenantInfo tenant) throws ContentIOException, JsonProcessingException {
        tenantAdminService.createTenant(tenant.getTenantDomain(), tenant.getTenantAdminPassword().toCharArray(),
                tenant.getTenantContentStoreRoot());
        saveTenantInfo(tenant);
    }

    private void saveTenantInfoMap(NodeRef mapNodeRef, Map<String, TenantInfo> map)
            throws ContentIOException, JsonProcessingException {
        ContentWriter writer = contentService.getWriter(mapNodeRef, ContentModel.PROP_CONTENT, true);
        ObjectMapper mapper = new ObjectMapper();
        writer.putContent(mapper.writeValueAsString(map));
    }

    private NodeRef getTenantModulesMapNodeRef() {
        return searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_XPATH,
                OpenESDHFoldersService.TENANTS_MODULES_MAP_XPATH).getNodeRef(0);
    }

    @SuppressWarnings("unchecked")
    private Map<String, TenantInfo> getTenantInfoMap(NodeRef mapNodeRef) {
        ContentReader reader = contentService.getReader(mapNodeRef, ContentModel.PROP_CONTENT);
        String content = reader.getContentString();
        try {
            return new ObjectMapper().readValue(content, new TypeReference<Map<String, TenantInfo>>() {});
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void checkCaseTypeModuleEnabled(QName caseTypeQName) {
        getCurrentTenant().ifPresent(tenant -> checkCaseTypeModuleEnabled(tenant, caseTypeQName));
    }

    private void checkCaseTypeModuleEnabled(String tenant, QName caseTypeQName) {
        String caseType = Utils.extractCaseType(caseTypeQName.toPrefixString(namespaceService));
        getOpeneModules()
                .stream()
                .filter(module -> module.startsWith(caseType))
                .findAny()
                .ifPresent(module -> checkTenantHasModuleEnabled(tenant, module));
    }

    @Override
    public void checkCurrentTenantModuleEnabled(String module) {
        getCurrentTenant().ifPresent(tenant -> checkTenantHasModuleEnabled(tenant, module));
    }

    @Override
    public Optional<String> getCurrentTenantUIContext() {
        return TenantUtil.runAsDefaultTenant(() -> {
            return getCurrentTenant().map(this::getTenantUIContext);
        });
    }

    private String getTenantUIContext(String tenantDomain) {
        NodeRef mapNodeRef = getTenantModulesMapNodeRef();
        return getTenantInfoMap(mapNodeRef).get(tenantDomain).getTenantUIContext();
    }

    private Optional<String> getCurrentTenant() {
        String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();
        if (!tenantService.isTenantUser(currentUser)) {
            return Optional.empty();
        }
        return Optional.of(tenantService.getUserDomain(currentUser));
    }

    private void checkTenantHasModuleEnabled(String tenant, String module) {
        try {
            TenantUtil.runAsDefaultTenant(() -> {
                if (!getTenantModules(tenant).contains(module)) {
                    throw new AccessDeniedException(I18NUtil.getMessage(
                            MSG_TENANT_ENABLED_MODULES_PERMISSION_VIOLATION, module, tenant));
                }
                return null;
            });
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            throw new AccessDeniedException("Problems checking tenant modules", e);
        }
    }
}
