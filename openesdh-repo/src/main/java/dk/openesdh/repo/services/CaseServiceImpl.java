package dk.openesdh.repo.services;

import dk.openesdh.repo.model.OpenESDHModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by torben on 19/08/14.
 */
public class CaseServiceImpl implements CaseService {

    /* TODO: Get the correct group names */
    private static final String CASE_CONSUMER = "CaseConsumer";
    private static final String CASE_COORDINATOR = "CaseCoordinator";
    private static final String DATE_FORMAT = "yyyyMMdd";

    private static Logger LOGGER = Logger.getLogger(CaseServiceImpl.class.toString());

    private static final String CASES = "openesdh_cases";

    /**
     * repositoryHelper cannot be autowired - seemingly
     */
    private Repository repositoryHelper;
    private NodeService nodeService;
    private SearchService searchService;
    private AuthorityService authorityService;
    private PermissionService permissionService;

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
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

    public void setRepositoryHelper(Repository repositoryHelper) {
        this.repositoryHelper = repositoryHelper;
    }

    @Override
    public NodeRef getCasesRootNodeRef() {
        NodeRef companyHomeNodeRef = repositoryHelper.getCompanyHome();
        LOGGER.info("companyHomeNodeRef" + companyHomeNodeRef);
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
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
        properties.put(ContentModel.PROP_NAME, CASES);
        NodeRef casesRootNodeRef = nodeService.createNode(companyHomeNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(OpenESDHModel.CASE_URI, CASES), ContentModel.TYPE_FOLDER, properties).getChildRef();
        return casesRootNodeRef;

    }

    /**
     * Creating Groups and assigning permission on New case folder
     * @param caseNodeRef
     * @param caseId
     */
    protected void createGroups(NodeRef caseNodeRef, String caseId) {

        Set<String> settablePermissions = permissionService.getSettablePermissions(caseNodeRef);

        for (Iterator<String> iterator = settablePermissions.iterator(); iterator.hasNext(); ) {
            String permission = iterator.next();

            String groupSuffix = "case_" + caseId + "_" + permission;
            String groupName = authorityService.getName(AuthorityType.GROUP, groupSuffix);

            if (!authorityService.authorityExists(groupName)) {
                groupName = authorityService.createAuthority(AuthorityType.GROUP, groupSuffix);
            }
            permissionService.setPermission(caseNodeRef, groupName, permission, true);
        }

        // Setup owners
        // Create the owner group
        String groupSuffix = "case_" + caseId + "_CaseOwners";
        String groupName = authorityService.getName(AuthorityType.GROUP, groupSuffix);

        if (!authorityService.authorityExists(groupName)) {
            groupName = authorityService.createAuthority(AuthorityType.GROUP, groupSuffix);
        }
        permissionService.setPermission(caseNodeRef, groupName, "CaseOwners", true);

        // Add the owners
        List<AssociationRef> owners = nodeService.getTargetAssocs(caseNodeRef, OpenESDHModel.PROP_CASE_OWNERS);
        for (Iterator<AssociationRef> iterator = owners.iterator(); iterator.hasNext(); ) {
            AssociationRef next = iterator.next();
            NodeRef owner = next.getTargetRef();

            // authorityName, userName
            String ownerName = "";
            if(nodeService.getType(owner).equals(ContentModel.TYPE_AUTHORITY_CONTAINER)) {
                ownerName = (String) nodeService.getProperty(owner, ContentModel.PROP_AUTHORITY_NAME);
            }
            else {
                ownerName = (String) nodeService.getProperty(owner, ContentModel.PROP_USERNAME);
            }
            authorityService.addAuthority(groupName, ownerName);
        }
    }

    /**
     * Create all required NodeRefs
     *
     * @param parentFolderNodeRef
     * @param name
     * @return
     */
    private NodeRef createNode(NodeRef parentFolderNodeRef, String name) {
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
        properties.put(ContentModel.PROP_NAME, name);
        return nodeService.createNode(parentFolderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(OpenESDHModel.CASE_URI, name), ContentModel.TYPE_FOLDER, properties).getChildRef();

    }

    private void setupCase(NodeRef caseNodeRef, NodeRef caseFolderNodeRef, int caseUniqueNumber) {
        String caseId = getCaseId(caseUniqueNumber);


        //Move Case to new location
        // TODO Perhaps use onBeforeCreateNode
        nodeService.moveNode(caseNodeRef, caseFolderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(OpenESDHModel.CASE_URI, caseId));

        //Create Groups and assign permission on new case
        createGroups(caseNodeRef, caseId);

        // Set id on case
        nodeService.setProperty(caseNodeRef, OpenESDHModel.PROP_OE_ID, caseId);

        //Copy Name to title
        Map<QName, Serializable> aspectProperties = new HashMap<QName, Serializable>();
        // TODO Use title from cm:folder as property
        aspectProperties.put(ContentModel.PROP_TITLE, nodeService.getProperty(caseNodeRef, ContentModel.PROP_NAME));
        nodeService.addAspect(caseNodeRef, ContentModel.ASPECT_TITLED, aspectProperties);

        //Renaming of Node to value of Case Id
        nodeService.setProperty(caseNodeRef, ContentModel.PROP_NAME, caseId);


    }

    private String getCaseId(Serializable uniqueNumber) {
        //Generating Case ID
        DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        Date date = new Date();
        StringBuilder caseId = new StringBuilder(dateFormat.format(date));
        caseId.append("-");
        caseId.append(String.format("%04d", uniqueNumber));
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
                int caseUniqueNumber = getUniqueNumber(caseFolderNodeRef);

                setupCase(caseNodeRef, caseFolderNodeRef, caseUniqueNumber);

                return null;
            }
        }, "admin");
    }

    /**
     * Synchronized method to get the unique number used in caseIds.
     * @param caseFolderNodeRef
     * @return The unique number
     */
    // TODO Cluster problems
    private synchronized int getUniqueNumber(NodeRef caseFolderNodeRef) {
        int caseUniqueNumber = 1;
        if (nodeService.hasAspect(caseFolderNodeRef, OpenESDHModel.ASPECT_CASE_COUNTER)) {
            caseUniqueNumber = (int) nodeService.getProperty(caseFolderNodeRef, OpenESDHModel.PROP_CASE_UNIQUE_NUMBER);
        }
        else {
            // Reset the counter for each day - thus storing it on the day nodes
            Map<QName, Serializable> caseNumberAspect = new HashMap<QName, Serializable>();
            // TODO Next line superfluous - test
            caseNumberAspect.put(OpenESDHModel.PROP_CASE_UNIQUE_NUMBER, caseUniqueNumber);
            nodeService.addAspect(caseFolderNodeRef, OpenESDHModel.ASPECT_CASE_COUNTER, caseNumberAspect);
        }
        LOGGER.info("Case Folder exist : " + caseUniqueNumber);
        //assigning Case number value for unique id generation
        nodeService.setProperty(caseFolderNodeRef, OpenESDHModel.PROP_CASE_UNIQUE_NUMBER, ++caseUniqueNumber);
        return caseUniqueNumber;
    }

    /**\
     * Get the nodeRef for the folder in which to place the case.
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
     * @param parent The nodeRef to start from
     * @param calendarType The type of calendar info to look up, i.e. Calendar.YEAR, Calendar.MONTH, or Calendar.DATE
     * @return
     */
    private NodeRef getCasePathNodeRef(NodeRef parent, int calendarType) {
        // Add 1 for months, as they are indexed form 0
        String casePathName = Integer.toString(Calendar.getInstance().get(calendarType) + (calendarType == Calendar.MONTH ? 1 : 0));
        NodeRef casePathNodeRef = nodeService.getChildByName(parent, ContentModel.ASSOC_CONTAINS, casePathName);
        if (casePathNodeRef == null) {
            casePathNodeRef = createNode(parent, casePathName);
        }
        return casePathNodeRef;
    }
}
