package dk.openesdh.repo.services.documents;

import dk.openesdh.repo.webscripts.cases.CaseInfo;
import dk.openesdh.repo.webscripts.documents.Documents;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by torben on 11/09/14.
 */
public interface DocumentService {

    /**
     * Gets the main document node in case document (i.e. The content with the doc:main aspect inside the folder)
     * @param caseDocNodeRef The actual case document nodeRef (i.e. The case card folder)
     * @return
     */
    public NodeRef getMainDocument(NodeRef caseDocNodeRef);

    /**
     * Get the owner of the document
     * @param caseDocNodeRef the Case document node
     * @return a PersonInfo structure from which we can query various properties of the person
     */
    public PersonService.PersonInfo getDocumentOwner(NodeRef caseDocNodeRef);

    /**
     * Get the person marked as being responsible for the document. We return a list to leave room for the
     * future possibility that there might be more than one person responsible for a document
     * @param caseDocNodeRef the Case document node
     * @return a PersonInfo structure from which we can query various properties of the person
     */
    public List<PersonService.PersonInfo> getDocResponsibles(NodeRef caseDocNodeRef);

    public java.util.List<ChildAssociationRef> getDocumentsForCase(NodeRef nodeRef);

    JSONObject buildJSON(List<ChildAssociationRef> childAssociationRefs, Documents documents, NodeRef caseNodeRef);

    ChildAssociationRef createDocumentFolder(final NodeRef parent, final String name);

    void createDocument(ChildAssociationRef childAssociationRef);

    /**
     * This method gets the <code>case:simple</code> NodeRef for the case which contains the given NodeRef.
     * If the given NodeRef is not contained within a case, then <code>null</code> is returned.
     *
     * @param nodeRef   the node whose containing case is to be found.
     * @return NodeRef  case node reference or null if node is not within a case
     */
    public NodeRef getCaseNodeRef(NodeRef nodeRef);
}
