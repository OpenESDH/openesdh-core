package dk.openesdh.repo.services.version;

import java.io.Serializable;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.version.Version2ServiceImpl;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import dk.openesdh.repo.model.OpenESDHModel;

@Service("CaseDocumentVersionService")
public class CaseDocumentVersionServiceImpl extends Version2ServiceImpl {

    @Autowired
    @Qualifier("permissionService")
    public void setPermissionService(PermissionService permissionService) {
        super.setPermissionService(permissionService);
    }

    @Override
    @Autowired
    @Qualifier("policyBehaviourFilter")
    public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
        super.setPolicyBehaviourFilter(policyBehaviourFilter);
    }

    @Override
    @Autowired
    @Qualifier("mtAwareNodeService")
    public void setDbNodeService(NodeService nodeService) {
        super.setDbNodeService(nodeService);
    }

    @Override
    @Autowired
    @Qualifier("versionSearchService")
    public void setSearcher(SearchService searcher) {
        super.setSearcher(searcher);
    }

    @Override
    @Value("${version.store.versionComparatorClass}")
    public void setVersionComparatorClass(String versionComparatorClass) {
        super.setVersionComparatorClass(versionComparatorClass);
    }

    @Override
    @Autowired
    @Qualifier("NodeService")
    public void setNodeService(NodeService nodeService) {
        super.setNodeService(nodeService);
    }

    @Override
    @Autowired
    @Qualifier("policyComponent")
    public void setPolicyComponent(PolicyComponent policyComponent) {
        super.setPolicyComponent(policyComponent);
    }

    @Override
    @Autowired
    @Qualifier("dictionaryService")
    public void setDictionaryService(DictionaryService dictionaryService) {
        super.setDictionaryService(dictionaryService);
    }

    @Override
    @PostConstruct
    public void initialise() {
        super.initialise();
    }

    @Override
    protected String invokeCalculateVersionLabel(QName classRef, Version preceedingVersion, int versionNumber,
            Map<String, Serializable> versionProperties) {
        String retainVersionLabel = (String) versionProperties.get(OpenESDHModel.RETAIN_VERSION_LABEL);
        if (StringUtils.isNotEmpty(retainVersionLabel)) {
            return retainVersionLabel;
        }
        return super.invokeCalculateVersionLabel(classRef, preceedingVersion, versionNumber, versionProperties);
    }

}
