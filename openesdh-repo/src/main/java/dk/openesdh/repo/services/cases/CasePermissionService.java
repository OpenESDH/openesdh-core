package dk.openesdh.repo.services.cases;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

public interface CasePermissionService {

    public String getPermissionName(String caseType, CasePermission casePermission);

    public String getPermissionName(QName caseType, CasePermission casePermission);

    public String getPermissionName(NodeRef caseNodeRef, CasePermission casePermission);

    void checkCaseCreatorPermissions(QName caseTypeQName);

    boolean hasCaseCreatorPermission(QName caseTypeQName);
}
