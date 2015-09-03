package dk.openesdh.repo.webscripts.cases;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import dk.openesdh.repo.services.cases.CaseService;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

public class CaseDocumentsFolderNodeRefByCaseId extends AbstractWebScript {

    private CaseService caseService;

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
        String caseId = templateArgs.get("caseId");
        NodeRef caseNodeRef = caseService.getCaseById(caseId);
        NodeRef caseDocumentsFolderNodeRef = caseService.getDocumentsFolder(caseNodeRef);
        Map<String, String> result = new HashMap<String, String>();
        result.put("caseDocsFolderNodeRef", caseDocumentsFolderNodeRef.toString());
        res.setContentEncoding("UTF-8");
        WebScriptUtils.writeJson(result, res);
    }

    public void setCaseService(CaseService caseService) {
        this.caseService = caseService;
    }
}
