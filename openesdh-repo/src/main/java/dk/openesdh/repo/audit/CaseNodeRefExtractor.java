package dk.openesdh.repo.audit;

import org.alfresco.repo.audit.extractor.AbstractDataExtractor;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.search.SearchService;

import java.io.Serializable;

public final class CaseNodeRefExtractor extends AbstractDataExtractor {
  private NodeService nodeService;
  private SearchService searchService;

  public void setNodeService(NodeService nodeService) {
    this.nodeService = nodeService;
  }

  public void setSearchService(SearchService searchService) {
    this.searchService = searchService;
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
      System.out.println("start to extract....");
    // TODO Ole,do we ever get an instance of a nodeRef?
    if (value instanceof NodeRef) {
      // received a NodeRef object, we know this is a permission change
      // therefore we always have a path
      NodeRef nodeRef = (NodeRef) value;
      Path path = nodeService.getPath(nodeRef);
      result = nodeRef.toString();
    } else if (value instanceof String) {
      String str = (String) value;
      System.out.println( "EXTRACT DATA: STRING:" + str + "\n\n" );
      if (str.startsWith("("+ getClass().getCanonicalName() + ") GROUP_case_")) {
        String[] parts = str.split("_");
        if (parts.length < 3) {
          return null;
        }
        result = getNodeRefFromCaseID(parts[2]);
      } else {
          System.out.println("this is a path thingie");
        result = getNodeRefFromPath(str);
      }
    }
    // TODO: check that what is returned is actually a case, return null otherwise
    return result;
  }

  private String getNodeRefFromPath(String path) {

      System.out.println("inside getNodeRefFromPath" + path);
    String prefix = "/app:company_home/case:openesdh_cases/";
    if (path.startsWith(prefix)) {


        String[] parts = path.split("/");
        System.out.println("parts6:" + parts[6]);
        System.out.println("split.length" + parts.length);

        if (parts.length >= 7) {
            String node_db_id = parts[6].split("-")[1];
            System.out.println(node_db_id);
            NodeRef nodeRef = nodeService.getNodeRef(Long.parseLong(node_db_id));
            System.out.println(nodeRef);
            if (nodeRef != null) {
              return nodeRef.toString();
            }
        }

    }
    return null;
  }


  private String getNodeRefFromCaseID(String caseID) {
    System.out.println( "EXTRACT DATA: getNodeRefFromCaseID:" + caseID + "\n\n" );
    int dashIndex = caseID.lastIndexOf('-');
    if (dashIndex != -1) {
        NodeRef nodeRef = nodeService.getNodeRef(Long.parseLong(caseID.substring(dashIndex+1)));
        return nodeRef.toString();
    } else {
        return null;
    }
  }
}
