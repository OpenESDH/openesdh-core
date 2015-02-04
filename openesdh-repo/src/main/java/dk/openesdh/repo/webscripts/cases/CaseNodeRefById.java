package dk.openesdh.repo.webscripts.cases;

import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.cases.CaseService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.util.Map;

/**
 * @author Lanre.
 */
public class CaseNodeRefById extends AbstractWebScript {
    private static Log logger = LogFactory.getLog(CaseNodeRefById.class);

    private CaseService caseService;
    private NodeService nodeService;
    @Override
    public void execute(WebScriptRequest req, WebScriptResponse resp) throws IOException {
        Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
        String caseId = templateArgs.get("caseId");

        JSONObject json = new JSONObject();
        try {
            NodeRef caseNodeRef = caseService.getCaseById(caseId);

            json.put("caseNodeRef", caseNodeRef);
            //The next three properties initially used for the module extension evaluator
            json.put("caseId", this.nodeService.getProperty(caseNodeRef, OpenESDHModel.PROP_OE_ID));
            json.put("caseStatus", this.nodeService.getProperty(caseNodeRef,OpenESDHModel.PROP_OE_STATUS));
            json.put("caseType", this.nodeService.getType(caseNodeRef));
            json.write(resp.getWriter());
        }

        catch (JSONException jse){
            logger.error("Unable to build the json model because of the following exception: "+ jse.getMessage());
        }
    }

    //<editor-fold desc="Injected service setters">
    public void setCaseService(CaseService caseService) {
        this.caseService = caseService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }
    //</editor-fold>

}
