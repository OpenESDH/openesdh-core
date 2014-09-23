package dk.openesdh.repo.services;

import dk.openesdh.repo.webscripts.cases.CaseInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.WebScript;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by torben on 11/09/14.
 */
public interface NodeInfoService {

    public Map<QName, Serializable> getNodeInfo(NodeRef nodeRef);

    JSONObject buildJSON(Map<QName, Serializable> nodeInfo, CaseInfo caseInfo);
}
