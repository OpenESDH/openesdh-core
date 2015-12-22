package dk.openesdh.repo.services.cases;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

public class CasePermissionServiceImpl implements CasePermissionService {

    private NodeService nodeService;
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

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setNamespaceService(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }
}
