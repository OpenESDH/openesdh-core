package dk.openesdh.repo.webscripts.documents;

import dk.openesdh.repo.services.cases.CaseService;
import dk.openesdh.repo.services.documents.DocumentService;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PersonService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.*;

import java.io.IOException;

/**
 * Created by rasmutor on 2/9/15.
 */
public class EmailDocument extends AbstractWebScript {

    private static final Log LOG = LogFactory.getLog(EmailDocument.class);
    private CaseService caseService;
    private DocumentService documentService;
    private PersonService personService;

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

        NodeRef nodeRef = caseService.getCaseById(caseId);
        NodeRef documentsFolder = caseService.getDocumentsFolder(nodeRef);
        NodeRef documentFolder = documentService.createDocumentFolder(documentsFolder, name).getChildRef();

        LOG.warn("responsible: " + responsible);
        if (responsible != null) {
            NodeRef personRef = personService.getPerson(responsible, false);
            LOG.warn("personRef: " + personRef);
            if (personRef == null) {
                LOG.warn("Person '" + responsible + "' not found.");
            }
        }
        LOG.warn("documentFolder: " + documentFolder.toString());
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
}
