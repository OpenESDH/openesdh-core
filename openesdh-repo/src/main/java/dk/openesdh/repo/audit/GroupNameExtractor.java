package dk.openesdh.repo.audit;

import java.io.Serializable;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.audit.extractor.AbstractDataExtractor;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

public final class GroupNameExtractor extends AbstractDataExtractor {

    private NodeService nodeService;

    public Serializable extractData(Serializable value) throws Throwable {
        if (value instanceof NodeRef) {
            NodeRef nodeRef = (NodeRef) value;
            QName type = nodeService.getType(nodeRef);
            if (type.isMatch(ContentModel.TYPE_AUTHORITY_CONTAINER)) {
                return nodeService.getProperty(nodeRef, ContentModel.PROP_AUTHORITY_DISPLAY_NAME);
            }
        }
        return null;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public boolean isSupported(Serializable data) {
        return true;
    }
}
