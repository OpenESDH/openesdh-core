package dk.openesdh.repo.webscripts.documents;

import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.UriVariable;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import com.github.dynamicextensionsalfresco.webscripts.resolutions.Resolution;

import dk.openesdh.repo.services.documents.CaseDocsFolderExplorerService;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

@Component
@WebScript(description = "Retrieves case documents and subfolders from provided folder", families = "Case Documents")
public class CaseDocsFolderExplorerWebScript {

    @Autowired
    @Qualifier("CaseDocsFolderExplorerService")
    private CaseDocsFolderExplorerService caseDocsFolderExplorerService;

    @Uri("/api/openesdh/case/docs/folder/{storeType}/{storeId}/{id}/contents")
    public Resolution getDocsFolderContents(
            @UriVariable String storeType, 
            @UriVariable String storeId,
            @UriVariable String id) {
        NodeRef folderRef = new NodeRef(storeType, storeId, id);
        return WebScriptUtils.jsonResolution(caseDocsFolderExplorerService.getCaseDocsFolderContents(folderRef));
    }

    @Uri("/api/openesdh/case/docs/folder/{storeType}/{storeId}/{id}/path")
    public Resolution getDocsFolderPath(@UriVariable String storeType, @UriVariable String storeId,
            @UriVariable String id) {
        NodeRef folderRef = new NodeRef(storeType, storeId, id);
        return WebScriptUtils.jsonResolution(caseDocsFolderExplorerService.getCaseDocsFolderPath(folderRef));
    }
}
