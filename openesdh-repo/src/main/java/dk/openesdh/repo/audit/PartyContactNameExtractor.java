package dk.openesdh.repo.audit;

import java.io.Serializable;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.contacts.ContactService;

@Service("audit.dk.openesdh.PartyContactNameExtractor")
public class PartyContactNameExtractor extends AbstractAnnotatedDataExtractor {

    @Autowired
    @Qualifier("ContactService")
    private ContactService contactService;
    @Autowired
    @Qualifier("NodeService")
    private NodeService nodeService;

    @Override
    public Serializable extractData(Serializable value) throws Throwable {
        NodeRef nodeRef = (NodeRef) value;
        QName nodeType = nodeService.getType(nodeRef);
        if (nodeType.equals(OpenESDHModel.TYPE_CONTACT_PARTY)) {
            NodeRef contactRef = (NodeRef) nodeService.getProperty(nodeRef, OpenESDHModel.PROP_CONTACT_CONTACT);
            return getContactName(contactRef);
        }
        return getContactName(nodeRef);
    }

    private String getContactName(NodeRef contactRef) {
        return contactService.getContactInfo(contactRef).getName();
    }
}
