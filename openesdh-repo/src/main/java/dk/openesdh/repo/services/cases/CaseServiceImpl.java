package dk.openesdh.repo.services.cases;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.search.impl.lucene.LuceneQueryParserException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
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
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

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
@Service(CaseService.BEAN_ID)
public class CaseServiceImpl implements CaseService {

    private final Logger logger = LoggerFactory.getLogger(CaseServiceImpl.class);

    @Autowired
    @Qualifier("NodeService")
    private NodeService nodeService;
    @Autowired
    @Qualifier("NodeInfoService")
    private NodeInfoService nodeInfoService;
    @Autowired
    @Qualifier("SearchService")
    private SearchService searchService;
    @Autowired
    @Qualifier("AuthorityService")
    private AuthorityService authorityService;
    @Autowired
    @Qualifier("PermissionService")
    private PermissionService permissionService;
    @Autowired
    @Qualifier("TransactionService")
    private TransactionService transactionService;
    @Autowired
    @Qualifier("DictionaryService")
    private DictionaryService dictionaryService;
    @Autowired
    @Qualifier("OwnableService")
    private OwnableService ownableService;
    @Autowired
    @Qualifier(DocumentService.BEAN_ID)
    private DocumentService documentService;
    @Autowired
    @Qualifier("OELockService")
    private OELockService oeLockService;
    @Autowired
    @Qualifier("OpenESDHFoldersService")
    private OpenESDHFoldersService openESDHFoldersService;
    @Autowired
    @Qualifier("NamespaceService")
    private NamespaceService namespaceService;
    @Autowired
    @Qualifier("policyBehaviourFilter")
    private BehaviourFilter behaviourFilter;
    @Autowired
    @Qualifier("CasePermissionService")
    private CasePermissionService casePermissionService;
    @Autowired
    @Qualifier(PartyService.BEAN_ID)
    private PartyService partyService;
    @Autowired
    @Qualifier("CaseOwnersService")
    private CaseOwnersService caseOwnersService;

    @Override
    public NodeRef getCasesRootNodeRef() {
        return openESDHFoldersService.getCasesRootNodeRef();
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
            logger.info("caseNodeRef {}", caseNodeRef);

            //Create folder structure
            NodeRef casesRootNodeRef = openESDHFoldersService.getCasesRootNodeRef();

            NodeRef caseFolderNodeRef = getCaseFolderNodeRef(casesRootNodeRef);
            // Get a unique number to append to the caseId.
            long caseUniqueNumber = getCaseUniqueId(caseNodeRef);

            setupCase(caseNodeRef, caseFolderNodeRef, caseUniqueNumber);

            return null;
        }, AuthenticationUtil.getAdminUserName());
    }

    @Override
    public void createFolderForCaseDocuments(NodeRef caseNodeRef) {
        NodeRef documentsNodeRef = createNode(caseNodeRef, OpenESDHModel.DOCUMENTS_FOLDER_NAME);
        nodeService.addAspect(documentsNodeRef, OpenESDHModel.ASPECT_DOCUMENT_CONTAINER, null);
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

        logger.debug("Search parameters are: {}", sp);

        ResultSet results = null;
        try {
            return searchService.query(sp)
                    .getNodeRefs()
                    .stream()
                    .map(this::getCaseInfo)
                    .collect(Collectors.toList());
        } catch (LuceneQueryParserException lqpe) {
            //Log the error but suppress is from the user
            logger.error("LuceneQueryParserException with findCases()", lqpe);
            return Collections.emptyList();
        } finally {
            if (results != null) {
                results.close();
            }
        }
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

    public void checkCanChangeStatus(NodeRef nodeRef, CaseStatus fromStatus, CaseStatus toStatus) throws AccessDeniedException {
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

    private boolean canLeaveStatus(CaseStatus status, String user, NodeRef nodeRef) {
        switch (status) {
            case ACTIVE:
                return true;
            case PASSIVE:
                return true;
            case CLOSED:
                return canReopenCase(user, nodeRef);
            case ARCHIVED:
                return false;
            default:
                return true;
        }
    }

    private boolean canEnterStatus(CaseStatus status, String user, NodeRef nodeRef) {
        switch (status) {
            case ACTIVE:
                return true;
            case PASSIVE:
                return true;
            case CLOSED:
                return canCloseCase(user, nodeRef);
            case ARCHIVED:
                // The system does this.
                return false;
            default:
                return true;
        }
    }

    private void changeStatusImpl(NodeRef nodeRef, CaseStatus fromStatus, CaseStatus newStatus) throws Exception {
        switch (fromStatus) {
            case ACTIVE:
                switch (newStatus) {
                    case PASSIVE:
                        passivate(nodeRef);
                        nodeService.setProperty(nodeRef, OpenESDHModel.PROP_OE_STATUS, CaseStatus.PASSIVE);
                        break;
                    case CLOSED:
                        closeCase(nodeRef);
                        break;
                }
                break;
            case PASSIVE:
                switch (newStatus) {
                    case ACTIVE:
                        unPassivate(nodeRef);
                        nodeService.setProperty(nodeRef, OpenESDHModel.PROP_OE_STATUS, CaseStatus.ACTIVE);
                        break;
                    case CLOSED:
                        unPassivate(nodeRef);
                        closeCase(nodeRef);
                        break;
                }
                break;
            case CLOSED:
                switch (newStatus) {
                    case ACTIVE:
                        reopenCase(nodeRef);
                        nodeService.setProperty(nodeRef, OpenESDHModel.PROP_OE_STATUS, CaseStatus.ACTIVE);
                        break;
                    case PASSIVE:
                        reopenCase(nodeRef);
                        passivate(nodeRef);
                        nodeService.setProperty(nodeRef, OpenESDHModel.PROP_OE_STATUS, CaseStatus.PASSIVE);
                        break;
                }
                break;
            case ARCHIVED:
                // TODO: Check if the user is the system doing the operation.
                break;
        }
    }

    @Override
    public CaseStatus getNodeStatus(NodeRef nodeRef) {
        String status = (String) nodeService.getProperty(nodeRef, OpenESDHModel.PROP_OE_STATUS);
        if (StringUtils.isEmpty(status)) {
            return null;
        }
        return ObjectUtils.firstNonNull(
                EnumUtils.getEnum(CaseStatus.class, status.toUpperCase()),
                CaseStatus.ACTIVE);
    }

    @Override
    public List<CaseStatus> getValidNextStatuses(NodeRef nodeRef) {
        String user = AuthenticationUtil.getRunAsUser();
        CaseStatus fromStatus = getNodeStatus(nodeRef);
        return Arrays.stream(CaseStatus.values())
                .filter(s -> canChangeNodeStatus(fromStatus, s, user, nodeRef))
                .collect(Collectors.toList());
    }

    @Override
    public boolean canChangeNodeStatus(CaseStatus fromStatus, CaseStatus toStatus, String user, NodeRef nodeRef) {
        return isCaseNode(nodeRef)
                && CaseStatus.isValidTransition(fromStatus, toStatus)
                && canLeaveStatus(fromStatus, user, nodeRef) && canEnterStatus(toStatus, user, nodeRef);
    }

    @Override
    public void changeNodeStatus(NodeRef nodeRef, CaseStatus newStatus) throws Exception {
        CaseStatus fromStatus = getNodeStatus(nodeRef);
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
        AuthenticationUtil.runAsSystem(() -> {
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
                    partyService.lockCasePartiesToVersions(nodeRef);
                    // Lock the case and all children, recursively
                    lockImpl(nodeRef);
                    lockCaseGroups(nodeRef);
                    return null;
                }
            });
            return null;
        });
    }

    private void lockCaseGroups(final NodeRef caseNodeRef) {
        AuthenticationUtil.runAsSystem(() -> {
            getCaseAuthoritiesStream(caseNodeRef)
                    .forEach(authorityNodeRef -> permissionService.setPermission(authorityNodeRef,
                            PermissionService.ALL_AUTHORITIES, "LockPermissionsToDeny", false));
            return null;
        });
    }

    private Stream<NodeRef> getCaseAuthoritiesStream(final NodeRef caseNodeRef) {
        String caseId = getCaseId(caseNodeRef);
        Set<String> roles = getAllRoles(caseNodeRef);
        return roles.stream()
                .map(role -> getCaseRoleGroupName(caseId, role))
                .map(authorityService::getAuthorityNodeRef);
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
                partyService.unlockCaseParties(nodeRef);
                return null;
            }

        });
    }

    private void unlockCaseGroups(final NodeRef caseNodeRef) {
        AuthenticationUtil.runAsSystem(() -> {
            getCaseAuthoritiesStream(caseNodeRef)
                    .forEach(authorityNodeRef -> permissionService.deletePermission(authorityNodeRef,
                            PermissionService.ALL_AUTHORITIES, "LockPermissionsToDeny"));
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
    @Override
    public void setupPermissionGroups(NodeRef caseNodeRef, String caseId) {
        permissionService.getSettablePermissions(caseNodeRef)
                .stream()
                .forEach(permission -> setupPermissionGroup(caseNodeRef, caseId, permission));
    }

    String setupPermissionGroup(NodeRef caseNodeRef, String caseId, String permission) {
        String groupSuffix = getCaseRoleGroupAuthorityName(caseId, permission);
        String groupName = getCaseRoleGroupName(caseId, permission);

        if (!authorityService.authorityExists(groupName)) {

            // Add the authority group to the Share zone so that it is not
            // searchable from the authority picker.
            groupName = authorityService.createAuthority(AuthorityType.GROUP, groupSuffix, groupSuffix,
                    CaseService.DEFAULT_CASE_GROUP_ZONES);
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
    protected NodeRef createNode(final NodeRef parentFolderNodeRef, final String name) {
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
        createFolderForCaseDocuments(caseNodeRef);
    }

    @Override
    public String getCaseId(long uniqueNumber) {
        //Generating Case ID
        DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        Date date = new Date();
        StringBuilder caseId = new StringBuilder(dateFormat.format(date));
        caseId.append("-");
        caseId.append(uniqueNumber);
        logger.debug("Case Id is {}", caseId);

        return caseId.toString();
    }

    /**
     * Get a node in the calendarbased path of the casefolders
     *
     * @param parent The nodeRef to start from
     * @param calendarType The type of calendar info to look up, i.e. Calendar.YEAR, Calendar.MONTH, or Calendar.DATE
     * @return
     */
    @Override
    public NodeRef getCasePathNodeRef(NodeRef parent, int calendarType) {
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
    public void checkCanUpdateCaseRoles(NodeRef caseNodeRef) {
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
        properties.put("owners", caseOwnersService.getCaseOwners(caseNodeRef));
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
