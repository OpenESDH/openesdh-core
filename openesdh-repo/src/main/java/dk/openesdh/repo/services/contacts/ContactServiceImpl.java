package dk.openesdh.repo.services.contacts;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.QueryConsistency;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import dk.openesdh.repo.exceptions.DomainException;
import dk.openesdh.repo.model.ContactInfo;
import dk.openesdh.repo.model.ContactType;
import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.TransactionRunner;
import dk.openesdh.repo.services.xsearch.ContactSearchService;
import dk.openesdh.repo.services.xsearch.XResultSet;

/**
 * @author Lanre Abiwon.
 */
@Service("ContactService")
public class ContactServiceImpl implements ContactService {

    private static final Log logger = LogFactory.getLog(ContactServiceImpl.class);
    static final String ERROR_EMAIL_IS_MANDATORY = "CONTACT.ERRORS.EMAIL_IS_MANDATORY";
    static final String ERROR_INVALID_TYPE = "CONTACT.ERRORS.INVALID_TYPE";
    static final String ERROR_NO_SUCH_CONTACT = "CONTACT.ERRORS.NO_SUCH_CONTACT";
    static final String ERROR_MORE_THEN_ONE_WITH_ID = "CONTACT.ERRORS.MORE_THAN_ONE_CONTACT_EXITS";
    static final String ERRORTEXT_TYPE_MUST_BE_X = "The type of contact must be ";

    @Autowired
    @Qualifier("NodeService")
    private NodeService nodeService;
    @Autowired
    @Qualifier("ContactDAO")
    private ContactDAOImpl contactDAO;
    @Autowired
    @Qualifier("searchService")
    private SearchService searchService;
    @Autowired
    @Qualifier("ContactSearchService")
    private ContactSearchService contactSearchService;
    @Autowired
    private TransactionRunner transactionRunner;
    //for later use
    private static final Set<String> DEFAULT_ZONES = new HashSet<>();

    static {
        DEFAULT_ZONES.add(AuthorityService.ZONE_APP_DEFAULT);
        DEFAULT_ZONES.add(ContactService.ZONE_CONTACT);
    }

    @Override
    public ContactType getContactType(NodeRef contact) {
        return this.nodeService.getType(contact).getLocalName().equalsIgnoreCase("PERSON") ? ContactType.PERSON : ContactType.ORGANIZATION;
    }

    @Override
    public NodeRef getContactsStorageRoot() {
        return contactDAO.getAuthorityContainerRef();
    }

    @Override
    public NodeRef createContact(String email, String type, Map<QName, Serializable> properties) {
        return createContact(email, type, properties, DEFAULT_ZONES);
    }

    @Override
    public NodeRef createContact(String email, String type, Map<QName, Serializable> properties, Set<String> authorityZones) {
        if (!type.equalsIgnoreCase(ContactType.valueOf(StringUtils.capitalize(type)).toString())) {
            throw new DomainException(ERROR_INVALID_TYPE);
        }

        if (StringUtils.isEmpty(email)) {
            throw new DomainException(ERROR_EMAIL_IS_MANDATORY);
        }
        return transactionRunner.runAsAdmin(() -> {
            return this.contactDAO.createContact(email, StringUtils.capitalize(type), properties, DEFAULT_ZONES);
        });
    }

    @Override
    public NodeRef getContactById(String id) {
        return transactionRunner.runInTransaction(() -> {
            SearchParameters searchParams = new SearchParameters();
            searchParams.setLanguage(SearchService.LANGUAGE_CMIS_ALFRESCO);
            searchParams.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
            searchParams.setQueryConsistency(QueryConsistency.TRANSACTIONAL);
            NodeRef contact = null;

            StringBuilder query = new StringBuilder(256);
            query.append("SELECT * FROM contact:base WHERE contact:email='").append(id).append("'");

            searchParams.setQuery(query.toString());
            ResultSet results = null;
            try {
                results = searchService.query(searchParams);
                if (results.getNodeRefs().size() > 1) {
                    throw new DomainException(ERROR_MORE_THEN_ONE_WITH_ID);
                }
                if (results.getNodeRefs().size() < 1) {
                    throw new DomainException(ERROR_NO_SUCH_CONTACT);
                }
                contact = results.getNodeRef(0);

            } finally {
                if (results != null) {
                    results.close();
                }
            }
            return contact;
        });
    }

    @Override
    public List<ContactInfo> getContactByFilter(String id, String type) {
        QName contactType = type.equalsIgnoreCase("PERSON") ? OpenESDHModel.TYPE_CONTACT_PERSON : OpenESDHModel.TYPE_CONTACT_ORGANIZATION;

        Map<String, String> params = new HashMap<>();
        params.put("baseType", contactType.toString());
        params.put("term", id);
        try {
            XResultSet results = contactSearchService.getNodes(params, 0, -1, "cm:name", true);
            return results.getNodeRefs()
                    .stream()
                    .map(this::getContactInfo)
                    .collect(Collectors.toList());
        } catch (Throwable err) {
            if (logger.isDebugEnabled()) {
                logger.debug("\t\t***** Error *****\n There was a problem "
                        + "finding the contact: " + id, err);
            }
            throw err;
        }
    }

    @Override
    public Map<QName, Serializable> getAddress(NodeRef contactRef) {
        Map<QName, Serializable> addressProps = new HashMap<>();
        if (this.nodeService.hasAspect(contactRef, OpenESDHModel.ASPECT_CONTACT_ADDRESS)) {
            Map<QName, Serializable> allProps = this.nodeService.getProperties(contactRef);
            addressProps.put(OpenESDHModel.PROP_CONTACT_HOUSE_NUMBER, allProps.get(OpenESDHModel.PROP_CONTACT_HOUSE_NUMBER));
            addressProps.put(OpenESDHModel.PROP_CONTACT_ADDRESS, allProps.get(OpenESDHModel.PROP_CONTACT_ADDRESS));
            addressProps.put(OpenESDHModel.PROP_CONTACT_ADDRESS_LINE1, allProps.get(OpenESDHModel.PROP_CONTACT_ADDRESS_LINE1));
            addressProps.put(OpenESDHModel.PROP_CONTACT_ADDRESS_LINE2, allProps.get(OpenESDHModel.PROP_CONTACT_ADDRESS_LINE2));
            addressProps.put(OpenESDHModel.PROP_CONTACT_ADDRESS_LINE3, allProps.get(OpenESDHModel.PROP_CONTACT_ADDRESS_LINE3));
            addressProps.put(OpenESDHModel.PROP_CONTACT_ADDRESS_LINE4, allProps.get(OpenESDHModel.PROP_CONTACT_ADDRESS_LINE4));
            addressProps.put(OpenESDHModel.PROP_CONTACT_ADDRESS_LINE5, allProps.get(OpenESDHModel.PROP_CONTACT_ADDRESS_LINE5));
            addressProps.put(OpenESDHModel.PROP_CONTACT_ADDRESS_LINE6, allProps.get(OpenESDHModel.PROP_CONTACT_ADDRESS_LINE6));
            addressProps.put(OpenESDHModel.PROP_CONTACT_STREET_NAME, allProps.get(OpenESDHModel.PROP_CONTACT_STREET_NAME));
            addressProps.put(OpenESDHModel.PROP_CONTACT_STREET_CODE, allProps.get(OpenESDHModel.PROP_CONTACT_STREET_CODE));
            addressProps.put(OpenESDHModel.PROP_CONTACT_FLOOR_IDENTIFIER, allProps.get(OpenESDHModel.PROP_CONTACT_FLOOR_IDENTIFIER));
            addressProps.put(OpenESDHModel.PROP_CONTACT_SUITE_IDENTIFIER, allProps.get(OpenESDHModel.PROP_CONTACT_SUITE_IDENTIFIER));
            addressProps.put(OpenESDHModel.PROP_CONTACT_CITY_NAME, allProps.get(OpenESDHModel.PROP_CONTACT_CITY_NAME));
            addressProps.put(OpenESDHModel.PROP_CONTACT_POST_CODE, allProps.get(OpenESDHModel.PROP_CONTACT_POST_CODE));
            addressProps.put(OpenESDHModel.PROP_CONTACT_POST_BOX, allProps.get(OpenESDHModel.PROP_CONTACT_POST_BOX));
            addressProps.put(OpenESDHModel.PROP_CONTACT_POST_DISTRICT, allProps.get(OpenESDHModel.PROP_CONTACT_POST_DISTRICT));
            addressProps.put(OpenESDHModel.PROP_CONTACT_MUNICIPALITY_CODE, allProps.get(OpenESDHModel.PROP_CONTACT_MUNICIPALITY_CODE));
            addressProps.put(OpenESDHModel.PROP_CONTACT_COUNTRY_CODE, allProps.get(OpenESDHModel.PROP_CONTACT_COUNTRY_CODE));
        }
        return addressProps;
    }

    @Override
    public ContactInfo getContactInfo(NodeRef nodeRef) {
        return new ContactInfo(nodeRef, getContactType(nodeRef), this.nodeService.getProperties(nodeRef));
    }

    @Override
    public NodeRef addPersonToOrganization(NodeRef organizationNodeRef, NodeRef personNodeRef) {
        if (!this.nodeService.getType(organizationNodeRef).equals(OpenESDHModel.TYPE_CONTACT_ORGANIZATION)) {
            throw new RuntimeException(ERRORTEXT_TYPE_MUST_BE_X + OpenESDHModel.TYPE_CONTACT_ORGANIZATION.getLocalName());
        }
        if (!this.nodeService.getType(personNodeRef).equals(OpenESDHModel.TYPE_CONTACT_PERSON)) {
            throw new RuntimeException(ERRORTEXT_TYPE_MUST_BE_X + OpenESDHModel.TYPE_CONTACT_PERSON.getLocalName());
        }
        AssociationRef association = nodeService.createAssociation(organizationNodeRef, personNodeRef, OpenESDHModel.ASSOC_CONTACT_MEMBERS);
        return association.getSourceRef();
    }

    @Override
    public Stream<NodeRef> getOrganizationPersons(NodeRef organizationNodeRef) {
        return this.nodeService.getTargetAssocs(organizationNodeRef, OpenESDHModel.ASSOC_CONTACT_MEMBERS)
                .stream().map(assocRef -> assocRef.getTargetRef());
    }

    @Override
    public void deleteContact(NodeRef nodeRef) {
        if (isLockedInCases(nodeRef)) {
            throw new AlfrescoRuntimeException("CONTACT_LOCKED_IN_CASES");
        }
        nodeService.deleteNode(nodeRef);
    }

    private boolean isLockedInCases(NodeRef nodeRef) {
        List<NodeRef> lockedInCases = (List<NodeRef>) nodeService.getProperty(nodeRef, OpenESDHModel.PROP_CONTACT_LOCKED_IN_CASES);
        return lockedInCases != null && lockedInCases.size() > 0;
    }
}
