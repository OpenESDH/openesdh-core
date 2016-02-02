package dk.openesdh.repo.services.cases;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

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
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import dk.openesdh.exceptions.contacts.InvalidContactTypeException;
import dk.openesdh.repo.model.ContactInfo;
import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.BehaviourFilterService;
import dk.openesdh.repo.services.TransactionRunner;
import dk.openesdh.repo.services.contacts.ContactService;

/**
 * @author Lanre Abiwon.
 */
@Service("PartyService")
public class PartyServiceImpl implements PartyService {

    private static final Logger LOG = Logger.getLogger(PartyServiceImpl.class);

    @Autowired
    @Qualifier("NodeService")
    private NodeService nodeService;
    @Autowired
    @Qualifier("CaseService")
    private CaseService caseService;
    @Autowired
    @Qualifier("ContactService")
    private ContactService contactService;
    @Autowired
    @Qualifier("AuthorityService")
    private AuthorityService authorityService;
    @Autowired
    @Qualifier("DictionaryService")
    private DictionaryService dictionaryService;
    @Autowired
    @Qualifier("namespaceService")
    private NamespacePrefixResolver namespacePrefixResolver;
    @Autowired
    @Qualifier("VersionService")
    private VersionService versionService;
    @Autowired
    private BehaviourFilterService behaviourFilterService;
    @Autowired
    private TransactionRunner transactionRunner;

    @Override
    public NodeRef createParty(String caseId, String role) {
        return createParty(caseId, role, null);
    }

    @Override
    public NodeRef createParty(String caseId, String role, List<String> contacts) {
        if (StringUtils.isAnyEmpty(caseId, role)) {
            throw new InvalidContactTypeException("The caseId and/or the role is missing");
        }
        NodeRef caseNodeRef = caseService.getCaseById(caseId);
        try {
            CaseRole caseRole = getCaseRole(caseNodeRef, role);
            String createdGroup;
            if (caseRole.isPresent()) {
                createdGroup = caseRole.getFullName();
            } else {
                createdGroup = transactionRunner.runAsAdmin(() -> {
                    return authorityService.createAuthority(AuthorityType.GROUP, caseRole.getName(), role, authorityService.getDefaultZones());
                });
            }

            NodeRef createdGroupRef = authorityService.getAuthorityNodeRef(createdGroup);

            if (contacts != null && StringUtils.isNotEmpty(contacts.get(0))) {
                NodeRef contactNodeRef;
                for (String contact : contacts) {
                    contactNodeRef = contactService.getContactById(contact);
                    nodeService.addChild(createdGroupRef, contactNodeRef, ContentModel.ASSOC_MEMBER, QName.createQName("cm", contact, namespacePrefixResolver));
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
            partyRef = authorityService.getAuthorityNodeRef(partyRole);
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
            return contactService.getContactById(contact);
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
        NodeRef caseNodeRef = caseService.getCaseById(caseId);
        CaseRole caseRole = getCaseRole(caseNodeRef, role);
        if (!caseRole.isPresent()) {
            return false;
        }

        caseService.checkCanUpdateCaseRoles(caseNodeRef);

        try {
            NodeRef partyRef = getContactNodeRefId(partyId);
            nodeService.removeChild(caseRole.getNodeRef().get(), partyRef);
        } catch (Exception ge) {
            throw new AlfrescoRuntimeException("Unable to remove contact from group for the following reason: " + ge.getMessage());
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, List<NodeRef>> getContactsByRole(String caseId) {
        NodeRef caseNodeRef = caseService.getCaseById(caseId);
        return getContactsByRole(caseNodeRef);
    }

    private Map<String, List<NodeRef>> getContactsByRole(NodeRef caseNodeRef) {
        if (nodeService.hasAspect(caseNodeRef, OpenESDHModel.ASPECT_CASE_FREEZABLE_PARTIES)) {
            List<String> roles = getAvailablePartyRoles();
            Map<String, List<NodeRef>> frozenContacts = new HashMap<>();
            for (String role : roles) {
                ArrayList<NodeRef> roleFrozenContacts = (ArrayList<NodeRef>) nodeService.getProperty(caseNodeRef,
                        getFrozenParamQName(role));
                if (roleFrozenContacts != null && roleFrozenContacts.size() > 0) {
                    frozenContacts.put(role, roleFrozenContacts);
                }
            }
            return frozenContacts;
        }
        return getCaseParties(caseNodeRef, ChildAssociationRef::getChildRef);
    }

    private QName getFrozenParamQName(String role) {
        return QName.createQName(OpenESDHModel.CASE_URI, OpenESDHModel.FROZEN_CASE_PARTIES_PROP_PREFIX + role);
    }

    private <T> Map<String, List<T>> getCaseParties(NodeRef caseNodeRef, Function<ChildAssociationRef, T> transformer) {
        List<String> roles = getAvailablePartyRoles();
        return roles.stream().collect(Collectors.toMap(
                role -> role,
                role -> {
                    CaseRole caseRole = getCaseRole(caseNodeRef, role);
                    if (!caseRole.isPresent()) {
                        return Collections.EMPTY_LIST;
                    }
                    List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(caseRole.getNodeRef().get());
                    return childAssocs
                    .stream()
                    .map(transformer::apply)
                    .collect(Collectors.toList());
                }
        ));
    }

    private List<String> getAvailablePartyRoles() {
        List<String> roles = (List<String>) dictionaryService
                .getConstraint(OpenESDHModel.CONSTRAINT_CASE_ALLOWED_PARTY_ROLES)
                .getConstraint()
                .getParameters()
                .get(ListOfValuesConstraint.ALLOWED_VALUES_PARAM);
        return roles;
    }

    @Override
    public List<ContactInfo> getPartiesInCase(String caseId) {
        NodeRef caseNodeRef = caseService.getCaseById(caseId);
        return getCaseParties(caseNodeRef,
                assoc -> new ContactInfo(assoc.getChildRef(),
                        contactService.getContactType(assoc.getChildRef()),
                        nodeService.getProperties(assoc.getChildRef())))
                .entrySet()
                .stream()
                .flatMap(t -> t.getValue().stream())
                .collect(Collectors.toList());
    }

    @Override
    public void lockCasePartiesToVersions(NodeRef caseNodeRef) {
        Map<QName, Serializable> props = new HashMap<>();
        getCaseParties(caseNodeRef, (ChildAssociationRef assoc) -> {
            lockContact(caseNodeRef, assoc.getChildRef());
            Version currentVersion = versionService.getCurrentVersion(assoc.getChildRef());
            return currentVersion.getFrozenStateNodeRef();
        }).entrySet()
                .stream()
                .filter(e -> e.getValue() != null && !e.getValue().isEmpty())
                .forEach(e -> {
                    props.put(getFrozenParamQName(e.getKey()), new ArrayList(e.getValue()));
                });
        behaviourFilterService.executeWithoutBehavior(caseNodeRef,
                () -> nodeService.addAspect(caseNodeRef, OpenESDHModel.ASPECT_CASE_FREEZABLE_PARTIES, props));
    }

    public void unlockCaseParties(NodeRef caseNodeRef) {
        behaviourFilterService.executeWithoutBehavior(caseNodeRef, () -> {
            //allow version to be deleted -> if any version is locked, then original node will not be deleted
            getContactsByRole(caseNodeRef).values()
                    .forEach(group -> {
                        group.forEach((contactVersionNodeRef) -> unlockContact(caseNodeRef, contactVersionNodeRef));
                    });
            nodeService.removeAspect(caseNodeRef, OpenESDHModel.ASPECT_CASE_FREEZABLE_PARTIES);
        });
    }

    private ArrayList<NodeRef> getLockedInCases(NodeRef contactNodeRef) {
        ArrayList<NodeRef> cases = (ArrayList<NodeRef>) nodeService.getProperty(contactNodeRef, OpenESDHModel.PROP_CONTACT_LOCKED_IN_CASES);
        if (cases == null) {
            cases = new ArrayList();
        }
        return cases;
    }

    private void lockContact(NodeRef caseNodeRef, NodeRef contactNodeRef) {
        LOG.info("locking contact: " + contactNodeRef);
        behaviourFilterService.executeWithoutBehavior(contactNodeRef, () -> {
            ArrayList<NodeRef> cases = getLockedInCases(contactNodeRef);
            cases.add(caseNodeRef);
            nodeService.setProperty(contactNodeRef, OpenESDHModel.PROP_CONTACT_LOCKED_IN_CASES, cases);
        });
    }

    private void unlockContact(NodeRef caseNodeRef, NodeRef contactVersionNodeRef) {
        Version currentVersion = versionService.getVersionHistory(contactVersionNodeRef).getHeadVersion();
        NodeRef contactNodeRef = currentVersion.getVersionedNodeRef();
        LOG.info("unlocking contact: " + contactNodeRef);
        behaviourFilterService.executeWithoutBehavior(contactNodeRef, () -> {
            ArrayList<NodeRef> cases = getLockedInCases(contactNodeRef);
            cases.remove(caseNodeRef);
            if (cases.size() > 0) {
                nodeService.setProperty(contactNodeRef, OpenESDHModel.PROP_CONTACT_LOCKED_IN_CASES, cases);
            } else {
                nodeService.removeProperty(contactNodeRef, OpenESDHModel.PROP_CONTACT_LOCKED_IN_CASES);
            }
        });
    }

    private CaseRole getCaseRole(NodeRef caseNodeRef, String roleName) {
        CaseRole caseRole = new CaseRole();
        Optional<Serializable> dbid = Optional.ofNullable(nodeService.getProperty(caseNodeRef, ContentModel.PROP_NODE_DBID));
        if (dbid.isPresent()) {
            caseRole.setName(dbid.get().toString(), roleName);
            caseRole.setNodeRef(Optional.ofNullable(authorityService.getAuthorityNodeRef(caseRole.getFullName())));
            return caseRole;
        }
        return caseRole;
    }
}
