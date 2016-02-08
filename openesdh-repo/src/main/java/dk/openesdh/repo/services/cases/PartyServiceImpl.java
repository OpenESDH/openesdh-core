package dk.openesdh.repo.services.cases;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import dk.openesdh.exceptions.contacts.InvalidContactTypeException;
import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.BehaviourFilterService;
import dk.openesdh.repo.services.contacts.ContactService;

/**
 * @author Lanre Abiwon.
 */
@Service("PartyService")
public class PartyServiceImpl implements PartyService {

    private String CONTENT_URI;

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
    @Qualifier("VersionService")
    private VersionService versionService;
    @Autowired
    private BehaviourFilterService behaviourFilterService;
    @Autowired
    @Qualifier("namespaceService")
    private NamespacePrefixResolver namespacePrefixResolver;

    @PostConstruct
    public void init() {
        CONTENT_URI = namespacePrefixResolver.getNamespaceURI("cm");
    }

    /**
     * {@inheritDoc}
     */
    public void addCaseParty(String caseId, String role, String... contactId) {
        addCaseParty(caseId, role, Arrays.asList(contactId));
    }

    /**
     * {@inheritDoc}
     */
    public void addCaseParty(String caseId, String role, List<String> contactIds) {
        if (StringUtils.isAnyEmpty(caseId, role)) {
            throw new InvalidContactTypeException("The caseId and/or the role is missing");
        }
        NodeRef caseNodeRef = caseService.getCaseById(caseId);
        if (!caseService.isLocked(caseNodeRef)) {
            caseService.checkCanUpdateCaseRoles(caseNodeRef);
            NodeRef casePartyRoleRef = getOrCreateCasePartyRole(caseNodeRef, role);
            addContactsToCaseRole(casePartyRoleRef, contactIds);
        }
    }

    private NodeRef getOrCreateCasePartyRole(NodeRef caseNodeRef, String role) {
        try {
            CaseRole caseRole = getCaseRole(caseNodeRef, role);
            String createdGroup;
            if (caseRole.isPresent()) {
                createdGroup = caseRole.getFullName();
            } else {
                createdGroup = AuthenticationUtil.runAsSystem(() -> {
                    return authorityService.createAuthority(AuthorityType.GROUP, caseRole.getName(), role, authorityService.getDefaultZones());
                });
            }
            return authorityService.getAuthorityNodeRef(createdGroup);
        } catch (Exception ge) {
            throw new AlfrescoRuntimeException("Unable to create party due to the following reason(s): " + ge.getMessage());
        }
    }

    private void addContactsToCaseRole(NodeRef casePartyRoleRef, List<String> contacts) {
        if (casePartyRoleRef == null) {
            throw new AlfrescoRuntimeException("Case party nodeRef is missing");
        }
        if (CollectionUtils.isNotEmpty(contacts)) {
            AuthenticationUtil.runAsSystem(() -> {
                List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(casePartyRoleRef);
                for (String email : contacts) {
                    if (hasNoChildWithName(childAssocs, email)) {
                        nodeService.addChild(
                                casePartyRoleRef,
                                getContactNodeRefId(email),
                                ContentModel.ASSOC_MEMBER,
                                QName.createQName(CONTENT_URI, email));
                    }
                }
                return null;
            });
        }
    }

    /**
     * check if child is not added yet
     *
     * @param childAssocs
     * @param name
     * @return
     */
    private boolean hasNoChildWithName(List<ChildAssociationRef> childAssocs, String name) {
        return childAssocs.stream()
                .map(ChildAssociationRef::getQName)
                .map(QName::getLocalName)
                .noneMatch(localName -> localName.equals(name));
    }

    private NodeRef getContactNodeRefId(String contactId) {
        return contactService.getContactById(contactId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeCaseParty(String caseId, String contactId, String role) {
        NodeRef caseNodeRef = caseService.getCaseById(caseId);
        caseService.checkCanUpdateCaseRoles(caseNodeRef);

        CaseRole caseRole = getCaseRole(caseNodeRef, role);
        if (!caseRole.isPresent()) {
            return;
        }

        try {
            NodeRef partyRef = getContactNodeRefId(contactId);
            nodeService.removeChild(caseRole.getNodeRef(), partyRef);
        } catch (Exception ge) {
            throw new AlfrescoRuntimeException("Unable to remove contact from group for the following reason: " + ge.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, List<NodeRef>> getCaseParties(String caseId) {
        NodeRef caseNodeRef = caseService.getCaseById(caseId);
        return getContactsByRole(caseNodeRef);
    }

    private Map<String, List<NodeRef>> getContactsByRole(NodeRef caseNodeRef) {
        if (nodeService.hasAspect(caseNodeRef, OpenESDHModel.ASPECT_CASE_FREEZABLE_PARTIES)) {
            return getFrozzenCaseParties(caseNodeRef);
        }
        return getCasePartiesByAssoc(caseNodeRef);
    }

    private Map<String, List<NodeRef>> getFrozzenCaseParties(NodeRef caseNodeRef) throws InvalidNodeRefException {
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

    private Map<String, List<NodeRef>> getCasePartiesByAssoc(NodeRef caseNodeRef) {
        List<String> roles = getAvailablePartyRoles();
        return roles.stream().collect(Collectors.toMap(
                role -> role,
                role -> {
                    CaseRole caseRole = getCaseRole(caseNodeRef, role);
                    if (!caseRole.isPresent()) {
                        return Collections.EMPTY_LIST;
                    }
                    List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(caseRole.getNodeRef());
                    return childAssocs
                    .stream()
                    .map(ChildAssociationRef::getChildRef)
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
    public void lockCasePartiesToVersions(NodeRef caseNodeRef) {
        Map<QName, Serializable> props = new HashMap<>();
        getCasePartiesByAssoc(caseNodeRef)
                .entrySet()
                .stream()
                .filter(roleEntry -> CollectionUtils.isNotEmpty(roleEntry.getValue()))
                .forEach(roleEntry -> {
                    ArrayList<NodeRef> frozenNodes = new ArrayList();
                    roleEntry.getValue()
                            .forEach(contactNodeRef -> {
                                lockContact(caseNodeRef, contactNodeRef);
                                frozenNodes.add(getFrozenStateNodeRef(contactNodeRef));
                            });
                    props.put(getFrozenParamQName(roleEntry.getKey()), frozenNodes);
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
        behaviourFilterService.executeWithoutBehavior(contactNodeRef, () -> {
            ArrayList<NodeRef> cases = getLockedInCases(contactNodeRef);
            cases.add(caseNodeRef);
            nodeService.setProperty(contactNodeRef, OpenESDHModel.PROP_CONTACT_LOCKED_IN_CASES, cases);
        });
    }

    private void unlockContact(NodeRef caseNodeRef, NodeRef contactVersionNodeRef) {
        Version currentVersion = versionService.getVersionHistory(contactVersionNodeRef).getHeadVersion();
        NodeRef contactNodeRef = currentVersion.getVersionedNodeRef();
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

    CaseRole getCaseRole(NodeRef caseNodeRef, String roleName) {
        CaseRole caseRole = new CaseRole();
        Optional<Serializable> dbid = Optional.ofNullable(nodeService.getProperty(caseNodeRef, ContentModel.PROP_NODE_DBID));
        if (dbid.isPresent()) {
            caseRole.setName(dbid.get().toString(), roleName);
            caseRole.setNodeRef(Optional.ofNullable(authorityService.getAuthorityNodeRef(caseRole.getFullName())));
            return caseRole;
        }
        return caseRole;
    }

    private NodeRef getFrozenStateNodeRef(NodeRef nodeRef) {
        Version currentVersion = versionService.getCurrentVersion(nodeRef);
        return currentVersion.getFrozenStateNodeRef();
    }

    private QName getFrozenParamQName(String role) {
        return QName.createQName(OpenESDHModel.CASE_URI, OpenESDHModel.FROZEN_CASE_PARTIES_PROP_PREFIX + role);
    }

}
