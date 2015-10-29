package dk.openesdh.repo.audit;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("audit.dk.openesdh.TitleExtractor")
public final class TitleExtractor extends AbstractNodeRefPropertyDataExtractor {

    @Autowired
    private NodeService nodeService;

    @Override
    protected String extract(NodeRef nodeRef) {
        return (String) nodeService.getProperty(nodeRef, ContentModel.PROP_TITLE);
    }

}
