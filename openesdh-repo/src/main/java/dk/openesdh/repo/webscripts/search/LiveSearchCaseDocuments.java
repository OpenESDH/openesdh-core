package dk.openesdh.repo.webscripts.search;

import dk.openesdh.repo.model.CaseInfo;
import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.cases.CaseService;
import dk.openesdh.repo.services.documents.DocumentService;
import dk.openesdh.repo.services.documents.DocumentTypeService;
import dk.openesdh.repo.utils.Utils;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.model.ContentModel;
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


public class LiveSearchCaseDocuments extends DeclarativeWebScript {

    //<editor-fold desc="injected services and initialised properties">
    private CaseService caseService;
    private DocumentService documentService;
    private DocumentTypeService documentTypeService;
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
    public void setDocumentTypeService(DocumentTypeService documentTypeService) {
        this.documentTypeService = documentTypeService;
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
            CaseInfo caseItem = caseService.getCaseInfo(docCase);
            //Create the case object which we'll stuff into the document object
            caseObj.put("caseId", caseItem.getCaseId());
            caseObj.put("caseTitle", caseItem.getTitle());

            documentObj.put("name", docProps.get(ContentModel.PROP_NAME));
            documentObj.put("type", documentTypeService.getDocumentTypeOfDocument(document).getName());
            documentObj.put("docState", docProps.get(OpenESDHModel.PROP_DOC_STATE));
            documentObj.put("docStatus", docProps.get(OpenESDHModel.PROP_OE_STATUS));
            documentObj.put("docCategory", docProps.get(OpenESDHModel.PROP_DOC_CATEGORY));
            documentObj.put("version", docProps.get(ContentModel.PROP_VERSION_LABEL));
            documentObj.put("case", caseObj); //This one isn't optional at the moment
            result.put(documentObj);
        }
        return result;
    }
}
