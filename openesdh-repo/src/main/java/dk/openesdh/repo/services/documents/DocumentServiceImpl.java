package dk.openesdh.repo.services.documents;

import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.cases.CaseService;
import dk.openesdh.repo.webscripts.documents.Documents;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypes;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.*;

/**
 * Created by torben on 11/09/14.
 */
public class DocumentServiceImpl implements DocumentService {

    private static Log LOGGER = LogFactory.getLog(DocumentServiceImpl.class);

    private NodeService nodeService;
    private DictionaryService dictionaryService;
    private PersonService personService;
    private TransactionService transactionService;
    private CaseService caseService;
    private NamespaceService namespaceService;

    private BehaviourFilter behaviourFilter;

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
    //</editor-fold>

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

//                JSONObject documentJSON = new JSONObject(props);
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
                        //It is common that users create a file without adding an extension to the file name
                        //so originally we decided to add a .txt by default but instead it is better to attempt
                        //Mimetype detection and add the extension
                        if (!DocumentServiceImpl.hasFileExtentsion(fileName)){
                            ContentData fileDataType = (ContentData) nodeService.getProperty(fileNodeRef, ContentModel.PROP_CONTENT);
                            MimeType contentMimeType = allMimeTypes.forName(fileDataType.getMimetype());
                            fileName += contentMimeType.getExtension();
                            nodeService.setProperty(fileNodeRef, ContentModel.PROP_NAME, fileName);
                        }

                        //TODO Check if disabling behaviour is needed
                        behaviourFilter.disableBehaviour();
                        try {
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
                        }finally {
                            behaviourFilter.enableBehaviour();
                        }

                        return null;
                    }
                });

                return null;
            }
        }, "admin");
    }

    @Override
    public NodeRef getMainDocument(NodeRef caseDocNodeRef){
        NodeRef mainDoc = null;
        try {
            List<ChildAssociationRef> children = this.nodeService.getChildAssocs(caseDocNodeRef);
            for(ChildAssociationRef child : children){
                NodeRef doc = child.getChildRef();
                if(this.nodeService.hasAspect(doc, OpenESDHModel.ASPECT_CASE_MAIN_DOC)) {
                    mainDoc = doc;
                    break;
                }
            }
        }
        finally {
            return mainDoc;
        }
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
        //Should
        List <AssociationRef> responsibleList = this.nodeService.getTargetAssocs(caseDocNodeRef, OpenESDHModel.ASSOC_DOC_RESPONSIBLE_PERSON);
        List <PersonService.PersonInfo> responsibles = new ArrayList<PersonService.PersonInfo>();

        for(AssociationRef person : responsibleList)
            responsibles.add(this.personService.getPerson(person.getTargetRef()));

        return responsibles;
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
}
