package dk.openesdh.repo.services.contacts;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.node.SystemNodeUtils;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dk.openesdh.repo.model.OpenESDHModel;

/**
 * Some of these methods will come  from the org.alfresco.repo.security.authority.AuthorityDAOImpl class
 * as this is the bean which inspires this one as we attempt to model contacts as an authority.
 *
 * @author Lanre Abiwon
 */
public class ContactDAOImpl {

    private static final Log logger = LogFactory.getLog(ContactDAOImpl.class);
    private NodeService nodeService;
    private Repository repositoryHelper;
    private NamespacePrefixResolver namespacePrefixResolver;


    private static final String contactsRoot = "sys:contacts";

    NodeRef createContact(String email, String contactType, HashMap<QName, Serializable> typeProps,  Set<String>  authorityZones) {

        HashMap<QName, Serializable> props = new HashMap<QName, Serializable>();
        props.put(ContentModel.PROP_NAME, DigestUtils.md5Hex(email));
        if(typeProps !=null){
            props.putAll(typeProps);
        } else {
            props.put(OpenESDHModel.PROP_CONTACT_EMAIL, email);
        }
        
        QName cType = contactType.equalsIgnoreCase("organization")? OpenESDHModel.TYPE_CONTACT_ORGANIZATION : OpenESDHModel.TYPE_CONTACT_PERSON;

        NodeRef childRef;
        NodeRef authorityContainerRef = getAuthorityContainerRef();
        childRef = nodeService.createNode(authorityContainerRef, ContentModel.ASSOC_CHILDREN, QName.createQName("cm", email, namespacePrefixResolver),
                cType, props).getChildRef();
        //TODO NOTE - Look at org.alfresco.repo.security.authority.AuthorityDAOImpl lines 379 - 391 to add the authority to zones. (Currently an issue)

        return childRef;
    }

    /**
     * Get the system container root node where contact are to be stored
     * @return
     */
    public NodeRef getAuthorityContainerRef() {
        NodeRef contactsRootNode;
        QName container = QName.createQName(contactsRoot, namespacePrefixResolver);

        contactsRootNode = SystemNodeUtils.getOrCreateSystemChildContainer(container, nodeService, repositoryHelper).getFirst();

        if (contactsRootNode == null) {
            throw new AlfrescoRuntimeException("Required system container for storing contacts not found: ");
        }

        return contactsRootNode;
    }

    //<editor-fold desc="Injected Bean setters">
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setRepositoryHelper(Repository repositoryHelper) {
        this.repositoryHelper = repositoryHelper;
    }

    public void setNamespacePrefixResolver(NamespacePrefixResolver namespacePrefixResolver){
        this.namespacePrefixResolver = namespacePrefixResolver;
    }

    //</editor-fold>

    public void afterPropertiesSet() throws Exception {
        PropertyCheck.mandatory(this, "namespacePrefixResolver", namespacePrefixResolver);
        PropertyCheck.mandatory(this, "nodeService", nodeService);
        PropertyCheck.mandatory(this, "repositoryHelper", repositoryHelper);
    }
}
