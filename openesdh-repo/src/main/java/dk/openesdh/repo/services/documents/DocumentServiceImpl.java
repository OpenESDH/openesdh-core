package dk.openesdh.repo.services.documents;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import dk.openesdh.repo.model.*;
import dk.openesdh.repo.services.lock.OELockService;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
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

import dk.openesdh.repo.services.cases.CaseService;
import dk.openesdh.repo.webscripts.documents.Documents;
import org.springframework.security.access.AccessDeniedException;

/**
 * Created by torben on 11/09/14.
 */

public class DocumentServiceImpl implements DocumentService, NodeServicePolicies.OnUpdatePropertiesPolicy {

    private static Log logger = LogFactory.getLog(DocumentServiceImpl.class);

    private NodeService nodeService;
    private DictionaryService dictionaryService;
    private PersonService personService;
    private TransactionService transactionService;
    private CaseService caseService;
    private NamespaceService namespaceService;
    private BehaviourFilter behaviourFilter;
    private CopyService copyService;

    private OELockService oeLockService;

    private VersionService versionService;

    private PolicyComponent policyComponent;

    private MimeTypes allMimeTypes = MimeTypes.getDefaultMimeTypes();

    private MimeTypes types;
    private Behaviour onUpdatePropertiesBehaviour;
    //<editor-fold desc="Setters">
    public void setCaseService(CaseService caseService) {
        this.caseService = caseService;
    }

    public void setOeLockService(OELockService oeLockService) {
        this.oeLockService = oeLockService;
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
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

    public void setVersionService(VersionService versionService) {
        this.versionService = versionService;
    }

    public void init() {
        onUpdatePropertiesBehaviour = new JavaBehaviour(this,
                "onUpdateProperties");
        this.policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME,
                OpenESDHModel.TYPE_CASE_BASE,
                onUpdatePropertiesBehaviour);
    }

    @Override
    public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
        String beforeStatus = (String) before.get(OpenESDHModel.PROP_OE_STATUS);
        if (beforeStatus == null) {
            return;
        }
        String afterStatus = (String) after.get(OpenESDHModel.PROP_OE_STATUS);
        if (beforeStatus.equals(afterStatus)) {
            return;
        }
        throw new AlfrescoRuntimeException("Document status cannot be " +
                "changed directly. Must call the DocumentService" +
                ".changeDocumentStatus method.");
    }

    @Override
    public boolean isDocNode(NodeRef nodeRef) {
        return dictionaryService.isSubClass(nodeService.getType(nodeRef), OpenESDHModel.TYPE_DOC_BASE);
    }

    @Override
    public boolean canChangeNodeStatus(String fromStatus, String toStatus,
                                           String user, NodeRef nodeRef) {
        return isDocNode(nodeRef) &&
                DocumentStatus.isValidTransition(fromStatus, toStatus) &&
                canLeaveStatus(fromStatus, user, nodeRef) && canEnterStatus(toStatus, user, nodeRef);
    }

    protected boolean canLeaveStatus(String status, String user, NodeRef nodeRef) {
        // For now anyone can exit any document status
        return true;
    }

    protected boolean canEnterStatus(String status, String user, NodeRef nodeRef) {
        // For now anyone can enter any document status
        return true;
    }

    @Override
    public void changeNodeStatus(NodeRef nodeRef, String newStatus) throws
            Exception {
        String fromStatus = getNodeStatus(nodeRef);
        if (newStatus.equals(fromStatus)) {
            return;
        }
        checkCanChangeStatus(nodeRef, fromStatus, newStatus);

        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
            @Override
            public Object execute() throws Throwable {
                try {
                    // Disable status behaviour to allow the system to set the
                    // status directly.
                    onUpdatePropertiesBehaviour.disable();
                    changeStatusImpl(nodeRef, fromStatus, newStatus);
                } finally {
                    onUpdatePropertiesBehaviour.enable();
                }
                return null;
            }
        });
    }

    public void checkCanChangeStatus(NodeRef nodeRef, String fromStatus, String toStatus) throws AccessDeniedException {
        String user = AuthenticationUtil.getRunAsUser();
        if (!canChangeNodeStatus(fromStatus, toStatus, user, nodeRef)) {
            throw new AccessDeniedException(user + " is not allowed to " +
                    "switch document from status " + fromStatus + " to " +
                    toStatus + " for document " + nodeRef);
        }
    }

    private void changeStatusImpl(NodeRef nodeRef, String fromStatus, String newStatus) {
        switch (newStatus) {
            case DocumentStatus.FINAL:
                nodeService.setProperty(nodeRef, OpenESDHModel.PROP_OE_STATUS, newStatus);
                // TODO: Convert to PDF
                oeLockService.lock(nodeRef);
                break;
            case DocumentStatus.DRAFT:
                oeLockService.unlock(nodeRef);
                // TODO: Revert to non-PDF original version
                nodeService.setProperty(nodeRef, OpenESDHModel.PROP_OE_STATUS, newStatus);
                break;
        }
    }

    @Override
    public String getNodeStatus(NodeRef nodeRef) {
        return (String) nodeService.getProperty(nodeRef, OpenESDHModel.PROP_OE_STATUS);
    }

    @Override
    public List<String> getValidNextStatuses(NodeRef nodeRef) {
        String user = AuthenticationUtil.getFullyAuthenticatedUser();
        String fromStatus = getNodeStatus(nodeRef);
        List<String> statuses;
        statuses = Arrays.asList(DocumentStatus.getStatuses()).stream().filter(
                s -> canChangeNodeStatus(fromStatus, s, user, nodeRef))
                .collect(Collectors.toList());
        return statuses;
    }

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
        
        return getAttachmentsChildAssociations(docRecordNodeRef)
                .stream()
                .map(childRef -> childRef.getChildRef())
                .collect(Collectors.toList());
    }

    @Override
    public ResultSet<CaseDocumentAttachment> getAttachmentsWithVersions(NodeRef docRecordNodeRef, int startIndex,
            int pageSize) {
        List<ChildAssociationRef> attachmentsAssocs = getAttachmentsChildAssociations(docRecordNodeRef);
        int totalItems = attachmentsAssocs.size();
        int resultEnd = startIndex + pageSize;
        if (totalItems < resultEnd) {
            resultEnd = totalItems;
        }
        List<CaseDocumentAttachment> attachments = getAttachmentsWithVersions(attachmentsAssocs.subList(
                startIndex, resultEnd));
        ResultSet<CaseDocumentAttachment> result = new ResultSet<CaseDocumentAttachment>();
        result.setResultList(attachments);
        result.setTotalItems(attachmentsAssocs.size());
        return result;
    }

    /**
     * Returns true if the file name has an extension
     * 
     * @param filename
     *            the string representation of the filename in question
     * @return {boolean}
     */
    public static boolean hasFileExtentsion(String filename) {
        String fileNameExt = FilenameUtils.getExtension(filename);
        return StringUtils.isNotEmpty(fileNameExt);
    }

    @Override
    public void moveDocumentToCase(NodeRef documentRecFolderToMove, String targetCaseId) throws Exception {

        NodeRef targetCase = getTargetCase(targetCaseId);
        NodeRef targetCaseDocumentsFolder = caseService.getDocumentsFolder(targetCase);

        if (isCaseContainsDocument(targetCaseDocumentsFolder, documentRecFolderToMove)) {
            throw new Exception(DocumentService.DOCUMENT_STORED_IN_CASE_MESSAGE + targetCaseId);
        }

        String documentFolderName = (String) nodeService.getProperty(documentRecFolderToMove,
                ContentModel.PROP_NAME);

        nodeService.moveNode(documentRecFolderToMove, targetCaseDocumentsFolder, ContentModel.ASSOC_CONTAINS,
                QName.createQName(OpenESDHModel.DOC_URI, documentFolderName));

        // Refer to CaseServiceImpl.setupAssignCaseIdRule and
        // AssignCaseIdActionExecuter
        // for automatic update of the caseId property rule.

    }

    @Override
    public void copyDocumentToCase(NodeRef documentRecFolderToCopy, String targetCaseId) throws Exception {

        NodeRef targetCase = getTargetCase(targetCaseId);
        NodeRef targetCaseDocumentsFolder = caseService.getDocumentsFolder(targetCase);

        if (isCaseContainsDocument(targetCaseDocumentsFolder, documentRecFolderToCopy)) {
            throw new Exception(DocumentService.DOCUMENT_STORED_IN_CASE_MESSAGE + targetCaseId);
        }

        copyDocumentToFolder(documentRecFolderToCopy, targetCaseDocumentsFolder);
    }

    @Override
    public void copyDocumentToFolder(NodeRef documentRecFolderToCopy, NodeRef targetFolder) throws Exception {
        String documentFolderName = (String) nodeService.getProperty(documentRecFolderToCopy,
                ContentModel.PROP_NAME);
        copyService.copy(documentRecFolderToCopy, targetFolder, ContentModel.ASSOC_CONTAINS,
                QName.createQName(OpenESDHModel.DOC_URI, documentFolderName), true);
    }

    @Override
    public List<CaseDocument> getCaseDocumentsWithAttachments(String caseId) {
        NodeRef caseNodeRef = caseService.getCaseById(caseId);
        return this.getDocumentsForCase(caseNodeRef)
            .stream()
            .map(documentAssoc -> getCaseDocument(documentAssoc.getChildRef()))
            .collect(Collectors.toList());
    }
    
    protected CaseDocument getCaseDocument(NodeRef docRecordNodeRef) {
        CaseDocument caseDocument = new CaseDocument();
        caseDocument.setNodeRef(docRecordNodeRef.toString());
        caseDocument.setMainDocNodeRef(getMainDocument(docRecordNodeRef).toString());
        Map<QName, Serializable> props = nodeService.getProperties(docRecordNodeRef);
        caseDocument.setTite(props.get(ContentModel.PROP_NAME).toString());
        caseDocument.setType(props.get(OpenESDHModel.PROP_DOC_TYPE).toString());
        caseDocument.setState(props.get(OpenESDHModel.PROP_DOC_STATE).toString());
        caseDocument.setCategory(props.get(OpenESDHModel.PROP_DOC_CATEGORY).toString());
        caseDocument.setCreated((Date) props.get(ContentModel.PROP_CREATED));
        caseDocument.setModified((Date) props.get(ContentModel.PROP_MODIFIED));
        caseDocument.setOwner(getDocumentOwner(docRecordNodeRef));
        String extension = FilenameUtils.getExtension(caseDocument.getTite());
        caseDocument.setFileType(extension);

        List<ChildAssociationRef> attachmentsAssocs = getAttachmentsChildAssociations(docRecordNodeRef);
        caseDocument.setAttachments(getAttachments(attachmentsAssocs));

        return caseDocument;
    }

    protected NodeRef getTargetCase(String targetCaseId) throws Exception {
        try {
            return caseService.getCaseById(targetCaseId);
        } catch (Exception e) {
            throw new Exception("Error trying to get target case for the case id: " + targetCaseId, e);
        }
    }

    protected boolean isCaseContainsDocument(NodeRef targetCaseDocumentsFolder, NodeRef documentRecFolderToCopy) {
        return nodeService.getChildAssocs(targetCaseDocumentsFolder).stream()
                .filter(assoc -> assoc.getChildRef().equals(documentRecFolderToCopy)).findAny().isPresent();
    }
    
    protected List<CaseDocumentAttachment> getAttachments(List<ChildAssociationRef> attachmentsAssocs) {
        return attachmentsAssocs
                .stream()
                .map(assoc -> getAttachment(assoc.getChildRef()))
                .collect(Collectors.toList());
    }

    protected List<CaseDocumentAttachment> getAttachmentsWithVersions(
            List<ChildAssociationRef> attachmentsAssocs) {
        return attachmentsAssocs
                .stream()
                .map(assoc -> getAttachmentWithVersions(assoc.getChildRef()))
                .collect(Collectors.toList());
    }

    protected CaseDocumentAttachment getAttachmentWithVersions(NodeRef nodeRef) {
        CaseDocumentAttachment attachment = getAttachment(nodeRef);

        if (!versionService.isVersioned(nodeRef)) {
            return attachment;
        }

        VersionHistory versionHistory = versionService.getVersionHistory(nodeRef);
        List<CaseDocumentAttachment> versions = versionHistory.getAllVersions()
                .stream()
                .map(version -> createAttachmentVersion(version))
                .collect(Collectors.toList());
        
        CaseDocumentAttachment currentAttachmentVersion = versions
                .stream()
                .filter(version -> version.getVersionLabel().equals(attachment.getVersionLabel()))
                .findFirst()
                .get();
        
        attachment.setCreated(currentAttachmentVersion.getCreated());
        attachment.setCreator(currentAttachmentVersion.getCreator());

        versions.remove(currentAttachmentVersion);
        attachment.getVersions().addAll(versions);
        
        return attachment;
    }

    protected CaseDocumentAttachment getAttachment(NodeRef nodeRef) {
        CaseDocumentAttachment attachment = new CaseDocumentAttachment();
        attachment.setNodeRef(nodeRef.toString());

        Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
        attachment.setName(properties.get(ContentModel.PROP_NAME).toString());
        attachment.setVersionLabel(properties.get(ContentModel.PROP_VERSION_LABEL).toString());
        attachment.setCreated((Date) properties.get(ContentModel.PROP_CREATED));
        attachment.setModified((Date) properties.get(ContentModel.PROP_MODIFIED));
        attachment.setType(nodeService.getType(nodeRef).toPrefixString(namespaceService));
        String extension = FilenameUtils.getExtension(attachment.getName());
        attachment.setFileType(extension);

        NodeRef creatorNodeRef = personService
                .getPersonOrNull(properties.get(ContentModel.PROP_CREATOR).toString());
        if (creatorNodeRef != null) {
            attachment.setCreator(personService.getPerson(creatorNodeRef));
        }

        NodeRef modifierNodeRef = personService.getPersonOrNull(properties.get(ContentModel.PROP_MODIFIER)
                .toString());
        if (modifierNodeRef != null) {
            attachment.setModifier(personService.getPerson(modifierNodeRef));
        }
        return attachment;
    }

    protected CaseDocumentAttachment createAttachmentVersion(Version version) {
        CaseDocumentAttachment docVers = new CaseDocumentAttachment();
        docVers.setName(version.getVersionProperty(OpenESDHModel.DOCUMENT_PROP_NAME).toString());
        docVers.setVersionLabel(version.getVersionLabel());
        docVers.setCreated((Date) version.getVersionProperty(VersionModel.PROP_CREATED_DATE));
        docVers.setModified((Date) version.getVersionProperty(OpenESDHModel.DOCUMENT_PROP_MODIFIED));
        docVers.setDescription(version.getDescription());
        docVers.setNodeRef(version.getFrozenStateNodeRef().toString());

        NodeRef creatorNodeRef = personService.getPersonOrNull(version
                .getVersionProperty(VersionModel.PROP_CREATOR).toString());
        if (creatorNodeRef != null) {
            docVers.setCreator(personService.getPerson(creatorNodeRef));
        }

        NodeRef modifierNodeRef = personService.getPersonOrNull((version
                .getVersionProperty(OpenESDHModel.DOCUMENT_PROP_MODIFIER).toString()).toString());
        if (modifierNodeRef != null) {
            docVers.setModifier(personService.getPerson(modifierNodeRef));
        }

        return docVers;
    }

    protected List<ChildAssociationRef> getAttachmentsChildAssociations(NodeRef docRecordNodeRef) {
        NodeRef mainDocNodeRef = getMainDocument(docRecordNodeRef);
        return this.nodeService.getChildAssocs(docRecordNodeRef, null, null)
                .stream()
                .filter(assoc -> !assoc.getChildRef().equals(mainDocNodeRef))
                .collect(Collectors.toList());
    }
}
