package dk.openesdh.repo.services.audit;

import static dk.openesdh.repo.services.audit.entryhandlers.CaseEmailSentAuditEntryHandler.CASE_EMAIL_RECIPIENTS;
import static dk.openesdh.repo.services.audit.entryhandlers.MemberAddAuditEntryHandler.MEMBER_ADD_PATH;
import static dk.openesdh.repo.services.audit.entryhandlers.MemberRemoveAuditEntryHandler.MEMBER_REMOVE_PATH;
import static dk.openesdh.repo.services.audit.entryhandlers.PartyAddAuditEntryHandler.PARTY_ADD_NAME;
import static dk.openesdh.repo.services.audit.entryhandlers.PartyRemoveAuditEntryHandler.PARTY_REMOVE_NAME;
import static dk.openesdh.repo.services.audit.entryhandlers.TransactionPathAuditEntryHandler.TRANSACTION_PATH;
import static dk.openesdh.repo.services.audit.entryhandlers.WorkflowCancelAuditEntryHandler.WORKFLOW_CANCEL_CASE;
import static dk.openesdh.repo.services.audit.entryhandlers.WorkflowStartAuditEntryHandler.WORKFLOW_START_CASE;
import static dk.openesdh.repo.services.audit.entryhandlers.WorkflowTaskEndAuditEntryHandler.WORKFLOW_END_TASK_CASE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.alfresco.model.BlogIntegrationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.model.ImapModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.audit.AuditQueryParameters;
import org.alfresco.service.cmr.audit.AuditService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.ObjectUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;

import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.audit.entryhandlers.CaseEmailSentAuditEntryHandler;
import dk.openesdh.repo.services.audit.entryhandlers.MemberAddAuditEntryHandler;
import dk.openesdh.repo.services.audit.entryhandlers.MemberRemoveAuditEntryHandler;
import dk.openesdh.repo.services.audit.entryhandlers.PartyAddAuditEntryHandler;
import dk.openesdh.repo.services.audit.entryhandlers.PartyRemoveAuditEntryHandler;
import dk.openesdh.repo.services.audit.entryhandlers.TransactionPathAuditEntryHandler;
import dk.openesdh.repo.services.audit.entryhandlers.WorkflowCancelAuditEntryHandler;
import dk.openesdh.repo.services.audit.entryhandlers.WorkflowStartAuditEntryHandler;
import dk.openesdh.repo.services.audit.entryhandlers.WorkflowTaskEndAuditEntryHandler;
import dk.openesdh.repo.services.cases.CasePermission;

@Service
public class AuditSearchServiceImpl implements AuditSearchService {

    @Autowired
    @Qualifier("AuditService")
    private AuditService auditService;
    @Autowired
    @Qualifier("AuthorityService")
    private AuthorityService authorityService;
    @Autowired
    @Qualifier("PermissionService")
    private PermissionService permissionService;
    @Autowired
    @Qualifier("DictionaryService")
    private DictionaryService dictionaryService;

    private static final String MSG_ACCESS_DENIED = "auditlog.permissions.err_access_denied";

    private final List<String> applications = new ArrayList<>();
    private final Map<String, AuditEntryHandler> auditEntryHandlers = new HashMap<>();
    private final List<QName> ignoredProperties = new ArrayList<>();

    @PostConstruct
    public void init() {
        initApplications();
        initAuditEntryHandlers();
        initIgnoredProperties();
    }

    private void initApplications() {
        applications.add("esdh");
    }

    private void initAuditEntryHandlers() {
        auditEntryHandlers.put(PARTY_REMOVE_NAME, new PartyRemoveAuditEntryHandler());
        auditEntryHandlers.put(PARTY_ADD_NAME, new PartyAddAuditEntryHandler());
        auditEntryHandlers.put(MEMBER_ADD_PATH, new MemberAddAuditEntryHandler());
        auditEntryHandlers.put(MEMBER_REMOVE_PATH, new MemberRemoveAuditEntryHandler());
        auditEntryHandlers.put(TRANSACTION_PATH, new TransactionPathAuditEntryHandler(dictionaryService, ignoredProperties));
        auditEntryHandlers.put(WORKFLOW_START_CASE, new WorkflowStartAuditEntryHandler());
        auditEntryHandlers.put(WORKFLOW_END_TASK_CASE, new WorkflowTaskEndAuditEntryHandler());
        auditEntryHandlers.put(WORKFLOW_CANCEL_CASE, new WorkflowCancelAuditEntryHandler());
        auditEntryHandlers.put(CASE_EMAIL_RECIPIENTS, new CaseEmailSentAuditEntryHandler());
    }

    private void initIgnoredProperties() {
        ignoredProperties.add(ContentModel.PROP_DEAD_PROPERTIES);
        ignoredProperties.add(ContentModel.PROP_NODE_REF);
        ignoredProperties.add(ContentModel.PROP_MODIFIED);
        ignoredProperties.add(ContentModel.PROP_MODIFIER);
        ignoredProperties.add(ContentModel.PROP_VERSION_LABEL);
        ignoredProperties.add(ForumModel.PROP_COMMENT_COUNT);
        ignoredProperties.add(ImapModel.PROP_CHANGE_TOKEN);
        ignoredProperties.add(ImapModel.PROP_UIDVALIDITY);
        ignoredProperties.add(ImapModel.PROP_MAXUID);
        ignoredProperties.add(BlogIntegrationModel.PROP_LINK);
        ignoredProperties.add(ContentModel.PROP_LAST_THUMBNAIL_MODIFICATION_DATA);
    }

    public void registerApplication(String name) {
        applications.add(name);
    }

    @Override
    public void registerEntryHandler(String key, AuditEntryHandler handler) {
        auditEntryHandlers.put(key, handler);
    }

    @Override
    public void registerIgnoredProperties(QName... props) {
        Collections.addAll(ignoredProperties, props);
    }

    @Override
    public JSONArray getAuditLogByCaseNodeRef(NodeRef nodeRef, int timespan) {

        if (!authorityService.hasAdminAuthority() && !isCurrentUserReaderWriterOrOwner(nodeRef)) {
            throw new AccessDeniedException(I18NUtil.getMessage(MSG_ACCESS_DENIED));
        }

        JSONArray result = new JSONArray();
        for (String app : applications) {
            final AuditQueryParameters auditQueryParameters = new AuditQueryParameters();
            auditQueryParameters.setForward(false);

            //auditQueryParameters.setFromTime((new Date(+1).getTime()));
            auditQueryParameters.addSearchKey(null, nodeRef.toString());

            // create auditQueryCallback inside this method, putting it outside, will make it a singleton as the class is a service.
            final OpenESDHAuditQueryCallBack auditQueryCallback = new OpenESDHAuditQueryCallBack(auditEntryHandlers);

            // Only users with ACL_METHOD.ROLE_ADMINISTRATOR are allowed to call
            // AuditService methods.
            runAsAdmin(() -> {
                auditQueryParameters.setApplicationName(app);
                auditService.auditQuery(auditQueryCallback, auditQueryParameters, OpenESDHModel.AUDIT_LOG_MAX);
                return null;
            });
            auditQueryCallback
                    .getResult()
                    .forEach(result::add);
        }
        // sortByTime
        Collections.sort(result, timePropertyDescComparator());
        return result;
    }

    private Comparator<JSONObject> timePropertyDescComparator() {
        return new Comparator<JSONObject>() {
            @Override
            public int compare(JSONObject o1, JSONObject o2) {
                return ObjectUtils.compare(getTime(o2), getTime(o1));
            }

            private long getTime(JSONObject o) {
                return o == null ? null : (long) o.get(AuditEntryHandler.TIME);
            }
        };
    }

    private boolean isCurrentUserReaderWriterOrOwner(NodeRef nodeRef) {
        List<String> rwoGroups = getCaseReadWriteOwnGroups(nodeRef);
        Set<String> currentUserGroups = getCurrentUserGroups();
        return (currentUserGroups.stream().anyMatch((userGroup) -> (rwoGroups.contains(userGroup))));
    }

    private Set<String> getCurrentUserGroups() {
        return runAsAdmin(() -> {
            String currentUserName = AuthenticationUtil.getFullyAuthenticatedUser();
            Set<String> userGroups = authorityService.getAuthoritiesForUser(currentUserName);
            return userGroups;
        });
    }

    private List<String> getCaseReadWriteOwnGroups(NodeRef nodeRef) {
        Set<AccessPermission> casePermissions = getAllCasePermissions(nodeRef);
        List<String> rwoGroups = casePermissions.stream()
                .filter(this::isReaderWriterOwnerGroup)
                .map(accessPermission -> accessPermission.getAuthority())
                .collect(Collectors.toList());
        return rwoGroups;
    }

    private boolean isReaderWriterOwnerGroup(AccessPermission accessPermission) {
        return accessPermission.getAuthorityType() == AuthorityType.GROUP
                && accessPermission.getPermission().matches(CasePermission.REGEXP_ANY);
    }

    private Set<AccessPermission> getAllCasePermissions(final NodeRef nodeRef) {
        return runAsAdmin(() -> permissionService.getAllSetPermissions(nodeRef));
    }

    private <R> R runAsAdmin(AuthenticationUtil.RunAsWork<R> r) {
        return AuthenticationUtil.runAs(r, AuthenticationUtil.getAdminUserName());
    }

    void setService4Tests(AuditService auditService, AuthorityService authorityService) {
        this.auditService = auditService;
        this.authorityService = authorityService;
    }
}
