package dk.openesdh.repo.services.cases;

import dk.openesdh.repo.model.CaseInfo;
import dk.openesdh.repo.model.CaseInfoImpl;
import dk.openesdh.repo.model.OpenESDHModel;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.permissions.PermissionReference;
import org.alfresco.repo.security.permissions.impl.ModelDAO;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.dictionary.*;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.*;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.security.access.AccessDeniedException;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by torben on 19/08/14.
 */
public class CaseServiceImpl implements CaseService {

    private static final String MSG_NO_CASE_CREATOR_PERMISSION_DEFINED = "security.permission.err_no_case_creator_permission_defined";
    private static final String MSG_NO_CASE_CREATOR_GROUP_DEFINED = "security.permission.err_no_case_creator_group_defined";
    private static final String MSG_CASE_CREATOR_PERMISSION_VIOLATION = "security.permission.err_case_creator_permission_violation";

    private static final String ASSIGN_CASE_ID_RULE_TITLE = "Assign caseId to case documents";

    private static final String CASE = "Case";
    private static final String CREATOR = "Creator";
    private static final String READER = "Reader";
    private static final String WRITER = "Writer";

    private static Logger LOGGER = Logger.getLogger(CaseServiceImpl.class);

    /**
     * repositoryHelper cannot be autowired - seemingly
     */
    private Repository repositoryHelper;
    private NodeService nodeService;
    private ContentService contentService;
    private SearchService searchService;
    private LockService lockService;

    private AuthorityService authorityService;

    private PermissionService permissionService;
    private ModelDAO permissionsModelDAO;
    private TransactionService transactionService;
    private DictionaryService dictionaryService;
    private OwnableService ownableService;
    private RuleService ruleService;
    private ActionService actionService;

    //<editor-fold desc="Service setters">
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    public void setLockService(LockService lockService) {
        this.lockService = lockService;
    }

    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    public void setRepositoryHelper(Repository repositoryHelper) {
        this.repositoryHelper = repositoryHelper;
    }

    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    public void setOwnableService(OwnableService ownableService) {
        this.ownableService = ownableService;
    }

    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    //</editor-fold>

    public void setPermissionsModelDAO(ModelDAO permissionsModelDAO) {
        this.permissionsModelDAO = permissionsModelDAO;
    }

    public void setRuleService(RuleService ruleService) {
        this.ruleService = ruleService;
    }

    public void setActionService(ActionService actionService) {
        this.actionService = actionService;
    }

    @Override
    public void addAuthorityToRole(final String authorityName, final String role, final NodeRef caseNodeRef) {
        checkCanUpdateCaseRoles(caseNodeRef);

        runAsAdmin(() -> {
            String caseId = getCaseId(caseNodeRef);
            String groupName = getCaseRoleGroupName(caseId, role);
            authorityService.addAuthority(groupName, authorityName);
            return null;
        });
    }

    @Override
    public void addAuthorityToRole(final NodeRef authorityNodeRef, final String role, final NodeRef caseNodeRef) {
        addAuthorityToRole(getAuthorityName(authorityNodeRef), role,
                caseNodeRef);
    }

    @Override
    public void addAuthoritiesToRole(final List<NodeRef> authorities, final String role, final NodeRef caseNodeRef) {
        checkCanUpdateCaseRoles(caseNodeRef);

        runAsAdmin(() -> {
            String caseId = getCaseId(caseNodeRef);
            final String groupName = getCaseRoleGroupName(caseId, role);
            if (!authorityService.authorityExists(groupName)) {
                return null;
            }
            transactionService.getRetryingTransactionHelper().doInTransaction(
                    new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
                        @Override
                        public Object execute() throws Throwable {
                            for (NodeRef authorityNodeRef : authorities) {
                                String authority = getAuthorityName(authorityNodeRef);
                                if (authority != null) {
                                    authorityService.addAuthority(groupName, authority);
                                }
                            }
                            return null;
                        }
                    });
            return null;
        });
    }

    @Override
    public boolean canUpdateCaseRoles(String user, NodeRef caseNodeRef) {
        if (isClosed(caseNodeRef)) {
            return false;
        }
        return authorityService.isAdminAuthority(user) ||
                isCaseOwner(user, caseNodeRef);
    }

    @Override
    public void changeAuthorityRole(final String authorityName, final String fromRole, final String toRole, final NodeRef caseNodeRef) {
        checkCanUpdateCaseRoles(caseNodeRef);

        runAsAdmin(() -> {
            // Do in transaction
            transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionHelper
                    .RetryingTransactionCallback<Object>() {
                @Override
                public Object execute() throws Throwable {
                    removeAuthorityFromRole(authorityName, fromRole, caseNodeRef);
                    addAuthorityToRole(authorityName, toRole, caseNodeRef);
                    return null;
                }
            });
            return null;
        });
    }

    @Override
    public void checkCaseCreatorPermissions(QName caseTypeQName) {
        String caseCreatorPermissionName = getCaseCreatorPermissionForCaseType(caseTypeQName);
        if (StringUtils.isEmpty(caseCreatorPermissionName)) {
            throw new AccessDeniedException(I18NUtil.getMessage(MSG_NO_CASE_CREATOR_PERMISSION_DEFINED,
                    caseTypeQName.getLocalName()));
        }

        String caseCreatorGroup = PermissionService.GROUP_PREFIX + caseCreatorPermissionName;
        if (!caseCreatorGroupExists(caseCreatorGroup)) {
            throw new AccessDeniedException(I18NUtil.getMessage(MSG_NO_CASE_CREATOR_GROUP_DEFINED,
                    caseTypeQName.getLocalName()));
        }

        if (AuthenticationUtil.isRunAsUserTheSystemUser()) {
            return;
        }

        Set<String> currentUserAuthorities = authorityService.getAuthorities();

        if (!currentUserAuthorities.contains(caseCreatorGroup)) {
            throw new AccessDeniedException(I18NUtil.getMessage(MSG_CASE_CREATOR_PERMISSION_VIOLATION,
                    caseTypeQName.getLocalName()));
        }
    }

    @Override
    public void createCase(final ChildAssociationRef childAssocRef) {
        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>() {
            @Override
            public Void doWork() {
                NodeRef caseNodeRef = childAssocRef.getChildRef();
                LOGGER.info("caseNodeRef " + caseNodeRef);

                //Create folder structure
                NodeRef casesRootNodeRef = getCasesRootNodeRef();

                NodeRef caseFolderNodeRef = getCaseFolderNodeRef(casesRootNodeRef);
                // Get a unique number to append to the caseId.
                long caseUniqueNumber = getCaseUniqueId(caseNodeRef);

                setupCase(caseNodeRef, caseFolderNodeRef, caseUniqueNumber);

                return null;
            }
        }, AuthenticationUtil.getAdminUserName());
    }

    @Override
    public NodeRef getOpenESDHRootFolder() {
        NodeRef companyHomeNodeRef = repositoryHelper.getCompanyHome();
        NodeRef openESDH_root_nodeRef = nodeService.getChildByName(companyHomeNodeRef, ContentModel.ASSOC_CONTAINS, OPENESDH_ROOT_CONTEXT);

        //Throw an exception. This should have been created on first boot
        if (openESDH_root_nodeRef == null)
            throw new AlfrescoRuntimeException("The openESDH \"ROOT\" context folder has not been initialised.");

        return openESDH_root_nodeRef;
    }

    @Override
    public NodeRef getCasesRootNodeRef() {
        NodeRef casesRootNodeRef = nodeService.getChildByName(getOpenESDHRootFolder(), ContentModel.ASSOC_CONTAINS, CASES_ROOT);

        //Throw an exception. This should have been created on first boot along with the context root folder
        if (casesRootNodeRef == null)
            throw new AlfrescoRuntimeException("The openESDH \"CASES\" root folder has not been initialised.");

        return casesRootNodeRef;
    }

    @Override
    public NodeRef getCasesTypeStorageRootNodeRef() {
        NodeRef typesRootNodeRef = nodeService.getChildByName(getCasesRootNodeRef(), ContentModel.ASSOC_CONTAINS, CASES_TYPES_ROOT);

        //Throw an exception. This should have been created on first boot along with the context root folder
        if (typesRootNodeRef == null)
            throw new AlfrescoRuntimeException("The openESDH folder for case types storage doesn't exist.");

        return typesRootNodeRef;
    }

    @Override
    public Set<String> getRoles(NodeRef caseNodeRef) {
        return permissionService.getSettablePermissions(caseNodeRef);
    }

    @Override
    public Set<String> getAllRoles(NodeRef caseNodeRef) {
        Set<String> roles = getRoles(caseNodeRef);
        roles.add("CaseOwners");
        return roles;
    }

    @Override
    public NodeRef getCaseById(String caseId) {
        Matcher matcher = CASE_ID_PATTERN.matcher(caseId);
        if (matcher.matches()) {
            // Get the DBID from the case ID, and grab the NodeRef
            try {
                final Long dbid = Long.parseLong(matcher.group(1));
                NodeRef caseNodeRef = AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<NodeRef>() {
                    @Override
                    public NodeRef doWork() throws Exception {
                        return nodeService.getNodeRef(dbid);
                    }
                });

                // Check that it exists and is really a case node (extending type
                // "case:base")
                if (!Objects.isNull(caseNodeRef) && nodeService.exists(caseNodeRef) && isCaseNode(caseNodeRef)) {
                    return caseNodeRef;
                }
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    public String getCaseId(NodeRef caseNodeRef) {
        return (String) nodeService.getProperty(caseNodeRef, OpenESDHModel.PROP_OE_ID);
    }

    @Override
    public CaseInfo getCaseInfo(NodeRef caseNodeRef) {
        CaseInfo caseInfo;

        // Get the properties
        Map<QName, Serializable> properties = this.getCaseProperties(caseNodeRef);
        String caseId = this.getCaseId(caseNodeRef);
        String title = (String) properties.get(ContentModel.PROP_TITLE);
        String description = (String) properties.get(ContentModel.PROP_DESCRIPTION);

        // Create and return the site information
        caseInfo = new CaseInfoImpl(caseNodeRef, caseId, title, description, properties);

        caseInfo.setCreatedDate(DefaultTypeConverter.INSTANCE.convert(Date.class, properties.get(ContentModel.PROP_CREATED)));
        caseInfo.setLastModifiedDate(DefaultTypeConverter.INSTANCE.convert(Date.class, properties.get(ContentModel.PROP_MODIFIED)));
        caseInfo.setStartDate(DefaultTypeConverter.INSTANCE.convert(Date.class, properties.get(OpenESDHModel.PROP_CASE_STARTDATE)));
        caseInfo.setEndDate(DefaultTypeConverter.INSTANCE.convert(Date.class, properties.get(OpenESDHModel.PROP_CASE_ENDDATE)));

        return caseInfo;
    }

    @Override
    public CaseInfo getCaseInfo(String caseId) {
        return getCaseInfo(getCaseById(caseId));
    }

    @Override
    public JSONArray getCaseCreateFormWidgets(String caseType) {
        JSONArray widgets = null;
        Pattern modelPattern = Pattern.compile("(\\w+):(\\w+)");
        Matcher matcher = modelPattern.matcher(caseType);
        if (matcher.find()) { // then get the folder name from the postfix
            caseType = StringUtils.substringAfter(caseType, ":");
        }
        //Recursively step 2 levels down to get the file
        NodeRef typesFolder;
        NodeRef caseFolder;
        NodeRef caseFormsFolder;
        try {
            typesFolder = getCasesTypeStorageRootNodeRef();
            caseFolder = nodeService.getChildByName(typesFolder, ContentModel.ASSOC_CONTAINS, caseType);
            caseFormsFolder = nodeService.getChildByName(caseFolder, ContentModel.ASSOC_CONTAINS, "forms");
            //Now read the file
            NodeRef formWidgetsFile = nodeService.getChildByName(caseFormsFolder, ContentModel.ASSOC_CONTAINS, "create-form.js");
            //Read the file contents
            ContentReader contentReader = contentService.getReader(formWidgetsFile, ContentModel.PROP_CONTENT);
            String tmp = contentReader.getContentString();
            JSONObject unparsedJSON = new JSONObject(tmp);
            widgets = unparsedJSON.getJSONArray("widgets");
        } catch (Exception ge) {
            LOGGER.warn("\n\n\n====>CaseServiceImpl - 379:\nerror with retrieving widgets: " + ge.getMessage() + "\n\n");
        }

        return widgets;
    }

    @Override
    public List<Long> getCaseDbIdsWhereAuthorityHasRole(NodeRef authorityNodeRef, String role) {
        List<Long> caseDbIds = new ArrayList<>();
        Set<String> containingAuthorities = authorityService.getContainingAuthorities(null,
                getAuthorityName(authorityNodeRef), false);

        Pattern pattern = Pattern.compile("GROUP_case_\\d+-(\\d+)_" + role);
        for (String containingAuthority : containingAuthorities) {
            Matcher matcher = pattern.matcher(containingAuthority);
            if (matcher.matches()) {
                caseDbIds.add(Long.parseLong(matcher.group(1)));
            }
        }
        return caseDbIds;
    }

    @Override
    public Map<String, Set<String>> getMembersByRole(NodeRef caseNodeRef, boolean noExpandGroups, boolean includeOwner) {
        String caseId = getCaseId(caseNodeRef);
        Set<String> roles = includeOwner ? getAllRoles(caseNodeRef) : getRoles(caseNodeRef);
        Map<String, Set<String>> membersByRole = new HashMap<>();
        for (String role : roles) {
            String groupName = getCaseRoleGroupName(caseId, role);
            Set<String> authorities = authorityService.getContainedAuthorities
                    (null, groupName, noExpandGroups);
            membersByRole.put(role, authorities);

        }
        return membersByRole;
    }

    /**
     * \
     * Get the nodeRef for the folder in which to place the case.
     *
     * @param casesRootNodeRef The root folder nodeRef in the case hierarchy
     * @return The NodeRef for the folder in which to place the case
     */
    @Override
    public NodeRef getCaseFolderNodeRef(NodeRef casesRootNodeRef) {
        return AuthenticationUtil.runAsSystem(() -> {
            NodeRef casesYearNodeRef = getCasePathNodeRef(casesRootNodeRef, Calendar.YEAR);
            NodeRef casesMonthNodeRef = getCasePathNodeRef(casesYearNodeRef, Calendar.MONTH);
            return getCasePathNodeRef(casesMonthNodeRef, Calendar.DATE);
        });
    }

    @Override
    public NodeRef getParentCase(NodeRef nodeRef) {
        if (isCaseNode(nodeRef)) {
            return nodeRef;
        }
        if (nodeRef.equals(getCasesRootNodeRef())) {
            // Case nodes and cases root don't have cases as ancestors
            return null;
        }

        NodeRef parent = nodeService.getPrimaryParent(nodeRef).getParentRef();
        if (parent == null) {
            return null;
        } else {
            if (isCaseNode(parent)) {
                return parent;
            }
            return getParentCase(parent);
        }
    }

    @Override
    public NodeRef getDocumentsFolder(NodeRef caseNodeRef) {
        return nodeService.getChildByName(caseNodeRef, ContentModel.ASSOC_CONTAINS, OpenESDHModel.DOCUMENTS_FOLDER_NAME);
    }

    @Override
    public Map<String, Object> getSearchDefinition(QName caseType) {

        Map<String, Object> model = new HashMap<>();
        Map<QName, ClassDefinition> classDefs = new HashMap<>();

        List<PropertyDefinition> propertyDefs = new ArrayList<>();

        for (QName classType : dictionaryService.getSubTypes(caseType, true)) {
            ClassDefinition classDefinition = dictionaryService.getClass(classType);
            classDefs.put(classType, classDefinition);

            Map<QName, PropertyDefinition> classProperties = classDefinition.getProperties();

            for (QName propertyName : classProperties.keySet()) {
                PropertyDefinition p = classProperties.get(propertyName);
                propertyDefs.addAll(classProperties.values());

            }

            for (AspectDefinition aspect : classDefinition.getDefaultAspects()) {
                ClassDefinition aspectClassDefinition = dictionaryService.getClass(aspect.getName());
                Map<QName, PropertyDefinition> aspectProperties = aspectClassDefinition.getProperties();
                for (QName propertyName : aspectProperties.keySet()) {
                    propertyDefs.addAll(aspectProperties.values());
                }
            }

            Map<QName, AssociationDefinition> associations = classDefinition.getAssociations();
        }

        JSONObject availableFilters = new JSONObject();
        ArrayList classDefinitions1 = new ArrayList(classDefs.values());
        model.put("classdefs", classDefinitions1);
        model.put("propertydefs", propertyDefs);
        model.put("availableFilters", availableFilters);
        model.put("messages", this.dictionaryService);
        return model;
    }

    @Override
    public List<String> getCaseUserPermissions(String caseId) {

        // Consumer doesn't have _ReadPermissions permission therefore run as
        // syste

        Set<AccessPermission> allPermissionsSetToCase =
                AuthenticationUtil.runAsSystem(() -> permissionService.getAllSetPermissions(getCaseById(caseId)));

        Set<String> currentUserAuthorities =
                AuthenticationUtil.runAsSystem(() -> authorityService.getAuthoritiesForUser(AuthenticationUtil.getFullyAuthenticatedUser()));

        Predicate<AccessPermission> isPermissionGrantedForCurrentUser =
                (permission) -> permission.getAccessStatus() == AccessStatus.ALLOWED && currentUserAuthorities.contains(permission.getAuthority());

        return allPermissionsSetToCase.stream()
                .filter(permission -> isPermissionGrantedForCurrentUser.test(permission))
                .map(permission -> permission.getPermission())
                .collect(Collectors.toList());
    }

    @Override
    public boolean isCaseNode(NodeRef nodeRef) {
        QName type = nodeService.getType(nodeRef);
        return dictionaryService.isSubClass(type, OpenESDHModel.TYPE_CASE_BASE);
    }

    @Override
    public boolean isCaseDocNode(NodeRef nodeRef) {
        QName type = nodeService.getType(nodeRef);
        return dictionaryService.isSubClass(type, OpenESDHModel.TYPE_DOC_BASE) && nodeService.hasAspect(nodeRef, OpenESDHModel.ASPECT_OE_CASE_ID);
    }

    @Override
    public void removeAuthorityFromRole(final String authorityName, final String role, final NodeRef caseNodeRef) {
        checkCanUpdateCaseRoles(caseNodeRef);
        runAsAdmin(() -> {
            String caseId = getCaseId(caseNodeRef);
            String groupName = getCaseRoleGroupName(caseId, role);
            if (authorityService.authorityExists(groupName) && authorityService.authorityExists(authorityName)) {
                authorityService.removeAuthority(groupName, authorityName);
            }
            return null;
        });
    }

    protected <R> R runAsAdmin(RunAsWork<R> r) {
        return AuthenticationUtil.runAs(r, OpenESDHModel.ADMIN_USER_NAME);
    }

    @Override
    public void removeAuthorityFromRole(final NodeRef authorityNodeRef, final String role, final NodeRef caseNodeRef) {
        removeAuthorityFromRole(getAuthorityName(authorityNodeRef), role, caseNodeRef);
    }

    public void checkCanClose(NodeRef nodeRef) throws
            AccessDeniedException {
        String user = AuthenticationUtil.getRunAsUser();
        if (!canClose(user, nodeRef)) {
            throw new AccessDeniedException(user + " is not allowed to " +
                    "close the case " + nodeRef);
        }
    }

    public void checkCanReopen(NodeRef nodeRef) throws
            AccessDeniedException {
        String user = AuthenticationUtil.getRunAsUser();
        if (!canReopen(user, nodeRef)) {
            throw new AccessDeniedException(user + " is not allowed to " +
                    "reopen the case " + nodeRef);
        }
    }

    @Override
    public boolean canClose(String user, NodeRef nodeRef) {
        return isCaseNode(nodeRef) && !isClosed(nodeRef);
    }

    @Override
    public boolean canReopen(String user, NodeRef nodeRef) {
        return isCaseNode(nodeRef) && authorityService.isAdminAuthority(user);
    }

    @Override
    public boolean isClosed(NodeRef nodeRef) {
        return nodeService.hasAspect(nodeRef, OpenESDHModel.ASPECT_OE_LOCKED);
    }

    @Override
    public void close(final NodeRef nodeRef) {
        checkCanClose(nodeRef);
        // Run it in a transaction
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
            @Override
            public Object execute() throws Throwable {
                lockImpl(nodeRef);
                lockCaseGroups(nodeRef);
                return null;
            }
        });
    }

    // Suppress warning about READ_ONLY_LOCK being deprecated
    @SuppressWarnings("deprecation")
    private void lockImpl(final NodeRef nodeRef) {
        if (nodeService.hasAspect(nodeRef, OpenESDHModel.ASPECT_OE_LOCKED)) {
            // Don't touch, already locked
            LOGGER.warn("Node already has locked aspect when locking: " + nodeRef);
            return;
        }
        AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Void>() {
            @Override
            public Void doWork() {
                // Set the owner to be the System user to prevent the
                // original owner from modifying the node
                String originalOwner = null;
                if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_OWNABLE)) {
                    originalOwner = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_OWNER);
                }
                ownableService.setOwner(nodeRef, AuthenticationUtil.getSystemUserName());

                // Add locked aspect
                Map<QName, Serializable> props = new HashMap<>();
                props.put(OpenESDHModel.PROP_OE_LOCKED_BY, AuthenticationUtil.getFullyAuthenticatedUser());
                props.put(OpenESDHModel.PROP_OE_LOCKED_DATE, new Date());
                // Save the original owner, or null if there wasn't any
                props.put(OpenESDHModel.PROP_OE_ORIGINAL_OWNER, originalOwner);
                nodeService.addAspect(nodeRef, OpenESDHModel.ASPECT_OE_LOCKED, props);

                // Add the LockPermissionsToDeny permission set to deny everyone
                permissionService.setPermission(nodeRef, PermissionService.ALL_AUTHORITIES, "LockPermissionsToDeny", false);

                // Add a never-expiring lock as the system user
                String authenticatedUser = AuthenticationUtil.getFullyAuthenticatedUser();
                try {
                    AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
                    lockService.lock(nodeRef, LockType.READ_ONLY_LOCK, 0);
                } finally {
                    AuthenticationUtil.setFullyAuthenticatedUser(authenticatedUser);
                }
                return null;
            }
        });

        List<ChildAssociationRef> childAssociationRefs = nodeService.getChildAssocs(nodeRef);
        for (ChildAssociationRef childAssociationRef : childAssociationRefs) {
            lockImpl(childAssociationRef.getChildRef());
        }
    }

    private void lockCaseGroups(final NodeRef caseNodeRef) {
        AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Void>() {
            @Override
            public Void doWork() {
                List<ChildAssociationRef> childAssociationRefs = nodeService.getChildAssocs(caseNodeRef);
                Set<String> roles = getAllRoles(caseNodeRef);
                for (String role : roles) {
                    NodeRef authorityNodeRef = authorityService.getAuthorityNodeRef(
                            getCaseRoleGroupName(getCaseId(caseNodeRef), role));
                    permissionService.setPermission(authorityNodeRef,
                            PermissionService.ALL_AUTHORITIES, "LockPermissionsToDeny", false);
                }
                return null;
            }
        });
    }

    @Override
    public void reopen(final NodeRef nodeRef) {
        checkCanReopen(nodeRef);
        // Run it in a transaction
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
            @Override
            public Object execute() throws Throwable {
                unlockImpl(nodeRef);
                unlockCaseGroups(nodeRef);
                return null;
            }
        });
    }

    private void unlockImpl(final NodeRef nodeRef) {
        AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Void>() {
            @Override
            public Void doWork() {
                lockService.unlock(nodeRef);

                // Only delete the permission if permissions are not
                // inherited (shared)
                if (!permissionService.getInheritParentPermissions(nodeRef)) {
                    permissionService.deletePermission(nodeRef, PermissionService.ALL_AUTHORITIES, "LockPermissionsToDeny");
                }

                String originalOwner = (String) nodeService.getProperty(nodeRef, OpenESDHModel.PROP_OE_ORIGINAL_OWNER);
                if (originalOwner != null) {
                    // Restore the node's original owner
                    ownableService.setOwner(nodeRef, originalOwner);
                } else {
                    // Remove the ownable aspect, since it wasn't there to
                    // begin with
                    nodeService.removeAspect(nodeRef, ContentModel.ASPECT_OWNABLE);
                }
                nodeService.removeAspect(nodeRef, OpenESDHModel.ASPECT_OE_LOCKED);
                return null;
            }
        });

        List<ChildAssociationRef> childAssociationRefs = nodeService.getChildAssocs(nodeRef);
        for (ChildAssociationRef childAssociationRef : childAssociationRefs) {
            unlockImpl(childAssociationRef.getChildRef());
        }
    }

    private void unlockCaseGroups(final NodeRef caseNodeRef) {
        AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Void>() {
            @Override
            public Void doWork() {
                List<ChildAssociationRef> childAssociationRefs = nodeService.getChildAssocs(caseNodeRef);
                Set<String> roles = getAllRoles(caseNodeRef);
                for (String role : roles) {
                    NodeRef authorityNodeRef = authorityService.getAuthorityNodeRef(
                            getCaseRoleGroupName(getCaseId(caseNodeRef), role));
                    permissionService.deletePermission(authorityNodeRef,
                            PermissionService.ALL_AUTHORITIES, "LockPermissionsToDeny");
                }
                return null;
            }
        });
    }
    //</editor-fold>

    protected boolean isCaseOwner(String user, NodeRef caseNodeRef) {
        String caseId = getCaseId(caseNodeRef);
        // Check that the user is a case owner
        Set<String> authorities = authorityService.getContainedAuthorities(
                AuthorityType.USER, getCaseRoleGroupName(caseId, "CaseOwners"),
                false);
        return authorities.contains(user);
    }

    // Copied (almost directly) from AuthorityDAOImpl because it is not exposed
    // in the AuthorityService public API
    protected String getAuthorityName(NodeRef authorityRef) {
        String name = null;
        if (nodeService.exists(authorityRef)) {
            QName type = nodeService.getType(authorityRef);
            if (dictionaryService.isSubClass(type, ContentModel.TYPE_AUTHORITY_CONTAINER)) {
                name = (String) nodeService.getProperty(authorityRef, ContentModel.PROP_AUTHORITY_NAME);
            } else if (dictionaryService.isSubClass(type, ContentModel.TYPE_PERSON)) {
                name = (String) nodeService.getProperty(authorityRef, ContentModel.PROP_USERNAME);
            }
        }
        return name;
    }

    /**
     * Creates individual groups for provided case and sets appropriate
     * permissions
     *
     * @param caseNodeRef
     * @param caseId
     */
    void setupPermissionGroups(NodeRef caseNodeRef, String caseId) {
        Set<String> settablePermissions = permissionService.getSettablePermissions(caseNodeRef);

        for (Iterator<String> iterator = settablePermissions.iterator(); iterator.hasNext(); ) {
            String permission = iterator.next();
            setupPermissionGroup(caseNodeRef, caseId, permission);
        }
    }

    String setupPermissionGroup(NodeRef caseNodeRef, String caseId, String permission) {
        String groupSuffix = getCaseRoleGroupAuthorityName(caseId, permission);
        String groupName = getCaseRoleGroupName(caseId, permission);

        if (!authorityService.authorityExists(groupName)) {
            HashSet<String> shareZones = new HashSet<>();
            shareZones.add(AuthorityService.ZONE_APP_SHARE);
            shareZones.add(AuthorityService.ZONE_AUTH_ALFRESCO);
            // Add the authority group to the Share zone so that it is not
            // searchable from the authority picker.
            groupName = authorityService.createAuthority(AuthorityType.GROUP, groupSuffix, groupSuffix, shareZones);
        }
        permissionService.setPermission(caseNodeRef, groupName, permission, true);
        NodeRef authorityNodeRef = authorityService.getAuthorityNodeRef(groupName);

        // TODO: Allow only certain roles to read case members from case
        // role groups.
        return groupName;
    }

    /**
     * Create all required NodeRefs
     *
     * @param parentFolderNodeRef
     * @param name
     * @return
     */
    private NodeRef createNode(final NodeRef parentFolderNodeRef, final String name) {
        Map<QName, Serializable> properties = new HashMap<>();
        properties.put(ContentModel.PROP_NAME, name);
        return nodeService.createNode(parentFolderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(OpenESDHModel.CASE_URI, name), ContentModel.TYPE_FOLDER, properties).getChildRef();
    }

    void setupCase(NodeRef caseNodeRef, NodeRef caseFolderNodeRef, long caseUniqueNumber) {
        String caseId = getCaseId(caseUniqueNumber);

        //Move Case to new location
        nodeService.moveNode(caseNodeRef, caseFolderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(OpenESDHModel.CASE_URI, caseId));

        //Create Groups and assign permission on new case
        managePermissions(caseNodeRef, caseId);

        // Set id on case
        nodeService.setProperty(caseNodeRef, OpenESDHModel.PROP_OE_ID, caseId);

        //Renaming of Node to value of Case Id
        nodeService.setProperty(caseNodeRef, ContentModel.PROP_NAME, caseId);

        // Create folder for documents
        // TODO: Test
        NodeRef documentsNodeRef = createNode(caseNodeRef, OpenESDHModel.DOCUMENTS_FOLDER_NAME);
        nodeService.addAspect(documentsNodeRef, OpenESDHModel.ASPECT_DOCUMENT_CONTAINER, null);
    }

    String getCaseId(long uniqueNumber) {
        //Generating Case ID
        DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        Date date = new Date();
        StringBuilder caseId = new StringBuilder(dateFormat.format(date));
        caseId.append("-");
        caseId.append(uniqueNumber);
        LOGGER.info("Case Id is " + caseId);

        return caseId.toString();
    }

    private <T> Collection<T> reorderedValues(List<ClassDefinition> sortedClassDefs, Map<QName, T> dependent) {
        ArrayList result = new ArrayList(sortedClassDefs.size());
        Iterator i$ = sortedClassDefs.iterator();

        while (i$.hasNext()) {
            ClassDefinition classDef = (ClassDefinition) i$.next();
            result.add(dependent.get(classDef.getName()));
        }

        return result;
    }

    public JSONArray buildConstraintsJSON(ConstraintDefinition constraint) throws JSONException {
        org.json.JSONArray result = new org.json.JSONArray();
        org.json.JSONObject lvPair;

        List<String> constraintValues = (List<String>) constraint.getConstraint().getParameters().get(ListOfValuesConstraint.ALLOWED_VALUES_PARAM);
        for (String constraintValue : constraintValues) {
            lvPair = new org.json.JSONObject();
            lvPair.put("label", ((ListOfValuesConstraint) constraint.getConstraint()).getDisplayLabel(constraintValue, dictionaryService));
            lvPair.put("value", constraintValue);
            result.put(lvPair);
        }
        return result;
    }

    private String getCaseCreatorPermissionForCaseType(QName caseTypeQName) {

        Set<PermissionReference> allPermissions = permissionsModelDAO.getAllPermissions(caseTypeQName);

        Predicate<PermissionReference> isCaseCreatorPermission = p -> p.getName().startsWith(CASE)
                && p.getName().endsWith(CREATOR);

        for (PermissionReference permission : allPermissions) {
            if (isCaseCreatorPermission.test(permission)) {
                return permission.getName();
            }
        }

        return null;
    }

    /**
     * Get a node in the calendarbased path of the casefolders
     *
     * @param parent       The nodeRef to start from
     * @param calendarType The type of calendar info to look up, i.e. Calendar.YEAR, Calendar.MONTH, or Calendar.DATE
     * @return
     */
    NodeRef getCasePathNodeRef(NodeRef parent, int calendarType) {
        // Add 1 for months, as they are indexed form 0
        String casePathName = Integer.toString(Calendar.getInstance().get(calendarType) + (calendarType == Calendar.MONTH ? 1 : 0));
        NodeRef casePathNodeRef = nodeService.getChildByName(parent, ContentModel.ASSOC_CONTAINS, casePathName);
        if (casePathNodeRef == null) {
            casePathNodeRef = createNode(parent, casePathName);
        }
        return casePathNodeRef;
    }

    /**
     * Gets a map containing all the case's properties
     *
     * @return Map<QName, Serializable>    map containing all the properties of the case
     */
    private Map<QName, Serializable> getCaseProperties(NodeRef caseNodeRef) {
        Map<QName, Serializable> allProperties = new HashMap<QName, Serializable>();
        Map<QName, Serializable> properties = this.nodeService.getProperties(caseNodeRef);

        for (Map.Entry<QName, Serializable> entry : properties.entrySet()) {
            allProperties.put(entry.getKey(), entry.getValue());
        }
        return allProperties;
    }

    long getCaseUniqueId(NodeRef caseNodeRef) {
        // We are using node-dbid, as it is unique across nodes in a cluster
        return (long) nodeService.getProperty(caseNodeRef, ContentModel.PROP_NODE_DBID);
    }

    protected String getCaseRoleGroupName(String role) {
        return PermissionService.GROUP_PREFIX + role;
    }

    protected String getCaseRoleGroupName(String caseId, String role) {
        return authorityService.getName(AuthorityType.GROUP, getCaseRoleGroupAuthorityName(caseId, role));
    }

    protected String getCaseRoleGroupAuthorityName(String caseId, String role) {
        return "case_" + caseId + "_" + role;
    }

    private boolean caseCreatorGroupExists(String caseCreatorGroup) {
        return authorityService.authorityExists(caseCreatorGroup);
    }

    /**
     * Creating Groups and assigning permission on New case folder
     *
     * @param caseNodeRef
     * @param caseId
     */
    void managePermissions(NodeRef caseNodeRef, String caseId) {
        // Add cm:ownable aspect and set the cm:owner to admin
        // so that the node creator does not have full control.
        ownableService.setOwner(caseNodeRef, AuthenticationUtil.getSystemUserName());

        // Do not inherit parent permissions (which probably has
        // GROUP_EVERYONE set to Consumer, which we do not want)
        permissionService.setInheritParentPermissions(caseNodeRef, false);

        String ownersPermissionGroupName = setupPermissionGroup(caseNodeRef,
                caseId, "CaseOwners");

        setupCaseTypePermissionGroups(caseNodeRef, caseId);

        setupPermissionGroups(caseNodeRef, caseId);

        // The CaseOwnersBehaviour takes care of adding the owners to the
        // CaseOwners group
    }

    public void checkCanUpdateCaseRoles(NodeRef caseNodeRef) throws AccessDeniedException {
        String user = AuthenticationUtil.getRunAsUser();
        if (!canUpdateCaseRoles(user, caseNodeRef)) {
            throw new AccessDeniedException(user + " is not allowed to " +
                    "update case roles for case " + caseNodeRef);
        }
    }

    protected void setupCaseTypePermissionGroups(NodeRef caseNodeRef, String caseId) {

        Set<String> settablePermissions = permissionService.getSettablePermissions(caseNodeRef);

        List<String> authoritiesWithSetPermissions = getAllAuthoritiesWithSetPermissions(caseNodeRef);

        for (String permission : settablePermissions) {
            String groupNameToGrant = PermissionService.GROUP_PREFIX + permission;
            if (authorityService.authorityExists(groupNameToGrant)
                    && !authoritiesWithSetPermissions.contains(groupNameToGrant)) {
                permissionService.setPermission(caseNodeRef, groupNameToGrant, permission, true);
            }
        }
    }

    protected List<String> getAllAuthoritiesWithSetPermissions(NodeRef caseNodeRef) {
        ArrayList<String> authorities = new ArrayList<String>();
        Set<AccessPermission> permissions = permissionService.getAllSetPermissions(caseNodeRef);
        for (AccessPermission permission : permissions) {
            authorities.add(permission.getAuthority());
        }
        return authorities;
    }

}
