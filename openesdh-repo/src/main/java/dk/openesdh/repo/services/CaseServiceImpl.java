package dk.openesdh.repo.services;

import dk.openesdh.repo.model.OpenESDHModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
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
     *  @param caseNodeRef
     * @param uniqueNumber
     */
    protected void createGroups(NodeRef caseNodeRef, int uniqueNumber) {

        String gName1 = CASE_CONSUMER + uniqueNumber;
        String groupName1 = authorityService.getName(AuthorityType.GROUP, gName1);

        if (!authorityService.authorityExists(groupName1)) {
            groupName1 = authorityService.createAuthority(AuthorityType.GROUP, gName1);
        }
        permissionService.setPermission(caseNodeRef, groupName1, PermissionService.CONSUMER, true);

        String gName2 = CASE_COORDINATOR + uniqueNumber;
        String groupName2 = authorityService.getName(AuthorityType.GROUP, gName2);

        if (!authorityService.authorityExists(groupName2)) {
            groupName2 = authorityService.createAuthority(AuthorityType.GROUP, gName2);
        }
        permissionService.setPermission(caseNodeRef, groupName2, PermissionService.COORDINATOR, true);

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
        nodeService.moveNode(caseNodeRef, caseFolderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(OpenESDHModel.CASE_URI, caseId));

        //Create Groups and assign permission on new case
        createGroups(caseNodeRef, caseUniqueNumber);

        // Set id on case
        nodeService.setProperty(caseNodeRef, OpenESDHModel.PROP_OE_ID, caseId);

        //Copy Name to title
        Map<QName, Serializable> aspectProperties = new HashMap<QName, Serializable>();
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
                int caseUniqueNumber = getUniqueNumber(caseFolderNodeRef);

                setupCase(caseNodeRef, caseFolderNodeRef, caseUniqueNumber);

                return null;
            }
        }, "admin");
    }

    private int getUniqueNumber(NodeRef caseFolderNodeRef) {
        int caseUniqueNumber = 1;
        if (nodeService.hasAspect(caseFolderNodeRef, OpenESDHModel.ASPECT_CASE_COUNTER)) {
            caseUniqueNumber = (int) nodeService.getProperty(caseFolderNodeRef, OpenESDHModel.PROP_CASE_UNIQUE_NUMBER);
        }
        else {
            // Reset the counter for each day - thus storing it on the day nodes
            Map<QName, Serializable> caseNumberAspect = new HashMap<QName, Serializable>();
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
