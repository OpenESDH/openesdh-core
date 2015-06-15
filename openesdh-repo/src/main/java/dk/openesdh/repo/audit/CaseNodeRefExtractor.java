package dk.openesdh.repo.audit;

import java.io.Serializable;
import java.util.regex.Matcher;

import org.alfresco.repo.audit.extractor.AbstractDataExtractor;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.search.SearchService;
import org.springframework.util.StringUtils;

import dk.openesdh.repo.services.cases.CaseService;

public final class CaseNodeRefExtractor extends AbstractDataExtractor {

    private NodeService nodeService;
    private SearchService searchService;
    private CaseService caseService;

  public void setNodeService(NodeService nodeService) {
    this.nodeService = nodeService;
  }

  public void setSearchService(SearchService searchService) {
    this.searchService = searchService;
  }

    public void setCaseService(CaseService caseService) {
        this.caseService = caseService;
    }

    public boolean isSupported(Serializable data) {
    return true;
  /*
        if (data == null || !(data instanceof NodeRef))
        {
            return false;
        }
        return nodeService.hasAspect((NodeRef)data, RecordsManagementModel.ASPECT_FILE_PLAN_COMPONENT);
	*/
  }

  public Serializable extractData(Serializable value) throws Throwable {
    String result = null;
//      System.out.println("start to extract....");

    // TODO Ole,do we ever get an instance of a nodeRef?
    if (value instanceof NodeRef) {
      // received a NodeRef object, we know this is a permission change
      // therefore we always have a path
      NodeRef nodeRef = (NodeRef) value;
      Path path = nodeService.getPath(nodeRef);
      result = nodeRef.toString();
    }
    else if (value instanceof String) {
      String str = (String) value;
//      System.out.println( "EXTRACT DATA: STRING:" + str + "\n\n" );
      if (str.startsWith("GROUP_case_")) {
        String[] parts = str.split("_");
        if (parts.length < 3) {
          return null;
        }
        result = getNodeRefFromCaseID(parts[2]);
      } else {
          // System.out.println("this is a path thingie");
        result = getNodeRefFromPath(str);
      }
    }
    // TODO: check that what is returned is actually a case, return null otherwise
    return result;
  }

    protected String getNodeRefFromPath(String path) {

        String prefix = caseService.getOpenEsdhCasesRootFolderPath();
        if (!path.startsWith(prefix)) {
            return null;
        }
        
        String caseId = getCaseIdFromPath(path);
        if (StringUtils.isEmpty(caseId)) {
            return null;
        }

        NodeRef caseNodeRef = caseService.getCaseById(caseId);
        if (caseNodeRef != null) {
            return caseNodeRef.toString();
        }
        return null;
    }

    /**
     * Retrieves case id from the provided path of the following format:
     *      /app:company_home/oe:OpenESDH/oe:cases/case:2015/case:6/case:15/case:20150615-916
     * 
     * @param path the path to retrieve a case id from
     * @return case id
     */
    protected String getCaseIdFromPath(String path) {
        Matcher m = CaseService.CASE_ID_PATTERN.matcher(path);
        if (m.find()) {
            return m.group();
        }
        return null;
    }

  private String getNodeRefFromCaseID(String caseID) {
//    System.out.println( "EXTRACT DATA: getNodeRefFromCaseID:" + caseID + "\n\n" );
    int dashIndex = caseID.lastIndexOf('-');
    if (dashIndex != -1) {
        NodeRef nodeRef = nodeService.getNodeRef(Long.parseLong(caseID.substring(dashIndex+1)));
        return nodeRef.toString();
    } else {
        return null;
    }
  }
}
