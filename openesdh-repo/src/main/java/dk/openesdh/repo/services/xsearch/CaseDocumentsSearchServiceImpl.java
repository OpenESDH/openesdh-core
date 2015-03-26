package dk.openesdh.repo.services.xsearch;

import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.cases.CaseService;
import dk.openesdh.repo.services.documents.DocumentService;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;
import org.alfresco.util.ISO9075;
import org.alfresco.util.Pair;

import java.util.*;

/**
 * Lists all the documents in a given case
 */
public class CaseDocumentsSearchServiceImpl extends AbstractXSearchService implements CaseDocumentsSearchService{

    protected CaseService caseService;

    protected DocumentService documentService;

    protected FileFolderService fileFolderService;

    protected NodeService nodeService;

    protected NamespaceService namespaceService;
    public XResultSet getNodes(Map<String, String> params, int startIndex, int pageSize, String sortField, boolean ascending) {
        NodeRef caseNodeRef = new NodeRef(params.get("nodeRef"));
        NodeRef documentsNodeRef = caseService.getDocumentsFolder(caseNodeRef);
        List<Pair<QName, Boolean>> sortProps = new ArrayList<>(2);

        final String queryExecutionId = "oe-doc-search";
        final int requestTotalCountMax = 1000;

        PagingRequest pageRequest = new PagingRequest(startIndex, pageSize,
                queryExecutionId);
        pageRequest.setRequestTotalCountMax(requestTotalCountMax);

        if (sortField != null && !sortField.isEmpty())
        {
            sortProps.add(new Pair<QName, Boolean>(QName.createQName(sortField, namespaceService), ascending));
        }

        PagingResults<FileInfo> results = fileFolderService.list(documentsNodeRef, false, true, null,
                sortProps, pageRequest);

        ArrayList<NodeRef> resultList = new ArrayList<NodeRef>();
        for (FileInfo fileInfo : results.getPage()) {
            resultList.add(fileInfo.getNodeRef());
        }

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
    public XResultSet getAttachments(Map<String, String> params) {
        NodeRef documentsNodeRef = new NodeRef(params.get("nodeRef"));
        return new XResultSet(documentService.getAttachments(documentsNodeRef));
    }

    public void setCaseService(CaseService caseService) {
        this.caseService = caseService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setNamespaceService(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }

    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }

    public void setFileFolderService(FileFolderService fileFolderService) {
        this.fileFolderService = fileFolderService;
    }
}
