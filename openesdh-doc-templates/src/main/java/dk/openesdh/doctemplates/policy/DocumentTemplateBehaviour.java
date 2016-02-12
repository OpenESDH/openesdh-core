package dk.openesdh.doctemplates.policy;

import java.io.Serializable;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.rendition.RenderCallback;
import org.alfresco.service.cmr.rendition.RenditionDefinition;
import org.alfresco.service.cmr.rendition.RenditionService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import dk.openesdh.doctemplates.model.OpenESDHDocTemplateModel;

/**
 * @author Lanre.
 */
@Service("DocumentTemplateBehaviour")
public class DocumentTemplateBehaviour implements NodeServicePolicies.OnAddAspectPolicy {

    private final Logger logger = LoggerFactory.getLogger(DocumentTemplateBehaviour.class);

    @Autowired
    @Qualifier("NodeService")
    private NodeService nodeService;
    @Autowired
    @Qualifier("RenditionService")
    private RenditionService renditionService;
    @Autowired
    @Qualifier("policyComponent")
    private PolicyComponent policyComponent;

    @PostConstruct
    public void init() {
        PropertyCheck.mandatory(this, "policyComponent", policyComponent);
        PropertyCheck.mandatory(this, "nodeService", nodeService);
        // Bind behaviours to node policies
        this.policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnAddAspectPolicy.QNAME,
                OpenESDHDocTemplateModel.ASPECT_DOC_TEMPLATE,
                new JavaBehaviour(this, "onAddAspect", Behaviour.NotificationFrequency.TRANSACTION_COMMIT)
        );
    }

    @Override
    public void onAddAspect(NodeRef nodeRef, QName aspectTypeQName) {
        Map<QName, Serializable> docProps = nodeService.getProperties(nodeRef);
        try {
            //Adding the thumbnail and adding the property should be done seperately hence both operations will not be
            //in the same try/catch logic
            ContentData contentData = (ContentData) docProps.get(ContentModel.PROP_CONTENT);
            String MimeType = contentData.getMimetype();
            nodeService.setProperty(nodeRef, OpenESDHDocTemplateModel.PROP_TEMPLATE_TYPE, MimeType);
        } catch (Exception ge) {
            logger.error("Unable to add mimetype to object. Reason: \n" + ge.getMessage());
        }

        QName thumbnailRenditionName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "cardViewThumbnail");

        // Trigger the renditionService to create Thumbnail image.
        RenditionDefinition docTemplateThumbnailRendition = renditionService.loadRenditionDefinition(thumbnailRenditionName);
        renditionService.render(nodeRef, docTemplateThumbnailRendition, new RenderCallback() {
            public void handleFailedRendition(Throwable t) {
                // In the event of a failed (re-)rendition, delete the rendition node
//                if (logger.isDebugEnabled()) {
                logger.warn("Unable to render thumbnail (cardViewThumbnail) for template.", t);
//                }
            }

            public void handleSuccessfulRendition(ChildAssociationRef primaryParentOfNewRendition) {
                nodeService.getProperty(nodeRef, ContentModel.PROP_TITLE).toString();
                logger.debug("==> Successfully rendered cardViewThumbnail for: ");
            }
        });

    }
}
