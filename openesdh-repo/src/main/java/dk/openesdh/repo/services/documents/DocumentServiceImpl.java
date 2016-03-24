package dk.openesdh.repo.services.documents;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.domain.node.ContentDataWithId;
import org.alfresco.repo.lock.mem.LockState;
import org.alfresco.repo.rendition.executer.ReformatRenderingEngine;
import org.alfresco.repo.search.impl.lucene.LuceneQueryParserException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.rendition.RenditionDefinition;
import org.alfresco.service.cmr.rendition.RenditionService;
import org.alfresco.service.cmr.rendition.RenditionServiceException;
import org.alfresco.service.cmr.repository.AspectMissingException;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.security.PersonService.PersonInfo;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.SearchLanguageConversion;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import dk.openesdh.repo.model.CaseDocument;
import dk.openesdh.repo.model.CaseDocumentAttachment;
import dk.openesdh.repo.model.DocumentCategory;
import dk.openesdh.repo.model.DocumentStatus;
import dk.openesdh.repo.model.DocumentType;
import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.model.ResultSet;
import dk.openesdh.repo.services.BehaviourFilterService;
import dk.openesdh.repo.services.TransactionRunner;
import dk.openesdh.repo.services.cases.CaseService;
import dk.openesdh.repo.services.lock.OELockService;
import dk.openesdh.repo.services.system.OpenESDHFoldersService;
import dk.openesdh.repo.webscripts.documents.Documents;

/**
 * Created by torben on 11/09/14.
 */
@Service(DocumentService.BEAN_ID)
public class DocumentServiceImpl implements DocumentService {

    private static final Log logger = LogFactory.getLog(DocumentServiceImpl.class);

    private static final QName FINAL_PDF_RENDITION_DEFINITION_NAME = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "finalPdfRenditionDefinition");

    @Autowired
    @Qualifier("NodeService")
    private NodeService nodeService;
    @Autowired
    @Qualifier("DictionaryService")
    private DictionaryService dictionaryService;
    @Autowired
    @Qualifier("PersonService")
    private PersonService personService;
    @Autowired
    @Qualifier("SearchService")
    private SearchService searchService;
    @Autowired
    @Qualifier(CaseService.BEAN_ID)
    private CaseService caseService;
    @Autowired
    @Qualifier("namespaceService")
    private NamespaceService namespaceService;
    @Autowired
    @Qualifier("OELockService")
    private OELockService oeLockService;
    @Autowired
    @Qualifier("VersionService")
    private VersionService versionService;
    @Autowired
    @Qualifier("ContentService")
    private ContentService contentService;
    @Autowired
    @Qualifier("mimetypeService")
    private MimetypeService mimetypeService;
    @Autowired
    @Qualifier("RenditionService")
    private RenditionService renditionService;
    @Autowired
    private TransactionRunner transactionRunner;
    @Autowired
    @Qualifier("DocumentTypeService")
    private DocumentTypeService documentTypeService;
    @Autowired
    @Qualifier("DocumentCategoryService")
    private DocumentCategoryService documentCategoryService;
    @Autowired
    private BehaviourFilterService behaviourFilterService;
    @Autowired
    @Qualifier("PermissionService")
    private PermissionService permissionService;
    @Autowired
    @Qualifier("LockService")
    private LockService lockService;
    @Autowired
    @Qualifier("AuthorityService")
    private AuthorityService authorityService;
    @Value("${openesdh.document.finalizedFileFormat}")
    private String finalizedFileFormat;
    @Value("${openesdh.document.acceptableFinalizedFileFormats}")
    private String acceptableFinalizedFileFormats;

    private final Set<String> acceptableFinalizedFileMimeTypes = new HashSet<>(20);
    private RenditionDefinition pdfRenditionDefinition;
    private final Set<String> otherPropNamespaceUris = new HashSet<>();

    /**
     * Returns true if the file name has an extension
     *
     * @param filename the string representation of the filename in question
     * @return {boolean}
     */
    public static boolean hasFileExtentsion(String filename) {
        String fileNameExt = FilenameUtils.getExtension(filename);
        return StringUtils.isNotEmpty(fileNameExt);
    }

    @PostConstruct
    public void init() {
        for (String extension : acceptableFinalizedFileFormats.split(",")) {
            String mimetype = mimetypeService.getMimetype(extension);
            if (!mimetype.equals(MimetypeMap.MIMETYPE_BINARY)) {
                acceptableFinalizedFileMimeTypes.add(mimetype);
            }
        }
    }

    public void addOtherPropNamespaceUris(String... nsUri) {
        Collections.addAll(otherPropNamespaceUris, nsUri);
    }

    private boolean canLeaveStatus(DocumentStatus status, String user, NodeRef nodeRef) {
        return !DocumentStatus.FINAL.equals(status) //not locked
                || authorityService.isAdminAuthority(user); //or admin
    }

    private boolean canEnterStatus(DocumentStatus status, String user, NodeRef nodeRef) {
        // For now anyone can enter any document status
        return true;
    }

    public void checkCanChangeStatus(NodeRef nodeRef, DocumentStatus fromStatus, DocumentStatus toStatus) {
        String user = AuthenticationUtil.getRunAsUser();
        if (!isDocNode(nodeRef)) {
            throw new AlfrescoRuntimeException("Node is not a document node:"
                    + " " + nodeRef);
        }
        if (!canChangeNodeStatus(fromStatus, toStatus, user, nodeRef)) {
            throw new AccessDeniedException(user + " is not allowed to "
                    + "switch document from status " + fromStatus + " to "
                    + toStatus + " for document " + nodeRef);
        }
    }

    private void changeStatusImpl(NodeRef nodeRef, DocumentStatus newStatus) {
        switch (newStatus) {
            case FINAL:
                AuthenticationUtil.runAsSystem(() -> {
                    nodeService.setProperty(nodeRef, OpenESDHModel.PROP_OE_STATUS, newStatus);
                    // Transform main doc and attachments to finalized formats
                    transformToFinalizedFileFormat(getMainDocument(nodeRef));
                    getAttachments(nodeRef).forEach(this::transformToFinalizedFileFormat);
                    oeLockService.lock(nodeRef, true);
                    return null;
                });
                break;
            case DRAFT:
                oeLockService.unlock(nodeRef, true);
                nodeService.setProperty(nodeRef, OpenESDHModel.PROP_OE_STATUS, newStatus);
                break;
        }
    }

    private void transformToFinalizedFileFormat(NodeRef nodeRef) {
        if (nodeRef == null) {
            return;
        }

        // For now, we only support transform to PDF.
        // Create the final rendition definition if it doesn't already exist.
        // For now, we only support PDF
        if (pdfRenditionDefinition == null) {
            pdfRenditionDefinition = renditionService.loadRenditionDefinition(FINAL_PDF_RENDITION_DEFINITION_NAME);
        }
        if (pdfRenditionDefinition == null) {
            pdfRenditionDefinition = renditionService.createRenditionDefinition(FINAL_PDF_RENDITION_DEFINITION_NAME, ReformatRenderingEngine.NAME);
            pdfRenditionDefinition.setParameterValue(
                    ReformatRenderingEngine.PARAM_MIME_TYPE,
                    MimetypeMap.MIMETYPE_PDF);
            renditionService.saveRenditionDefinition(pdfRenditionDefinition);
        }

        // Get the source and target mimetypes
        String fileName = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
        ContentReader reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
        if (reader == null) {
            // Empty node.
            // TODO: Throw an error?
            return;
        }
        String sourceMimetype = reader.getMimetype();
        if (sourceMimetype.equals(MimetypeMap.MIMETYPE_BINARY)) {
            // Try to guess the mimetype if it is application/octet-stream
            sourceMimetype = mimetypeService.guessMimetype(fileName, reader);
        }
        String targetMimetype = mimetypeService.getMimetype(finalizedFileFormat);

        String sourceFormatDisplay = mimetypeService.getDisplaysByMimetype().getOrDefault(sourceMimetype,
                sourceMimetype);
        String targetFormatDisplay = mimetypeService.getDisplaysByMimetype().getOrDefault(targetMimetype, targetMimetype);

        // Acceptable file formats and image files can be transformed to the
        // finalized file format
        if (!acceptableFinalizedFileMimeTypes.contains(sourceMimetype) && !sourceMimetype.startsWith("image/") && !sourceMimetype.startsWith("text/")) {
            throw new AutomaticFinalizeFailureException("openesdh.document.err.automatic_finalize_not_acceptable_format",
                    new Object[]{sourceFormatDisplay, targetFormatDisplay});
        }

        try {
            renditionService.render(nodeRef, pdfRenditionDefinition);
        } catch (RenditionServiceException | ContentIOException e) {
            throw new AutomaticFinalizeFailureException("openesdh.document.err.exception_during_transformation",
                    new Object[]{sourceFormatDisplay, targetFormatDisplay}, e);
        }
    }

    @Override
    public DocumentStatus getNodeStatus(NodeRef nodeRef) {
        String status = (String) nodeService.getProperty(nodeRef, OpenESDHModel.PROP_OE_STATUS);
        if (StringUtils.isEmpty(status)) {
            return null;
        }
        return DocumentStatus.valueOf(status.toUpperCase());
    }

    @Override
    public List<DocumentStatus> getValidNextStatuses(NodeRef nodeRef) {
        String user = AuthenticationUtil.getFullyAuthenticatedUser();
        DocumentStatus fromStatus = getNodeStatus(nodeRef);
        return Arrays.stream(DocumentStatus.values())
                .filter(s -> canChangeNodeStatus(fromStatus, s, user, nodeRef))
                .collect(Collectors.toList());
    }

    @Override
    public boolean canChangeNodeStatus(DocumentStatus fromStatus, DocumentStatus toStatus, String user, NodeRef nodeRef) {
        NodeRef parentCase = caseService.getParentCase(nodeRef);
        if (parentCase != null) {
            // Don't allow user to change status of documents which are in a
            // locked case
            if (caseService.isLocked(parentCase)) {
                return false;
            }
        }
        return isDocNode(nodeRef)
                && DocumentStatus.isValidTransition(fromStatus, toStatus)
                && canLeaveStatus(fromStatus, user, nodeRef) && canEnterStatus(toStatus, user, nodeRef);
    }

    @Override
    public void changeNodeStatus(NodeRef nodeRef, DocumentStatus newStatus) throws
            Exception {
        DocumentStatus fromStatus = getNodeStatus(nodeRef);
        if (newStatus.equals(fromStatus)) {
            return;
        }
        checkCanChangeStatus(nodeRef, fromStatus, newStatus);
        transactionRunner.runInTransaction(() -> {
            // Disable status behaviour to allow the system to set the
            // status directly.
            behaviourFilterService.executeWithoutBehavior(nodeRef, () -> {
                changeStatusImpl(nodeRef, newStatus);
            });
            return null;
        });
    }

    @Override
    public NodeRef getMainDocument(NodeRef caseDocNodeRef) {
        return nodeService
                .getChildAssocs(caseDocNodeRef, OpenESDHModel.ASSOC_DOC_MAIN, OpenESDHModel.ASSOC_DOC_MAIN)
                .stream()
                .map(assoc -> assoc.getChildRef())
                .findAny()
                .orElse(null);
    }

    @Override
    public PersonService.PersonInfo getDocumentOwner(NodeRef caseDocNodeRef) {
        List<AssociationRef> ownerList = this.nodeService.getTargetAssocs(caseDocNodeRef, OpenESDHModel.ASSOC_DOC_OWNER);
        PersonService.PersonInfo owner = null;

        if (ownerList.size() >= 1) //should always = 1 but just in case
        {
            owner = this.personService.getPerson(ownerList.get(0).getTargetRef()); //return the first one in the list
        }
        return owner;
    }

    @Override
    public List<PersonService.PersonInfo> getDocResponsibles(NodeRef caseDocNodeRef) {
        //TODO could it be the case that in the future there could be more than one person responsible for a document
        List<AssociationRef> responsibleList = this.nodeService.getTargetAssocs(caseDocNodeRef, OpenESDHModel.ASSOC_DOC_RESPONSIBLE_PERSON);
        List<PersonService.PersonInfo> responsibles = new ArrayList<>();

        for (AssociationRef person : responsibleList) {
            responsibles.add(this.personService.getPerson(person.getTargetRef()));
        }

        return responsibles;
    }

    @Override
    public boolean isDocNode(NodeRef nodeRef) {
        return dictionaryService.isSubClass(nodeService.getType(nodeRef), OpenESDHModel.TYPE_DOC_BASE);
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
            result.put("documentsNodeRef", caseService.getDocumentsFolder(caseNodeRef));
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
                        } else if (key.getPrefixString().equals("modifier") || key.getPrefixString().equals("creator")) {
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
        return this.createDocumentFolder(documentsFolder, name, props);
    }

    @Override
    public ChildAssociationRef createDocumentFolder(NodeRef documentsFolder, String name, Map<QName, Serializable> props) {
        props.put(ContentModel.PROP_NAME, name);
        ChildAssociationRef documentAssociationRef = nodeService.createNode(documentsFolder, ContentModel.ASSOC_CONTAINS,
                QName.createQName(OpenESDHModel.DOC_URI, name), OpenESDHModel.TYPE_DOC_SIMPLE, props);
        NodeRef documentNodeRef = documentAssociationRef.getChildRef();

        NodeRef person = personService.getPerson(AuthenticationUtil.getFullyAuthenticatedUser());
        nodeService.createAssociation(documentNodeRef, person, OpenESDHModel.ASSOC_DOC_OWNER);
        nodeService.createAssociation(documentNodeRef, person, OpenESDHModel.ASSOC_DOC_RESPONSIBLE_PERSON);
        return documentAssociationRef;
    }

    public NodeRef createCaseDocument(String caseId, String title, String fileName, NodeRef docType,
            NodeRef docCatagory, Consumer<ContentWriter> contentWriter) {
        return createCaseDocument(caseService.getCaseById(caseId), title, fileName, docType, docCatagory, contentWriter);
    }

    public NodeRef createCaseDocument(NodeRef caseNodeRef, String title, String fileName, NodeRef docType,
            NodeRef docCatagory, Consumer<ContentWriter> contentWriter) {
        NodeRef caseDocumentsFolder = caseService.getDocumentsFolder(caseNodeRef);

        //we need new transaction for DocumentBehavior to kick in
        NodeRef file = transactionRunner.runInNewTransaction(() -> {
            return createDocumentFile(caseDocumentsFolder, title, fileName, docType, docCatagory, contentWriter);
        });

        return nodeService.getPrimaryParent(file).getParentRef();
    }

    public NodeRef createDocumentFile(NodeRef documentFolder, String title, String fileName,
            NodeRef docType, NodeRef docCatagory, Consumer<ContentWriter> contentWriter) {
        title = StringUtils.defaultIfEmpty(title, fileName);
        String name = getUniqueName(documentFolder, sanitizeName(StringUtils.defaultIfEmpty(fileName, title)), true);
        Map<QName, Serializable> props;
        props = new HashMap<>();
        props.put(ContentModel.PROP_NAME, name);
        props.put(ContentModel.PROP_TITLE, title);
        props.put(OpenESDHModel.PROP_DOC_TYPE, docType);
        props.put(OpenESDHModel.PROP_DOC_CATEGORY, docCatagory);
        NodeRef node = nodeService.createNode(
                documentFolder,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name),
                ContentModel.TYPE_CONTENT,
                props).getChildRef();
        ContentWriter writer = contentService.getWriter(node, ContentModel.PROP_CONTENT, true);
        contentWriter.accept(writer);
        return node;
    }

    public static String sanitizeName(String name) {
        return name.replaceAll("[^a-zA-Z0-9_() \\.]", "_");
    }

    /**
     * Gets the nodeRef of a document or folder within a case by recursively trawling up the tree until the caseType is
     * detected
     *
     * @param nodeRef the node whose containing case is to be found.
     * @return the case container noderef
     */
    @Override
    public NodeRef getCaseNodeRef(NodeRef nodeRef) {
        NodeRef caseNodeRef = null;
        QName nodeRefType = this.nodeService.getType(nodeRef);
        if (dictionaryService.isSubClass(nodeRefType, OpenESDHModel.TYPE_CASE_BASE)) {
            caseNodeRef = nodeRef;
        } else {
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
        return getAttachmentsChildAssociations(mainDocNodeRef)
                .stream()
                .map(childRef -> childRef.getChildRef())
                .collect(Collectors.toList());
    }

    @Override
    public ResultSet<CaseDocumentAttachment> getDocumentVersionAttachments(NodeRef mainDocVersionNodeRef,
            int startIndex, int pageSize) {
        List<ChildAssociationRef> attachmentsAssocs = getAttachmentsChildAssociations(mainDocVersionNodeRef);
        int totalItems = attachmentsAssocs.size();
        int resultEnd = startIndex + pageSize;
        if (totalItems < resultEnd) {
            resultEnd = totalItems;
        }
        List<CaseDocumentAttachment> attachments = getAttachmentsWithVersions(attachmentsAssocs.subList(
                startIndex, resultEnd));
        ResultSet<CaseDocumentAttachment> result = new ResultSet<>();
        result.setResultList(attachments);
        result.setTotalItems(attachmentsAssocs.size());
        return result;
    }

    @Override
    public List<CaseDocument> getCaseDocumentsWithAttachments(String caseId) {
        NodeRef caseNodeRef = caseService.getCaseById(caseId);
        return this.getDocumentsForCase(caseNodeRef)
                .stream()
                .map(documentAssoc -> getCaseDocument(documentAssoc.getChildRef()))
                .collect(Collectors.toList());
    }

    @Override
    public void updateCaseDocumentProperties(CaseDocument caseDocument) {
        NodeRef documentNodeRef = new NodeRef(caseDocument.getNodeRef());
        Map<QName, Serializable> properties = nodeService.getProperties(documentNodeRef);
        properties.put(ContentModel.PROP_TITLE, caseDocument.getTitle());
        nodeService.setProperties(documentNodeRef, properties);
        //associations:
        updateDocumentType(documentNodeRef, caseDocument.getType());
        updateDocumentCategory(documentNodeRef, caseDocument.getCategory());
    }

    @Override
    public List<NodeRef> findCaseDocuments(String filter, int size) {
        List<NodeRef> result;

        NodeRef caseRoot = caseService.getCasesRootNodeRef();
        if (caseRoot == null) {
            result = Collections.emptyList();
        } else {
            // get the cases that match the specified names
            StringBuilder query = new StringBuilder(128);
            query.append("PATH:\"").append(OpenESDHFoldersService.CASES_ROOT_PATH).append("/*\" ");
            query.append(" AND +TYPE:\"").append(OpenESDHModel.TYPE_DOC_FILE).append('"');
            query.append(" AND(-TYPE:\"cm:thumbnail\" AND -TYPE:\"cm:failedThumbnail\" AND -TYPE:\"cm:rating\" AND -TYPE:\"fm:post\" AND -ASPECT:\"sys:hidden\" AND -cm:creator:system");

            final boolean filterIsPresent = filter != null && filter.length() > 0;

            if (filterIsPresent) {
                query.append(" AND ");
                String escNameFilter = SearchLanguageConversion.escapeLuceneQuery(StringUtils.replace(filter, "\"", " "));
                String[] tokenizedFilter = SearchLanguageConversion.tokenizeString(escNameFilter);

                //cm:name
                query.append("cm:name:\" ");
                for (int i = 0; i < tokenizedFilter.length; i++) {
                    if (i != 0) //Not first element
                    {
                        query.append("?");
                    }
                    query.append(tokenizedFilter[i].toLowerCase());
                }
                query.append("*\"");

                //cm:title
                query.append(" OR ")
                        .append(" cm:title: (");
                for (int i = 0; i < tokenizedFilter.length; i++) {
                    if (i != 0) //Not first element
                    {
                        query.append(" AND ");
                    }
                    query.append("\"" + tokenizedFilter[i] + "*\" ");
                }
                query.append(")");

                query.append(" OR cm:description:\"" + escNameFilter + "*\"");
                query.append(" OR TEXT:\"" + escNameFilter + "*\"");
                query.append(")");
            }

            SearchParameters sp = new SearchParameters();
            sp.addQueryTemplate("_CASE_DOCUMENTS", "|%name OR |%title OR |%description TEXT TAG");
            sp.setDefaultFieldName("_CASE_DOCUMENTS");
            sp.addStore(caseRoot.getStoreRef());
            sp.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
            sp.setQuery(query.toString());
            if (size > 0) {
                sp.setLimit(size);
                sp.setLimitBy(LimitBy.FINAL_SIZE);
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Search parameters are: " + sp);
            }

            org.alfresco.service.cmr.search.ResultSet results = null;
            try {
                results = this.searchService.query(sp);
                result = new ArrayList<>(results.length());
                for (NodeRef site : results.getNodeRefs()) {
                    result.add(site);
                }
            } catch (LuceneQueryParserException lqpe) {
                //Log the error but suppress is from the user
                logger.error("LuceneQueryParserException finding case documents", lqpe);
                result = Collections.emptyList();
            } finally {
                if (results != null) {
                    results.close();
                }
            }
        }

        return result;
    }

    @Override
    public CaseDocument getCaseDocument(NodeRef docRecordNodeRef) {
        NodeRef mainDocNodeRef = getMainDocument(docRecordNodeRef);
        CaseDocument caseDocument = new CaseDocument();
        caseDocument.setNodeRef(docRecordNodeRef.toString());
        caseDocument.setMainDocNodeRef(mainDocNodeRef.toString());
        Map<QName, Serializable> props = nodeService.getProperties(docRecordNodeRef);
        caseDocument.setTitle(props.get(ContentModel.PROP_TITLE).toString());
        caseDocument.setType(getDocumentType(docRecordNodeRef));
        caseDocument.setStatus(props.get(OpenESDHModel.PROP_OE_STATUS).toString());
        caseDocument.setCategory(getDocumentCategory(docRecordNodeRef));
        caseDocument.setCreated((Date) props.get(ContentModel.PROP_CREATED));
        caseDocument.setModified((Date) props.get(ContentModel.PROP_MODIFIED));
        caseDocument.setOwner(getDocumentOwner(docRecordNodeRef));

        LockState state = AuthenticationUtil.runAsSystem(() -> {
            return lockService.getLockState(mainDocNodeRef);
        });

        caseDocument.setLocked(state.isLockInfo());

        List<ChildAssociationRef> attachmentsAssocs = getAttachmentsChildAssociations(mainDocNodeRef);
        caseDocument.setAttachments(getAttachments(attachmentsAssocs));

        return caseDocument;
    }

    private List<CaseDocumentAttachment> getAttachments(List<ChildAssociationRef> attachmentsAssocs) {
        return attachmentsAssocs
                .stream()
                .map(assoc -> getAttachment(assoc.getChildRef()))
                .collect(Collectors.toList());
    }

    private List<CaseDocumentAttachment> getAttachmentsWithVersions(
            List<ChildAssociationRef> attachmentsAssocs) {
        return attachmentsAssocs
                .stream()
                .map(assoc -> getAttachmentWithVersions(assoc.getChildRef()))
                .collect(Collectors.toList());
    }

    private CaseDocumentAttachment getAttachmentWithVersions(NodeRef nodeRef) {
        CaseDocumentAttachment attachment = getAttachment(nodeRef);

        if (versionService.isVersioned(nodeRef)) {
            setAttachmentVersions(nodeRef, attachment);
        }

        attachment.getOtherProps().putAll(getOtherDocProperties(nodeRef));
        return attachment;
    }

    private void setAttachmentVersions(NodeRef nodeRef, CaseDocumentAttachment attachment) throws AspectMissingException {
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
    }

    private Map<String, Serializable> getOtherDocProperties(NodeRef nodeRef) {
        Map<String, Serializable> props = new HashMap<>();
        nodeService.getProperties(nodeRef).entrySet()
                .stream()
                .filter(e -> otherPropNamespaceUris.contains(e.getKey().getNamespaceURI()))
                .forEach(e -> {
                    namespaceService.getPrefixes(e.getKey().getNamespaceURI()).stream()
                            .findAny()
                            .ifPresent(ns -> {
                                props.put(ns + "_" + e.getKey().getLocalName(), Objects.toString(e.getValue()));
                            });
                });
        return props;
    }

    private CaseDocumentAttachment getAttachment(NodeRef nodeRef) {
        CaseDocumentAttachment attachment = new CaseDocumentAttachment();
        attachment.setNodeRef(nodeRef.toString());

        Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
        attachment.setName(properties.get(ContentModel.PROP_NAME).toString());
        attachment.setVersionLabel((String) properties.get(ContentModel.PROP_VERSION_LABEL));
        attachment.setCreated((Date) properties.get(ContentModel.PROP_CREATED));
        attachment.setModified((Date) properties.get(ContentModel.PROP_MODIFIED));
        attachment.setType(nodeService.getType(nodeRef).toPrefixString(namespaceService));
        String extension = FilenameUtils.getExtension(attachment.getName());
        attachment.setFileType(extension);
        attachment.setMimetype(getMimetype(properties.get(ContentModel.PROP_CONTENT)));

        LockState state = AuthenticationUtil.runAsSystem(() -> {
            return lockService.getLockState(nodeRef);
        });

        if (state.isLockInfo()) {
            attachment.setLocked(true);
            attachment.setLockOwner(state.getOwner());
            attachment.setLockOwnerInfo(getPersonInfo(state.getOwner()));
        }

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

    private static String getMimetype(Serializable content) {
        return content == null ? null : ((ContentDataWithId) content).getMimetype();
    }

    private CaseDocumentAttachment createAttachmentVersion(Version version) {
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

        NodeRef modifierNodeRef = personService.getPersonOrNull((version.getVersionProperty(OpenESDHModel.DOCUMENT_PROP_MODIFIER).toString()));
        if (modifierNodeRef != null) {
            docVers.setModifier(personService.getPerson(modifierNodeRef));
        }

        return docVers;
    }

    private List<ChildAssociationRef> getAttachmentsChildAssociations(NodeRef mainDocNodeRef) {
        return this.nodeService.getChildAssocs(mainDocNodeRef, OpenESDHModel.ASSOC_DOC_ATTACHMENTS,
                RegexQNamePattern.MATCH_ALL);
    }

    @Override
    public DocumentType getDocumentType(NodeRef docNodeRef) {
        Optional<AssociationRef> assocRef = nodeService.getTargetAssocs(docNodeRef, OpenESDHModel.ASSOC_DOC_TYPE).stream().findFirst();
        return assocRef.isPresent() ? documentTypeService.getDocumentType(assocRef.get().getTargetRef()) : null;
    }

    @Override
    public void updateDocumentType(NodeRef docNodeRef, DocumentType type) {
        nodeService.setAssociations(docNodeRef, OpenESDHModel.ASSOC_DOC_TYPE, Arrays.asList(type.getNodeRef()));
    }

    @Override
    public DocumentCategory getDocumentCategory(NodeRef docNodeRef) {
        Optional<AssociationRef> assocRef = nodeService.getTargetAssocs(docNodeRef, OpenESDHModel.ASSOC_DOC_CATEGORY).stream().findFirst();
        return assocRef.isPresent() ? documentCategoryService.getDocumentCategory(assocRef.get().getTargetRef()) : null;
    }

    @Override
    public void updateDocumentCategory(NodeRef docNodeRef, DocumentCategory category) {
        nodeService.setAssociations(docNodeRef, OpenESDHModel.ASSOC_DOC_CATEGORY, Arrays.asList(category.getNodeRef()));
    }

    @Override
    public NodeRef getDocRecordNodeRef(NodeRef docOrAttachmentNodeRef) {
        return nodeService.getParentAssocs(docOrAttachmentNodeRef)
                .stream()
                .findFirst()
                .map(assoc -> assoc.getParentRef())
                .get();
    }

    @Override
    public String getDocumentEditOnlinePath(NodeRef docOrAttachmentNodeRef) {
        return nodeService.getPaths(docOrAttachmentNodeRef, false)
                .stream()
                .map(path -> path.toDisplayPath(nodeService, permissionService))
                .filter(path -> path.startsWith(OpenESDHFoldersService.SITES_PATH_ROOT))
                .map(path -> path.replace(OpenESDHFoldersService.SITES_PATH_ROOT, ""))
                .findAny()
                .orElse("");
    }

    @Override
    public JSONObject getDocumentEditLockState(NodeRef docOrAttachmentNodeRef) throws JSONException {
        JSONObject lockState = new JSONObject();
        LockState state = AuthenticationUtil.runAsSystem(() -> {
            return lockService.getLockState(docOrAttachmentNodeRef);
        });
        lockState.put("isLocked", state.isLockInfo());
        if (!state.isLockInfo()) {
            return lockState;
        }
        if (nodeService.hasAspect(docOrAttachmentNodeRef, OpenESDHModel.ASPECT_OE_LOCKED)) {
            String lockOwner = (String) nodeService.getProperty(docOrAttachmentNodeRef, OpenESDHModel.PROP_OE_LOCKED_BY);
            Date lockDate = (Date) nodeService.getProperty(docOrAttachmentNodeRef, OpenESDHModel.PROP_OE_LOCKED_DATE);
            lockState.put("lockOwner", lockOwner);
            lockState.put("lockDate", lockDate);
            lockState.put("lockOwnerInfo", getPersonInfo(lockOwner));
            return lockState;
        }
        String lockOwner = state.getOwner();
        lockState.put("lockOwner", lockOwner);
        lockState.put("lockOwnerInfo", getPersonInfo(lockOwner));
        return lockState;
    }

    private String getPersonInfo(String userName) {
        if (authorityService.authorityExists(userName)) {
            PersonInfo personInfo = personService.getPerson(personService.getPerson(userName));
            String personInfoStr = personInfo.getFirstName() + " " + personInfo.getLastName();
            return personInfoStr.trim();
        }
        return userName;
    }

    @Override
    public String getUniqueName(NodeRef inFolder, String name, boolean isUniqueWithoutExtension) {
        String baseName = FilenameUtils.removeExtension(name).trim();
        String extension = getExtensionOrEmpty(name);
        int counter = 1;
        NodeRef child;
        String newName;
        do {
            newName = baseName + (counter > 1 ? "(" + counter + ")" : "") + (isUniqueWithoutExtension ? "" : extension);
            child = nodeService.getChildByName(inFolder, ContentModel.ASSOC_CONTAINS, newName);
            counter++;
        } while (child != null);
        return newName + (isUniqueWithoutExtension ? extension : "");
    }

    private String getExtensionOrEmpty(String name) {
        String extension = FilenameUtils.getExtension(name);
        return StringUtils.isEmpty(extension) ? "" : "." + extension;
    }

    @Override
    public List<ChildAssociationRef> getAttachmentsAssoc(NodeRef mainDocNodeRef) {
        return this.nodeService.getChildAssocs(mainDocNodeRef, OpenESDHModel.ASSOC_DOC_ATTACHMENTS,
                RegexQNamePattern.MATCH_ALL);
    }

    @Override
    public boolean isDocBelongsToCase(NodeRef docRef) {
        return Objects.nonNull(getCaseNodeRef(docRef));
    }
}
