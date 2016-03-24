package dk.openesdh.repo.webscripts.documents;

import static dk.openesdh.repo.model.CaseDocumentJson.EDIT_ONLINE_PATH;
import static dk.openesdh.repo.model.CaseDocumentJson.IS_LOCKED;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionService;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.RequestParam;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import com.github.dynamicextensionsalfresco.webscripts.resolutions.Resolution;

import dk.openesdh.repo.services.documents.DocumentService;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

@Component
@WebScript(description = "Checks if document can be edited with MS Office", families = {"Case Document Tools"})
public class DocumentEditOnlineCheckWebscript {

    private static final String CURRENT_VERSION_DOCUMENT_NAME = "currentVersionDocumentName";

    @Autowired
    @Qualifier(DocumentService.BEAN_ID)
    private DocumentService documentService;
    @Autowired
    @Qualifier("VersionService")
    private VersionService versionService;
    @Autowired
    @Qualifier("LockService")
    private LockService lockService;

    @Uri(value = "/api/openesdh/document/edit/spp?nodeRefId=", method = HttpMethod.GET, defaultFormat = "json")
    public Resolution checkByDocNodeRef(@RequestParam(value = "nodeRefId", required = true) final NodeRef documentNodeRef) throws JSONException {
        NodeRef mainDocNodeRef = documentService.getMainDocument(documentNodeRef);
        Version currentVersion = versionService.getCurrentVersion(mainDocNodeRef);
        JSONObject result = new JSONObject();
        result.put(IS_LOCKED, isLocked(mainDocNodeRef));
        result.put(EDIT_ONLINE_PATH, documentService.getDocumentEditOnlinePath(mainDocNodeRef));
        result.put(CURRENT_VERSION_DOCUMENT_NAME, currentVersion.getVersionProperty("name"));
        return WebScriptUtils.jsonResolution(result);
    }

    private boolean isLocked(NodeRef nodeRef) {
        return AuthenticationUtil.runAsSystem(() -> {
            return lockService.getLockState(nodeRef);
        }).isLockInfo();
    }
}
