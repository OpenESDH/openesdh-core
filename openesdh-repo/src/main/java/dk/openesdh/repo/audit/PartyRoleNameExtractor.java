package dk.openesdh.repo.audit;

import java.io.Serializable;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import dk.openesdh.repo.model.OpenESDHModel;

@Service("audit.dk.openesdh.PartyRoleNameExtractor")
public class PartyRoleNameExtractor extends AbstractAnnotatedDataExtractor {

    @Autowired
    @Qualifier("NodeService")
    private NodeService nodeService;

    @Override
    public Serializable extractData(Serializable value) throws Throwable {
        NodeRef roleRef = (NodeRef) value;
        return nodeService.getProperty(roleRef, OpenESDHModel.PROP_CLASSIF_DISPLAY_NAME);
    }

}
