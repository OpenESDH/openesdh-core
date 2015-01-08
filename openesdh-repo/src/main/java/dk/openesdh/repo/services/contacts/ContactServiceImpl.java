package dk.openesdh.repo.services.contacts;

import dk.openesdh.exceptions.contacts.NoSuchContactException;
import dk.openesdh.repo.model.ContactInfo;
import dk.openesdh.repo.model.OpenESDHModel;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
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

    public NodeRef getContactsStorageRoot() {
        StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
        ResultSet rs = this.searchService.query(storeRef, SearchService.LANGUAGE_LUCENE, "PATH:\"/app:company_home/app:dictionary/contact:contacts\"");
        NodeRef contactStorageRootNodeRef = null;
        try {
            if (rs.length() == 0) {
                throw new AlfrescoRuntimeException("Unable to find contacts storage root");
            }
            contactStorageRootNodeRef = rs.getNodeRef(0);
        } finally {
            rs.close();
            return contactStorageRootNodeRef;
        }
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
    public Pair<Boolean, NodeRef> hasAssociatedLogin(NodeRef contactRef) {
        try {
            List<AssociationRef> people = this.nodeService.getTargetAssocs(contactRef, OpenESDHModel.ASSOC_CONTACT_LOGIN);
            if (!people.isEmpty())
                return Pair.of(true, people.get(0).getTargetRef());
        } catch (NullPointerException npe) {
            logger.warn("***** Error *****\n\t\t The contact has no associated login or somehow has too many logins assigned");
        }
        return Pair.of(false, null);
    }

    @Override
    public ContactInfo getContact(NodeRef contactRef) throws NoSuchContactException {
        Map<QName, Serializable> props = null;
        try {
            props = this.nodeService.getProperties(contactRef);
        } catch (InvalidNodeRefException inre) {
            throw new NoSuchContactException(contactRef.toString());
        }

        String email = (String) props.get(OpenESDHModel.PROP_CONTACT_EMAIL);
        if (email == null) {
            throw new NoSuchContactException(contactRef.toString());
        }
        String type = (String) props.get(OpenESDHModel.PROP_CONTACT_TYPE);
        if (type == null) {
            throw new NoSuchContactException(contactRef.toString());
        }

        return new ContactInfo(contactRef, email, type);
    }

    //TODO catch exceptions??
    @Override
    public ContactInfo createContact(NodeRef personRef) {
        Map<QName, Serializable> personProps = this.nodeService.getProperties(personRef);
        String firstName = (String) personProps.get(ContentModel.PROP_FIRSTNAME);
        String lastName = (String) personProps.get(ContentModel.PROP_LASTNAME);
        String email = (String) personProps.get(ContentModel.PROP_EMAIL);

        NodeRef newContact = createContact(firstName, lastName, email);
        //Just to be sure
        String newContactMail = (String) this.nodeService.getProperty(newContact, OpenESDHModel.PROP_CONTACT_EMAIL);
        ContactInfo contact = new ContactInfo(newContact, newContactMail, ContactInfo.ContactType.PERSON.name() );
        return contact;
    }

    @Override
    public NodeRef createContact(String firstName, String lastName, String email) {
        NodeRef contactStoreRef = getContactsStorageRoot();
        NodeRef contact = null;
        try{
            Map<QName,Serializable> contactProps = new HashMap<>();
            contactProps.put(OpenESDHModel.PROP_CONTACT_FIRST_NAME, firstName);
            contactProps.put(OpenESDHModel.PROP_CONTACT_LAST_NAME, lastName);
            contactProps.put(OpenESDHModel.PROP_CONTACT_EMAIL, email);
            contactProps.put(OpenESDHModel.PROP_CONTACT_FIRST_NAME, ContactInfo.ContactType.PERSON.name());

            ChildAssociationRef newContact = this.nodeService.createNode(contactStoreRef, ContentModel.ASSOC_CONTAINS, OpenESDHModel.PROP_CONTACT_LOGIN_ASSOC,
                                                                         OpenESDHModel.TYPE_CONTACT_PERSON, contactProps);
            contact = newContact.getChildRef();
        }
        catch (InvalidNodeRefException inre){
            throw new AlfrescoRuntimeException("Unable to create a contact for this reason :" + inre.getMessage());
        }
        return contact;
    }


    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }
}