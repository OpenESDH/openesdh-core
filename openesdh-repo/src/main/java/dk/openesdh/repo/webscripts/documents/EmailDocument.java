package dk.openesdh.repo.webscripts.documents;

import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.cases.CaseService;
import dk.openesdh.repo.services.documents.DocumentService;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.*;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by rasmutor on 2/9/15.
 */
public class EmailDocument extends AbstractWebScript {

    private static final Log LOG = LogFactory.getLog(EmailDocument.class);
//    private EmailBean emailBean;
    private CaseService caseService;
    private DocumentService documentService;
    private PersonService personService;
    private NodeService nodeService;
    private ContentService contentService;

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse resp) throws IOException {
        LOG.warn("in EmailDocument");

        // Parse the JSON
        JSONObject json = null;
        String contentType = req.getContentType();
        if (contentType != null && contentType.indexOf(';') != -1) {
            contentType = contentType.substring(0, contentType.indexOf(';'));
        }
        if (MimetypeMap.MIMETYPE_JSON.equals(contentType)) {
            JSONParser parser = new JSONParser();
            try {
                String content = req.getContent().getContent();
                LOG.warn("content: " + content);
                json = (JSONObject) parser.parse(content);
            } catch (IOException | ParseException e) {
                throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Invalid JSON: " + e.getMessage());
            }
        } else {
            throw new WebScriptException(Status.STATUS_UNSUPPORTED_MEDIA_TYPE, "Wrong Content-Type");
        }

        String caseId = (String) json.get("caseId");
        String name = (String) json.get("name");
        String responsible = (String) json.get("responsible");
        JSONObject email = (JSONObject) json.get("email");

        NodeRef nodeRef = caseService.getCaseById(caseId);
        NodeRef documentsFolder = caseService.getDocumentsFolder(nodeRef);
        Map<QName, Serializable> props = new HashMap<>();
        props.put(OpenESDHModel.PROP_DOC_TYPE, "letter");
        props.put(OpenESDHModel.PROP_DOC_CATEGORY, "other");
        props.put(OpenESDHModel.PROP_DOC_STATE, "received");
        NodeRef documentFolder = documentService.createDocumentFolder(documentsFolder, name, props).getChildRef();

        nodeService.setProperties(documentFolder, props);

        LOG.warn("responsible: " + responsible);
        if (responsible != null) {
            NodeRef personRef = personService.getPerson(responsible, false);
            LOG.warn("personRef: " + personRef);
            if (personRef == null) {
                LOG.warn("Person '" + responsible + "' not found.");
            }
        }
        LOG.warn("documentFolder: " + documentFolder.toString());

        String filename = name + ".txt";
        LOG.warn("Creating mail file: " + filename);
        String bodyText = (String) email.get("BodyText");
        props = new HashMap<>();
        props.put(ContentModel.PROP_NAME, filename);
        NodeRef node = nodeService.createNode(
                documentFolder,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, filename),
                ContentModel.TYPE_CONTENT,
                props).getChildRef();
        ContentWriter writer = contentService.getWriter(node, ContentModel.PROP_CONTENT, true);
        writer.setMimetype("text/plain");
        writer.putContent(bodyText);

        JSONObject result = new JSONObject();
        result.put("nodeRef", documentFolder.toString());
        result.writeJSONString(resp.getWriter());
    }

    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }

    public void setCaseService(CaseService caseService) {
        this.caseService = caseService;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }
}
