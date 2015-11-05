package dk.openesdh.repo.policy;

import dk.openesdh.repo.model.OpenESDHModel;
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
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author Lanre.
 */
public class DocumentTemplateBehaviour implements NodeServicePolicies.OnAddAspectPolicy {
    private static Logger logger = Logger.getLogger(DocumentTemplateBehaviour.class);

    private NodeService nodeService;
    private RenditionService renditionService;
    private PolicyComponent policyComponent;

    public void init() {
        PropertyCheck.mandatory(this, "policyComponent", policyComponent);
        PropertyCheck.mandatory(this, "nodeService", nodeService);
        // Bind behaviours to node policies
        this.policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnAddAspectPolicy.QNAME,
                OpenESDHModel.ASPECT_DOC_TEMPLATE,
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
            nodeService.setProperty(nodeRef, OpenESDHModel.PROP_TEMPLATE_TYPE, MimeType);
        } catch (Exception ge) {
            logger.error("Unable to add mimetype to object. Reason: \n" + ge.getMessage());
        }

        QName thumbnailRenditionName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "cardViewThumbnail");

        // Trigger the renditionService to create Thumbnail image.
        List<RenditionDefinition> definitions = renditionService.loadRenditionDefinitions();
        RenditionDefinition docTemplateThumbnailRendition = renditionService.loadRenditionDefinition(thumbnailRenditionName);
        renditionService.render(nodeRef, docTemplateThumbnailRendition, new RenderCallback() {
            public void handleFailedRendition(Throwable t) {
                // In the event of a failed (re-)rendition, delete the rendition node
//                if (logger.isDebugEnabled()) {
                    logger.warn("Unable to render thumbnail (cardViewThumbnail) for template.", t);
//                }
            }

            public void handleSuccessfulRendition(ChildAssociationRef primaryParentOfNewRendition) {
                String name = nodeService.getProperty(nodeRef, ContentModel.PROP_TITLE).toString();
                logger.info("==> Successfully rendered cardViewThumbnail for: ");
            }
        });

    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setRenditionService(RenditionService renditionService) {
        this.renditionService = renditionService;
    }
}
