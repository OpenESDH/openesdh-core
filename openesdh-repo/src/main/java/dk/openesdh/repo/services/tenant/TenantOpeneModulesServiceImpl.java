package dk.openesdh.repo.services.tenant;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.module.ModuleDetails;
import org.alfresco.service.cmr.module.ModuleService;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import dk.openesdh.repo.services.system.OpenESDHFoldersService;

@Service("TenantOpeneModulesService")
public class TenantOpeneModulesServiceImpl implements TenantOpeneModulesService {

    @Autowired
    @Qualifier("SearchService")
    private SearchService searchService;
    @Autowired
    @Qualifier("ContentService")
    private ContentService contentService;
    
    @Autowired
    @Qualifier("ModuleService")
    private ModuleService moduleService;

    @Override
    public Map<String, List<String>> getAllTenantsModules() throws JsonParseException, JsonMappingException,
            IOException {
        NodeRef mapNodeRef = getTenantModulesMapNodeRef();
        return getTenantModulesMap(mapNodeRef);
    }

    @Override
    public List<String> getTenantModules(String tenant) throws JsonParseException, JsonMappingException,
            IOException {
        return getAllTenantsModules().get(tenant);
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
}
