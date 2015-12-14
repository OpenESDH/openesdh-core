package dk.openesdh.repo.services.cases;

import dk.openesdh.exceptions.contacts.InvalidContactTypeException;
import dk.openesdh.repo.model.ContactInfo;
import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.contacts.ContactService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Lanre Abiwon.
 */
public class PartyServiceImpl implements PartyService {

    private static final Log logger = LogFactory.getLog(PartyServiceImpl.class);
    private NodeService nodeService;
    private CaseService caseService;
    private ContactService contactService;
    private AuthorityService authorityService;
    private DictionaryService dictionaryService;
    private NamespacePrefixResolver namespacePrefixResolver;

    public static final Set<String> PARTY_ZONES = new HashSet<>();

    static {
        PARTY_ZONES.add(AuthorityService.ZONE_AUTH_ALFRESCO);
        PARTY_ZONES.add(AuthorityService.ZONE_APP_SHARE); //Adding to this zone prevents searching within authority Finder
    }

    @Override
    public NodeRef createParty(String caseId, String role) {
        return createParty(caseId, role, null);
    }

    @Override
    public NodeRef createParty(String caseId, String role, List<String> contacts) {
        if (StringUtils.isAnyEmpty(caseId, role)) {
            throw new InvalidContactTypeException("The caseId and/or the role is missing");
        }

        try {
            NodeRef caseNodeRef = caseService.getCaseById(caseId);
            String dbid = Objects.toString(this.nodeService.getProperty(caseNodeRef, ContentModel.PROP_NODE_DBID).toString(), null);
            String partyName = PartyService.PARTY_PREFIX + dbid + "_" + role;
            String createdGroup;
            if (roleExists(caseId, role).getFirst()) {
                createdGroup = "GROUP_" + partyName;
            } else {
                createdGroup = this.authorityService.createAuthority(AuthorityType.GROUP, partyName, role, PARTY_ZONES);
            }

            NodeRef createdGroupRef = this.authorityService.getAuthorityNodeRef(createdGroup);

            if (contacts != null && StringUtils.isNotEmpty(contacts.get(0))) {
                NodeRef contactNodeRef;
                for (String contact : contacts) {
                    contactNodeRef = contactService.getContactById(contact);
                    this.nodeService.addChild(createdGroupRef, contactNodeRef, ContentModel.ASSOC_MEMBER, QName.createQName("cm", contact, namespacePrefixResolver));
                }
            }
            return createdGroupRef;
        } catch (Exception ge) {
            throw new AlfrescoRuntimeException("Unable to create party due to the following reason(s): " + ge.getMessage());
        }
    }

    @Override
    public boolean addContactToParty(String caseId, NodeRef partyRef, String partyRole, String contact) {
        return addContactsToParty(caseId, partyRef, partyRole, Collections.singletonList(contact));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addContactsToParty(String caseId, NodeRef partyRef, String partyRole, List<String> contacts) {
        NodeRef caseNodeRef = caseService.getCaseById(caseId);
        caseService.checkCanUpdateCaseRoles(caseNodeRef);

        if (partyRef == null) {
            partyRef = this.authorityService.getAuthorityNodeRef(partyRole);
            if (partyRef == null) {
                partyRef = createParty(caseId, partyRole);
            }
        }
        final NodeRef partyGroupRef = partyRef;
        boolean result = AuthenticationUtil.runAs((() -> {
            boolean created = false;
            for (String contact : contacts) {
                NodeRef childRef = getContactNodeRefId(contact);
                String childAssocName = (String) nodeService.getProperty(childRef, ContentModel.PROP_NAME);
                NodeRef contactNode = nodeService.addChild(partyGroupRef, childRef, ContentModel.ASSOC_MEMBER,
                        QName.createQName("cm", childAssocName, namespacePrefixResolver)).getChildRef();
                if (contactNode != null) {
                    created = true;
                }
            }
            return created;
        }), OpenESDHModel.ADMIN_USER_NAME);
        return result;
    }

    private NodeRef getContactNodeRefId(String contact) {
        if (contact.contains("@")) {
            return this.contactService.getContactById(contact);
        } else if (NodeRef.isNodeRef(contact)) {
            return new NodeRef(contact);
        } else {
            throw new AlfrescoRuntimeException("The contact id supplied is neither an email or nodeRef");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removePartyRole(String caseId, String partyId, String role) {
        Pair<Boolean, NodeRef> caseRole = roleExists(caseId, role);
        if (!caseRole.getFirst()) {
            return false;
        }

        NodeRef caseNodeRef = caseService.getCaseById(caseId);
        caseService.checkCanUpdateCaseRoles(caseNodeRef);

        try {
            NodeRef partyRef = getContactNodeRefId(partyId);
            this.nodeService.removeChild(caseRole.getSecond(), partyRef);
        } catch (Exception ge) {
            throw new AlfrescoRuntimeException("Unable to remove contact from group for the following reason: " + ge.getMessage());
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Pair<Boolean, NodeRef> roleExists(String caseId, String roleName) {
        NodeRef caseNodeRef = this.caseService.getCaseById(caseId);
        String dbid = Objects.toString(this.nodeService.getProperty(caseNodeRef, ContentModel.PROP_NODE_DBID), null);
        if (StringUtils.isNotBlank(dbid)) {
            final String roleGroup = "GROUP_PARTY_" + dbid + "_" + roleName;
            return new Pair<>(this.authorityService.authorityExists(roleGroup), this.authorityService.getAuthorityNodeRef(roleGroup));
        } else {
            return new Pair<>(false, null);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeRef getCaseParty(NodeRef caseNodeRef, String caseId, String partyRole) {
        if (caseNodeRef == null && caseId == null) {
            throw new NullPointerException("Both the caseId and the case nodeRef can't be null");
        }

        if (caseNodeRef == null) { //then get the nodeRef of the case by case id.
            caseNodeRef = this.caseService.getCaseById(caseId);
        }
        Pair<Boolean, NodeRef> nbPartyRolePair = roleExists(caseId, partyRole);
        if (nbPartyRolePair.getFirst()) {
            return nbPartyRolePair.getSecond();
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Set<String>> getContactsByRole(String caseId) {
        List<String> roles = (List<String>) dictionaryService.getConstraint(OpenESDHModel.CONSTRAINT_CASE_ALLOWED_PARTY_ROLES).getConstraint().getParameters().get(ListOfValuesConstraint.ALLOWED_VALUES_PARAM);
        Map<String, Set<String>> contactRoleMap = new HashMap<>();
        for (String role : roles) {
            Pair<Boolean, NodeRef> temp = roleExists(caseId, role);
            if (temp.getFirst()) {
                //We don't bother specify the type of assoc name because there should only ever be contact types in these groups.
                List<ChildAssociationRef> contacts = this.nodeService.getChildAssocs(temp.getSecond());
                Set<String> parties = new HashSet<>();
                for (ChildAssociationRef associationRef : contacts) {
                    NodeRef childRef = associationRef.getChildRef();
                    ContactInfo contact = new ContactInfo(childRef, this.contactService.getContactType(childRef), this.nodeService.getProperties(childRef));
                    parties.add(contact.getEmail());
                }
                contactRoleMap.put(role, parties);
            }
        }

        return contactRoleMap;
    }

    @Override
    public List<ContactInfo> getPartiesInCase(String caseId) {
        Map<String, Set<String>> partiesWithRoles = getContactsByRole(caseId);
        List<ContactInfo> parties = new ArrayList<>();

        for (Map.Entry<String, Set<String>> role : partiesWithRoles.entrySet()) {
            Set<String> value = role.getValue();
            for (String contact : value) {
                NodeRef contactRef = this.contactService.getContactById(contact);
                ContactInfo contactInfo = new ContactInfo(contactRef, this.contactService.getContactType(contactRef),
                        this.nodeService.getProperties(contactRef));
                parties.add(contactInfo);
            }
        }
        return parties;
    }

    //<editor-fold desc="Injected service bean setters">
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setCaseService(CaseService caseService) {
        this.caseService = caseService;
    }

    public void setContactService(ContactService contactService) {
        this.contactService = contactService;
    }

    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    public void setNamespacePrefixResolver(NamespacePrefixResolver namespacePrefixResolver) {
        this.namespacePrefixResolver = namespacePrefixResolver;
    }

    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }
    //</editor-fold>

    public void afterPropertiesSet() throws Exception {
        PropertyCheck.mandatory(this, "namespacePrefixResolver", namespacePrefixResolver);
        PropertyCheck.mandatory(this, "nodeService", nodeService);
        PropertyCheck.mandatory(this, "caseService", caseService);
        PropertyCheck.mandatory(this, "contactService", contactService);
    }
}
