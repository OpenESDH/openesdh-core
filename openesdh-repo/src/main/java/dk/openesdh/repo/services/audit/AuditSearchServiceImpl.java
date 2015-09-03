package dk.openesdh.repo.services.audit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.audit.AuditQueryParameters;
import org.alfresco.service.cmr.audit.AuditService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.json.JSONObject;
import org.json.simple.JSONArray;
import org.springframework.extensions.surf.util.I18NUtil;

import dk.openesdh.repo.model.OpenESDHModel;

/**
 * Created by flemmingheidepedersen on 18/11/14.
 */
public class AuditSearchServiceImpl implements AuditSearchService {

    protected JSONObject result;

    private AuditService auditService;

    private AuthorityService authorityService;

    private PermissionService permissionService;

    private static final String MSG_ACCESS_DENIED = "auditlog.permissions.err_access_denied";

    // create auditQueryCallback inside this method, putting it outside, will
    // make it a singleton as the class is a service.
    private OpenESDHAuditQueryCallBack auditQueryCallback = new OpenESDHAuditQueryCallBack();

    public AuditSearchServiceImpl(Map<String, Boolean> validKeys) {
        auditQueryCallback = new OpenESDHAuditQueryCallBack(validKeys);
    }

    public AuditSearchServiceImpl() {
        auditQueryCallback = new OpenESDHAuditQueryCallBack();
    }

    public void setAuditService(AuditService auditService) {
        this.auditService = auditService;
    }

    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @Override
    public JSONArray getAuditLogByCaseNodeRef(NodeRef nodeRef, int timespan) {

        if (!authorityService.hasAdminAuthority() && !isCurrentUserReaderWriterOrOwner(nodeRef)) {
            throw new AccessDeniedException(I18NUtil.getMessage(MSG_ACCESS_DENIED));
        }

        final AuditQueryParameters auditQueryParameters = new AuditQueryParameters();
        auditQueryParameters.setForward(false);
        auditQueryParameters.setApplicationName("esdh");

        //auditQueryParameters.setFromTime((new Date(+1).getTime()));
        auditQueryParameters.addSearchKey(null, nodeRef.toString());

        // create auditQueryCallback inside this method, putting it outside, will make it a singleton as the class is a service.
        final OpenESDHAuditQueryCallBack auditQueryCallback = new OpenESDHAuditQueryCallBack();

        // Only users with ACL_METHOD.ROLE_ADMINISTRATOR are allowed to call
        // AuditService methods.
        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>() {
            @Override
            public Void doWork() throws Exception {
                auditService.auditQuery(auditQueryCallback, auditQueryParameters, OpenESDHModel.AUDIT_LOG_MAX);
                return null;
            }
        }, AuthenticationUtil.getAdminUserName());

        // test comment

        return auditQueryCallback.getResult();

    };

    private boolean isCurrentUserReaderWriterOrOwner(NodeRef nodeRef) {
        List<String> rwoGroups = getCaseReadWriteOwnGroups(nodeRef);
        Set<String> currentUserGroups = getCurrentUserGroups();
        for (String userGroup : currentUserGroups) {
            if (rwoGroups.contains(userGroup)) {
                return true;
            }
        }
        return false;
    }

    private Set<String> getCurrentUserGroups() {
        return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Set<String>>() {
            @Override
            public Set<String> doWork() throws Exception {
                String currentUserName = AuthenticationUtil.getFullyAuthenticatedUser();
                Set<String> userGroups = authorityService.getAuthoritiesForUser(currentUserName);
                return userGroups;
            }
        }, AuthenticationUtil.getAdminUserName());
    }

    private List<String> getCaseReadWriteOwnGroups(NodeRef nodeRef){
        List<String> rwoPermissions = Arrays.asList(new String[]{
                OpenESDHModel.PERMISSION_NAME_CASE_SIMPLE_READER,
                OpenESDHModel.PERMISSION_NAME_CASE_SIMPLE_WRITER,
                OpenESDHModel.PERMISSION_NAME_CASE_OWNERS
        });
        ArrayList<String> rwoGroups = new ArrayList<String>();
        Set<AccessPermission> casePermissions = getAllCasePermissions(nodeRef);
        for(AccessPermission accessPermission : casePermissions){
            if (accessPermission.getAuthorityType() == AuthorityType.GROUP
                    && rwoPermissions.contains(accessPermission.getPermission())) {
                rwoGroups.add(accessPermission.getAuthority());
            }
        }
        return rwoGroups;
    }

    private Set<AccessPermission> getAllCasePermissions(final NodeRef nodeRef) {
        return AuthenticationUtil.runAs(
                new AuthenticationUtil.RunAsWork<Set<AccessPermission>>() {
                    @Override
                    public Set<AccessPermission> doWork() throws Exception {
                        return permissionService.getAllSetPermissions(nodeRef);
                    }
                }, AuthenticationUtil.getAdminUserName());
    }

}
