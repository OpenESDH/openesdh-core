package dk.openesdh.repo.webscripts.documents;

import java.io.IOException;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.stereotype.Component;

import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.RequestParam;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import com.github.dynamicextensionsalfresco.webscripts.resolutions.Resolution;

import dk.openesdh.repo.services.documents.CaseDocumentCopyService;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

@Component
@WebScript(description = "Provides API for case documents", families = "Case Document")
public class DocumentsWebScript {

    @Autowired
    @Qualifier("CaseDocumentCopyService")
    private CaseDocumentCopyService caseDocumentCopyService;

    @Uri(value = "/api/openesdh/case/document/detach", method = HttpMethod.PUT)
    public Resolution detachDocument(
            @RequestParam final NodeRef documentRef,
            @RequestParam final NodeRef newOwnerRef,
            @RequestParam(required = false) final String comment) throws IOException {
        caseDocumentCopyService.detachCaseDocument(documentRef, newOwnerRef, comment);
        return WebScriptUtils.jsonResolution("success");
    }
}
