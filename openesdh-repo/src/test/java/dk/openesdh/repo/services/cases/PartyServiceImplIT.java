package dk.openesdh.repo.services.cases;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.collect.Sets;
import com.tradeshift.test.remote.Remote;
import com.tradeshift.test.remote.RemoteTestRunner;

import dk.openesdh.repo.helper.CaseDocumentTestHelper;
import dk.openesdh.repo.helper.CaseHelper;
import dk.openesdh.repo.model.CaseStatus;
import dk.openesdh.repo.model.ContactType;
import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.TransactionRunner;
import dk.openesdh.repo.services.contacts.ContactServiceImpl;
import dk.openesdh.repo.services.contacts.PartyRoleService;

@RunWith(RemoteTestRunner.class)
@Remote(runnerClass = SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:alfresco/application-context.xml")
public class PartyServiceImplIT {

    @Autowired
    @Qualifier("NodeService")
    private NodeService nodeService;

    @Autowired
    @Qualifier("ContactService")
    private ContactServiceImpl contactService;

    @Autowired
    @Qualifier(CaseService.BEAN_ID)
    private CaseService caseService;

    @Autowired
    private TransactionRunner transactionRunner;

    @Autowired
    @Qualifier("CaseDocumentTestHelper")
    private CaseDocumentTestHelper caseTestHelper;

    @Autowired
    @Qualifier(PartyService.BEAN_ID)
    private PartyServiceImpl partyService;

    @Autowired
    @Qualifier("PartyRoleService")
    private PartyRoleService partyRoleService;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private static final String TEST_CASE_NAME = "Test_case";
    private static final String EMAIL = UUID.randomUUID() + "@openesdh.org";
    private static final String TEST_PERSON_CONTACT_EMAIL = "person_" + EMAIL;
    private static final String TEST_ORG_CONTACT_EMAIL = "org_" + EMAIL;

    private NodeRef caseNodeRef;
//    private NodeRef partyGroupNodeRef;
    private NodeRef testPersonContact;
    private NodeRef testPersonContact2;
    private NodeRef testOrgContact;
    private final List<NodeRef> casesToClean = new ArrayList<>();

    @Before
    public void setUp() throws Exception {
        AuthenticationUtil.setFullyAuthenticatedUser(CaseHelper.ADMIN_USER_NAME);
        caseNodeRef = caseTestHelper.createCaseBehaviourOn(TEST_CASE_NAME, caseService.getCasesRootNodeRef(), CaseHelper.ADMIN_USER_NAME);
        transactionRunner.runInTransactionAsAdmin(() -> {
            testPersonContact = contactService.createContact(
                    TEST_PERSON_CONTACT_EMAIL,
                    ContactType.PERSON.name(),
                    createContactProperties(TEST_PERSON_CONTACT_EMAIL));
            testOrgContact = contactService.createContact(
                    TEST_ORG_CONTACT_EMAIL,
                    ContactType.ORGANIZATION.name(),
                    createContactProperties(TEST_ORG_CONTACT_EMAIL));
            return null;
        });
    }

    private HashMap<QName, Serializable> createContactProperties(String email) {
        HashMap<QName, Serializable> props = new HashMap<>();
        props.put(OpenESDHModel.PROP_CONTACT_EMAIL, email);
        return props;
    }

    @After
    public void tearDown() throws Exception {
        AuthenticationUtil.setFullyAuthenticatedUser(CaseHelper.ADMIN_USER_NAME);
        ArrayList<NodeRef> nodes = new ArrayList<>();
        if (testPersonContact != null) {
            nodes.add(testPersonContact);
        }

        if (testPersonContact2 != null) {
            nodes.add(testPersonContact2);
        }

        if (testOrgContact != null) {
            nodes.add(testOrgContact);
        }

        if (caseNodeRef != null) {
            casesToClean.add(caseNodeRef);
        }
        caseTestHelper.removeNodesAndDeleteUsersInTransaction(nodes, casesToClean, Collections.emptyList());
    }

    @Test
    public void shouldAddPersonAndOrgContactsAsCaseParties() throws Exception {
        String caseId = caseService.getCaseId(caseNodeRef);

        partyService.addCaseParty(caseId, getMemberPartyRoleRef(), TEST_PERSON_CONTACT_EMAIL,
                TEST_ORG_CONTACT_EMAIL);

        List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(caseNodeRef,
                Sets.newHashSet(OpenESDHModel.TYPE_CONTACT_PARTY));

        Assert.assertFalse("Created parties list shouldn't be empty", childAssocs.isEmpty());

        List<NodeRef> resultContacts = childAssocs.stream()
                .map(assoc -> assoc.getChildRef())
                .map(this::getPartyContactRef)
                .collect(Collectors.toList());

        Assert.assertTrue("Created party should contain added test person contact",
                resultContacts.contains(testPersonContact));

        Assert.assertTrue("Created party should contain added test organization contact",
                resultContacts.contains(testOrgContact));
    }
    
    @Test(expected = AccessDeniedException.class)
    public void shouldFailAddingPersonContactBecauseOfPermissions() throws Exception {
        String caseId = caseService.getCaseId(caseNodeRef);
        // login as other user
        AuthenticationUtil.setFullyAuthenticatedUser(CaseHelper.MIKE_JACKSON);
        // should fail
        partyService.addCaseParty(caseId, getMemberPartyRoleRef(), TEST_PERSON_CONTACT_EMAIL,
                TEST_ORG_CONTACT_EMAIL);
     }

    @Test
    public void shouldCreatePartiesWithContactsAndGetCasePartiesJson() throws Exception {
        String caseId = caseService.getCaseId(caseNodeRef);
        NodeRef memberRoleRef = getMemberPartyRoleRef();
        partyService.addCaseParty(caseId, memberRoleRef, TEST_PERSON_CONTACT_EMAIL, TEST_ORG_CONTACT_EMAIL);
        
        JSONArray partiesArray = partyService.getCasePartiesJson(caseId);
        Assert.assertNotNull("The retrieved case parties json cannot be null", partiesArray);
        
        Assert.assertEquals("Wrong number of retrieved case parties", 2, partiesArray.size());

        JSONObject jsonObj = (JSONObject) partiesArray.get(0);
        String partyRoleRef = (String) jsonObj.get(PartyService.FIELD_ROLE_REF);
        Assert.assertEquals("Wrong party role ref. Should be one of 'Member' role.", memberRoleRef.toString(),
                partyRoleRef);
        
        List<NodeRef> casePartyContactRefs = getPartyContactRefs(partiesArray);
        
        Assert.assertTrue("The retrieved case parties json should contain receiver test person contact",
                casePartyContactRefs.contains(testPersonContact));
        Assert.assertTrue("The retrieved case parties json should contain receiver test organization contact",
                casePartyContactRefs.contains(testOrgContact));
    }

    @Test
    public void shouldCreatePartyThenRemove() {
        String caseId = caseService.getCaseId(caseNodeRef);
        NodeRef memberRoleRef = getMemberPartyRoleRef();
        List<NodeRef> partiesRef = partyService.addCaseParty(caseId, memberRoleRef, TEST_PERSON_CONTACT_EMAIL);

        List<ChildAssociationRef> partiesAssocsBeforeRemove = nodeService.getChildAssocs(caseNodeRef,
                Sets.newHashSet(OpenESDHModel.TYPE_CONTACT_PARTY));
        Assert.assertFalse("The case should contain created party assoc", partiesAssocsBeforeRemove.isEmpty());

        partyService.removeCaseParty(caseId, partiesRef.get(0));

        List<ChildAssociationRef> partiesAssocsAfterRemove = nodeService.getChildAssocs(caseNodeRef,
                Sets.newHashSet(OpenESDHModel.TYPE_CONTACT_PARTY));
        Assert.assertTrue("The case should not contain removed parties", partiesAssocsAfterRemove.isEmpty());
    }

    @Test
    public void closedCaseShouldHaveUnchangedContacs() throws Exception {
        String caseId = caseService.getCaseId(caseNodeRef);
        NodeRef memberRoleRef = getMemberPartyRoleRef();
        // add party
        partyService.addCaseParty(caseId, memberRoleRef, TEST_PERSON_CONTACT_EMAIL);
        // set property
        String originalCity = "London";
        String changedCity = "Leeds";
        nodeService.setProperty(testPersonContact, OpenESDHModel.PROP_CONTACT_CITY_NAME, originalCity);
        try {
            // lock case
            caseService.changeNodeStatus(caseNodeRef, CaseStatus.CLOSED);
            // change contact property
            nodeService.setProperty(testPersonContact, OpenESDHModel.PROP_CONTACT_CITY_NAME, changedCity);

            // check
            JSONArray partiesArray = partyService.getCasePartiesJson(caseId);
            List<NodeRef> contactRefs = getPartyContactRefs(partiesArray);
            NodeRef versionNodeRef = contactRefs.get(0);
            String verEmail = (String) nodeService.getProperty(versionNodeRef, OpenESDHModel.PROP_CONTACT_EMAIL);
            Assert.assertEquals("Contact original email has not been preserved", TEST_PERSON_CONTACT_EMAIL,
                    verEmail);
            String verCity = (String) nodeService.getProperty(versionNodeRef, OpenESDHModel.PROP_CONTACT_CITY_NAME);
            Assert.assertEquals("Contact original city has not been preserved", originalCity, verCity);
        } finally {
            // unlock case, that it could be deleted
            caseService.changeNodeStatus(caseNodeRef, CaseStatus.ACTIVE);
        }
        // reopened case should have new contacs
        JSONArray partiesArray = partyService.getCasePartiesJson(caseId);
        List<NodeRef> contactRefs = getPartyContactRefs(partiesArray);
        NodeRef partyContactNodeRef = contactRefs.get(0);
        String email = (String) nodeService.getProperty(partyContactNodeRef, OpenESDHModel.PROP_CONTACT_EMAIL);
        Assert.assertEquals("Contact original email has not been preserved", TEST_PERSON_CONTACT_EMAIL, email);
        String city = (String) nodeService.getProperty(partyContactNodeRef, OpenESDHModel.PROP_CONTACT_CITY_NAME);
        Assert.assertEquals("Contact email is not up to date after case has been reopened", changedCity, city);
    }

    @Test
    public void shoulAllowCaseOwnerAddAndRemoveParties() {
        NodeRef userCaseNodeRef = caseTestHelper.createCaseBehaviourOn(TEST_CASE_NAME + CaseHelper.ALICE_BEECHER,
                caseService.getCasesRootNodeRef(), CaseHelper.ALICE_BEECHER);
        casesToClean.add(userCaseNodeRef);
        String userCaseId = caseService.getCaseId(userCaseNodeRef);

        AuthenticationUtil.setFullyAuthenticatedUser(CaseHelper.ALICE_BEECHER);
        NodeRef memberRoleRef = getMemberPartyRoleRef();
        List<NodeRef> partiesRef = partyService.addCaseParty(userCaseId, memberRoleRef, TEST_PERSON_CONTACT_EMAIL,
                TEST_ORG_CONTACT_EMAIL);

        partiesRef.forEach(party -> partyService.removeCaseParty(userCaseId, party));
    }

    @Test(expected = AccessDeniedException.class)
    public void shouldNotRemovePartyFromClosedCase() throws Exception {
        String caseId = caseService.getCaseId(caseNodeRef);
        NodeRef memberRoleRef = getMemberPartyRoleRef();
        List<NodeRef> partiesRef = partyService.addCaseParty(caseId, memberRoleRef, TEST_PERSON_CONTACT_EMAIL);
        try {
            // lock case
            caseService.changeNodeStatus(caseNodeRef, CaseStatus.CLOSED);
            // try to remove party
            partyService.removeCaseParty(caseId, partiesRef.get(0));
            Assert.fail("Party removal must fail");
        } finally {
            // unlock cases so it can be deleted
            caseService.changeNodeStatus(caseNodeRef, CaseStatus.ACTIVE);
        }
    }

    private NodeRef getMemberPartyRoleRef() {
        return partyRoleService.getClassifValueByName(PartyRoleService.MEMBER_ROLE).get().getNodeRef();
    }

    private NodeRef getPartyContactRef(NodeRef partyRef) {
        return (NodeRef) nodeService.getProperty(partyRef, OpenESDHModel.PROP_CONTACT_CONTACT);
    }

    private List<NodeRef> getPartyContactRefs(JSONArray jsonArray) {
        List<NodeRef> casePartyContactRefs = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject party = (JSONObject) jsonArray.get(i);
            JSONObject contact = (JSONObject) party.get(PartyService.FIELD_CONTACT);
            casePartyContactRefs.add(new NodeRef((String) contact.get(PartyService.FIELD_NODE_REF)));
        }
        return casePartyContactRefs;
    }
}

