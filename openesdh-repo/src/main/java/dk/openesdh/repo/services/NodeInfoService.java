package dk.openesdh.repo.services;

import dk.openesdh.repo.webscripts.cases.CaseInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.WebScript;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by torben on 11/09/14.
 */
public interface NodeInfoService {
    class NodeInfo {
        public Map<QName, Serializable> properties;
        public Set<QName> aspects;
    }

    public NodeInfo getNodeInfo(NodeRef nodeRef);

    JSONObject buildJSON(NodeInfo nodeInfo, CaseInfo caseInfo);

    JSONObject getSelectedProperties(NodeInfo nodeInfo, CaseInfo caseInfo, List<QName> objectProps);
}
