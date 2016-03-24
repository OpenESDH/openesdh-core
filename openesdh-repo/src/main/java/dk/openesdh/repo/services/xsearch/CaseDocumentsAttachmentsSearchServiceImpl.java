package dk.openesdh.repo.services.xsearch;

import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.io.FilenameUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import dk.openesdh.repo.services.documents.DocumentService;

@Service("CaseDocumentsAttachmentsSearchService")
public class CaseDocumentsAttachmentsSearchServiceImpl extends XSearchServiceImpl implements
        CaseDocumentsAttachmentsSearchService {

    @Autowired
    @Qualifier(DocumentService.BEAN_ID)
    protected DocumentService documentService;

    @Override
    protected JSONObject nodeToJSON(NodeRef nodeRef) throws JSONException {
        JSONObject node = super.nodeToJSON(nodeRef);
        String fileName = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
        String extension = FilenameUtils.getExtension(fileName);
        node.put("fileType", extension);
        return node;
    }

    @Override
    public XResultSet getAttachments(Map<String, String> params) {
        NodeRef documentsNodeRef = new NodeRef(params.get("nodeRef"));
        XResultSet result = new XResultSet(documentService.getAttachments(documentsNodeRef));
        result.setNodes(getNodesJSON(result.getNodeRefs()));
        return result;
    }
}
