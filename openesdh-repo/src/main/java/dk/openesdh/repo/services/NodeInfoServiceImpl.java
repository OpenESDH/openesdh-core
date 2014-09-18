package dk.openesdh.repo.services;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by torben on 11/09/14.
 */
public class NodeInfoServiceImpl implements NodeInfoService {

    private NodeService nodeService;

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }


    @Override
    public Map<QName, Serializable> getNodeInfo(NodeRef nodeRef) {
        Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
        return properties;
    }
}
