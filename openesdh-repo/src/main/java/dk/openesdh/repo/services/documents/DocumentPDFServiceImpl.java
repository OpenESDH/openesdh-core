package dk.openesdh.repo.services.documents;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Optional;
import org.alfresco.model.ContentModel;
import org.alfresco.model.RenditionModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.rendition.executer.ReformatRenderingEngine;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.rendition.RenditionDefinition;
import org.alfresco.service.cmr.rendition.RenditionService;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DocumentPDFServiceImpl implements DocumentPDFService {

    private static final QName FINAL_PDF_RENDITION_DEFINITION_NAME = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "finalPdfRenditionDefinition");
    private RenditionDefinition pdfRenditionDefinition;

    @Autowired
    private NodeService nodeService;
    @Autowired
    private RenditionService renditionService;
    @Autowired
    private ContentService contentService;

    @Override
    public Optional<InputStream> getDocumentPdfThumbnailStream(NodeRef documentNodeRef) {
        initRenditionDefinition();

        if (!nodeService.hasAspect(documentNodeRef, RenditionModel.ASPECT_RENDITIONED)) {
            renditionService.render(documentNodeRef, pdfRenditionDefinition);
        }

        Optional<ContentReader> pdfContentOpt = renditionService.getRenditions(documentNodeRef).stream()
                .map(assoc -> getContentReader(assoc.getChildRef()))
                .filter(contentReader -> contentReader.getMimetype().startsWith(MimetypeMap.MIMETYPE_PDF))
                .findFirst();

        if (!pdfContentOpt.isPresent()) {
            return Optional.empty();
        }
        return Optional.of(pdfContentOpt.get().getContentInputStream());
    }

    private ContentReader getContentReader(NodeRef nodeRef) {
        return contentService.getReader(nodeRef, getContentProperty(nodeRef));
    }

    private QName getContentProperty(NodeRef nodeRef) {
        Serializable contentPropertyName = nodeService
                .getProperty(nodeRef, ContentModel.PROP_CONTENT_PROPERTY_NAME);
        return contentPropertyName != null ? (QName) contentPropertyName : ContentModel.PROP_CONTENT;
    }

    private void initRenditionDefinition() {
        if (pdfRenditionDefinition == null) {
            AuthenticationUtil.runAsSystem(() -> {
                // For now, we only support transform to PDF.
                // Create the final rendition definition if it doesn't already exist.
                // For now, we only support PDF
                pdfRenditionDefinition = renditionService.loadRenditionDefinition(FINAL_PDF_RENDITION_DEFINITION_NAME);
                if (pdfRenditionDefinition == null) {
                    pdfRenditionDefinition = renditionService.createRenditionDefinition(FINAL_PDF_RENDITION_DEFINITION_NAME, ReformatRenderingEngine.NAME);
                    pdfRenditionDefinition.setParameterValue(ReformatRenderingEngine.PARAM_MIME_TYPE, MimetypeMap.MIMETYPE_PDF);
                    renditionService.saveRenditionDefinition(pdfRenditionDefinition);
                }
                return null;
            });
        }
    }
}
