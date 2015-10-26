package dk.openesdh.repo.audit;

import java.io.Serializable;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dk.openesdh.repo.model.OpenESDHModel;

@Service("audit.dk.openesdh.ContactNameExtractor")
public final class ContactNameExtractor extends AbstractAnnotatedDataExtractor {

    @Autowired
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

}
