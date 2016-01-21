package dk.openesdh.repo.services.cases;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.search.impl.lucene.LuceneQueryParserException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.SearchLanguageConversion;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.security.access.AccessDeniedException;

import dk.openesdh.repo.model.CaseInfo;
import dk.openesdh.repo.model.CaseInfoImpl;
import dk.openesdh.repo.model.CaseStatus;
import dk.openesdh.repo.model.DocumentStatus;
import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.NodeInfoService;
import dk.openesdh.repo.services.documents.DocumentService;
import dk.openesdh.repo.services.lock.OELockService;
import dk.openesdh.repo.services.system.OpenESDHFoldersService;

/**
 * Created by torben on 19/08/14.
 */
public class CaseServiceImpl implements CaseService {

    private static final String MSG_NO_CASE_CREATOR_PERMISSION_DEFINED = "security.permission.err_no_case_creator_permission_defined";
    private static final String MSG_NO_CASE_CREATOR_GROUP_DEFINED = "security.permission.err_no_case_creator_group_defined";
    private static final String MSG_CASE_CREATOR_PERMISSION_VIOLATION = "security.permission.err_case_creator_permission_violation";

    private static final Logger LOGGER = Logger.getLogger(CaseServiceImpl.class);
    private NodeService nodeService;
    private NodeInfoService nodeInfoService;
    private SearchService searchService;
    private AuthorityService authorityService;
    private PermissionService permissionService;
    private TransactionService transactionService;
    private DictionaryService dictionaryService;
    private OwnableService ownableService;
    private DocumentService documentService;
    private OELockService oeLockService;
    private OpenESDHFoldersService openESDHFoldersService;
    private NamespaceService namespaceService;
    private BehaviourFilter behaviourFilter;
    private CasePermissionService casePermissionService;

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }

    public void setOeLockService(OELockService oeLockService) {
        this.oeLockService = oeLockService;
    }

    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
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

    public void setOpenESDHFoldersService(OpenESDHFoldersService openESDHFoldersService) {
        this.openESDHFoldersService = openESDHFoldersService;
    }

    public void setNodeInfoService(NodeInfoService nodeInfoService) {
        this.nodeInfoService = nodeInfoService;
    }

    public void setNamespaceService(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }

    public void setBehaviourFilter(BehaviourFilter behaviourFilter) {
        this.behaviourFilter = behaviourFilter;
    }

    public void setCasePermissionService(CasePermissionService casePermissionService) {
        this.casePermissionService = casePermissionService;
    }

    @Override
    public NodeRef getCasesRootNodeRef() {
        return openESDHFoldersService.getCasesRootNodeRef();
    }

    public void init() {

    }

    @Override
    public Set<String> getRoles(NodeRef caseNodeRef) {
        return permissionService.getSettablePermissions(caseNodeRef);
    }

    @Override
    public Set<String> getAllRoles(NodeRef caseNodeRef) {
        Set<String> roles = getRoles(caseNodeRef);
        roles.add(casePermissionService.getPermissionName(caseNodeRef, CasePermission.OWNER));
        return roles;
    }

    public String getCaseId(NodeRef caseNodeRef) {
        return (String) nodeService.getProperty(caseNodeRef, OpenESDHModel.PROP_OE_ID);
    }

    @Override
    public NodeRef getCaseById(String caseId) {
        Matcher matcher = CASE_ID_PATTERN.matcher(caseId);
        if (matcher.matches()) {
            // Get the DBID from the case ID, and grab the NodeRef
            try {
                final Long dbid = Long.parseLong(matcher.group(1));
                NodeRef caseNodeRef = AuthenticationUtil.runAsSystem(() -> nodeService.getNodeRef(dbid));

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

    @Override
    public Collection<QName> getRegisteredCaseTypes() {
        Collection<QName> oeCaseTypes = dictionaryService.getSubTypes(OpenESDHModel.TYPE_CASE_BASE, true);
        //remove the base type
        oeCaseTypes.remove(OpenESDHModel.TYPE_CASE_BASE);
        return oeCaseTypes;
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
    public boolean canUpdateCaseRoles(String user, NodeRef caseNodeRef) {
        if (isLocked(caseNodeRef)) {
            return false;
        }
        return authorityService.isAdminAuthority(user)
                || isCaseOwner(user, caseNodeRef);
    }

    @Override
    public void createCase(final ChildAssociationRef childAssocRef) {
        AuthenticationUtil.runAs(() -> {
            NodeRef caseNodeRef = childAssocRef.getChildRef();
            LOGGER.info("caseNodeRef " + caseNodeRef);

            //Create folder structure
            NodeRef casesRootNodeRef = openESDHFoldersService.getCasesRootNodeRef();

            NodeRef caseFolderNodeRef = getCaseFolderNodeRef(casesRootNodeRef);
            // Get a unique number to append to the caseId.
            long caseUniqueNumber = getCaseUniqueId(caseNodeRef);

            setupCase(caseNodeRef, caseFolderNodeRef, caseUniqueNumber);

            return null;
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
        return AuthenticationUtil.runAsSystem(() -> {
            NodeRef casesYearNodeRef = getCasePathNodeRef(casesRootNodeRef, Calendar.YEAR);
            NodeRef casesMonthNodeRef = getCasePathNodeRef(casesYearNodeRef, Calendar.MONTH);
            return getCasePathNodeRef(casesMonthNodeRef, Calendar.DATE);
        });
    }

    @Override
    public boolean isLocked(NodeRef nodeRef) {
        return oeLockService.isLocked(nodeRef);
    }

    @Override
    public boolean isCaseNode(NodeRef nodeRef) {
        QName type = nodeService.getType(nodeRef);
        return dictionaryService.isSubClass(type, OpenESDHModel.TYPE_CASE_BASE);
    }

    @Override
    public boolean isCaseDocNode(NodeRef nodeRef) {
        QName type = nodeService.getType(nodeRef);
        return (dictionaryService.isSubClass(type, OpenESDHModel.TYPE_DOC_BASE) || dictionaryService.isSubClass(type, OpenESDHModel.TYPE_DOC_DIGITAL_FILE));
    }

    @Override
    public NodeRef getParentCase(NodeRef nodeRef) {
        if (isCaseNode(nodeRef)) {
            return nodeRef;
        }
        if (nodeRef.equals(openESDHFoldersService.getCasesRootNodeRef())) {
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
    public List<CaseInfo> findCases(String filter, int size) {
        List<CaseInfo> result;

        NodeRef caseRoot = openESDHFoldersService.getCasesRootNodeRef();
        if (caseRoot == null) {
            return Collections.emptyList();
        }
        // get the cases that match the specified names
        StringBuilder query = new StringBuilder(128);
        query.append("+TYPE:\"").append(OpenESDHModel.TYPE_CASE_BASE).append('"');

        final boolean filterIsPresent = filter != null && filter.length() > 0;

        if (filterIsPresent) {
            query.append(" AND (");
            // Tokenize the filter and wildcard each token
            String escNameFilter = SearchLanguageConversion.escapeLuceneQuery(filter);
            String[] tokenizedFilter = SearchLanguageConversion.tokenizeString(escNameFilter);
            for (String aTokenizedFilter : tokenizedFilter) {
                query.append(aTokenizedFilter).append("* ");
            }
            query.append(")");
        }

        SearchParameters sp = new SearchParameters();
        sp.addQueryTemplate("_CASES", "|%oe:id |%title "
                + "|%description |%oe:journalKeyIndexed "
                + "|%oe:journalFacetIndexed");
        sp.setDefaultFieldName("_CASES");
        sp.addStore(caseRoot.getStoreRef());
        sp.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
        sp.setQuery(query.toString());
        if (size > 0) {
            sp.setLimit(size);
            sp.setLimitBy(LimitBy.FINAL_SIZE);
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Search parameters are: " + sp);
        }

        ResultSet results = null;
        try {
            return searchService.query(sp)
                    .getNodeRefs()
                    .stream()
                    .map(this::getCaseInfo)
                    .collect(Collectors.toList());
        } catch (LuceneQueryParserException lqpe) {
            //Log the error but suppress is from the user
            LOGGER.error("LuceneQueryParserException with findCases()", lqpe);
            return Collections.emptyList();
        } finally {
            if (results != null) {
                results.close();
            }
        }
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
        }

        JSONObject availableFilters = new JSONObject();
        ArrayList classDefinitions1 = new ArrayList(classDefs.values());
        model.put("classdefs", classDefinitions1);
        model.put("propertydefs", propertyDefs);
        model.put("availableFilters", availableFilters);
        model.put("messages", this.dictionaryService);
        return model;
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

    @Override
    public void checkCaseCreatorPermissions(QName caseTypeQName) {
        String caseCreatorPermissionName = casePermissionService.getPermissionName(caseTypeQName, CasePermission.CREATOR);
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

        Set<String> currentUserContainingGroups = authorityService.getContainingAuthoritiesInZone(
                AuthorityType.GROUP,
                AuthenticationUtil.getFullyAuthenticatedUser(), AuthorityService.ZONE_APP_DEFAULT, null, 0);

        if (!currentUserContainingGroups.contains(caseCreatorGroup)) {
            throw new AccessDeniedException(I18NUtil.getMessage(MSG_CASE_CREATOR_PERMISSION_VIOLATION,
                    caseTypeQName.getLocalName()));
        }
    }

    @Override
    public List<String> getCaseUserPermissions(String caseId) {

        // Consumer doesn't have _ReadPermissions permission therefore run as
        // syste
        Set<AccessPermission> allPermissionsSetToCase
                = AuthenticationUtil.runAsSystem(() -> permissionService.getAllSetPermissions(getCaseById(caseId)));

        Set<String> currentUserAuthorities
                = AuthenticationUtil.runAsSystem(() -> authorityService.getAuthoritiesForUser(AuthenticationUtil.getFullyAuthenticatedUser()));

        Predicate<AccessPermission> isPermissionGrantedForCurrentUser
                = (permission) -> permission.getAccessStatus() == AccessStatus.ALLOWED && currentUserAuthorities.contains(permission.getAuthority());

        return allPermissionsSetToCase.stream()
                .filter(permission -> isPermissionGrantedForCurrentUser.test(permission))
                .map(permission -> permission.getPermission())
                .collect(Collectors.toList());
    }

    private <R> R runAsAdmin(RunAsWork<R> r) {
        return AuthenticationUtil.runAs(r, OpenESDHModel.ADMIN_USER_NAME);
    }

    public void checkCanChangeStatus(NodeRef nodeRef, String fromStatus, String toStatus) throws AccessDeniedException {
        String user = AuthenticationUtil.getRunAsUser();
        if (!isCaseNode(nodeRef)) {
            throw new AlfrescoRuntimeException("Node is not a case node: "
                    + nodeRef);
        }
        if (!canChangeNodeStatus(fromStatus, toStatus, user, nodeRef)) {
            throw new AccessDeniedException(user + " is not allowed to "
                    + "switch case from status " + fromStatus + " to "
                    + toStatus + " for case " + nodeRef);
        }
    }

    private boolean canLeaveStatus(String status, String user, NodeRef nodeRef) {
        switch (status) {
            case CaseStatus.ACTIVE:
                return true;
            case CaseStatus.PASSIVE:
                return true;
            case CaseStatus.CLOSED:
                return canReopenCase(user, nodeRef);
            case CaseStatus.ARCHIVED:
                return false;
            default:
                return true;
        }
    }

    private boolean canEnterStatus(String status, String user, NodeRef nodeRef) {
        switch (status) {
            case CaseStatus.ACTIVE:
                return true;
            case CaseStatus.PASSIVE:
                return true;
            case CaseStatus.CLOSED:
                return canCloseCase(user, nodeRef);
            case CaseStatus.ARCHIVED:
                // The system does this.
                return false;
            default:
                return true;
        }
    }

    private void changeStatusImpl(NodeRef nodeRef, String fromStatus, String newStatus) throws Exception {
        switch (fromStatus) {
            case CaseStatus.ACTIVE:
                switch (newStatus) {
                    case CaseStatus.PASSIVE:
                        passivate(nodeRef);
                        nodeService.setProperty(nodeRef, OpenESDHModel.PROP_OE_STATUS, CaseStatus.PASSIVE);
                        break;
                    case CaseStatus.CLOSED:
                        closeCase(nodeRef);
                        break;
                }
                break;
            case CaseStatus.PASSIVE:
                switch (newStatus) {
                    case CaseStatus.ACTIVE:
                        unPassivate(nodeRef);
                        nodeService.setProperty(nodeRef, OpenESDHModel.PROP_OE_STATUS, CaseStatus.ACTIVE);
                        break;
                    case CaseStatus.CLOSED:
                        unPassivate(nodeRef);
                        closeCase(nodeRef);
                        break;
                }
                break;
            case CaseStatus.CLOSED:
                switch (newStatus) {
                    case CaseStatus.ACTIVE:
                        reopenCase(nodeRef);
                        nodeService.setProperty(nodeRef, OpenESDHModel.PROP_OE_STATUS, CaseStatus.ACTIVE);
                        break;
                    case CaseStatus.PASSIVE:
                        reopenCase(nodeRef);
                        passivate(nodeRef);
                        nodeService.setProperty(nodeRef, OpenESDHModel.PROP_OE_STATUS, CaseStatus.PASSIVE);
                        break;
                }
                break;
            case CaseStatus.ARCHIVED:
                // TODO: Check if the user is the system doing the operation.
                break;
        }
    }

    @Override
    public String getNodeStatus(NodeRef nodeRef) {
        return (String) nodeService.getProperty(nodeRef, OpenESDHModel.PROP_OE_STATUS);
    }

    @Override
    public List<String> getValidNextStatuses(NodeRef nodeRef) {
        String user = AuthenticationUtil.getRunAsUser();
        String fromStatus = getNodeStatus(nodeRef);
        List<String> statuses;
        statuses = Arrays.asList(CaseStatus.getStatuses()).stream().filter(
                s -> canChangeNodeStatus(fromStatus, s, user, nodeRef))
                .collect(Collectors.toList());
        return statuses;
    }

    @Override
    public boolean canChangeNodeStatus(String fromStatus, String toStatus, String user, NodeRef nodeRef) {
        return isCaseNode(nodeRef)
                && CaseStatus.isValidTransition(fromStatus, toStatus)
                && canLeaveStatus(fromStatus, user, nodeRef) && canEnterStatus(toStatus, user, nodeRef);
    }

    @Override
    public void changeNodeStatus(NodeRef nodeRef, String newStatus) throws Exception {
        String fromStatus = getNodeStatus(nodeRef);
        if (newStatus.equals(fromStatus)) {
            return;
        }
        checkCanChangeStatus(nodeRef, fromStatus, newStatus);

        transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
            try {
                // Disable status behaviour to allow the system to set the status directly.
                behaviourFilter.disableBehaviour(nodeRef);
                changeStatusImpl(nodeRef, fromStatus, newStatus);
            } finally {
                behaviourFilter.enableBehaviour(nodeRef);
            }
            return null;
        });
    }

    private boolean canReopenCase(String user, NodeRef nodeRef) {
        return authorityService.isAdminAuthority(user);
    }

    private boolean canCloseCase(String user, NodeRef nodeRef) {
        return true;
    }

    /**
     * Close a case node.
     *
     * @param nodeRef
     * @throws java.lang.Exception
     */
    private void closeCase(NodeRef nodeRef) throws Exception {
        if (!isCaseNode(nodeRef)) {
            throw new Exception("Cannot close a non-case node!");
        }
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
            private void lockImpl(NodeRef nodeRef) throws Exception {
                for (ChildAssociationRef childAssociationRef : nodeService.getChildAssocs(nodeRef)) {
                    NodeRef childNodeRef = childAssociationRef.getChildRef();
                    if (!documentService.isDocNode(childNodeRef)) {
                        // Lock recursively all children
                        lockImpl(childNodeRef);
                    } else {
                        // Finalize documents (DocumentService will handle
                        // locking).
                        documentService.changeNodeStatus(childNodeRef, DocumentStatus.FINAL);
                    }
                }
                // Lock the node itself
                oeLockService.lock(nodeRef);
            }

            @Override
            public Object execute() throws Throwable {
                nodeService.setProperty(nodeRef, OpenESDHModel.PROP_OE_STATUS, CaseStatus.CLOSED);
                // Lock the case and all children, recursively
                lockImpl(nodeRef);
                lockCaseGroups(nodeRef);
                return null;
            }

        });
    }

    private void lockCaseGroups(final NodeRef caseNodeRef) {
        AuthenticationUtil.runAsSystem(() -> {
            Set<String> roles = getAllRoles(caseNodeRef);
            for (String role : roles) {
                NodeRef authorityNodeRef = authorityService.getAuthorityNodeRef(
                        getCaseRoleGroupName(getCaseId(caseNodeRef), role));
                permissionService.setPermission(authorityNodeRef,
                        PermissionService.ALL_AUTHORITIES, "LockPermissionsToDeny", false);
            }
            return null;
        });
    }

    /**
     * Passivates a case.
     * <p/>
     * Passive cases and their documents are not searchable by default.
     *
     * @param nodeRef
     */
    private void passivate(NodeRef nodeRef) {
        // TODO: Passivate documents in the case by adding an aspect
        // oe:passive
    }

    private void unPassivate(NodeRef nodeRef) {
        // TODO: Unpassivate documents in the case by removing an aspect
        // oe:passive
    }

    /**
     * Reopen a closed case node.
     *
     * @param nodeRef
     * @throws java.lang.Exception
     */
    private void reopenCase(NodeRef nodeRef) throws Exception {
        if (!isCaseNode(nodeRef)) {
            throw new Exception("Not a case node");
        }
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
            private void unlockImpl(NodeRef nodeRef) {
                oeLockService.unlock(nodeRef);
                for (ChildAssociationRef childAssociationRef : nodeService.getChildAssocs(nodeRef)) {
                    NodeRef childNodeRef = childAssociationRef.getChildRef();
                    // Do NOT unlock documents (they should remain in FINAL status)
                    if (!documentService.isDocNode(childNodeRef)) {
                        unlockImpl(childNodeRef);
                    }
                }
            }

            @Override
            public Object execute() throws Throwable {
                unlockImpl(nodeRef);
                unlockCaseGroups(nodeRef);
                return null;
            }

        });
    }
    //</editor-fold>

    private void unlockCaseGroups(final NodeRef caseNodeRef) {
        AuthenticationUtil.runAsSystem(() -> {
            Set<String> roles = getAllRoles(caseNodeRef);
            for (String role : roles) {
                NodeRef authorityNodeRef = authorityService.getAuthorityNodeRef(
                        getCaseRoleGroupName(getCaseId(caseNodeRef), role));
                permissionService.deletePermission(authorityNodeRef,
                        PermissionService.ALL_AUTHORITIES, "LockPermissionsToDeny");
            }
            return null;
        });
    }

    private boolean isCaseOwner(String user, NodeRef caseNodeRef) {
        String caseId = getCaseId(caseNodeRef);
        // Check that the user is a case owner
        Set<String> authorities = authorityService.getContainedAuthorities(
                AuthorityType.USER,
                getCaseRoleGroupName(caseId, casePermissionService.getPermissionName(caseNodeRef, CasePermission.OWNER)),
                false);
        return authorities.contains(user);
    }

    /**
     * Creates individual groups for provided case and sets appropriate permissions
     *
     * @param caseNodeRef
     * @param caseId
     */
    void setupPermissionGroups(NodeRef caseNodeRef, String caseId) {
        permissionService.getSettablePermissions(caseNodeRef)
                .stream()
                .forEach(permission -> setupPermissionGroup(caseNodeRef, caseId, permission));
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

    /**
     * Get a node in the calendarbased path of the casefolders
     *
     * @param parent The nodeRef to start from
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
     * @return Map<QName, Serializable> map containing all the properties of the case
     */
    private Map<QName, Serializable> getCaseProperties(NodeRef caseNodeRef) {
        return nodeService.getProperties(caseNodeRef);
    }

    long getCaseUniqueId(NodeRef caseNodeRef) {
        // We are using node-dbid, as it is unique across nodes in a cluster
        return (long) nodeService.getProperty(caseNodeRef, ContentModel.PROP_NODE_DBID);
    }

    @Override
    public String getCaseRoleGroupName(String caseId, String role) {
        return authorityService.getName(AuthorityType.GROUP, getCaseRoleGroupAuthorityName(caseId, role));
    }

    private String getCaseRoleGroupAuthorityName(String caseId, String role) {
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

        setupCaseTypePermissionGroups(caseNodeRef, caseId);

        setupPermissionGroups(caseNodeRef, caseId);

        // The CaseOwnersBehaviour takes care of adding the owners to the
        // CaseOwners group
    }

    @Override
    public void checkCanUpdateCaseRoles(NodeRef caseNodeRef) throws AccessDeniedException {
        String user = AuthenticationUtil.getRunAsUser();
        if (!canUpdateCaseRoles(user, caseNodeRef)) {
            throw new AccessDeniedException(user + " is not allowed to "
                    + "update case roles for case " + caseNodeRef);
        }
    }

    private void setupCaseTypePermissionGroups(NodeRef caseNodeRef, String caseId) {

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

    private List<String> getAllAuthoritiesWithSetPermissions(NodeRef caseNodeRef) {
        return permissionService.getAllSetPermissions(caseNodeRef)
                .stream()
                .map(AccessPermission::getAuthority)
                .collect(Collectors.toList());
    }

    @Override
    public JSONObject getCaseInfoJson(NodeRef caseNodeRef) throws JSONException {
        NodeInfoService.NodeInfo nodeInfo = nodeInfoService.getNodeInfo(caseNodeRef);
        JSONObject json = nodeInfoService.buildJSON(nodeInfo);
        json.put("isLocked", isLocked(caseNodeRef));
        json.put("statusChoices", getValidNextStatuses(caseNodeRef));

        JSONObject properties = (JSONObject) json.get("properties");
        addEmptyPropsIfNull(properties);
        properties.put("nodeRef", caseNodeRef.toString());
        return json;
    }

    private static final List<QName> NOT_NULL_PROPS = Arrays.asList(OpenESDHModel.PROP_OE_ID,
            ContentModel.PROP_TITLE, OpenESDHModel.PROP_OE_STATUS, ContentModel.PROP_CREATOR,
            ContentModel.PROP_CREATED, OpenESDHModel.PROP_CASE_ENDDATE, ContentModel.PROP_MODIFIED, ContentModel.PROP_MODIFIER,
            ContentModel.PROP_DESCRIPTION, OpenESDHModel.PROP_OE_JOURNALKEY, OpenESDHModel.PROP_OE_JOURNALFACET,
            OpenESDHModel.PROP_OE_LOCKED_BY, OpenESDHModel.PROP_OE_LOCKED_DATE, OpenESDHModel.PROP_CASE_STARTDATE);

    private void addEmptyPropsIfNull(JSONObject json) throws JSONException {
        for (QName qname : NOT_NULL_PROPS) {
            String property = qname.toPrefixString(namespaceService);
            if (!json.has(property)) {
                json.put(property, "");
            }
        }
    }
}
