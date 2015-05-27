package dk.openesdh.repo.services.audit;

import java.util.Map;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.audit.AuditQueryParameters;
import org.alfresco.service.cmr.audit.AuditService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.json.JSONObject;
import org.json.simple.JSONArray;
import org.springframework.extensions.surf.util.I18NUtil;

import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.NodeInfoService;

/**
 * Created by flemmingheidepedersen on 18/11/14.
 */
public class AuditSearchServiceImpl implements AuditSearchService {

    protected JSONObject result;

    private AuditService auditService;

    private AuthorityService authorityService;

    private NodeInfoService nodeInfoService;

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

    public void setNodeInfoService(NodeInfoService nodeInfoService) {
        this.nodeInfoService = nodeInfoService;
    }

    @Override
    public JSONArray getAuditLogByCaseNodeRef(NodeRef nodeRef, int timespan) {

        final AuditQueryParameters auditQueryParameters = new AuditQueryParameters();
        auditQueryParameters.setForward(false);
        auditQueryParameters.setApplicationName("esdh");

        //auditQueryParameters.setFromTime((new Date(+1).getTime()));
        auditQueryParameters.addSearchKey(null, nodeRef.toString());

        if (!authorityService.hasAdminAuthority() && !isCurrentUserCaseOwner(nodeRef)) {
            throw new AccessDeniedException(I18NUtil.getMessage(MSG_ACCESS_DENIED));
        }

        // create auditQueryCallback inside this method, putting it outside, will make it a singleton as the class is a service.
        final OpenESDHAuditQueryCallBack auditQueryCallback = new OpenESDHAuditQueryCallBack();

        // Only users with ACL_METHOD.ROLE_ADMINISTRATOR are allowed to call
        // AuditService methods.
        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>() {
            @Override
            public Object doWork() throws Exception {
                auditService.auditQuery(auditQueryCallback, auditQueryParameters, OpenESDHModel.AUDIT_LOG_MAX);
                return null;
            }
        }, AuthenticationUtil.getAdminUserName());

        // test comment

        return auditQueryCallback.getResult();

    };

    private boolean isCurrentUserCaseOwner(NodeRef nodeRef) {
        String currentUserName = AuthenticationUtil.getFullyAuthenticatedUser();
        return nodeInfoService.isCaseOwner(currentUserName, nodeRef);
    }

}
