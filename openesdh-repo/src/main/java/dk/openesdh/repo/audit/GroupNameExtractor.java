package dk.openesdh.repo.audit;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("audit.dk.openesdh.GroupNameExtractor")
public final class GroupNameExtractor extends AbstractNodeRefPropertyDataExtractor {

    @Autowired
    private NodeService nodeService;

    @Override
    protected String extract(NodeRef nodeRef) {
        QName type = nodeService.getType(nodeRef);
        if (type.isMatch(ContentModel.TYPE_AUTHORITY_CONTAINER)) {
            return (String) nodeService.getProperty(nodeRef, ContentModel.PROP_AUTHORITY_DISPLAY_NAME);
        }
        return null;
    }

}
