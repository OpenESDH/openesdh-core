package dk.openesdh.repo.services.documents;

import com.google.gdata.data.Person;
import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.webscripts.documents.Documents;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.SystemNodeUtils;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.io.FilenameUtils;
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
    private TransactionService transactionService;

    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Override
    public List<ChildAssociationRef> getDocumentsForCase(NodeRef nodeRef) {
        NodeRef documentsFolder = getDocumentsFolder(nodeRef);
        Set<QName> types = new HashSet<>();
        types.add(OpenESDHModel.TYPE_DOC_BASE);
        List<ChildAssociationRef> childAssociationRefs = nodeService.getChildAssocs(documentsFolder, types);
        return childAssociationRefs;
    }

    private NodeRef getDocumentsFolder(NodeRef caseNodeRef) {
        return nodeService.getChildByName(caseNodeRef, ContentModel.ASSOC_CONTAINS, "documents");
    }

    @Override
    public JSONObject buildJSON(List<ChildAssociationRef> childAssociationRefs, Documents documents, NodeRef caseNodeRef) {
        JSONObject result = new JSONObject();
        JSONArray documentsJSON = new JSONArray();
        try {
            result.put("documents", documentsJSON);
            result.put("documentsNodeRef", getDocumentsFolder(caseNodeRef));
            for (int i = 0; i < childAssociationRefs.size(); i++) {
                ChildAssociationRef childAssociationRef = childAssociationRefs.get(i);
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


    @Override
    public void createDocument(final ChildAssociationRef childAssociationRef) {

        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>() {
            @Override
            public Void doWork() {

                transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
                    @Override
                    public Object execute() throws Throwable {
                        NodeRef folderNodeRef = childAssociationRef.getParentRef();
                        NodeRef fileNodeRef = childAssociationRef.getChildRef();

                        String fileName = (String) nodeService.getProperty(fileNodeRef, ContentModel.PROP_NAME);
                        String documentName = FilenameUtils.removeExtension(fileName);

                        // Create document
                        ChildAssociationRef documentAssociationRef = nodeService.createNode(folderNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(OpenESDHModel.DOC_URI, documentName), OpenESDHModel.TYPE_DOC_SIMPLE, Collections.<QName, Serializable>singletonMap(ContentModel.PROP_NAME, documentName));
                        NodeRef documentNodeRef = documentAssociationRef.getChildRef();

                        nodeService.moveNode(fileNodeRef, documentNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(OpenESDHModel.DOC_URI, "content_" + documentName));

                        //Tag the case document as the main document for the case
                        nodeService.addAspect(fileNodeRef, OpenESDHModel.ASPECT_CASE_MAIN_DOC, null);

                        nodeService.setType(fileNodeRef, OpenESDHModel.TYPE_DOC_DIGITAL_FILE);
                        // TODO Get start value, localize
                        nodeService.setProperty(fileNodeRef, OpenESDHModel.PROP_DOC_VARIANT, "Produktion");

                        NodeRef person = personService.getPerson(AuthenticationUtil.getFullyAuthenticatedUser());
                        nodeService.createAssociation(documentNodeRef, person, OpenESDHModel.ASSOC_DOC_RESPONSIBLE_PERSON);
                        nodeService.createAssociation(documentNodeRef, person, OpenESDHModel.ASSOC_DOC_OWNER);

                        return null;
                    }
                });

                return null;
            }
        }, "admin");
    }
}
