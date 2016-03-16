package dk.openesdh.repo.services.contacts;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.node.SystemNodeUtils;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import dk.openesdh.repo.model.OpenESDHModel;

/**
 * Some of these methods will come  from the org.alfresco.repo.security.authority.AuthorityDAOImpl class
 * as this is the bean which inspires this one as we attempt to model contacts as an authority.
 *
 * @author Lanre Abiwon
 */
@Service("ContactDAO")
public class ContactDAOImpl {

    private static final Log logger = LogFactory.getLog(ContactDAOImpl.class);
    private static final String CONTACTS_ROOT = "sys:contacts";

    @Autowired
    @Qualifier("NodeService")
    private NodeService nodeService;
    @Autowired
    @Qualifier("repositoryHelper")
    private Repository repositoryHelper;
    @Autowired
    @Qualifier("namespaceService")
    private NamespacePrefixResolver namespacePrefixResolver;

    NodeRef createContact(String email, String contactType, Map<QName, Serializable> typeProps, Set<String> authorityZones) {
        // Prepending time stamp to prevent PROP_NAME duplicates when contact email is changed and the old value is used for a new contact.
        // Duplicate PROP_NAME's cause "Duplicate child name" exceptions when adding contacts to cases, since the PROP_NAME is used to create association name.
        typeProps.put(ContentModel.PROP_NAME, DigestUtils.md5Hex(new Date().getTime() + email));
        QName cType = contactType.equalsIgnoreCase("organization")? OpenESDHModel.TYPE_CONTACT_ORGANIZATION : OpenESDHModel.TYPE_CONTACT_PERSON;

        NodeRef childRef;
        NodeRef authorityContainerRef = getAuthorityContainerRef();
        childRef = nodeService.createNode(authorityContainerRef, ContentModel.ASSOC_CHILDREN, QName.createQName("cm", email, namespacePrefixResolver),
                cType, typeProps).getChildRef();
        //TODO NOTE - Look at org.alfresco.repo.security.authority.AuthorityDAOImpl lines 379 - 391 to add the authority to zones. (Currently an issue)

        return childRef;
    }

    /**
     * Get the system container root node where contact are to be stored
     * @return
     */
    public NodeRef getAuthorityContainerRef() {
        NodeRef contactsRootNode;
        QName container = QName.createQName(CONTACTS_ROOT, namespacePrefixResolver);

        contactsRootNode = SystemNodeUtils.getOrCreateSystemChildContainer(container, nodeService, repositoryHelper).getFirst();

        if (contactsRootNode == null) {
            throw new AlfrescoRuntimeException("Required system container for storing contacts not found: ");
        }

        return contactsRootNode;
    }
}
