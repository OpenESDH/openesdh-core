package dk.openesdh.repo.services.files;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.forum.CommentService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.documents.DocumentService;
import dk.openesdh.repo.services.system.OpenESDHFoldersService;

@Service("OeAuthorityFilesService")
public class OeAuthorityFilesServiceImpl implements OeAuthorityFilesService {

    @Autowired
    @Qualifier("OeFilesService")
    private OeFilesService filesService;
    @Autowired
    @Qualifier("AuthorityService")
    private AuthorityService authorityService;
    @Autowired
    @Qualifier("NodeService")
    private NodeService nodeService;
    @Autowired
    @Qualifier(DocumentService.BEAN_ID)
    private DocumentService documentService;
    @Autowired
    @Qualifier("CommentService")
    private CommentService commentService;
    @Autowired
    private OpenESDHFoldersService foldersService;

    @Override
    public List<JSONObject> getFiles(String authorityName) {
        Optional<NodeRef> authorityFolder = getAuthorityFolder(authorityName);
        if (authorityFolder.isPresent()) {
            List<JSONObject> files = filesService.getFiles(authorityFolder.get());
            if (authorityName.startsWith("GROUP_")) {
                files.forEach(json -> addGroup(json, authorityName));
            }
            return files;
        }
        return Collections.emptyList();
    }

    private void addGroup(JSONObject json, String authorityName) {
        if (authorityName.startsWith("GROUP_")) {
            json.put("group", authorityService.getAuthorityDisplayName(authorityName.substring(6)));
        }
    }

    @Override
    public NodeRef addFile(NodeRef owner, String fileName, String mimetype, InputStream fileInputStream, String comment) {
        String authorityName = getAuthorityName(owner);
        NodeRef folder = AuthenticationUtil.runAsSystem(() -> {
            return getOrCreateAuthorityFolder(authorityName);
        });
        return filesService.addFile(folder, fileName, mimetype, fileInputStream, comment);
    }

    @Override
    public void move(NodeRef file, NodeRef newOwner, String comment) {
        String authorityName = getAuthorityName(newOwner);
        //checks permissions and selects association
        ChildAssociationRef oldAssociation = nodeService.getParentAssocs(file).get(0);

        AuthenticationUtil.runAsSystem(() -> {
            String oldOwner = (String) nodeService.getProperty(oldAssociation.getParentRef(), ContentModel.PROP_NAME);
            if (authorityName.equals(oldOwner)) {
                //nothing to do
                return null;
            }
            //move
            NodeRef toFolder = getOrCreateAuthorityFolder(authorityName);
            String title = (String) nodeService.getProperty(oldAssociation.getChildRef(), ContentModel.PROP_TITLE);
            String uniqueName = documentService.getUniqueName(toFolder, title, false);
            nodeService.moveNode(
                    file,
                    toFolder,
                    ContentModel.ASSOC_CONTAINS,
                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, uniqueName));
            if (StringUtils.isNotEmpty(comment)) {
                commentService.createComment(file, title, comment, false);
            }
            return null;
        });
    }

    private String getAuthorityName(NodeRef owner) throws InvalidNodeRefException {
        Map<QName, Serializable> properties = nodeService.getProperties(owner);
        String authorityName = (String) properties.getOrDefault(
                ContentModel.PROP_AUTHORITY_NAME,
                properties.get(ContentModel.PROP_USERNAME));
        return authorityName;
    }

    private NodeRef getOrCreateAuthorityFolder(String authorityName) {
        return getAuthorityFolder(authorityName)
                .orElseGet(() -> createAuthorityFolder(authorityName));
    }

    Optional<NodeRef> getAuthorityFolder(String authorityName) {
        NodeRef documentsRoot = foldersService.getFilesRootNodeRef();
        Optional<NodeRef> folder = Optional.ofNullable(
                nodeService.getChildByName(documentsRoot, ContentModel.ASSOC_CONTAINS, authorityName));
        return folder;
    }

    private NodeRef createAuthorityFolder(String authorityName) {
        return AuthenticationUtil.runAsSystem(() -> {
            Map<QName, Serializable> params = new HashMap<>();
            params.put(ContentModel.PROP_NAME, authorityName);

            NodeRef authorityFolder = nodeService.createNode(
                    foldersService.getFilesRootNodeRef(),
                    ContentModel.ASSOC_CONTAINS,
                    QName.createQName(OpenESDHModel.DOC_URI, authorityName),
                    OpenESDHModel.TYPE_OE_AUTHORITY_FILES_FOLDER,
                    params)
                    .getChildRef();
            return authorityFolder;
        });
    }

}
