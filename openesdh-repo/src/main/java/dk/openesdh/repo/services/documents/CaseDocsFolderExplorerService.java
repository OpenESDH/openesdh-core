package dk.openesdh.repo.services.documents;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import dk.openesdh.repo.model.CaseDocsFolder;
import dk.openesdh.repo.model.CaseFolderItem;

public interface CaseDocsFolderExplorerService {

    List<CaseDocsFolder> getCaseDocsFolderPath(NodeRef folderRef);

    List<CaseFolderItem> getCaseDocsFolderContents(NodeRef folderRef);
}
