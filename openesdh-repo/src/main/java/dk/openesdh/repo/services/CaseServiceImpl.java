package dk.openesdh.repo.services;

import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.logging.Logger;

/**
 * Created by torben on 19/08/14.
 */
public class CaseServiceImpl implements CaseService {

  private Logger logger = Logger.getLogger(String.valueOf(this.getClass()));

  @Autowired
  @Qualifier("NodeService")
  protected NodeService nodeService;

  @Autowired
  @Qualifier("SearchService")
  protected SearchService searchService;

  @Autowired
  @Qualifier("AuthorityService")
  protected AuthorityService authorityService;

  @Autowired
  @Qualifier("PermissionService")
  protected PermissionService permissionService;


  @Override
  public NodeRef getNewCaseFolder() {
/*
    AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>() {
      @Override
      public Void doWork() {


        NodeRef caseNodeRef = childAssocRef.getChildRef();
        LOGGER.info("caseNodeRef "+caseNodeRef);

        NodeRef casesFolderNodeRef;

        //Create folder structure
        ResultSet resultSet = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_XPATH, CaseConstants.COMPANY_HOME_QUERY);
        if(resultSet.length()==0)
        {
          throw new AlfrescoRuntimeException(CaseConstants.COMPANY_HOME_ERROR);

        }else {
          NodeRef companyHomeNodeRef = resultSet.getNodeRef(0);
          LOGGER.info("companyHomeNodeRef"+companyHomeNodeRef);
          casesFolderNodeRef= nodeService.getChildByName(companyHomeNodeRef, ContentModel.ASSOC_CONTAINS, CaseConstants.CASES);

          if(casesFolderNodeRef==null){
            //Creating case folder
            casesFolderNodeRef=createCaseFolder(companyHomeNodeRef) ;
          }else{
            int caseUniqueNumber = 1;
            if(nodeService.getProperty(casesFolderNodeRef, CaseConstants.PROP_CASEUNIQUENUMBER)!=null){
              String uniqueCaseNo = (String) nodeService.getProperty(casesFolderNodeRef, CaseConstants.PROP_CASEUNIQUENUMBER);
              caseUniqueNumber= Integer.parseInt(uniqueCaseNo);
            }
            LOGGER.info("Case Folder exist : "+caseUniqueNumber);
            //assigning Case number value for unique id generation
            nodeService.setProperty(casesFolderNodeRef, CaseConstants.PROP_CASEUNIQUENUMBER, String.format("%03d", ++caseUniqueNumber));
          }

          NodeRef casesYearNodeRef = nodeService.getChildByName(casesFolderNodeRef, ContentModel.ASSOC_CONTAINS,Integer.toString(Calendar.getInstance().get(Calendar.YEAR)));
          if(casesYearNodeRef==null){
            casesYearNodeRef= createNode(casesFolderNodeRef,Integer.toString(Calendar.getInstance().get(Calendar.YEAR)));
          }

          String currentMonth=Integer.toString(Calendar.getInstance().get(Calendar.MONTH)+1);
          NodeRef casesMonthNodeRef = nodeService.getChildByName(casesYearNodeRef, ContentModel.ASSOC_CONTAINS, currentMonth);
          if(casesMonthNodeRef==null){
            casesMonthNodeRef= createNode(casesYearNodeRef,currentMonth);
          }

          NodeRef casesDateNodeRef = nodeService.getChildByName(casesMonthNodeRef, ContentModel.ASSOC_CONTAINS,Integer.toString(Calendar.getInstance().get(Calendar.DATE)));
          if(casesDateNodeRef==null){
            casesDateNodeRef=createNode(casesMonthNodeRef,Integer.toString(Calendar.getInstance().get(Calendar.DATE)));
          }


          //Move Case to new location
          nodeService.moveNode(caseNodeRef,casesDateNodeRef , ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CHILDREN);

          //Create Groups and assign permission on new case
          createGroups(caseNodeRef,(String)nodeService.getProperty(casesFolderNodeRef,CaseConstants.PROP_CASEUNIQUENUMBER));

          //Generating Case ID and setting on Case Folder
          DateFormat dateFormat = new SimpleDateFormat(CaseConstants.DATE_FORMAT);
          Date date = new Date();
          StringBuffer caseid=new StringBuffer(dateFormat.format(date));
          caseid.append("-");
          caseid.append(nodeService.getProperty(casesFolderNodeRef,CaseConstants.PROP_CASEUNIQUENUMBER));
          LOGGER.info("Case Id is "+caseid);
          nodeService.setProperty(caseNodeRef, CaseConstants.PROP_CASEID, caseid.toString());

          //Copy Name to title
          Map<QName, Serializable> aspectProperties=new HashMap<QName, Serializable>();
          aspectProperties.put(ContentModel.PROP_TITLE, nodeService.getProperty(caseNodeRef, ContentModel.PROP_NAME));
          nodeService.addAspect(caseNodeRef, ContentModel.ASPECT_TITLED, aspectProperties);

          //Renaming of Node to value of Case Id
          nodeService.setProperty(caseNodeRef, ContentModel.PROP_NAME,caseid.toString());

        }
        return null;
      }
    }, "admin");


*/

    return null;
  }

  @Override
  public NodeRef createCase(ChildAssociationRef childAssociationRef) {
    logger.info("Create Case was called");
    return null;
  }
}
