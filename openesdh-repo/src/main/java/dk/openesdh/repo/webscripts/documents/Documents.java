package dk.openesdh.repo.webscripts.documents;

import dk.openesdh.repo.services.NodeInfoService;
import dk.openesdh.repo.services.documents.DocumentService;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by torben on 11/09/14.
 */
public class Documents extends AbstractWebScript {

    private NodeService nodeService;
    private DocumentService documentService;

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        NodeRef caseNodeRef = new NodeRef(req.getParameter("nodeRef"));
        List<ChildAssociationRef> documents = documentService.getDocumentsForCase(caseNodeRef);
        JSONObject json = documentService.buildJSON(documents, this);

        try {
            json.put("caseTitle", nodeService.getProperty(caseNodeRef, ContentModel.PROP_NAME));
            json.write(res.getWriter());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
