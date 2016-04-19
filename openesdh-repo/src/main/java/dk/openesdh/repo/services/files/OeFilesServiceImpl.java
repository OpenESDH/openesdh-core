package dk.openesdh.repo.services.files;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.repo.domain.node.ContentDataWithId;
import org.alfresco.repo.forum.CommentService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.NodeInfoService;
import dk.openesdh.repo.services.cases.CaseService;
import dk.openesdh.repo.services.documents.CaseDocumentCopyService;
import dk.openesdh.repo.services.documents.DocumentService;
import dk.openesdh.repo.utils.JSONArrayCollector;

@Service("OeFilesService")
public class OeFilesServiceImpl implements OeFilesService {

    private final Map<QName, QName> ASSOC_BY_PARENT_TYPE = new HashMap<>();

    @Autowired
    @Qualifier("NodeService")
    private NodeService nodeService;
    @Autowired
    @Qualifier("ContentService")
    private ContentService contentService;
    @Autowired
    @Qualifier("PersonService")
    private PersonService personService;
    @Autowired
    @Qualifier(DocumentService.BEAN_ID)
    private DocumentService documentService;
    @Autowired
    @Qualifier(CaseService.BEAN_ID)
    private CaseService caseService;
    @Autowired
    @Qualifier("CommentService")
    private CommentService commentService;
    @Autowired
    @Qualifier("CaseDocumentCopyService")
    private CaseDocumentCopyService caseDocumentCopyService;
    @Autowired
    @Qualifier("NodeInfoService")
    private NodeInfoService nodeInfoService;
    @Autowired
    @Qualifier("DictionaryService")
    private DictionaryService dictionaryService;

    public OeFilesServiceImpl() {
        setAssocionTypes();
    }

    private void setAssocionTypes() {
        ASSOC_BY_PARENT_TYPE.put(ContentModel.TYPE_FOLDER, ContentModel.ASSOC_CONTAINS);
        ASSOC_BY_PARENT_TYPE.put(OpenESDHModel.TYPE_CONTACT_BASE, OpenESDHModel.ASSOC_CONTACT_FILES);
    }

    @Override
    public JSONObject getFile(NodeRef nodeRef) {
        return fileNodeToJSONObject(nodeRef);
    }

    @Override
    public List<JSONObject> getFiles(NodeRef nodeRef) {
        List<JSONObject> fileList = nodeService.getChildAssocs(nodeRef, getAssociationByParent(nodeRef), RegexQNamePattern.MATCH_ALL)
                .stream()
                .map(ChildAssociationRef::getChildRef)
                .map(this::fileNodeToJSONObject)
                .collect(Collectors.toList());
        return fileList;
    }

    @SuppressWarnings("unchecked")
    private JSONObject fileNodeToJSONObject(NodeRef fileNode) {
        JSONObject json = nodeInfoService.getNodeParametersJSON(fileNode);
        json.put("nodeRef", fileNode.toString());
        json.put("comments", getComments(fileNode));
        return json;
    }

    private JSONArray getComments(NodeRef fileNode) {
        return commentService.listComments(fileNode, new PagingRequest(100)).getPage()
                .stream()
                .map(this::commentNodeToJSONObject)
                .collect(JSONArrayCollector.simple());
    }

    @SuppressWarnings("unchecked")
    private JSONObject commentNodeToJSONObject(NodeRef commentNode) {
        Map<QName, Serializable> props = nodeService.getProperties(commentNode);
        JSONObject json = new JSONObject();
        NodeRef creatorNodeRef = personService.getPersonOrNull((String) props.get(ContentModel.PROP_CREATOR));
        if (creatorNodeRef != null) {
            PersonService.PersonInfo person = personService.getPerson(creatorNodeRef);
            json.put("creator", (person.getFirstName() + " " + person.getLastName().trim()));
        }
        json.put("created", ((Date) props.get(ContentModel.PROP_CREATED)).getTime());
        ContentReader reader = AuthenticationUtil.runAsSystem(() -> {
            return contentService.getRawReader(((ContentDataWithId) props.get(ContentModel.PROP_CONTENT)).getContentUrl());
        });
        json.put("comment", reader.getContentString());
        return json;
    }

    @Override
    public NodeRef addFile(NodeRef parent, String fileName, String mimetype, InputStream fileInputStream, String comment) {
        return AuthenticationUtil.runAsSystem(() -> {
            NodeRef file = writeFile(fileName, parent, mimetype, fileInputStream);
            if (StringUtils.isNotEmpty(comment)) {
                commentService.createComment(file, fileName, comment, false);
            }
            return file;
        });
    }

    private NodeRef writeFile(String fileName, NodeRef parent, String mimetype, InputStream fileInputStream) {
        String title = fileName;
        String uniqueName = documentService.getUniqueName(parent, fileName, false);

        Map<QName, Serializable> props = new HashMap<>();
        props.put(ContentModel.PROP_NAME, uniqueName);
        props.put(ContentModel.PROP_TITLE, title);
        NodeRef fileNode = nodeService.createNode(
                parent,
                getAssociationByParent(parent),
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, uniqueName),
                ContentModel.TYPE_CONTENT,
                props).getChildRef();
        ContentWriter writer = contentService.getWriter(fileNode, ContentModel.PROP_CONTENT, true);
        writer.setMimetype(mimetype);
        writer.putContent(fileInputStream);
        return fileNode;
    }

    private QName getAssociationByParent(NodeRef parent) {
        final QName parentType = nodeService.getType(parent);
        if (ASSOC_BY_PARENT_TYPE.containsKey(parentType)) {
            return ASSOC_BY_PARENT_TYPE.get(parentType);
        }
        return ASSOC_BY_PARENT_TYPE.get(ASSOC_BY_PARENT_TYPE.keySet().stream()
                .filter(type -> dictionaryService.isSubClass(parentType, type))
                .findFirst().orElseThrow(() -> new RuntimeException("Invalid parrent type")));
    }

    @Override
    public void delete(NodeRef nodeRef) {
        nodeService.deleteNode(nodeRef);
    }

    @Override
    public void addToCase(String caseId, NodeRef file, String title, NodeRef docType, NodeRef docCategory, String description) {
        //checks permissions
        nodeService.getParentAssocs(file).get(0);
        NodeRef caseNodeRef = caseService.getCaseById(caseId);

        AuthenticationUtil.runAsSystem(() -> {
            caseDocumentCopyService.moveAsCaseDocument(
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
