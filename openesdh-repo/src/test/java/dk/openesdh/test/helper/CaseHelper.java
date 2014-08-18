package dk.openesdh.test.helper;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Map;

/**
 * Created by ole on 18/08/14.
 */
public class CaseHelper {

    public ChildAssociationRef createCase( NodeService nodeService, String username, NodeRef parent, String cmName, QName caseType, Map properties) {
        AuthenticationUtil.setFullyAuthenticatedUser(username);

        return nodeService.createNode(
            parent,
            ContentModel.ASSOC_CONTAINS,
            QName.createQName(NamespaceService.CONTENT_MODEL_PREFIX, cmName),
            caseType,
            properties
        );


    }


}
