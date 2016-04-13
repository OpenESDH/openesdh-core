package dk.openesdh.repo.services.cases;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.google.common.collect.Sets;

import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.BehaviourFilterService;
import dk.openesdh.repo.services.contacts.ContactService;
import dk.openesdh.repo.utils.JSONArrayCollector;

/**
 * @author Lanre Abiwon.
 */
@Service(PartyService.BEAN_ID)
public class PartyServiceImpl implements PartyService {

    @Autowired
    @Qualifier("NodeService")
    private NodeService nodeService;
    @Autowired
    @Qualifier(CaseService.BEAN_ID)
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

    /**
     * {@inheritDoc}
     */
    @Override
    public List<NodeRef> addCaseParty(String caseId, NodeRef role, String... contactId) {
        return addCaseParty(caseId, role, Arrays.asList(contactId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<NodeRef> addCaseParty(String caseId, NodeRef roleRef, List<String> contactIds) {
        if (StringUtils.isAnyEmpty(caseId)) {
            throw new RuntimeException("The caseId is missing");
        }
        if (Objects.isNull(roleRef)) {
            throw new RuntimeException("The roleRef is missing");
        }
        NodeRef caseNodeRef = caseService.getCaseById(caseId);
        if (caseService.isLocked(caseNodeRef)) {
            return Collections.emptyList();
        }
        caseService.checkCanUpdateCaseRoles(caseNodeRef);
        return contactIds.stream()
            .map(contactId -> addContactToCaseParty(caseNodeRef, roleRef, contactId))
            .collect(Collectors.toList());
    }

    @Override
    public void updateCaseParty(NodeRef partyRef, NodeRef roleRef) {
        nodeService.setProperty(partyRef, OpenESDHModel.PROP_CONTACT_PARTY_ROLE, roleRef);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeCaseParty(String caseId, NodeRef partyRef) {
        NodeRef caseNodeRef = caseService.getCaseById(caseId);
        caseService.checkCanUpdateCaseRoles(caseNodeRef);
        AuthenticationUtil.runAsSystem(() -> {
            nodeService.deleteNode(partyRef);
            return null;
        });
    }

    @Override
    public JSONArray getCasePartiesJson(String caseId){
        NodeRef caseNodeRef = caseService.getCaseById(caseId);
        return nodeService.getChildAssocs(caseNodeRef, Sets.newHashSet(OpenESDHModel.TYPE_CONTACT_PARTY))
                .stream()
                .map(ChildAssociationRef::getChildRef)
                .map(this::getCasePartyJson)
                .collect(JSONArrayCollector.simple());
    }

    @Override
    public void lockCasePartiesToVersions(NodeRef caseNodeRef) {
        getCasePartiesRefsStream(caseNodeRef).forEach(partyRef -> freezePartyContact(caseNodeRef, partyRef));
    }

    @Override
    public void unlockCaseParties(NodeRef caseNodeRef) {
        behaviourFilterService.executeWithoutBehavior(caseNodeRef, () -> {
            // allow version to be deleted -> if any version is locked, then
            // original node will not be deleted
            getCasePartiesRefsStream(caseNodeRef).forEach(partyRef -> unfreezePartyContact(caseNodeRef, partyRef));
        });
    }
    
    private JSONObject getCasePartyJson(NodeRef partyRef) {
        JSONObject json = new JSONObject();
        json.put(PartyService.FIELD_NODE_REF, partyRef.toString());
        NodeRef contactRef = (NodeRef) nodeService.getProperty(partyRef, OpenESDHModel.PROP_CONTACT_CONTACT);
        JSONObject contactJson = contactService.getContactInfo(contactRef).toJSONObject();
        json.put(PartyService.FIELD_CONTACT, contactJson);
        NodeRef roleRef = (NodeRef) nodeService.getProperty(partyRef, OpenESDHModel.PROP_CONTACT_PARTY_ROLE);
        json.put(PartyService.FIELD_ROLE_REF, roleRef.toString());
        json.put(PartyService.FIELD_ROLE_DISPLAY_NAME,
                nodeService.getProperty(roleRef, OpenESDHModel.PROP_CLASSIF_DISPLAY_NAME));
        return json;
    }

    private NodeRef addContactToCaseParty(NodeRef caseRef, NodeRef partyRoleRef, String contactId) {
        Map<QName, Serializable> props = new HashMap<>();
        props.put(OpenESDHModel.PROP_CONTACT_PARTY_ROLE, partyRoleRef);
        props.put(OpenESDHModel.PROP_CONTACT_CONTACT, getContactNodeRefId(contactId));
        return nodeService.createNode(caseRef, ContentModel.ASSOC_CONTAINS, QName.createQName(contactId),
                OpenESDHModel.TYPE_CONTACT_PARTY, props).getChildRef();
    }

    private NodeRef getContactNodeRefId(String contactId) {
        if (NodeRef.isNodeRef(contactId)) {
            return new NodeRef(contactId);
        }
        return contactService.getContactById(contactId);
    }

    private void freezePartyContact(NodeRef caseRef, NodeRef partyRef) {
        NodeRef contactRef = (NodeRef) nodeService.getProperty(partyRef, OpenESDHModel.PROP_CONTACT_CONTACT);
        NodeRef frozenContactRef = getFrozenStateNodeRef(contactRef);
        nodeService.setProperty(partyRef, OpenESDHModel.PROP_CONTACT_CONTACT, frozenContactRef);
        lockContact(caseRef, contactRef);
    }
    
    private void unfreezePartyContact(NodeRef caseNodeRef, NodeRef partyRef) {
        NodeRef frozenContactRef = (NodeRef) nodeService.getProperty(partyRef, OpenESDHModel.PROP_CONTACT_CONTACT);
        NodeRef contactRef = versionService.getVersionHistory(frozenContactRef).getHeadVersion()
                .getVersionedNodeRef();
        nodeService.setProperty(partyRef, OpenESDHModel.PROP_CONTACT_CONTACT, contactRef);
        unlockContact(caseNodeRef, frozenContactRef);
    }

    private Stream<NodeRef> getCasePartiesRefsStream(NodeRef caseRef){
        return nodeService.getChildAssocs(caseRef,Sets.newHashSet(OpenESDHModel.TYPE_CONTACT_PARTY))
                .stream()
                .map(ChildAssociationRef::getChildRef);
    }

    private ArrayList<NodeRef> getLockedInCases(NodeRef contactNodeRef) {
        ArrayList<NodeRef> cases = (ArrayList<NodeRef>) nodeService.getProperty(contactNodeRef, OpenESDHModel.PROP_CONTACT_LOCKED_IN_CASES);
        if (cases == null) {
            cases = new ArrayList<>();
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

    private NodeRef getFrozenStateNodeRef(NodeRef nodeRef) {
        Version currentVersion = versionService.getCurrentVersion(nodeRef);
        return currentVersion.getFrozenStateNodeRef();
    }

}
