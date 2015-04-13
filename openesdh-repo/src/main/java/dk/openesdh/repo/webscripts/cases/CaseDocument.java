package dk.openesdh.repo.webscripts.cases;

import dk.openesdh.repo.services.cases.CaseService;
import dk.openesdh.repo.services.documents.DocumentService;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.*;

import javax.activation.MimeType;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by rasmutor on 2/6/15.
 */
public class CaseDocument extends AbstractWebScript {

    private static final Log LOG = LogFactory.getLog(CaseDocument.class);

    private CaseService caseService;
    private DocumentService documentService;
    private NodeService nodeService;
    private ContentService contentService;

    @Override
    public void execute(WebScriptRequest request, WebScriptResponse response) throws IOException {
        Map<String, String> templateArgs = request.getServiceMatch().getTemplateVars();
        String caseId = templateArgs.get("caseId");
        LOG.info("CaseDocument: caseId = " + caseId);

        String documentName = request.getParameter("name");
        LOG.info("document name: " + documentName);

        String mimeType = request.getParameter("mimeType");
        NodeRef theCase = caseService.getCaseById(caseId);
        if (theCase == null) {
            LOG.error("Case not found: " + caseId);
            throw new WebScriptException(Status.STATUS_NOT_FOUND, "Case not found: " + caseId);
        }
        LOG.debug(theCase.toString());

        NodeRef documentsFolder = caseService.getDocumentsFolder(theCase);
        LOG.debug("documentsFolder: " + documentsFolder.toString());
        NodeRef documentFolder;

        String nameWithoutExtension =  FilenameUtils.removeExtension(documentName);

        LOG.debug("document: " + nameWithoutExtension);
        try {
            documentFolder = documentService.createDocumentFolder(documentsFolder, nameWithoutExtension).getChildRef();
        } catch (RuntimeException e) {
            throw new WebScriptException(Status.STATUS_CONFLICT, e.getMessage());
        }

        InputStream is = request.getContent().getInputStream();

        if (is != null) {
            Map<QName, Serializable> props = new HashMap<>();
            props.put(ContentModel.PROP_NAME, documentName);
            NodeRef node = nodeService.createNode(
                    documentFolder,
                    ContentModel.ASSOC_CONTAINS,
                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, documentName),
                    ContentModel.TYPE_CONTENT,
                    props).getChildRef();
            ContentWriter writer = contentService.getWriter(node, ContentModel.PROP_CONTENT, true);
            writer.setMimetype(mimeType);
            writer.putContent(is);
        }
        try {
            new JSONObject().put("nodeRef", documentFolder.toString()).write(response.getWriter());
        } catch (JSONException e) {
            throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public void setCaseService(CaseService caseService) {
        this.caseService = caseService;
    }

    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }
}
