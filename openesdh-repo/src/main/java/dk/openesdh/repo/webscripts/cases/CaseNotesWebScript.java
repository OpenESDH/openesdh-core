package dk.openesdh.repo.webscripts.cases;

import dk.openesdh.repo.services.cases.CaseService;
import dk.openesdh.repo.webscripts.WebScriptParams;
import dk.openesdh.repo.webscripts.notes.NotesWebScript;

import java.util.Map;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class CaseNotesWebScript extends NotesWebScript {

    private CaseService caseService;

    @Override
    protected NodeRef getNodeRef(WebScriptRequest req, Map<String, String> templateArgs) {
        String caseId = templateArgs.get(WebScriptParams.CASE_ID);
        return caseService.getCaseById(caseId);
    }

    public void setCaseService(CaseService caseService) {
        this.caseService = caseService;
    }
}
