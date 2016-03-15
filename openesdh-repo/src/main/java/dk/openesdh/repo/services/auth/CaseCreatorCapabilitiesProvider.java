package dk.openesdh.repo.services.auth;

import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.cases.CasePermission;
import dk.openesdh.repo.services.cases.CasePermissionService;

@Component
public class CaseCreatorCapabilitiesProvider {

    @Autowired
    @Qualifier(CapabilitiesService.BEAN_ID)
    private CapabilitiesService capabilitiesService;

    @Autowired
    @Qualifier("CasePermissionService")
    private CasePermissionService casePermissionService;

    @Autowired
    @Qualifier("DictionaryService")
    private DictionaryService dictionaryService;

    @PostConstruct
    public void init() {
        capabilitiesService.registerCapabilitiesProvider(this::getCapabilities);
    }
    
    private Map<String, Boolean> getCapabilities(){
        
        return dictionaryService.getSubTypes(OpenESDHModel.TYPE_CASE_BASE, true)
                .stream()
                .filter(type -> !OpenESDHModel.TYPE_CASE_BASE.equals(type))
                .collect(Collectors.toMap(
                        this::getCreatorPermissionName,
                        casePermissionService::hasCaseCreatorPermission
                        ));
    }
    
    private String getCreatorPermissionName(QName caseType){
        return casePermissionService.getPermissionName(caseType, CasePermission.CREATOR);
    }
}
