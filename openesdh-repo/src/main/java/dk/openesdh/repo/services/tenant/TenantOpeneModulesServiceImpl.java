package dk.openesdh.repo.services.tenant;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
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
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    @Qualifier("NamespaceService")
    private NamespaceService namespaceService;

    @Override
    public Map<String, List<String>> getAllTenantsModules() throws JsonParseException, JsonMappingException,
            IOException {
        NodeRef mapNodeRef = getTenantModulesMapNodeRef();
        return getTenantModulesMap(mapNodeRef);
    }

    @Override
    public List<String> getTenantModules(String tenant) throws JsonParseException, JsonMappingException,
            IOException {
        return Optional.ofNullable(getAllTenantsModules().get(tenant)).orElse(Collections.emptyList());
    }

    @Override
    public void saveTenantModules(String tenant, List<String> modules) throws JsonParseException,
            JsonMappingException, IOException, ContentIOException, JSONException {
        NodeRef mapNodeRef = getTenantModulesMapNodeRef();
        Map<String, List<String>> map = getTenantModulesMap(mapNodeRef);
        map.put(tenant, modules);
        saveTenantModules(mapNodeRef, map);
    }
    
    @Override
    public List<String> getOpeneModules(){
        return moduleService.getAllModules()
                .stream()
                .filter(module -> MultiTenantAdminModulesAspect.isOpeneMultitenantModule(module))
                .map(ModuleDetails::getId)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteTenantModules(String tenant) throws JsonParseException, JsonMappingException, IOException {
        NodeRef mapNodeRef = getTenantModulesMapNodeRef();
        Map<String, List<String>> map = getTenantModulesMap(mapNodeRef);
        map.remove(tenant);
        saveTenantModules(mapNodeRef, map);
    }

    private void saveTenantModules(NodeRef mapNodeRef, Map<String, List<String>> map) {
        ContentWriter writer = contentService.getWriter(mapNodeRef, ContentModel.PROP_CONTENT, true);
        JSONObject obj = new JSONObject(map);
        writer.putContent(obj.toString());
    }

    private NodeRef getTenantModulesMapNodeRef() {
        return searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_XPATH,
                OpenESDHFoldersService.TENANTS_MODULES_MAP_XPATH).getNodeRef(0);
    }

    @SuppressWarnings("unchecked")
    private Map<String, List<String>> getTenantModulesMap(NodeRef mapNodeRef) throws JsonParseException,
            JsonMappingException, IOException {
        ContentReader reader = contentService.getReader(mapNodeRef, ContentModel.PROP_CONTENT);
        String content = reader.getContentString();
        return new ObjectMapper().readValue(content, Map.class);
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
