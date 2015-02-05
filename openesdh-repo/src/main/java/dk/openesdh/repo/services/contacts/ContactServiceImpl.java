package dk.openesdh.repo.services.contacts;

import dk.openesdh.exceptions.contacts.InvalidContactTypeException;
import dk.openesdh.exceptions.contacts.NoSuchContactException;
import dk.openesdh.repo.model.ContactInfo;
import dk.openesdh.repo.model.OpenESDHModel;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.node.SystemNodeUtils;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Lanre Abiwon.
 */
public class ContactServiceImpl implements ContactService {

    private static final Log logger = LogFactory.getLog(ContactServiceImpl.class);
    private NodeService nodeService;
    private SearchService searchService;
    private Repository repositoryHelper;
    private NamespaceService namespaceService;
    private NamespacePrefixResolver namespacePrefixResolver;


    private static final String contactsRoot = "sys:contacts";
    private QName qnameAssocContainers = QName.createQName("sys", "contacts", namespacePrefixResolver);

    public NodeRef getContactsStorageRoot() {
        NodeRef contactsrootNode;
        QName container = QName.createQName(contactsRoot, namespaceService);

        contactsrootNode = SystemNodeUtils.getOrCreateSystemChildContainer(container, nodeService, repositoryHelper).getFirst();

        if (contactsrootNode == null) {
            throw new AlfrescoRuntimeException("Required system container for storing contacts not found: ");
        }

        return contactsrootNode;
    }

    /**
     * It should never be the case that a contact has multiple logins associated.
     * If only because of the cardinality specified in the model.
     * We return the associated person nodeRef so as to only retrieve if only it exists (via the service)
     *
     * @param contactRef
     * @return Pair<Boolean, NodeRef>
     */
    @Override
/*
    public Pair<Boolean, NodeRef> hasAssociatedLogin(NodeRef contactRef) {
        try {
            List<AssociationRef> contactLogin = this.nodeService.getTargetAssocs(contactRef, ContentModel.ASSOC_MEMBER);
            if (!contactLogin.isEmpty())
                return Pair.of(true, contactLogin.get(0).getTargetRef());
        } catch (NullPointerException npe) {
            logger.warn("***** Error *****\n\t\t The contact has no associated login or somehow has too many logins assigned");
        }
        return Pair.of(false, null);
    }
*/

    @Override

    public NodeRef createContact(String email, String type) {
        return createContact(email, type, null, false);
    }

    @Override
    public NodeRef createContact(String email, String type, Map<QName, Serializable> properties) {
        return createContact(email, type, properties, false);
    }

    @Override
    public NodeRef createContact(String email, String type, Map<QName, Serializable> properties, boolean createAssociatedLogin) {
        if (!type.equalsIgnoreCase(ContactInfo.ContactType.PERSON.name()) || !type.equalsIgnoreCase(ContactInfo.ContactType.ORGANIZATION.name()) )
            throw new InvalidContactTypeException("The type of contact is not recognised. Can only create types PERSON/ORGANIZATION");

        if(StringUtils.isEmpty(email))
            throw new NullPointerException("Email is mandatory for contact creation");


        return null;
    }


}