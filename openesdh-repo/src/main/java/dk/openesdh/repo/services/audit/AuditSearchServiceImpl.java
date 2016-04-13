package dk.openesdh.repo.services.audit;

import static dk.openesdh.repo.services.audit.entryhandlers.CaseEmailSentAuditEntryHandler.CASE_EMAIL_RECIPIENTS;
import static dk.openesdh.repo.services.audit.entryhandlers.MemberAddAuditEntryHandler.MEMBER_ADD_PATH;
import static dk.openesdh.repo.services.audit.entryhandlers.MemberRemoveAuditEntryHandler.MEMBER_REMOVE_PATH;
import static dk.openesdh.repo.services.audit.entryhandlers.TransactionPathAuditEntryHandler.TRANSACTION_USER;
import static dk.openesdh.repo.services.audit.entryhandlers.WorkflowCancelAuditEntryHandler.WORKFLOW_CANCEL_CASE;
import static dk.openesdh.repo.services.audit.entryhandlers.WorkflowStartAuditEntryHandler.WORKFLOW_START_CASE;
import static dk.openesdh.repo.services.audit.entryhandlers.WorkflowTaskEndAuditEntryHandler.WORKFLOW_END_TASK_CASE;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
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
import org.alfresco.service.cmr.repository.NodeService;
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
import dk.openesdh.repo.services.audit.entryhandlers.CaseNoteAuditEntryHandler;
import dk.openesdh.repo.services.audit.entryhandlers.MemberAddAuditEntryHandler;
import dk.openesdh.repo.services.audit.entryhandlers.MemberRemoveAuditEntryHandler;
import dk.openesdh.repo.services.audit.entryhandlers.NodePropertyChangesAuditEntrySubHandler;
import dk.openesdh.repo.services.audit.entryhandlers.PartyAuditEntryHandler;
import dk.openesdh.repo.services.audit.entryhandlers.TransactionPathAuditEntryHandler;
import dk.openesdh.repo.services.audit.entryhandlers.WorkflowCancelAuditEntryHandler;
import dk.openesdh.repo.services.audit.entryhandlers.WorkflowStartAuditEntryHandler;
import dk.openesdh.repo.services.audit.entryhandlers.WorkflowTaskEndAuditEntryHandler;
import dk.openesdh.repo.services.cases.CasePermission;
import dk.openesdh.repo.services.contacts.ContactService;

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
    @Autowired
    @Qualifier("ContactService")
    private ContactService contactService;
    @Autowired
    @Qualifier("NodeService")
    private NodeService nodeService;

    private static final String MSG_ACCESS_DENIED = "auditlog.permissions.err_access_denied";

    private final List<String> applications = new ArrayList<>();
    private final Map<String, AuditEntryHandler> auditEntryHandlers = new HashMap<>();
    private final Set<QName> ignoredProperties = new HashSet<>();
    private final Map<Predicate<Map<String, Serializable>>, IAuditEntryHandler> transactionPathEntryHandlers = new HashMap<>();
    private final Set<QName> ignoredAspects = new HashSet<>();

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
        NodePropertyChangesAuditEntrySubHandler nodePropertyChangesHandler = new NodePropertyChangesAuditEntrySubHandler(dictionaryService, ignoredProperties);

        auditEntryHandlers.put(MEMBER_ADD_PATH, new MemberAddAuditEntryHandler());
        auditEntryHandlers.put(MEMBER_REMOVE_PATH, new MemberRemoveAuditEntryHandler());
        auditEntryHandlers.put(TRANSACTION_USER, new TransactionPathAuditEntryHandler(
                dictionaryService,
                nodePropertyChangesHandler,
                ignoredAspects,
                transactionPathEntryHandlers));
        auditEntryHandlers.put(WORKFLOW_START_CASE, new WorkflowStartAuditEntryHandler());
        auditEntryHandlers.put(WORKFLOW_END_TASK_CASE, new WorkflowTaskEndAuditEntryHandler());
        auditEntryHandlers.put(WORKFLOW_CANCEL_CASE, new WorkflowCancelAuditEntryHandler());
        auditEntryHandlers.put(CASE_EMAIL_RECIPIENTS, new CaseEmailSentAuditEntryHandler());

        PartyAuditEntryHandler partyAuditEntryHandler = new PartyAuditEntryHandler();
        auditEntryHandlers.put(PartyAuditEntryHandler.CASE_PARTIES_REMOVE, partyAuditEntryHandler);

        addTransactionPathEntryHandler(CaseNoteAuditEntryHandler::canHandle, new CaseNoteAuditEntryHandler(nodePropertyChangesHandler));
        addTransactionPathEntryHandler(PartyAuditEntryHandler::canHandleTransactionEntry, partyAuditEntryHandler);
    }

    private void initIgnoredProperties() {
        ignoredProperties.add(ContentModel.PROP_DEAD_PROPERTIES);
        ignoredProperties.add(ContentModel.PROP_NODE_REF);
        ignoredProperties.add(ContentModel.PROP_MODIFIED);
        ignoredProperties.add(ContentModel.PROP_MODIFIER);
        ignoredProperties.add(ContentModel.PROP_VERSION_LABEL);
        ignoredProperties.add(ContentModel.PROP_AUTO_VERSION);
        ignoredProperties.add(ForumModel.PROP_COMMENT_COUNT);
        ignoredProperties.add(ImapModel.PROP_CHANGE_TOKEN);
        ignoredProperties.add(ImapModel.PROP_UIDVALIDITY);
        ignoredProperties.add(ImapModel.PROP_MAXUID);
        ignoredProperties.add(BlogIntegrationModel.PROP_LINK);
        ignoredProperties.add(ContentModel.PROP_LAST_THUMBNAIL_MODIFICATION_DATA);
        //locked node properties:
        ignoredProperties.add(ContentModel.PROP_EXPIRY_DATE);
        ignoredProperties.add(ContentModel.PROP_LOCK_LIFETIME);
        ignoredProperties.add(ContentModel.PROP_LOCK_OWNER);
        ignoredProperties.add(ContentModel.PROP_LOCK_TYPE);
        ignoredProperties.add(ContentModel.PROP_LOCK_ADDITIONAL_INFO);
        ignoredProperties.add(OpenESDHModel.PROP_OE_LOCKED_BY);
        ignoredProperties.add(OpenESDHModel.PROP_OE_ORIGINAL_OWNER);
        ignoredProperties.add(OpenESDHModel.PROP_OE_LOCKED_DATE);
        ignoredProperties.add(ContentModel.PROP_OWNER);
    }

    public void registerApplication(String name) {
        applications.add(name);
    }

    @Override
    public void registerEntryHandler(String key, AuditEntryHandler handler) {
        auditEntryHandlers.put(key, handler);
    }

    @Override
    public void registerIgnoredAspects(QName... aspect) {
        Collections.addAll(ignoredAspects, aspect);
    }

    @Override
    public void addTransactionPathEntryHandler(Predicate<Map<String, Serializable>> predicate,
            IAuditEntryHandler handler) {
        transactionPathEntryHandlers.put(predicate, handler);
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
