package dk.openesdh.repo.services.files;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.repo.domain.node.ContentDataWithId;
import org.alfresco.repo.forum.CommentService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.cases.CaseService;
import dk.openesdh.repo.services.documents.DocumentService;
import dk.openesdh.repo.services.system.OpenESDHFoldersService;
import dk.openesdh.repo.utils.JSONArrayCollector;

@Component
public class OeFilesServiceImpl implements OeFilesService {

    @Autowired
    private OpenESDHFoldersService foldersService;
    @Autowired
    @Qualifier("NodeService")
    private NodeService nodeService;
    @Autowired
    @Qualifier("AuthorityService")
    private AuthorityService authorityService;
    @Autowired
    @Qualifier("ContentService")
    private ContentService contentService;
    @Autowired
    @Qualifier("PersonService")
    private PersonService personService;
    @Autowired
    @Qualifier("DocumentService")
    private DocumentService documentService;
    @Autowired
    @Qualifier("CaseService")
    private CaseService caseService;
    @Autowired
    @Qualifier("CommentService")
    private CommentService commentService;

    @Override
    public JSONObject getFile(NodeRef nodeRef) {
        NodeRef folder = nodeService.getPrimaryParent(nodeRef).getParentRef();
        String authorityName = (String) nodeService.getProperty(folder, ContentModel.PROP_NAME);
        return fileNodeToJSONObject(nodeRef, authorityName);
    }

    @Override
    public List<JSONObject> getFiles(String authorityName) {
        Optional<NodeRef> authorityFolder = getAuthorityFolder(authorityName);
        if (authorityFolder.isPresent()) {
            List fileList = nodeService.getChildAssocs(authorityFolder.get())
                    .stream()
                    .map(ChildAssociationRef::getChildRef)
                    .map(fileNode -> fileNodeToJSONObject(fileNode, authorityName))
                    .collect(Collectors.toList());
            return fileList;
        }
        return Collections.EMPTY_LIST;
    }

    private JSONObject fileNodeToJSONObject(NodeRef fileNode, String authorityName) {
        Map<QName, Serializable> props = nodeService.getProperties(fileNode);
        JSONObject json = new JSONObject();
        json.put("nodeRef", fileNode.toString());
        json.put("mimetype", ((ContentDataWithId) props.get(ContentModel.PROP_CONTENT)).getMimetype());
        //there is no need to display filename
        //json.put("name", (String) props.get(ContentModel.PROP_NAME));
        json.put("title", (String) props.get(ContentModel.PROP_TITLE));

        if (authorityName.startsWith("GROUP_")) {
            json.put("group", authorityService.getAuthorityDisplayName(authorityName.substring(6)));
        }

        NodeRef creatorNodeRef = personService.getPersonOrNull((String) props.get(ContentModel.PROP_CREATOR));
        if (creatorNodeRef != null) {
            PersonService.PersonInfo person = personService.getPerson(creatorNodeRef);
            json.put("creator", (person.getFirstName() + " " + person.getLastName().trim()));
        }
        json.put("created", ((Date) props.get(ContentModel.PROP_CREATED)).getTime());
        if (!props.get(ContentModel.PROP_MODIFIED).equals(props.get(ContentModel.PROP_CREATED))) {
            NodeRef modifierNodeRef = personService.getPersonOrNull((String) props.get(ContentModel.PROP_MODIFIER));
            if (modifierNodeRef != null) {
                PersonService.PersonInfo person = personService.getPerson(modifierNodeRef);
                json.put("modifier", (person.getFirstName() + " " + person.getLastName().trim()));
            }
            json.put("modified", ((Date) props.get(ContentModel.PROP_MODIFIED)).getTime());
        }
        json.put("comments", getComments(fileNode));
        return json;
    }

    private JSONArray getComments(NodeRef fileNode) {
        return commentService.listComments(fileNode, new PagingRequest(100)).getPage()
                .stream()
                .map(this::commentNodeToJSONObject)
                .collect(JSONArrayCollector.simple());
    }

    private JSONObject commentNodeToJSONObject(NodeRef commentNode) {
        Map<QName, Serializable> props = nodeService.getProperties(commentNode);
        JSONObject json = new JSONObject();
        json.put("creator", props.get(ContentModel.PROP_CREATOR));
        json.put("created", ((Date) props.get(ContentModel.PROP_CREATED)).getTime());
        ContentReader reader = AuthenticationUtil.runAsSystem(() -> {
            return contentService.getRawReader(((ContentDataWithId) props.get(ContentModel.PROP_CONTENT)).getContentUrl());
        });
        json.put("comment", reader.getContentString());
        return json;
    }

    @Override
    public NodeRef addFile(NodeRef owner, String fileName, String mimetype, InputStream fileInputStream, String comment) {
        String authorityName = getAuthorityName(owner);
        return AuthenticationUtil.runAsSystem(() -> {
            NodeRef folder = getOrCreateAuthorityFolder(authorityName);
            NodeRef file = writeFile(fileName, folder, mimetype, fileInputStream);
            commentService.createComment(file, fileName, comment, false);
            return file;
        });
    }

    private String getAuthorityName(NodeRef owner) throws InvalidNodeRefException {
        Map<QName, Serializable> properties = nodeService.getProperties(owner);
        String authorityName = (String) properties.getOrDefault(
                ContentModel.PROP_AUTHORITY_NAME,
                properties.get(ContentModel.PROP_USERNAME));
        return authorityName;
    }

    private NodeRef writeFile(String fileName, NodeRef folder, String mimetype, InputStream fileInputStream) {
        String title = fileName;
        String uniqueName = documentService.getUniqueName(folder, fileName, false);

        Map<QName, Serializable> props = new HashMap<>();
        props.put(ContentModel.PROP_NAME, uniqueName);
        props.put(ContentModel.PROP_TITLE, title);
        NodeRef fileNode = nodeService.createNode(
                folder,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, uniqueName),
                ContentModel.TYPE_CONTENT,
                props).getChildRef();
        ContentWriter writer = contentService.getWriter(fileNode, ContentModel.PROP_CONTENT, true);
        writer.setMimetype(mimetype);
        writer.putContent(fileInputStream);
        return fileNode;
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

    @Override
    public void delete(NodeRef nodeRef) {
        nodeService.deleteNode(nodeRef);
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
            commentService.createComment(file, title, comment, false);
            return null;
        });
    }

    @Override
    public void addToCase(String caseId, NodeRef file, String title, NodeRef docType, NodeRef docCategory, String description) {
        //checks permissions
        nodeService.getParentAssocs(file).get(0);
        NodeRef caseNodeRef = caseService.getCaseById(caseId);

        AuthenticationUtil.runAsSystem(() -> {
            documentService.moveAsCaseDocument(
                    caseNodeRef,
                    file,
                    title,
                    title,
                    docType,
                    docCategory,
                    description);
            return null;
        });
    }

}
