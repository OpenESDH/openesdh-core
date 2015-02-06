package dk.openesdh.repo.services.contacts;

import dk.openesdh.repo.model.OpenESDHModel;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.node.SystemNodeUtils;
import org.alfresco.repo.security.authority.AuthorityDAOImpl;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Lanre Abiwon
 */
public class ContactDAOImpl {

    private static final Log logger = LogFactory.getLog(ContactDAOImpl.class);
    private NodeService nodeService;
    private Repository repositoryHelper;
    private NamespacePrefixResolver namespacePrefixResolver;


    private static final String contactsRoot = "sys:contacts";

    NodeRef createContact(String email, String contactType, Set<String>  authorityZones,  HashMap<QName, Serializable> typeProps) {

        typeProps.put(ContentModel.PROP_NAME, DigestUtils.md5Hex(email));

        NodeRef childRef;
        NodeRef authorityContainerRef = getAuthorityContainerRef();
        childRef = nodeService.createNode(authorityContainerRef, ContentModel.ASSOC_CHILDREN, QName.createQName("cm", email, namespacePrefixResolver),
                OpenESDHModel.TYPE_CONTACT_PERSON, typeProps).getChildRef();
      /*  if (authorityZones != null)
        {
            Set<NodeRef> zoneRefs = new HashSet<NodeRef>(authorityZones.size() * 2);
            String currentUserDomain = tenantService.getCurrentUserDomain();
            for (String authorityZone : authorityZones)
            {
                zoneRefs.add(getOrCreateZone(authorityZone));
                zoneAuthorityCache.remove(new Pair<String, String>(currentUserDomain, authorityZone));
            }
            zoneAuthorityCache.remove(new Pair<String, String>(currentUserDomain, null));
            nodeService.addChild(zoneRefs, childRef, ContentModel.ASSOC_IN_ZONE, QName.createQName("contact", email, namespacePrefixResolver));
        }*/
        //authorityLookupCache.put(cacheKey(email), childRef);

        return childRef;
    }

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
