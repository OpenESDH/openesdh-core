package dk.openesdh.repo.services.documents;

import dk.openesdh.repo.webscripts.cases.CaseInfo;
import dk.openesdh.repo.webscripts.documents.Documents;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by torben on 11/09/14.
 */
public interface DocumentService {

    public java.util.List<ChildAssociationRef> getDocumentsForCase(NodeRef nodeRef);

    JSONObject buildJSON(List<ChildAssociationRef> childAssociationRefs, Documents documents);
}
