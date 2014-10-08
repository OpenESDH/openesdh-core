package dk.openesdh.repo.services.documents;

import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.webscripts.documents.Documents;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.*;

/**
 * Created by torben on 11/09/14.
 */
public class DocumentServiceImpl implements DocumentService {

    private NodeService nodeService;
    private DictionaryService dictionaryService;
    private PersonService personService;

    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    @Override
    public List<ChildAssociationRef> getDocumentsForCase(NodeRef nodeRef) {
        Set<QName> types = new HashSet<>();
        types.add(OpenESDHModel.TYPE_DOC_BASE);
        List<ChildAssociationRef> childAssociationRefs = nodeService.getChildAssocs(nodeRef, types);
        return childAssociationRefs;
    }

    @Override
    public JSONObject buildJSON(List<ChildAssociationRef> childAssociationRefs, Documents documents) {
        JSONObject result = new JSONObject();
        JSONArray documentsJSON = new JSONArray();
        try {
            result.put("documents", documentsJSON);
            for (int i = 0; i < childAssociationRefs.size(); i++) {
                ChildAssociationRef childAssociationRef =  childAssociationRefs.get(i);
                NodeRef childNodeRef = childAssociationRef.getChildRef();

                JSONObject documentJSON = new JSONObject();
                documentJSON.put((String) nodeService.getProperty(childNodeRef, ContentModel.PROP_NAME), nodeService.getProperty(childNodeRef, ContentModel.PROP_UPDATED));
                documentsJSON.put(documentJSON);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }
}
