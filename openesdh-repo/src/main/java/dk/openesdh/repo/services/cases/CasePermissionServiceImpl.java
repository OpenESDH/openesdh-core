package dk.openesdh.repo.services.cases;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("CasePermissionService")
public class CasePermissionServiceImpl implements CasePermissionService {

    @Autowired
    @Qualifier("NodeService")
    private NodeService nodeService;
    @Autowired
    @Qualifier("NamespaceService")
    private NamespaceService namespaceService;

    @Override
    public String getPermissionName(String caseType, CasePermission casePermission) {
        return casePermission.getFullName(caseType);
    }

    @Override
    public String getPermissionName(QName caseType, CasePermission casePermission) {
        return getPermissionName(caseType.toPrefixString(namespaceService), casePermission);
    }

    @Override
    public String getPermissionName(NodeRef caseNodeRef, CasePermission casePermission) {
        return getPermissionName(nodeService.getType(caseNodeRef), casePermission);
    }
}
