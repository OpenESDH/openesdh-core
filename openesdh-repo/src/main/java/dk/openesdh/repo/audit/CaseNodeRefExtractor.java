package dk.openesdh.repo.audit;

import dk.openesdh.repo.services.cases.CaseService;
import org.alfresco.repo.audit.extractor.AbstractDataExtractor;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;

import java.io.Serializable;

public final class CaseNodeRefExtractor extends AbstractDataExtractor {
    private NodeService nodeService;
    private CaseService caseService;

    //<editor-fold desc="bean definition property setters">
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setCaseService(CaseService caseService) {
        this.caseService = caseService;
    }

    public boolean isSupported(Serializable data) {
        return true;
    }
    //</editor-fold>

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

    private String getNodeRefFromPath(String path) {

        String prefix = caseService.OPENESDH_ROOT_CONTEXT_PATH;
        if (path.startsWith(prefix)) {


            String[] parts = path.split("/");

            if (parts.length >= 7) {
                String node_db_id = parts[6].split("-")[1];
                NodeRef nodeRef = nodeService.getNodeRef(Long.parseLong(node_db_id));
//            System.out.println(nodeRef);
                if (nodeRef != null) {
                    return nodeRef.toString();
                }
            }

        }
        return null;
    }

    private String getNodeRefFromCaseID(String caseID) {
        int dashIndex = caseID.lastIndexOf('-');
        if (dashIndex != -1) {
            NodeRef nodeRef = nodeService.getNodeRef(Long.parseLong(caseID.substring(dashIndex + 1)));
            return nodeRef.toString();
        } else {
            return null;
        }
    }
}
