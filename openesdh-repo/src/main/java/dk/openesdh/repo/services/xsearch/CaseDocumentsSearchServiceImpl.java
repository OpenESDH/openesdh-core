package dk.openesdh.repo.services.xsearch;

import static dk.openesdh.repo.model.CaseDocumentJson.DOC_CATEGORY;
import static dk.openesdh.repo.model.CaseDocumentJson.DOC_TYPE;
import static dk.openesdh.repo.model.CaseDocumentJson.FILE_MIME_TYPE;
import static dk.openesdh.repo.model.CaseDocumentJson.MAIN_DOC_NODE_REF;
import static dk.openesdh.repo.model.CaseDocumentJson.MAIN_DOC_VERSION;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import dk.openesdh.repo.model.DocumentCategory;
import dk.openesdh.repo.model.DocumentType;
import dk.openesdh.repo.services.cases.CaseService;
import dk.openesdh.repo.services.documents.DocumentService;
/**
 * Lists all the documents in a given case
 */
@Service("CaseDocumentsSearchService")
public class CaseDocumentsSearchServiceImpl extends AbstractXSearchService {

    @Autowired
    @Qualifier(CaseService.BEAN_ID)
    protected CaseService caseService;

    @Autowired
    @Qualifier(DocumentService.BEAN_ID)
    protected DocumentService documentService;

    @Autowired
    @Qualifier("FileFolderService")
    protected FileFolderService fileFolderService;

    public XResultSet getNodes(Map<String, String> params, int startIndex, int pageSize, String sortField, boolean ascending) {
        NodeRef caseNodeRef = new NodeRef(params.get(XSearchService.NODE_REF));
        NodeRef documentsNodeRef = caseService.getDocumentsFolder(caseNodeRef);
        List<Pair<QName, Boolean>> sortProps = new ArrayList<>(2);

        final String queryExecutionId = "oe-doc-search";
        final int requestTotalCountMax = 1000;

        PagingRequest pageRequest = new PagingRequest(startIndex, pageSize,
                queryExecutionId);
        pageRequest.setRequestTotalCountMax(requestTotalCountMax);

        if (sortField != null && !sortField.isEmpty()) {
            sortProps.add(new Pair<>(QName.createQName(sortField, namespaceService), ascending));
        }

        PagingResults<FileInfo> results = fileFolderService.list(documentsNodeRef, false, true, null,
                sortProps, pageRequest);

        List<NodeRef> resultList = results.getPage().stream()
                .map(FileInfo::getNodeRef)
                .collect(Collectors.toList());
        Pair<Integer, Integer> totalResultCount = results.getTotalResultCount();
        if (totalResultCount == null) {
            return new XResultSet(resultList);
        } else {
            int lower = 0, upper = 0;
            if (totalResultCount.getFirst() != null) {
                lower = totalResultCount.getFirst();
            }
            if (totalResultCount.getSecond() != null) {
                upper = totalResultCount.getSecond();
            }
            int totalResults = Math.max(lower, upper);
            return new XResultSet(resultList, totalResults);
        }

//        Path path = nodeService.getPath(documentsNodeRef);
//
//        String query = "TYPE:" + quote(OpenESDHModel.TYPE_DOC_BASE.toString());
//        query += " AND PATH:" + quote(path.toPrefixString(namespaceService)
//                + "/*");
//        return executeQuery(query, startIndex, pageSize, sortField, ascending);
    }

    /**
     * Adds the main document nodeRef to the results.
     *
     * @param nodeRef
     * @return
     * @throws JSONException
     */
    @Override
    public JSONObject nodeToJSON(NodeRef nodeRef) throws JSONException {
        JSONObject json = super.nodeToJSON(nodeRef);
        NodeRef mainDocNodeRef = documentService.getMainDocument(nodeRef);

        if (mainDocNodeRef == null) {
            return json;
        }

        json.put(MAIN_DOC_NODE_REF, mainDocNodeRef.toString());
        // get document type
        DocumentType documentType = documentService.getDocumentType(nodeRef);
        if (documentType != null) {
            json.put(DOC_TYPE, documentType.getDisplayName());
        }
        // get document category
        DocumentCategory documentCategory = documentService.getDocumentCategory(nodeRef);
        if (documentCategory != null) {
            json.put(DOC_CATEGORY, documentCategory.getDisplayName());
        }

        // Get the main document version string
        String mainDocVersion = (String) nodeService.getProperty(mainDocNodeRef, ContentModel.PROP_VERSION_LABEL);
        json.put(MAIN_DOC_VERSION, mainDocVersion);

        // also return the filename extension
        String fileName = (String) nodeService.getProperty(mainDocNodeRef, ContentModel.PROP_NAME);
        ContentData docData = (ContentData) nodeService.getProperty(mainDocNodeRef, ContentModel.PROP_CONTENT);
        if (Objects.nonNull(docData)) {
            json.put(FILE_MIME_TYPE, docData.getMimetype());
        }
        return json;
    }
}
