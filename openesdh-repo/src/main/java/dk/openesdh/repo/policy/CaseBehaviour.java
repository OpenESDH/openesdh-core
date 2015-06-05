package dk.openesdh.repo.policy;

import java.util.Set;

import org.alfresco.repo.node.NodeServicePolicies.BeforeCreateNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.util.StringUtils;

import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.cases.CaseService;

/**
 * Created by torben on 19/08/14.
 */
public class CaseBehaviour implements OnCreateNodePolicy, BeforeCreateNodePolicy {

    private static Log LOGGER = LogFactory.getLog(CaseBehaviour.class);

    private static final String MSG_NO_CASE_CREATOR_PERMISSION_DEFINED = "security.permission.err_no_case_creator_permission_defined";
    private static final String MSG_NO_CASE_CREATOR_GROUP_DEFINED = "security.permission.err_no_case_creator_group_defined";
    private static final String MSG_CASE_CREATOR_PERMISSION_VIOLATION = "security.permission.err_case_creator_permission_violation";

    // Dependencies
    private CaseService caseService;
    private PolicyComponent policyComponent;
    private PermissionService permissionService;
    private AuthorityService authorityService;

    // Behaviours
    private Behaviour onCreateNode;
    private Behaviour beforeCreateNode;

    public void setCaseService(CaseService caseService) {
        this.caseService = caseService;
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    public void init() {

        // Create behaviours
        this.onCreateNode = new JavaBehaviour(this, "onCreateNode",
                Behaviour.NotificationFrequency.TRANSACTION_COMMIT);
        this.beforeCreateNode = new JavaBehaviour(this, "beforeCreateNode",
                Behaviour.NotificationFrequency.TRANSACTION_COMMIT);

        // Bind behaviours to node policies
        this.policyComponent.bindClassBehaviour(BeforeCreateNodePolicy.QNAME, OpenESDHModel.TYPE_CASE_BASE,
                this.beforeCreateNode);

        this.policyComponent.bindClassBehaviour(OnCreateNodePolicy.QNAME, OpenESDHModel.TYPE_CASE_BASE,
                this.onCreateNode);
    }

    @Override
    public void onCreateNode(ChildAssociationRef childAssociationRef) {
        caseService.createCase(childAssociationRef);
    }

    @Override
    public void beforeCreateNode(NodeRef parentRef, QName assocTypeQName, QName assocQName, QName caseTypeQName) {
        checkCaseCreatorPermissions(caseTypeQName);
    }

    private void checkCaseCreatorPermissions(QName caseTypeQName) {

        String caseCreatorPermissionName = getCaseCreatorPermissionForCaseType(caseTypeQName);
        if (StringUtils.isEmpty(caseCreatorPermissionName)) {
            throw new AccessDeniedException(I18NUtil.getMessage(
                    MSG_NO_CASE_CREATOR_PERMISSION_DEFINED, caseTypeQName.getLocalName()));
        }

        String caseCreatorGroup = PermissionService.GROUP_PREFIX + caseCreatorPermissionName;
        if (!caseCreatorGroupExists(caseCreatorGroup)) {
            throw new AccessDeniedException(I18NUtil.getMessage(
                    MSG_NO_CASE_CREATOR_GROUP_DEFINED, caseTypeQName.getLocalName()));
        }

        if (AuthenticationUtil.isRunAsUserTheSystemUser()) {
            return;
        }

        Set<String> currentUserAuthorities = authorityService.getAuthorities();

        if (!currentUserAuthorities.contains(caseCreatorGroup)) {
            throw new AccessDeniedException(I18NUtil.getMessage(
                    MSG_CASE_CREATOR_PERMISSION_VIOLATION, caseTypeQName.getLocalName()));
        }
    }

    private String getCaseCreatorPermissionForCaseType(QName caseTypeQName) {
        Set<String> settablePermissions = permissionService.getSettablePermissions(caseTypeQName);
        for (String permission : settablePermissions) {
            if (permission.startsWith("Case") && permission.endsWith("Creator")) {
                return permission;
            }
        }
        return null;
    }

    private boolean caseCreatorGroupExists(String caseCreatorGroup) {
        return authorityService.authorityExists(caseCreatorGroup);
    }
}
