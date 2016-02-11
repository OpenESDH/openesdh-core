package dk.openesdh.repo.services.xsearch;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import dk.openesdh.repo.webscripts.PageableWebScript;

@Service("GroupCasesSearchService")
public class GroupCasesSearchServiceImpl extends XSearchServiceImpl {

    @Autowired
    @Qualifier("CaseDocumentsSearchService")
    private XSearchService documentsSearchService;

    @Override
    protected JSONObject nodeToJSON(NodeRef nodeRef) throws JSONException {
        JSONObject json = super.nodeToJSON(nodeRef);
        Map<String, String> params = new HashMap<>();
        params.put(XSearchService.NODE_REF, nodeRef.toString());
        XResultSet results = documentsSearchService.getNodesJSON(params, 0, PageableWebScript.DEFAULT_PAGE_SIZE,
                null, true);
        json.put(XSearchService.CASE_DOCUMENTS, results.getNodes());
        return json;
    }

}
