package dk.openesdh.repo.webscripts;

import java.io.IOException;
import java.util.Map;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.lang.NotImplementedException;
import org.json.JSONException;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Created by syastrov on 2/6/15.
 */
public class AbstractRESTWebscript extends AbstractWebScript {
    private static final String NODE_ID = "node_id";
    private static final String STORE_ID = "store_id";
    private static final String STORE_TYPE = "store_type";

    protected NodeRef getNodeRef(WebScriptRequest req, Map<String, String> templateArgs) {
        NodeRef nodeRef = null;
        String storeType = templateArgs.get(STORE_TYPE);
        String storeId = templateArgs.get(STORE_ID);
        String nodeId = templateArgs.get(NODE_ID);
        if (storeType != null && storeId != null && nodeId != null) {
            nodeRef = new NodeRef(storeType, storeId, nodeId);
        }
        return nodeRef;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
        NodeRef nodeRef = getNodeRef(req, templateArgs);
        String method = req.getServiceMatch().getWebScript().getDescription().getMethod();
        try {
            switch (method) {
                case "GET":
                    get(nodeRef, req, res);
                    break;
                case "POST":
                    post(nodeRef, req, res);
                    break;
                case "DELETE":
                    delete(nodeRef, req, res);
                    break;
                case "PUT":
                    put(nodeRef, req, res);
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected void get(NodeRef nodeRef, WebScriptRequest req, WebScriptResponse res) throws IOException, JSONException {
        throw new NotImplementedException();
    }

    protected void post(NodeRef nodeRef, WebScriptRequest req, WebScriptResponse res) throws IOException, JSONException {
        throw new NotImplementedException();
    }

    protected void put(NodeRef nodeRef, WebScriptRequest req,
                       WebScriptResponse res) throws IOException, JSONException {
        throw new NotImplementedException();
    }

    protected void delete(NodeRef nodeRef, WebScriptRequest req,
                          WebScriptResponse res) throws IOException, JSONException {
        throw new NotImplementedException();
    }
}
