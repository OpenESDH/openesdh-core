package dk.openesdh.repo.services.cases;

import dk.openesdh.repo.actions.AssignCaseIdActionExecuter;
import dk.openesdh.repo.model.OpenESDHModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.rule.RuleType;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.log4j.Logger;
import org.springframework.security.access.AccessDeniedException;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by torben on 19/08/14.
 */
public class CaseServiceImpl implements CaseService {

    private static Logger LOGGER = Logger.getLogger(CaseServiceImpl.class);

    /**
     * repositoryHelper cannot be autowired - seemingly
     */
    private Repository repositoryHelper;
    private NodeService nodeService;
    private SearchService searchService;
    private LockService lockService;

    private AuthorityService authorityService;

    private PermissionService permissionService;
    private TransactionService transactionService;
    private DictionaryService dictionaryService;
    private RuleService ruleService;
    private ActionService actionService;
    private OwnableService ownableService;
    //<editor-fold desc="Service setters">
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
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

    public void setRuleService(RuleService ruleService) {
        this.ruleService = ruleService;
    }

    public void setActionService(ActionService actionService) {
        this.actionService = actionService;
    }

    @Override
    public NodeRef getCasesRootNodeRef() {

        NodeRef companyHomeNodeRef = repositoryHelper.getCompanyHome();
        NodeRef casesRootNodeRef = nodeService.getChildByName(companyHomeNodeRef, ContentModel.ASSOC_CONTAINS, CASES);

        if (casesRootNodeRef == null) {
            //Creating case folder
            casesRootNodeRef = createCasesRoot(companyHomeNodeRef);
        }
        return casesRootNodeRef;
    }

    /**
     * Creating Case Folder Ref
     *
     * @param companyHomeNodeRef
     * @return
     */
    protected NodeRef createCasesRoot(NodeRef companyHomeNodeRef) {
        Map<QName, Serializable> properties = new HashMap<>();
        properties.put(ContentModel.PROP_NAME, CASES);
        NodeRef casesRootNodeRef = nodeService.createNode(companyHomeNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(OpenESDHModel.CASE_URI, CASES), ContentModel.TYPE_FOLDER, properties).getChildRef();
        setupAssignCaseIdRule(casesRootNodeRef);
        return casesRootNodeRef;
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
        setupPermissionGroups(caseNodeRef, caseId);
        // The CaseOwnersBehaviour takes care of adding the owners to the
        // CaseOwners group
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
        SearchParameters sp = new SearchParameters();
        sp.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        sp.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
        sp.setQuery("TYPE:\""+OpenESDHModel.TYPE_CASE_SIMPLE+"\"AND @oe\\:id:\""+ caseId+"\"");
        ResultSet results = null;
        NodeRef caseNodeRef = null;
        try {
            results = this.searchService.query(sp);
            if(results.length() == 1) //Because "There there can be only one"
                caseNodeRef = results.getRow(0).getNodeRef();
        }
        finally {
            if(results != null) {
                results.close();
            }
            return caseNodeRef;
        }
    }

    public String getCaseId(NodeRef caseNodeRef) {
        return (String) nodeService.getProperty(caseNodeRef, OpenESDHModel.PROP_OE_ID);
    }

    long getCaseUniqueId(NodeRef caseNodeRef) {
        // We are using node-dbid, as it is unique across nodes in a cluster
        return (long) nodeService.getProperty(caseNodeRef, ContentModel.PROP_NODE_DBID);
    }

    protected String getCaseRoleGroupName(String caseId, String role) {
        return authorityService.getName(AuthorityType.GROUP, getCaseRoleGroupAuthorityName(caseId, role));
    }

    protected String getCaseRoleGroupAuthorityName(String caseId, String role) {
        return "case_" + caseId + "_" + role;
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

    @Override
    public void removeAuthorityFromRole(final String authorityName, final String role, final NodeRef caseNodeRef) {
        checkCanUpdateCaseRoles(caseNodeRef);
        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>() {
            @Override
            public Object doWork() throws Exception {
                String caseId = getCaseId(caseNodeRef);
                String groupName = getCaseRoleGroupName(caseId, role);
                if (authorityService.authorityExists(groupName) &&
                        authorityService.authorityExists(authorityName)) {
                    authorityService.removeAuthority(groupName, authorityName);
                }
                return null;
            }
        }, "admin");
    }

    @Override
    public void removeAuthorityFromRole(final NodeRef authorityNodeRef,
                                        final String role,
                                        final NodeRef caseNodeRef) {
        removeAuthorityFromRole(getAuthorityName(authorityNodeRef), role, caseNodeRef);
    }

    @Override
    public void addAuthorityToRole(final String authorityName,
                                   final String role,
                                   final NodeRef caseNodeRef) {
        checkCanUpdateCaseRoles(caseNodeRef);

        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>() {
            @Override
            public Object doWork() throws Exception {
                String caseId = getCaseId(caseNodeRef);
                String groupName = getCaseRoleGroupName(caseId, role);
                authorityService.addAuthority(groupName, authorityName);
                return null;
            }
        }, "admin");
    }

    @Override
    public void addAuthorityToRole(final NodeRef authorityNodeRef,
                                   final String role,
                                   final NodeRef caseNodeRef) {
        addAuthorityToRole(getAuthorityName(authorityNodeRef), role,
                caseNodeRef);
    }

    @Override
    public void addAuthoritiesToRole(final List<NodeRef> authorities,
                                     final String role,
                                     final NodeRef caseNodeRef) {
        checkCanUpdateCaseRoles(caseNodeRef);

        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>() {
            @Override
            public Object doWork() throws Exception {
                String caseId = getCaseId(caseNodeRef);
                final String groupName = getCaseRoleGroupName(caseId, role);
                if (!authorityService.authorityExists(groupName)) {
                    return null;
                }
                transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionHelper
                        .RetryingTransactionCallback<Object>() {
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
            }
        }, "admin");
    }

    @Override
    public void changeAuthorityRole(final String authorityName,
                                    final String fromRole,
                                    final String toRole,
                                    final NodeRef caseNodeRef) {
        checkCanUpdateCaseRoles(caseNodeRef);

        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>() {
            @Override
            public Object doWork() throws Exception {
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
            }
        }, "admin");
    }

    public void checkCanUpdateCaseRoles(NodeRef caseNodeRef) throws
            AccessDeniedException {
        String user = AuthenticationUtil.getRunAsUser();
        if (!canUpdateCaseRoles(user, caseNodeRef)) {
            throw new AccessDeniedException(user + " is not allowed to " +
                    "update case roles for case " + caseNodeRef);
        }
    }

    @Override
    public boolean canUpdateCaseRoles(String user, NodeRef caseNodeRef) {
        if (isJournalized(caseNodeRef)) {
            return false;
        }
        return authorityService.isAdminAuthority(user) ||
                isCaseOwner(user, caseNodeRef);
    }

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

    void setupPermissionGroups(NodeRef caseNodeRef, String caseId) {
        Set<String> settablePermissions = permissionService.getSettablePermissions(caseNodeRef);

        for (Iterator<String> iterator = settablePermissions.iterator(); iterator.hasNext(); ) {
            String permission = iterator.next();
            setupPermissionGroup(caseNodeRef, caseId, permission);
        }
    }

    String setupPermissionGroup(NodeRef caseNodeRef, String caseId,
                                String permission) {
        String groupSuffix = getCaseRoleGroupAuthorityName(caseId, permission);
        String groupName = getCaseRoleGroupName(caseId, permission);

        if (!authorityService.authorityExists(groupName)) {
            HashSet<String> shareZones = new HashSet<>();
            shareZones.add(AuthorityService.ZONE_APP_SHARE);
            shareZones.add(AuthorityService.ZONE_AUTH_ALFRESCO);
            // Add the authority group to the Share zone so that it is not
            // searchable from the authority picker.
            groupName = authorityService.createAuthority(AuthorityType
                    .GROUP, groupSuffix, groupSuffix, shareZones);
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
    private NodeRef createNode(NodeRef parentFolderNodeRef, String name) {
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

    protected void setupAssignCaseIdRule(final NodeRef folderNodeRef) {
        Action action = actionService.createAction(AssignCaseIdActionExecuter.NAME);
        action.setTitle("Assign caseId");
        action.setExecuteAsynchronously(true);
        Rule rule = new Rule();
        rule.setRuleType(RuleType.INBOUND);
        rule.setTitle("Assign caseId to case documents");
        rule.applyToChildren(true);
        rule.setAction(action);
        ruleService.saveRule(folderNodeRef, rule);
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

    /**
     * \
     * Get the nodeRef for the folder in which to place the case.
     *
     * @param casesRootNodeRef The root folder nodeRef in the case hierarchy
     * @return The NodeRef for the folder in which to place the case
     */
    @Override
    public NodeRef getCaseFolderNodeRef(NodeRef casesRootNodeRef) {
        NodeRef casesYearNodeRef = getCasePathNodeRef(casesRootNodeRef, Calendar.YEAR);
        NodeRef casesMonthNodeRef = getCasePathNodeRef(casesYearNodeRef, Calendar.MONTH);
        return getCasePathNodeRef(casesMonthNodeRef, Calendar.DATE);
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

    //<editor-fold desc="Journalization methods">
    public void checkCanJournalize(NodeRef nodeRef) throws
            AccessDeniedException {
        String user = AuthenticationUtil.getRunAsUser();
        if (!canJournalize(user, nodeRef)) {
            throw new AccessDeniedException(user + " is not allowed to " +
                    "journalize the node " + nodeRef);
        }
    }

    public void checkCanUnJournalize(NodeRef nodeRef) throws
            AccessDeniedException {
        String user = AuthenticationUtil.getRunAsUser();
        if (!canUnJournalize(user, nodeRef)) {
            throw new AccessDeniedException(user + " is not allowed to " +
                    "unjournalize the node " + nodeRef);
        }
    }

    private boolean isJournalizableType(NodeRef nodeRef) {
        return isCaseNode(nodeRef) || isCaseDocNode(nodeRef);
    }

    @Override
    public boolean canJournalize(String user, NodeRef nodeRef) {
        if (!isJournalizableType(nodeRef) || isJournalized(nodeRef)) {
            return false;
        }
        if (authorityService.isAdminAuthority(user)) {
            return true;
        }
        if (isCaseNode(nodeRef)) {
            // Type: Case
            // Only case owners can journalize a case
            return isCaseOwner(user, nodeRef);
        } else {
            // Type: Case doc
            // Only doc owners and case owners can journalize a case doc
            return ownableService.getOwner(nodeRef).equals(user) ||
                    isCaseOwner(user, getParentCase(nodeRef));
        }
    }

    @Override
    public boolean canUnJournalize(String user, NodeRef nodeRef) {
        if (!isJournalizableType(nodeRef) || !isJournalized(nodeRef)) {
            return false;
        }
        if (isCaseDocNode(nodeRef) && isJournalized(getParentCase(nodeRef))) {
            // Cannot unjournalize documents from a case that is journalized
            // Must unjournalize the case instead.
            return false;
        }
        // Only admins can unjournalize anything (for now)
        return authorityService.isAdminAuthority(user);
    }

    @Override
    public boolean isJournalized(NodeRef nodeRef) {
        return nodeService.hasAspect(nodeRef,
                OpenESDHModel.ASPECT_OE_JOURNALIZED);
    }

    @Override
    public void journalize(final NodeRef nodeRef,
                           final NodeRef journalKey) {
        checkCanJournalize(nodeRef);
        // Run it in a transaction
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
            @Override
            public Object execute() throws Throwable {
                journalizeImpl(nodeRef, journalKey);
                if (isCaseNode(nodeRef)) {
                    journalizeCaseGroups(nodeRef);
                }
                return null;
            }
        });
    }

    // Suppress warning about READ_ONLY_LOCK being deprecated
    @SuppressWarnings("deprecation")
    private void journalizeImpl(final NodeRef nodeRef, final NodeRef journalKey) {
        if (nodeService.hasAspect(nodeRef, OpenESDHModel.ASPECT_OE_JOURNALIZED)) {
            // Don't touch, already journalized
            LOGGER.warn("Node already has journalized aspect when journalizing: " + nodeRef);
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

                // Add journalized aspect
                Map<QName, Serializable> props = new HashMap<>();
                props.put(OpenESDHModel.PROP_OE_JOURNALKEY, journalKey);
                props.put(OpenESDHModel.PROP_OE_JOURNALIZED_BY, AuthenticationUtil.getFullyAuthenticatedUser());
                props.put(OpenESDHModel.PROP_OE_JOURNALIZED_DATE, new Date());
                // Save the original owner, or null if there wasn't any
                props.put(OpenESDHModel.PROP_OE_ORIGINAL_OWNER, originalOwner);
                nodeService.addAspect(nodeRef, OpenESDHModel.ASPECT_OE_JOURNALIZED, props);

                // Add the Journalized permission set to deny everyone
                permissionService.setPermission(nodeRef, PermissionService.ALL_AUTHORITIES, "Journalized", false);

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
            journalizeImpl(childAssociationRef.getChildRef(), journalKey);
        }
    }

    private void journalizeCaseGroups(final NodeRef caseNodeRef) {
        AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Void>() {
            @Override
            public Void doWork() {
                List<ChildAssociationRef> childAssociationRefs = nodeService.getChildAssocs(caseNodeRef);
                Set<String> roles = getAllRoles(caseNodeRef);
                for (String role : roles) {
                    NodeRef authorityNodeRef = authorityService.getAuthorityNodeRef(
                            getCaseRoleGroupName(getCaseId(caseNodeRef), role));
                    permissionService.setPermission(authorityNodeRef,
                            PermissionService.ALL_AUTHORITIES, "Journalized", false);
                }
                return null;
            }
        });
    }

    @Override
    public void unJournalize(final NodeRef nodeRef) {
        checkCanUnJournalize(nodeRef);
        // Run it in a transaction
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
            @Override
            public Object execute() throws Throwable {
                unJournalizeImpl(nodeRef);
                if (isCaseNode(nodeRef)) {
                    unJournalizeCaseGroups(nodeRef);
                }
                return null;
            }
        });
    }

    private void unJournalizeImpl(final NodeRef nodeRef) {
        AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Void>() {
            @Override
            public Void doWork() {
                lockService.unlock(nodeRef);

                // Only delete the permission if permissions are not
                // inherited (shared)
                if (!permissionService.getInheritParentPermissions(nodeRef)) {
                    permissionService.deletePermission(nodeRef, PermissionService.ALL_AUTHORITIES, "Journalized");
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
                nodeService.removeAspect(nodeRef, OpenESDHModel.ASPECT_OE_JOURNALIZED);
                return null;
            }
        });

        List<ChildAssociationRef> childAssociationRefs = nodeService.getChildAssocs(nodeRef);
        for (ChildAssociationRef childAssociationRef : childAssociationRefs) {
            unJournalizeImpl(childAssociationRef.getChildRef());
        }
    }

    private void unJournalizeCaseGroups(final NodeRef caseNodeRef) {
        AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Void>() {
            @Override
            public Void doWork() {
                List<ChildAssociationRef> childAssociationRefs = nodeService.getChildAssocs(caseNodeRef);
                Set<String> roles = getAllRoles(caseNodeRef);
                for (String role : roles) {
                    NodeRef authorityNodeRef = authorityService.getAuthorityNodeRef(
                            getCaseRoleGroupName(getCaseId(caseNodeRef), role));
                    permissionService.deletePermission(authorityNodeRef,
                            PermissionService.ALL_AUTHORITIES, "Journalized");
                }
                return null;
            }
        });
    }
    //</editor-fold>

}
