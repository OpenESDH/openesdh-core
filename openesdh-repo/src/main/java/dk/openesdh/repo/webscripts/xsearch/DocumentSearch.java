package dk.openesdh.repo.webscripts.xsearch;

import dk.openesdh.repo.services.documents.DocumentService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class DocumentSearch extends XSearchWebscript {

    protected DocumentService documentService;

    /**
     * Adds the main document nodeRef to the results.
     * @param nodeRef
     * @return
     * @throws JSONException
     */
    protected JSONObject nodeToJSON(NodeRef nodeRef) throws JSONException {
        JSONObject json = super.nodeToJSON(nodeRef);
        NodeRef mainDocNodeRef = documentService.getMainDocument(nodeRef);
        if (mainDocNodeRef != null) {
            json.put("mainDocNodeRef", mainDocNodeRef.toString());
        }
        return json;
    }

    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }

}


