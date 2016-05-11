package dk.openesdh.repo.services.cases;

import java.util.Set;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;

@Service("CasePermissionService")
public class CasePermissionServiceImpl implements CasePermissionService {

    private static final String MSG_NO_CASE_CREATOR_PERMISSION_DEFINED = "security.permission.err_no_case_creator_permission_defined";
    private static final String MSG_NO_CASE_CREATOR_GROUP_DEFINED = "security.permission.err_no_case_creator_group_defined";
    private static final String MSG_CASE_CREATOR_PERMISSION_VIOLATION = "security.permission.err_case_creator_permission_violation";

    @Autowired
    @Qualifier("NodeService")
    private NodeService nodeService;
    @Autowired
    @Qualifier("NamespaceService")
    private NamespaceService namespaceService;
    @Autowired
    @Qualifier("AuthorityService")
    private AuthorityService authorityService;

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

    @Override
    public void checkCaseCreatorPermissions(QName caseTypeQName) {
        if (!hasCaseCreatorPermission(caseTypeQName)) {
            throw new AccessDeniedException(
                    I18NUtil.getMessage(MSG_CASE_CREATOR_PERMISSION_VIOLATION, caseTypeQName.getLocalName()));
        }
    }

    @Override
    public boolean hasCaseCreatorPermission(QName caseTypeQName) {
        String caseCreatorGroup = getCaseCreatorGroup(caseTypeQName);

        if (AuthenticationUtil.isRunAsUserTheSystemUser()) {
            return true;
        }

        Set<String> currentUserContainingGroups = authorityService.getContainingAuthoritiesInZone(
                AuthorityType.GROUP, AuthenticationUtil.getFullyAuthenticatedUser(),
                AuthorityService.ZONE_APP_DEFAULT, null, 0);

        return currentUserContainingGroups.contains(caseCreatorGroup);
    }

    private String getCaseCreatorGroup(QName caseTypeQName) {
        String caseCreatorPermissionName = getPermissionName(caseTypeQName, CasePermission.CREATOR);
        if (StringUtils.isEmpty(caseCreatorPermissionName)) {
            throw new AccessDeniedException(
                    I18NUtil.getMessage(MSG_NO_CASE_CREATOR_PERMISSION_DEFINED, caseTypeQName.getLocalName()));
        }

        String caseCreatorGroup = PermissionService.GROUP_PREFIX + caseCreatorPermissionName;
        if (!caseCreatorGroupExists(caseCreatorGroup)) {
            throw new AccessDeniedException(
                    I18NUtil.getMessage(MSG_NO_CASE_CREATOR_GROUP_DEFINED, caseTypeQName.getLocalName()));
        }
        return caseCreatorGroup;
    }

    private boolean caseCreatorGroupExists(String caseCreatorGroup) {
        return authorityService.authorityExists(caseCreatorGroup);
    }
}
