package dk.openesdh.repo.webscripts.xsearch;

import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.WebScriptRequest;

import dk.openesdh.repo.services.cases.CaseService;
import dk.openesdh.repo.utils.Utils;

public class DocumentSearch extends XSearchWebscript {

    private static final String NODE_REF_PARAM_NAME = "nodeRef";
    private static final String CASE_ID_PARAM_NAME = "caseId";

    protected CaseService caseService;

    protected Map<String, String> getParams(WebScriptRequest req) {

        Map<String, String> params = Utils.parseParameters(req.getURL());
        Set<String> paramNames = params.keySet();
        if (paramNames.contains(NODE_REF_PARAM_NAME) || !paramNames.contains(CASE_ID_PARAM_NAME)) {
            return params;
        }

        String caseId = params.get(CASE_ID_PARAM_NAME);
        params.remove(CASE_ID_PARAM_NAME, caseId);
        NodeRef caseNodeRef = caseService.getCaseById(caseId);
        params.put(NODE_REF_PARAM_NAME, caseNodeRef.toString());
        return params;
    }

    public void setCaseService(CaseService caseService) {
        this.caseService = caseService;
    }
}
