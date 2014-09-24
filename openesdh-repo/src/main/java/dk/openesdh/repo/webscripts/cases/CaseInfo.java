package dk.openesdh.repo.webscripts.cases;

import dk.openesdh.repo.services.NodeInfoService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

/**
 * Created by torben on 11/09/14.
 */
public class CaseInfo extends AbstractWebScript {

    private NodeInfoService nodeInfoService;

    private NamespaceService namespaceService;
    private DictionaryService dictionaryService;

    public void setNodeInfoService(NodeInfoService nodeInfoService) {
        this.nodeInfoService = nodeInfoService;
    }

    public NamespaceService getNamespaceService() { return namespaceService; }
    public void setNamespaceService(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }

    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        NodeRef caseNodeRef = new NodeRef(req.getParameter("nodeRef"));
        Map<QName, Serializable> nodeInfo = nodeInfoService.getNodeInfo(caseNodeRef);
        JSONObject json = nodeInfoService.buildJSON(nodeInfo, this);

        try {
            json.write(res.getWriter());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


}
