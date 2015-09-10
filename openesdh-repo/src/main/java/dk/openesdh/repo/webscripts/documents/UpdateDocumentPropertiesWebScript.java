package dk.openesdh.repo.webscripts.documents;

import java.io.IOException;

import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import dk.openesdh.repo.model.CaseDocument;
import dk.openesdh.repo.services.documents.DocumentService;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

public class UpdateDocumentPropertiesWebScript extends AbstractWebScript {

    private DocumentService documentService;

    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        CaseDocument document = (CaseDocument) WebScriptUtils.readJson(CaseDocument.class, req);
        documentService.updateCaseDocumentProperties(document);
    }

}
