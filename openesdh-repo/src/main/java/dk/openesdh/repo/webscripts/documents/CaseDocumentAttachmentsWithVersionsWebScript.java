package dk.openesdh.repo.webscripts.documents;

import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.github.dynamicextensionsalfresco.webscripts.annotations.RequestParam;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import com.github.dynamicextensionsalfresco.webscripts.resolutions.Resolution;

import dk.openesdh.repo.model.CaseDocumentAttachment;
import dk.openesdh.repo.services.documents.DocumentService;
import dk.openesdh.repo.webscripts.PageableWebScript;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

@Component
@WebScript(description = "Retrieves attachments of a case document", families = "Case Documents")
public class CaseDocumentAttachmentsWithVersionsWebScript{

    @Autowired
    @Qualifier("DocumentService")
    private DocumentService documentService;

    @Uri(value = "/api/openesdh/case/document/attachments/versions", defaultFormat = "json")
    public Resolution execute(@RequestParam(value = WebScriptUtils.NODE_REF) String nodeRef){

        PageableWebScript<CaseDocumentAttachment> ws = 
            (int startIndex, int pageSize) -> documentService.getAttachmentsWithVersions(new NodeRef(nodeRef), startIndex, pageSize);
                
        return PageableWebScript.getItemsPage(ws);
    }

}
