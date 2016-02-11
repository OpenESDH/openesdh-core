package dk.openesdh.repo.audit;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dk.openesdh.repo.webscripts.contacts.ContactUtils;

@Service("audit.dk.openesdh.ContactNameExtractor")
public final class ContactNameExtractor extends AbstractNodeRefPropertyDataExtractor {

    @Autowired
    private NodeService nodeService;

    @Override
    protected String extract(NodeRef nodeRef) {
        if (!nodeService.exists(nodeRef)) {
            return null;
        }
        return ContactUtils.getDisplayName(nodeService.getProperties(nodeRef));
    }

}
