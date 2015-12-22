package dk.openesdh.repo.services.cases;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

public class CasePermissionServiceImpl implements CasePermissionService {

    private NodeService nodeService;
    private NamespaceService namespaceService;

    @Override
    public String getCaseReaderName(String caseType) {
        return getPermision(caseType, CasePermission.READER);
    }

    @Override
    public String getCaseReaderName(QName caseType) {
        return getCaseReaderName(caseType.toPrefixString(namespaceService));
    }

    @Override
    public String getCaseReaderName(NodeRef caseNodeRef) {
        return getCaseReaderName(nodeService.getType(caseNodeRef));
    }

    @Override
    public String getCaseWriterName(String caseType) {
        return getPermision(caseType, CasePermission.WRITER);
    }

    @Override
    public String getCaseWriterName(QName caseType) {
        return getCaseWriterName(caseType.toPrefixString(namespaceService));
    }

    @Override
    public String getCaseWriterName(NodeRef caseNodeRef) {
        return getCaseWriterName(nodeService.getType(caseNodeRef));
    }

    public String getCaseOwnerName(String caseType) {
        return getPermision(caseType, CasePermission.OWNER);
    }

    public String getCaseOwnerName(QName caseType) {
        return getCaseOwnerName(caseType.toPrefixString(namespaceService));
    }

    public String getCaseOwnerName(NodeRef caseNodeRef) {
        return getCaseOwnerName(nodeService.getType(caseNodeRef));
    }


    private String getPermision(String caseType, CasePermission role) {
        return role.getFullName(caseType);
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setNamespaceService(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }
}
