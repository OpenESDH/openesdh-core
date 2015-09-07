package dk.openesdh.repo.webscripts.documents;

import java.io.IOException;

import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import dk.openesdh.repo.model.CaseDocumentAttachment;
import dk.openesdh.repo.services.documents.DocumentService;
import dk.openesdh.repo.utils.Utils;
import dk.openesdh.repo.webscripts.PageableWebScript;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

public class CaseDocumentAttachmentsWithVersionsWebScript extends AbstractWebScript {

    private DocumentService documentService;

    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        String nodeRef = Utils.parseParameters(req.getURL()).get(WebScriptUtils.NODE_REF);
        if (nodeRef == null) {
            return;
        }

        PageableWebScript<CaseDocumentAttachment> ws = 
                (int startIndex, int pageSize) -> documentService.getAttachmentsWithVersions(new NodeRef(nodeRef), startIndex, pageSize);
        PageableWebScript.getItemsPage(req, res, ws);
    }

}
