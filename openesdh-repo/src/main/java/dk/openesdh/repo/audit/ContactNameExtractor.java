package dk.openesdh.repo.audit;

import dk.openesdh.repo.model.OpenESDHModel;
import java.io.Serializable;
import org.alfresco.repo.audit.extractor.AbstractDataExtractor;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

public final class ContactNameExtractor extends AbstractDataExtractor {

    private NodeService nodeService;

    public Serializable extractData(Serializable value) throws Throwable {
        if (value instanceof NodeRef) {
            NodeRef nodeRef = (NodeRef) value;
            QName type = nodeService.getType(nodeRef);
            if (type.isMatch(OpenESDHModel.TYPE_CONTACT_ORGANIZATION)) {
                return nodeService.getProperty(nodeRef, OpenESDHModel.PROP_CONTACT_ORGANIZATION_NAME);
            } else if (type.isMatch(OpenESDHModel.TYPE_CONTACT_PERSON)) {
                return nodeService.getProperty(nodeRef, OpenESDHModel.PROP_CONTACT_FIRST_NAME)
                        + " " + nodeService.getProperty(nodeRef, OpenESDHModel.PROP_CONTACT_LAST_NAME);
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
