package dk.openesdh.repo.services.documents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.ibm.icu.text.Collator;

import dk.openesdh.repo.model.CaseDocsFolder;
import dk.openesdh.repo.model.CaseFolderItem;
import dk.openesdh.repo.model.OpenESDHModel;

@Service("CaseDocsFolderExplorerService")
public class CaseDocsFolderExplorerServiceImpl implements CaseDocsFolderExplorerService {

    @Autowired
    @Qualifier("NodeService")
    private NodeService nodeService;
    @Autowired
    @Qualifier(DocumentService.BEAN_ID)
    private DocumentService documentService;

    @Override
    public List<CaseDocsFolder> getCaseDocsFolderPath(NodeRef folderRef) {
        List<CaseDocsFolder> pathNodeRefs = getCaseDocsFolderPathRefs(folderRef).stream()
                .map(documentService::getCaseDocsFolder)
                .collect(Collectors.toList());
        Collections.reverse(pathNodeRefs);
        return pathNodeRefs;
    }

    @Override
    public List<CaseFolderItem> getCaseDocsFolderContents(NodeRef folderRef) {
        List<CaseFolderItem> folderContents = nodeService.getChildAssocs(folderRef)
                .stream()
                .map(ChildAssociationRef::getChildRef)
                .map(this::getFolderItem)
                .collect(Collectors.toList());

        folderContents.sort(this::compareFolderItems);
        return folderContents;
    }

    private List<NodeRef> getCaseDocsFolderPathRefs(NodeRef folderRef) {
        NodeRef parentRef = nodeService.getPrimaryParent(folderRef).getParentRef();
        if (!nodeService.hasAspect(parentRef, OpenESDHModel.ASPECT_DOCUMENT_CONTAINER)) {
            return Collections.emptyList();
        }
        List<NodeRef> path = new ArrayList<>();
        path.add(folderRef);
        path.addAll(getCaseDocsFolderPathRefs(parentRef));
        return path;
    }
    
    private CaseFolderItem getFolderItem(NodeRef nodeRef){
        if(ContentModel.TYPE_FOLDER.equals(nodeService.getType(nodeRef))){
            return documentService.getCaseDocsFolder(nodeRef);
        }
        return documentService.getCaseDocument(nodeRef);
    }

    private int compareFolderItems(CaseFolderItem o1, CaseFolderItem o2) {
        if (o1.isFolder() && !o2.isFolder()) {
            return -1;
        }
        if (!o1.isFolder() && o2.isFolder()) {
            return 1;
        }
        return Collator.getInstance().compare(o1.getTitle(), o2.getTitle());
    }

    @Override
    public List<CaseFolderItem> getCaseDocsFoldersHierarchy(NodeRef folderRef) {
        List<CaseFolderItem> rootLevel = getCaseDocsFolderContents(folderRef);
        rootLevel.stream()
            .filter(CaseFolderItem::isFolder)
            .map(item -> (CaseDocsFolder)item)
            .forEach(this::populateChildrenCaseFolderItems);
        return rootLevel;
    }

    private void populateChildrenCaseFolderItems(CaseDocsFolder parent) {
        parent.setChildren(getCaseDocsFoldersHierarchy(parent.getNodeRef()));
    }

}
