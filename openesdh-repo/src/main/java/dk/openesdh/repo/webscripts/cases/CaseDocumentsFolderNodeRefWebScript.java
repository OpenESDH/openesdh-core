package dk.openesdh.repo.webscripts.cases;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.UriVariable;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import com.github.dynamicextensionsalfresco.webscripts.resolutions.Resolution;

import dk.openesdh.repo.services.cases.CaseService;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

@Component
@WebScript(description = "Provide api to retrieve case folder nodeRef", families = "Case Tools")
public class CaseDocumentsFolderNodeRefWebScript {

    @Autowired
    @Qualifier("CaseService")
    private CaseService caseService;

    @Uri("/api/openesdh/case/{caseId}/docfolder/noderef")
    public Resolution getCaseDocsFolderByCaseId(@UriVariable(WebScriptUtils.CASE_ID) String caseId) {
        NodeRef caseNodeRef = caseService.getCaseById(caseId);
        return getCaseDocsFolderRef(caseNodeRef);
    }

    @Uri("/api/openesdh/case/{protocol}/{storeId}/{id}/docfolder/noderef")
    public Resolution getCaseDocsFolderByCaseRef(@UriVariable("protocol") String protocol,
            @UriVariable("storeId") String storeId, @UriVariable("id") String id) {
        NodeRef caseNodeRef = new NodeRef(protocol, storeId, id);
        return getCaseDocsFolderRef(caseNodeRef);
    }

    private Resolution getCaseDocsFolderRef(NodeRef caseNodeRef) {
        NodeRef caseDocumentsFolderNodeRef = caseService.getDocumentsFolder(caseNodeRef);
        Map<String, String> result = new HashMap<>();
        result.put("caseDocsFolderNodeRef", caseDocumentsFolderNodeRef.toString());
        return WebScriptUtils.jsonResolution(result);
    }
}
