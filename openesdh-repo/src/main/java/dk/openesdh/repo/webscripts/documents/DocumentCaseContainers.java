package dk.openesdh.repo.webscripts.documents;

import dk.openesdh.repo.services.cases.CaseService;
import dk.openesdh.repo.services.documents.DocumentService;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PersonService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lanre.
 */
public class DocumentCaseContainers extends DeclarativeWebScript {
    private static Log logger = LogFactory.getLog(DocumentCaseContainers.class);
    private CaseService caseService;
    private DocumentService documentService;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache){
        Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();

        String storeType = templateArgs.get("store_type");
        String storeId = templateArgs.get("store_id");
        String id = templateArgs.get("id");
        NodeRef documentNode = new NodeRef(storeType, storeId, id);

        Map<String, Object> model = new HashMap<String, Object>();
        try{
            NodeRef caseNodeRef = caseService.getParentCase(documentNode);
            NodeRef caseDocumentNodeRef  = caseService.getDocumentsFolder(caseNodeRef);

            model.put("caseNodeRef", caseNodeRef.toString());
            model.put("caseDocumentNodeRef", caseDocumentNodeRef.toString());
        }
        catch (InvalidNodeRefException inre) {
            logger.error("Unable to get case document containers due to the following error: "+ inre.getMessage());
        }

        return model;
    }

    public void setCaseService(CaseService caseService) {
        this.caseService = caseService;
    }

    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }
}
