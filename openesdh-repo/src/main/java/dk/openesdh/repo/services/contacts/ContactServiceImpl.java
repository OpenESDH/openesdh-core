package dk.openesdh.repo.services.contacts;

//import dk.openesdh.exceptions.contacts.InvalidContactTypeException;

import dk.openesdh.exceptions.contacts.GenericContactException;
import dk.openesdh.exceptions.contacts.InvalidContactTypeException;
import dk.openesdh.exceptions.contacts.NoSuchContactException;
import dk.openesdh.repo.model.ContactInfo;
import dk.openesdh.repo.model.ContactType;
import dk.openesdh.repo.model.OpenESDHModel;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.*;

/**
 * @author Lanre Abiwon.
 */
public class ContactServiceImpl implements ContactService {

    private static final Log logger = LogFactory.getLog(ContactServiceImpl.class);
    private NodeService nodeService;
    private ContactDAOImpl contactDAO;
    private SearchService searchService;

    //for later use
    private static Set<String> DEFAULT_ZONES = new HashSet<String>();

    static {
        DEFAULT_ZONES.add(AuthorityService.ZONE_APP_DEFAULT);
        DEFAULT_ZONES.add(ContactService.ZONE_CONTACT);
    }

    @Override
    public ContactType getContactType(NodeRef contact) {
        return this.nodeService.getType(contact).getLocalName().equalsIgnoreCase("PERSON") ? ContactType.PERSON: ContactType.ORGANIZATION;
    }

    @Override
    public NodeRef getContactsStorageRoot() {
        return contactDAO.getAuthorityContainerRef();
    }

    @Override
    public NodeRef createContact(String email, String type) {
        return createContact(email, type, null, DEFAULT_ZONES);
    }

    @Override
    public NodeRef createContact(String email, String type, HashMap<QName, Serializable> properties) {
        return createContact(email, type, properties, DEFAULT_ZONES);
    }

    @Override
    public NodeRef createContact(String email, String type, HashMap<QName, Serializable> properties, Set<String> authorityZones) {
        if (!type.equalsIgnoreCase(ContactType.valueOf(StringUtils.capitalize(type)).toString()))
            throw new InvalidContactTypeException("The type of contact is not recognised. Can only create types PERSON/ORGANIZATION");

        if (StringUtils.isEmpty(email))
            throw new NullPointerException("Email is mandatory for contact creation");

        return this.contactDAO.createContact(email, StringUtils.capitalize(type), properties, DEFAULT_ZONES);
    }

    @Override
    public NodeRef getContactById(String id) {

        SearchParameters searchParams = new SearchParameters();
        searchParams.setLanguage(SearchService.LANGUAGE_LUCENE);
        searchParams.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        NodeRef contact = null;

        StringBuilder query = new StringBuilder(256);
        query.append("TYPE:\"").append(OpenESDHModel.TYPE_CONTACT_PERSON).append("\" AND ");
        query.append("@contact\\:email").append(":\"").append(id);
        query.append("\"");

        searchParams.setQuery(query.toString());
        ResultSet results = null;
        try {
            results = this.searchService.query(searchParams);
            if (results.getNodeRefs().size() > 1)
                throw new GenericContactException("There is more than one contact associated with this id (" + id + ").");
            if (results.getNodeRefs().size() < 1)
                throw new NoSuchContactException();
            contact = results.getNodeRef(0);
        } catch (Throwable err) {
            if (logger.isDebugEnabled()) {
                logger.debug("\t\t***** Error *****\n There was a problem finding the contact: " + query.toString(), err);
            }
            throw err;
        } finally {
            if (results != null) {
                results.close();
            }
        }
        return contact;
    }

    @Override
    public List<ContactInfo> getContactByFilter(String id, String type) {

        SearchParameters searchParams = new SearchParameters();
        searchParams.setLanguage(SearchService.LANGUAGE_LUCENE);
        searchParams.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        List<ContactInfo> contacts = null;
        QName contactType = type.equalsIgnoreCase("PERSON") ? OpenESDHModel.TYPE_CONTACT_PERSON : OpenESDHModel.TYPE_CONTACT_ORGANIZATION;

        StringBuilder query = new StringBuilder(256);
        query.append("TYPE:\"").append(contactType).append("\" AND (");
        query.append("@contact\\:email").append(":\"").append(id).append("*\"");
        query.append(" OR @contact\\:firstName").append(":\"").append(id).append("*\"");
        query.append(" OR @contact\\:lastName").append(":\"").append(id).append("*");
        query.append("\")");

        logger.warn("The contact query: "+query.toString());

        searchParams.setQuery(query.toString());
        ResultSet results = null;
        try {
            results = this.searchService.query(searchParams);
            if (results.getNodeRefs().size() < 1)
                throw new NoSuchContactException();
            contacts = new ArrayList<ContactInfo>();
            for(NodeRef contactNode : results.getNodeRefs()){
                contacts.add(new ContactInfo(contactNode, getContactType(contactNode), this.nodeService.getProperties(contactNode)) );
            }
        } catch (Throwable err) {
            if (logger.isDebugEnabled()) {
                logger.debug("\t\t***** Error *****\n There was a problem finding the contact: " + query.toString(), err);
            }
            throw err;
        } finally {
            if (results != null) {
                results.close();
            }
        }
        return contacts;
    }


    public Map<QName,Serializable> getAddress(NodeRef contactRef){
        Map<QName,Serializable> addressProps = new HashMap<>();
        if(this.nodeService.hasAspect(contactRef, OpenESDHModel.ASPECT_CONTACT_ADDRESS)){
            Map<QName,Serializable> allProps = this.nodeService.getProperties(contactRef);
            addressProps.put(OpenESDHModel.PROP_CONTACT_HOUSE_NUMBER,  allProps.get(OpenESDHModel.PROP_CONTACT_HOUSE_NUMBER));
            addressProps.put(OpenESDHModel.PROP_CONTACT_ADDRESS,       allProps.get(OpenESDHModel.PROP_CONTACT_ADDRESS));
            addressProps.put(OpenESDHModel.PROP_CONTACT_ADDRESS_LINE1, allProps.get(OpenESDHModel.PROP_CONTACT_ADDRESS_LINE1));
            addressProps.put(OpenESDHModel.PROP_CONTACT_ADDRESS_LINE2, allProps.get(OpenESDHModel.PROP_CONTACT_ADDRESS_LINE2));
            addressProps.put(OpenESDHModel.PROP_CONTACT_ADDRESS_LINE3, allProps.get(OpenESDHModel.PROP_CONTACT_ADDRESS_LINE3));
            addressProps.put(OpenESDHModel.PROP_CONTACT_ADDRESS_LINE4, allProps.get(OpenESDHModel.PROP_CONTACT_ADDRESS_LINE4));
            addressProps.put(OpenESDHModel.PROP_CONTACT_ADDRESS_LINE5, allProps.get(OpenESDHModel.PROP_CONTACT_ADDRESS_LINE5));
            addressProps.put(OpenESDHModel.PROP_CONTACT_ADDRESS_LINE6, allProps.get(OpenESDHModel.PROP_CONTACT_ADDRESS_LINE6));
            addressProps.put(OpenESDHModel.PROP_CONTACT_STREET_NAME,   allProps.get(OpenESDHModel.PROP_CONTACT_STREET_NAME));
            addressProps.put(OpenESDHModel.PROP_CONTACT_STREET_CODE,   allProps.get(OpenESDHModel.PROP_CONTACT_STREET_CODE));
            addressProps.put(OpenESDHModel.PROP_CONTACT_FLOOR_IDENTIFIER, allProps.get(OpenESDHModel.PROP_CONTACT_FLOOR_IDENTIFIER));
            addressProps.put(OpenESDHModel.PROP_CONTACT_SUITE_IDENTIFIER, allProps.get(OpenESDHModel.PROP_CONTACT_SUITE_IDENTIFIER));
            addressProps.put(OpenESDHModel.PROP_CONTACT_CITY_NAME, allProps.get(OpenESDHModel.PROP_CONTACT_CITY_NAME));
            addressProps.put(OpenESDHModel.PROP_CONTACT_POST_CODE, allProps.get(OpenESDHModel.PROP_CONTACT_POST_CODE));
            addressProps.put(OpenESDHModel.PROP_CONTACT_POST_BOX,  allProps.get(OpenESDHModel.PROP_CONTACT_POST_BOX));
            addressProps.put(OpenESDHModel.PROP_CONTACT_POST_DISTRICT, allProps.get(OpenESDHModel.PROP_CONTACT_POST_DISTRICT));
            addressProps.put(OpenESDHModel.PROP_CONTACT_MUNICIPALITY_CODE, allProps.get(OpenESDHModel.PROP_CONTACT_MUNICIPALITY_CODE));
            addressProps.put(OpenESDHModel.PROP_CONTACT_COUNTRY_CODE, allProps.get(OpenESDHModel.PROP_CONTACT_COUNTRY_CODE));
        }
        return addressProps;
    }

    //<editor-fold desc="Injected service bean setters">
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setContactDAO(ContactDAOImpl contactDAO) {
        this.contactDAO = contactDAO;
    }

    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }
    //</editor-fold>

}

