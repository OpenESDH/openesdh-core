package dk.openesdh.repo.webscripts.documents;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.util.StringUtils;

import dk.openesdh.repo.model.CaseDocument;
import dk.openesdh.repo.services.documents.DocumentService;
import dk.openesdh.repo.webscripts.WebScriptParams;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

public class CaseDocumentsWithAttachmentsWebScript extends AbstractWebScript {

    private DocumentService documentService;

    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
        String caseId = templateArgs.get(WebScriptParams.CASE_ID);
        if (StringUtils.isEmpty(caseId)) {
            return;
        }
        List<CaseDocument> caseDocuments = documentService.getCaseDocumentsWithAttachments(caseId);
        res.setContentEncoding(WebScriptUtils.CONTENT_ENCODING_UTF_8);
        WebScriptUtils.writeJson(caseDocuments, res);
    }

}
