package dk.openesdh.repo.webscripts;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.lang.NotImplementedException;
import org.json.JSONException;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.util.Map;

/**
 * Created by syastrov on 2/6/15.
 */
public class AbstractRESTWebscript extends AbstractWebScript {
    private static final String NODE_ID = "node_id";
    private static final String STORE_ID = "store_id";
    private static final String STORE_TYPE = "store_type";

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws
            IOException {
        Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
        NodeRef nodeRef = null;
        String storeType = templateArgs.get(STORE_TYPE);
        String storeId = templateArgs.get(STORE_ID);
        String nodeId = templateArgs.get(NODE_ID);
        if (storeType != null && storeId != null && nodeId != null) {
            nodeRef = new NodeRef(storeType, storeId, nodeId);
        }

        String method = req.getServiceMatch().getWebScript().getDescription().getMethod();
        try {
            if (method.equals("GET")) {
                get(nodeRef, req, res);
            } else if (method.equals("POST")) {
                post(nodeRef, req, res);
            } else if (method.equals("DELETE")) {
                delete(nodeRef, req, res);
            } else if (method.equals("PUT")) {
                put(nodeRef, req, res);
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
