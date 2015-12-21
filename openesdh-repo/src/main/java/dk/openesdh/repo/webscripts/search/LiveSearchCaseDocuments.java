package dk.openesdh.repo.webscripts.search;

import dk.openesdh.repo.model.CaseInfo;
import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.cases.CaseService;
import dk.openesdh.repo.services.documents.DocumentService;
import dk.openesdh.repo.utils.Utils;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyCheck;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Keeping this until not needed anymore
 */
@Deprecated
public class LiveSearchCaseDocuments extends DeclarativeWebScript {

    //<editor-fold desc="injected services and initialised properties">
    private CaseService caseService;
    private DocumentService documentService;
    private NodeService nodeService;

    public void setCaseService(CaseService caseService) {
        this.caseService = caseService;
    }
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }
    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }
    //</editor-fold>
    private static final Logger logger = Logger.getLogger(LiveSearchCaseDocuments.class);

    public void init() {
        PropertyCheck.mandatory(this, "DocumentService", documentService);
        PropertyCheck.mandatory(this, "CaseService", caseService);
        PropertyCheck.mandatory(this, "NodeService", nodeService);
    }

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req,  Status status, Cache cache) {
        Map<String, String> params = Utils.parseParameters(req.getURL());
        Map<String, Object> model = new HashMap<>();
        int maxResults = 3;
        try {
            maxResults = Integer.parseInt(params.get("maxResults"));
        }
        catch (NumberFormatException nfe){
            if(logger.isDebugEnabled())
                logger.warn("\n\n-----> Max results parameter was unreadable from the webscript request parameter:\n\t\t\t"+ nfe.getLocalizedMessage());
        }

        try {
            List<NodeRef> foundDocuments = this.documentService.findCaseDocuments(params.get("t"), maxResults);
            JSONArray jsonArray = buildJSON(foundDocuments);
            model.put("documentList", jsonArray);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return model;
    }

    JSONArray buildJSON(List<NodeRef> documents) throws JSONException {
        JSONArray result = new JSONArray();
        for(NodeRef document : documents){
            JSONObject documentObj = new JSONObject();
            JSONObject caseObj = new JSONObject();
            Map<QName, Serializable> docProps = nodeService.getProperties(document);
            //The case to which the document belongs
            NodeRef docCase = documentService.getCaseNodeRef(document);
            //The actual docRecord (Folder) representing the document itself. This contains the "main document" we're interested in
            NodeRef docRecord = nodeService.getPrimaryParent(document).getParentRef();

            CaseInfo caseItem = caseService.getCaseInfo(docCase);
            //Create the case object which we'll stuff into the document object
            caseObj.put("caseNodeRef", caseItem.getNodeRef());
            caseObj.put("caseId", caseItem.getCaseId());
            caseObj.put("caseTitle", caseItem.getTitle());
            //Needed to get the mimetype
            ContentData docData = (ContentData) docProps.get(ContentModel.PROP_CONTENT);

            documentObj.put("name", docProps.get(ContentModel.PROP_NAME));
            documentObj.put("title", docProps.get(ContentModel.PROP_TITLE));
            documentObj.put("nodeRef", document);
            documentObj.put("docRecordNodeRef", docRecord);
            documentObj.put("docStatus", nodeService.getProperty(docRecord, OpenESDHModel.PROP_OE_STATUS));
            documentObj.put("version", docProps.get(ContentModel.PROP_VERSION_LABEL));
            documentObj.put("fileMimeType", docData.getMimetype());
            documentObj.put("case", caseObj); //This one isn't optional at the moment
            result.put(documentObj);
        }
        return result;
    }
}
