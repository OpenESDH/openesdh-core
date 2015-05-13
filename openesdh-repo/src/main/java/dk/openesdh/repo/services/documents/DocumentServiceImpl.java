package dk.openesdh.repo.services.documents;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tika.mime.MimeTypes;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.cases.CaseService;
import dk.openesdh.repo.webscripts.documents.Documents;

/**
 * Created by torben on 11/09/14.
 */

public class DocumentServiceImpl implements DocumentService {

    private static Log logger = LogFactory.getLog(DocumentServiceImpl.class);

	private static final String DOCUMENT_STORED_IN_CASE_MESSAGE = "The document has already been stored in the case ";

    private NodeService nodeService;
    private DictionaryService dictionaryService;
    private PersonService personService;
    private TransactionService transactionService;
    private CaseService caseService;
    private NamespaceService namespaceService;
    private BehaviourFilter behaviourFilter;
    private CopyService copyService;

    private MimeTypes allMimeTypes = MimeTypes.getDefaultMimeTypes();
    private MimeTypes types;

    //<editor-fold desc="Setters">
    public void setCaseService(CaseService caseService) {
        this.caseService = caseService;
    }

    public void setNamespaceService(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }

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

    public void setBehaviourFilter(BehaviourFilter behaviourFilter) {
        this.behaviourFilter = behaviourFilter;
    }

    public void setCopyService(CopyService copyService) {
        this.copyService = copyService;
    }
	// </editor-fold>

    @Override
    public List<ChildAssociationRef> getDocumentsForCase(NodeRef nodeRef) {
        NodeRef documentsFolder = caseService.getDocumentsFolder(nodeRef);
        Set<QName> types = new HashSet<>();
        types.add(OpenESDHModel.TYPE_DOC_SIMPLE);
        List<ChildAssociationRef> childAssociationRefs = nodeService.getChildAssocs(documentsFolder, types);
        return childAssociationRefs;
    }

    @Override
    public JSONObject buildJSON(List<ChildAssociationRef> childAssociationRefs, Documents documents, NodeRef caseNodeRef) {
        JSONObject result = new JSONObject();
        JSONArray documentsJSON = new JSONArray();

        try {
            result.put("documents", documentsJSON);
            result.put("documentsNodeRef", caseService.getDocumentsFolder
                    (caseNodeRef));
            for (int i = 0; i < childAssociationRefs.size(); i++) {
                ChildAssociationRef childAssociationRef = childAssociationRefs.get(i);
                NodeRef childNodeRef = childAssociationRef.getChildRef();

                JSONObject documentJSON = new JSONObject();

                Map<QName, Serializable> props = nodeService.getProperties(childNodeRef);

                for (Map.Entry<QName, Serializable> entry : props.entrySet()) {
                    Serializable value = entry.getValue();
                    QName key = entry.getKey();
                    JSONObject valueObj = new JSONObject();
                    if (value != null) {
                        if (Date.class.equals(value.getClass())) {
                            valueObj.put("type", "Date");
                            valueObj.put("value", ((Date) value).getTime());
                        }
                        else if(key.getPrefixString().equals("modifier") || key.getPrefixString().equals("creator")) {
                            valueObj.put("type", "UserName");
                            valueObj.put("value", value);
                            NodeRef personNodeRef = personService.getPerson((String) value);
                            String firstName = (String) nodeService.getProperty(personNodeRef, ContentModel.PROP_FIRSTNAME);
                            String lastName = (String) nodeService.getProperty(personNodeRef, ContentModel.PROP_LASTNAME);
                            valueObj.put("fullname", firstName + " " + lastName);
                        } else {
                            valueObj.put("value", value);
                            valueObj.put("type", "String");
                        }

                        valueObj.put("label", dictionaryService.getProperty(key).getTitle(dictionaryService));

                        documentJSON.put(entry.getKey().toPrefixString(this.namespaceService), valueObj);
                    }
                }

                documentsJSON.put(documentJSON);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public ChildAssociationRef createDocumentFolder(final NodeRef documentsFolder, final String name) {
        Map<QName, Serializable> props = new HashMap<>(1);
        return this.createDocumentFolder(documentsFolder,name, props);
    }

    @Override
    public ChildAssociationRef createDocumentFolder(NodeRef documentsFolder, String name, Map<QName, Serializable> props) {
        props.put(ContentModel.PROP_NAME, name);
        ChildAssociationRef documentAssociationRef = nodeService.createNode(documentsFolder, ContentModel.ASSOC_CONTAINS, QName.createQName(OpenESDHModel.DOC_URI, name), OpenESDHModel.TYPE_DOC_SIMPLE, props);
        NodeRef documentNodeRef = documentAssociationRef.getChildRef();

        NodeRef person = personService.getPerson(AuthenticationUtil.getFullyAuthenticatedUser());
        nodeService.createAssociation(documentNodeRef, person, OpenESDHModel.ASSOC_DOC_OWNER);
        nodeService.createAssociation(documentNodeRef, person, OpenESDHModel.ASSOC_DOC_RESPONSIBLE_PERSON);
        return documentAssociationRef;
    }

    @Override
    public NodeRef getMainDocument(NodeRef caseDocNodeRef){
        List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(caseDocNodeRef, OpenESDHModel
                .ASSOC_DOC_MAIN, QName.createQName(OpenESDHModel.DOC_URI, "main"));
        return childAssocs.get(0).getChildRef();
    }

    @Override
    public PersonService.PersonInfo getDocumentOwner(NodeRef caseDocNodeRef) {
        List <AssociationRef> ownerList = this.nodeService.getTargetAssocs(caseDocNodeRef, OpenESDHModel.ASSOC_DOC_OWNER);
        PersonService.PersonInfo owner =null ;

        if(ownerList.size() >= 1) //should always = 1 but just in case
            owner = this.personService.getPerson(ownerList.get(0).getTargetRef()); //return the first one in the list

        return owner;
    }

    @Override
    public List<PersonService.PersonInfo> getDocResponsibles(NodeRef caseDocNodeRef) {
        //TODO could it be the case that in the future there could be more than one person responsible for a document
        List <AssociationRef> responsibleList = this.nodeService.getTargetAssocs(caseDocNodeRef, OpenESDHModel.ASSOC_DOC_RESPONSIBLE_PERSON);
        List <PersonService.PersonInfo> responsibles = new ArrayList<>();

        for(AssociationRef person : responsibleList)
            responsibles.add(this.personService.getPerson(person.getTargetRef()));

        return responsibles;
    }

    /**
     * Gets the nodeRef of a document or folder within a case by recursively trawling up the tree until the caseType is detected
     * @param nodeRef the node whose containing case is to be found.
     * @return the case container noderef
     */
    @Override
    public NodeRef getCaseNodeRef(NodeRef nodeRef) {
        NodeRef caseNodeRef = null;
        QName nodeRefType = this.nodeService.getType(nodeRef);
        if (dictionaryService.isSubClass(nodeRefType, OpenESDHModel.TYPE_CASE_BASE) ) {
            caseNodeRef = nodeRef;
        }
        else {
            ChildAssociationRef primaryParent = this.nodeService.getPrimaryParent(nodeRef);
            if (primaryParent != null && primaryParent.getParentRef() != null) {
                caseNodeRef = getCaseNodeRef(primaryParent.getParentRef());
            }
        }
        return caseNodeRef;
    }

    @Override
    public List<NodeRef> getAttachments(NodeRef docRecordNodeRef) {
        NodeRef mainDocNodeRef = getMainDocument(docRecordNodeRef);

        Collection<ChildAssociationRef> attachmentRefs = this.nodeService.getChildAssocs(docRecordNodeRef, null, null);

        List<NodeRef> attachmentNodeRefs = new ArrayList<>();
        for(ChildAssociationRef childRef : attachmentRefs){
            if (!childRef.getChildRef().equals(mainDocNodeRef)) {
                attachmentNodeRefs.add(childRef.getChildRef());
            }
        }
        return attachmentNodeRefs;
    }

    /**
     * Returns true if the file name has an extension
     * @param filename the string representation of the filename in question
     * @return {boolean}
     */
    public static boolean hasFileExtentsion(String filename){
        String fileNameExt =  FilenameUtils.getExtension(filename);
        return StringUtils.isNotEmpty(fileNameExt);
    }

    @Override
    public void moveDocumentToCase(NodeRef documentToMove, String targetCaseId) throws Exception {

        ChildAssociationRef docToFolderAssoc = getDocumentPrimaryParent(documentToMove);

        if (isCaseContainsDocument(targetCaseId, documentToMove)) {
            throw new Exception(DOCUMENT_STORED_IN_CASE_MESSAGE + targetCaseId);
        }

        NodeRef targetCase = getTargetCase(targetCaseId);

        NodeRef documentFolderToMove = docToFolderAssoc.getParentRef();

        String documentFolderName = (String) nodeService.getProperty(documentFolderToMove, ContentModel.PROP_NAME);

        NodeRef targetCaseDocumentsFolder = caseService.getDocumentsFolder(targetCase);

        nodeService.moveNode(documentFolderToMove, targetCaseDocumentsFolder, ContentModel.ASSOC_CONTAINS, QName.createQName(OpenESDHModel.DOC_URI, documentFolderName));

        // Refer to CaseServiceImpl.setupAssignCaseIdRule and
        // AssignCaseIdActionExecuter
        // for automatic update of the caseId property rule.

    }

    @Override
	public void copyDocumentToCase(NodeRef documentToCopy, String targetCaseId)
			throws Exception {
		ChildAssociationRef docToFolderAssoc = getDocumentPrimaryParent(documentToCopy);

		if (isCaseContainsDocument(targetCaseId, documentToCopy)) {
			throw new Exception(DOCUMENT_STORED_IN_CASE_MESSAGE + targetCaseId);
		}

		NodeRef targetCase = getTargetCase(targetCaseId);
        NodeRef targetCaseDocumentsFolder = caseService.getDocumentsFolder(targetCase);

		NodeRef documentFolderToCopy = docToFolderAssoc.getParentRef();
		
		String documentFolderName = (String) nodeService.getProperty(documentFolderToCopy, ContentModel.PROP_NAME);

        copyService.copy(documentFolderToCopy, targetCaseDocumentsFolder, ContentModel.ASSOC_CONTAINS,
                QName.createQName(OpenESDHModel.DOC_URI, documentFolderName), true);
		
	}

    private ChildAssociationRef getDocumentPrimaryParent(NodeRef document) throws Exception {
        ChildAssociationRef docToFolderAssoc = nodeService.getPrimaryParent(document);
        if (docToFolderAssoc == null) {
            throw new Exception("No primary parent was found for node ref: " + document.toString());
        }
        return docToFolderAssoc;
    }

    private NodeRef getTargetCase(String targetCaseId) throws Exception {
        try {
            return caseService.getCaseById(targetCaseId);
        } catch (Exception e) {
            throw new Exception("Error trying to get target case for the case id: " + targetCaseId, e);
        }
    }

    private boolean isCaseContainsDocument(String caseId, NodeRef document) {
        String documentCurrentCaseId = (String) nodeService.getProperty(document, OpenESDHModel.PROP_OE_CASE_ID);
        return StringUtils.equals(documentCurrentCaseId, caseId);
    }
}
