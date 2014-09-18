package dk.openesdh.repo.services;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by torben on 11/09/14.
 */
public interface NodeInfoService {
    public Map<QName, Serializable> getNodeInfo(NodeRef nodeRef);

}
