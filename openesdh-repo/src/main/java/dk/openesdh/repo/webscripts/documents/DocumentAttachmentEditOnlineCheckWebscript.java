package dk.openesdh.repo.webscripts.documents;

import static dk.openesdh.repo.model.CaseDocumentJson.EDIT_ONLINE_PATH;
import static dk.openesdh.repo.model.CaseDocumentJson.IS_LOCKED;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.RegexQNamePattern;
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

import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.documents.DocumentService;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

@Component
@WebScript(description = "Checks if document attachment can be edited with MS Office", families = {"Case Document Tools"})
public class DocumentAttachmentEditOnlineCheckWebscript {

    @Autowired
    @Qualifier("DocumentService")
    private DocumentService documentService;
    @Autowired
    @Qualifier("VersionService")
    private VersionService versionService;
    @Autowired
    @Qualifier("LockService")
    private LockService lockService;
    @Autowired
    @Qualifier("NodeService")
    private NodeService nodeService;

    @Uri(value = "/api/openesdh/document/attachment/edit/spp?nodeRefId=", method = HttpMethod.GET, defaultFormat = "json")
    public Resolution checkByDocNodeRef(@RequestParam(value = "nodeRefId", required = true) final NodeRef attachmentVersionNodeRef) throws JSONException {
        Version currentVersion = versionService.getCurrentVersion(attachmentVersionNodeRef);
        NodeRef mainDocNodeRef = getMainDocNodeRef(currentVersion.getVersionedNodeRef());
        JSONObject result = new JSONObject();
        result.put(IS_LOCKED, isLocked(mainDocNodeRef) || isLocked(currentVersion.getVersionedNodeRef()));
        result.put(EDIT_ONLINE_PATH, documentService.getDocumentEditOnlinePath(currentVersion.getVersionedNodeRef()));
        result.put("currentVersionDocumentName", currentVersion.getVersionProperty("name"));
        return WebScriptUtils.jsonResolution(result);
    }

    private NodeRef getMainDocNodeRef(NodeRef attachmentNodeRef) {
        return nodeService.getParentAssocs(
                attachmentNodeRef, OpenESDHModel.ASSOC_DOC_ATTACHMENTS, RegexQNamePattern.MATCH_ALL)
                .stream().findFirst().orElseThrow(RuntimeException::new)
                .getParentRef();
    }

    private boolean isLocked(NodeRef nodeRef) {
        return AuthenticationUtil.runAsSystem(() -> {
            return lockService.getLockState(nodeRef);
        }).isLockInfo();
    }
}
