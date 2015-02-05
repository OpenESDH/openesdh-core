package dk.openesdh.repo.audit;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.audit.extractor.AbstractDataExtractor;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.util.ISO9075;

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

    // TODO Ole,do we ever get an instance of a nodeRef?
    if (value instanceof NodeRef) {
      // received a NodeRef object, we know this is a permission change
      // therefore we always have a path
      NodeRef nodeRef = (NodeRef) value;
      Path path = nodeService.getPath(nodeRef);
      result = nodeRef.toString();
    } else if (value instanceof String) {
      String str = (String) value;
      //System.out.println( "EXTRACT DATA: STRING:" + str + "\n\n" );
      if (str.startsWith("("+ getClass().getCanonicalName() + ") GROUP_case_")) {
        String[] parts = str.split("_");
        if (parts.length < 3) {
          return null;
        }
        result = getNodeRefFromCaseID(parts[2]);
      } else {
          //System.out.println("this is a path thingie");
        result = getNodeRefFromPath(str);
      }
    }
    // TODO: check that what is returned is actually a case, return null otherwise
    return result;
  }

  private String getNodeRefFromPath(String path) {
    String prefix = "/app:company_home/case:openesdh_cases/";
    if (path.startsWith(prefix)) {
      String[] parts = path.split("/");
        if (parts.length >= 7) {
            String node_db_id = parts[6].substring(parts[6].length() - 4);
            NodeRef nodeRef = nodeService.getNodeRef(Long.parseLong(node_db_id));
            if (nodeRef != null) {
              return nodeRef.toString();
            }
        }

    }
    return null;
  }

  private String getNodeRefFromFullPath(Path path) {
    //System.out.println( "EXTRACT DATA: getNodeRefFromFullPath.path: " + path + "\n\n" );
    String prefix = "/{http://www.alfresco.org/model/application/1.0}company_home/{http://www.magenta-aps.dk/model/case/1.0}Sager/{http://www.alfresco.org/model/content/1.0}Alle_x0020_sager";
    String prefixEncoded = "/app:company_home/esdh:Sager/cm:Alle_x0020_sager/";
    if (path.toString().startsWith(prefix)) {
      if (path.size() > 4) {
        String[] caseParts = path.get(4).getElementString().split("}");
        return search(prefixEncoded, "cm", caseParts[caseParts.length - 1]);
      }
    }
    return null;
  }

  private String search(String prefixEncoded, String namespace, String name) {
    String searchStr = prefixEncoded + namespace + ":" + name;
    String resultStr = null;
    ResultSet res = null;
    try {
      res = searchService.query(
          StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
          SearchService.LANGUAGE_XPATH,
          searchStr);
      if (res.length() >= 1) {
        resultStr = res.getNodeRef(0).toString();
      }
    }
    finally {
      if(res != null) {
        res.close();
      }
    }

    if(resultStr == null) {
      // the case has not been indexed yet... look it up with nodeService
      searchStr = "PATH:\"/app:company_home/esdh:Sager/cm:Alle_x0020_sager\"";
      try {
        res = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_LUCENE, searchStr);
        NodeRef nodeRef = res.getNodeRef(0);
        NodeRef caseNodeRef = nodeService.getChildByName(nodeRef, ContentModel.ASSOC_CONTAINS, ISO9075.decode(name));
        resultStr = caseNodeRef.toString();
      }
      finally {
        if(res != null) {
          res.close();
        }
      }
    }
    return resultStr;
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
