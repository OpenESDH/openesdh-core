package dk.openesdh.repo.services.cases;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.namespace.QName;
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
import dk.openesdh.repo.model.ContactInfo;
import dk.openesdh.repo.model.ContactType;
import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.TransactionRunner;
import dk.openesdh.repo.services.contacts.ContactServiceImpl;

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
    @Qualifier("PartyService")
    private PartyServiceImpl partyService;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private static final String TEST_CASE_NAME = "Test_case";
    private static final String SENDER_ROLE = "Afsender";
    private static final String RECEIVER_ROLE = "Modtager";
    private static final String EMAIL = UUID.randomUUID() + "@openesdh.org";
    private static final String TEST_PERSON_CONTACT_EMAIL = "person_" + EMAIL;
    private static final String TEST_PERSON2_CONTACT_EMAIL = "person2_" + EMAIL;
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
        PartyService.CaseRole sender = getCasePartyGroupNodeRef(caseNodeRef, SENDER_ROLE);
        if (sender.isPresent()) {
            nodes.add(sender.getNodeRef());
        }
        PartyService.CaseRole receiver = getCasePartyGroupNodeRef(caseNodeRef, RECEIVER_ROLE);
        if (receiver.isPresent()) {
            nodes.add(receiver.getNodeRef());
        }
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
        caseTestHelper.removeNodesAndDeleteUsersInTransaction(nodes, casesToClean, new ArrayList<>());
    }

    @Test
    public void shouldCreateSenderPartyGroup() throws Exception {
        String caseId = caseService.getCaseId(caseNodeRef);
        partyService.addCaseParty(caseId, SENDER_ROLE);

        String dbid = this.nodeService.getProperty(caseNodeRef, ContentModel.PROP_NODE_DBID).toString();
        String expectedPartyGroupName = AuthorityType.GROUP.getPrefixString() + PartyService.PARTY_PREFIX
                + dbid + "_" + SENDER_ROLE;

        String resultPartyGroupName = (String) nodeService.getProperty(
                getCasePartyGroupNodeRef(caseNodeRef, SENDER_ROLE).getNodeRef(),
                ContentModel.PROP_AUTHORITY_NAME);

        Assert.assertEquals("Created party group name differs from expected", expectedPartyGroupName,
                resultPartyGroupName);
    }

    private PartyService.CaseRole getCasePartyGroupNodeRef(NodeRef caseNodeRef, String casePartyGroupName) {
        return partyService.getCaseRole(caseNodeRef, casePartyGroupName);
    }

    @Test
    public void shouldCreatePartyAddPersonAndOrgContacts() throws Exception {
        String caseId = caseService.getCaseId(caseNodeRef);

        partyService.addCaseParty(caseId, SENDER_ROLE, TEST_PERSON_CONTACT_EMAIL, TEST_ORG_CONTACT_EMAIL);

        List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(
                getCasePartyGroupNodeRef(caseNodeRef, SENDER_ROLE).getNodeRef(),
                Sets.newHashSet(OpenESDHModel.TYPE_CONTACT_PERSON, OpenESDHModel.TYPE_CONTACT_ORGANIZATION));

        Assert.assertFalse("Created party shouldn't be empty", childAssocs.isEmpty());

        List<NodeRef> resultContacts = childAssocs.stream().map(assoc -> assoc.getChildRef())
                .collect(Collectors.toList());

        Assert.assertTrue("Created party should contain added test person contact",
                resultContacts.contains(testPersonContact));

        Assert.assertTrue("Created party should contain added test organization contact",
                resultContacts.contains(testOrgContact));
    }

    @Test
    public void shouldUpdatePartyAddPersonAndOrgContacts() throws Exception {
        String caseId = caseService.getCaseId(caseNodeRef);

        partyService.addCaseParty(caseId, SENDER_ROLE, TEST_PERSON_CONTACT_EMAIL);
        partyService.addCaseParty(caseId, SENDER_ROLE, TEST_ORG_CONTACT_EMAIL);

        List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(
                getCasePartyGroupNodeRef(caseNodeRef, SENDER_ROLE).getNodeRef(),
                Sets.newHashSet(OpenESDHModel.TYPE_CONTACT_PERSON, OpenESDHModel.TYPE_CONTACT_ORGANIZATION));

        Assert.assertFalse("Created party shouldn't be empty", childAssocs.isEmpty());

        List<NodeRef> resultContacts = childAssocs.stream().map(assoc -> assoc.getChildRef())
                .collect(Collectors.toList());

        Assert.assertTrue("Created party should contain added test person contact",
                resultContacts.contains(testPersonContact));

        Assert.assertTrue("Created party should contain added test organization contact",
                resultContacts.contains(testOrgContact));
    }

    @Test(expected = AccessDeniedException.class)
    public void shuldFailAddingPersonContactBecauseOfPermissions() throws Exception {
        String caseId = caseService.getCaseId(caseNodeRef);

        //login as other user
        AuthenticationUtil.setFullyAuthenticatedUser(CaseHelper.MIKE_JACKSON);

        //should fail
        partyService.addCaseParty(caseId, SENDER_ROLE, TEST_PERSON_CONTACT_EMAIL, TEST_ORG_CONTACT_EMAIL);
    }

    @Test
    public void shouldCreatePartyAddPersonContact() throws Exception {
        String caseId = caseService.getCaseId(caseNodeRef);

        partyService.addCaseParty(caseId, SENDER_ROLE, TEST_PERSON_CONTACT_EMAIL);

        List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(
                getCasePartyGroupNodeRef(caseNodeRef, SENDER_ROLE).getNodeRef(),
                Sets.newHashSet(OpenESDHModel.TYPE_CONTACT_PERSON));

        Assert.assertFalse("Created party shouldn't be empty", childAssocs.isEmpty());

        List<NodeRef> resultContacts = childAssocs.stream().map(assoc -> assoc.getChildRef())
                .collect(Collectors.toList());

        Assert.assertTrue("Created party should contain added test person contact",
                resultContacts.contains(testPersonContact));
    }

    @Test
    public void shouldCreatePartyWithContactsAndGetContactsByRole() throws Exception {
        String caseId = caseService.getCaseId(caseNodeRef);
        partyService.addCaseParty(caseId, RECEIVER_ROLE, TEST_PERSON_CONTACT_EMAIL, TEST_ORG_CONTACT_EMAIL);

        Map<String, List<NodeRef>> contactsByRoleMap = partyService.getCaseParties(caseId);
        Assert.assertNotNull("The retrieved contacts by role map cannot be null", contactsByRoleMap);

        Assert.assertTrue("The retrieved contacts by role map should contain RECEIVER_ROLE contacts",
                contactsByRoleMap.keySet().contains(RECEIVER_ROLE));

        List<NodeRef> receivers = contactsByRoleMap.get(RECEIVER_ROLE);
        Assert.assertTrue("The retrieved map should contain receiver test person contact",
                receivers.contains(testPersonContact));
        Assert.assertTrue("The retrieved map should contain receiver test organization contact",
                receivers.contains(testOrgContact));
    }

    @Test
    public void shouldCreatePartyWithContactsAndGetContactsByCaseId() throws Exception {
        shouldCreatePartyWithContactsAndGetContactsByCaseId(CaseHelper.ADMIN_USER_NAME, caseNodeRef);
    }

    private void shouldCreatePartyWithContactsAndGetContactsByCaseId(String userName, NodeRef nodeRef) {
        String caseId = caseService.getCaseId(nodeRef);
        partyService.addCaseParty(caseId, RECEIVER_ROLE, TEST_PERSON_CONTACT_EMAIL, TEST_ORG_CONTACT_EMAIL);

        List<String> addedContactEmails = partyService.getCaseParties(caseId)
                .entrySet()
                .stream()
                .flatMap(entry -> entry.getValue().stream())
                .map(contactService::getContactInfo)
                .map(ContactInfo::getEmail)
                .collect(Collectors.toList());
        Assert.assertNotNull(userName + ": The retrieved parties contacts list should not be null", addedContactEmails);

        Assert.assertTrue(userName + ": The retrieved parties contacts should contain person contact email",
                addedContactEmails.contains(TEST_PERSON_CONTACT_EMAIL));
        Assert.assertTrue(userName + ": The retrieved parties contacts should contain organization contact email",
                addedContactEmails.contains(TEST_ORG_CONTACT_EMAIL));
    }

    @Test
    public void shouldCreatePartyThenRemoveParty_byEmail() throws Exception {
        String caseId = caseService.getCaseId(caseNodeRef);
        partyService.addCaseParty(caseId, RECEIVER_ROLE, TEST_PERSON_CONTACT_EMAIL);
        partyService.removeCaseParty(caseId, TEST_PERSON_CONTACT_EMAIL, RECEIVER_ROLE);
        List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(
                getCasePartyGroupNodeRef(caseNodeRef, RECEIVER_ROLE).getNodeRef(),
                Sets.newHashSet(OpenESDHModel.TYPE_CONTACT_PERSON));
        Assert.assertTrue("The party shouldn't contain removed person contact", childAssocs.isEmpty());
    }

    @Test
    public void shouldCreatePartyThenRemoveParty_byNodeRef() throws Exception {
        String caseId = caseService.getCaseId(caseNodeRef);
        partyService.addCaseParty(caseId, RECEIVER_ROLE, testOrgContact.toString());
        partyService.removeCaseParty(caseId, testOrgContact.toString(), RECEIVER_ROLE);
        List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(
                getCasePartyGroupNodeRef(caseNodeRef, RECEIVER_ROLE).getNodeRef(),
                Sets.newHashSet(OpenESDHModel.TYPE_CONTACT_PERSON));
        Assert.assertTrue("The party shouldn't contain removed organization contact", childAssocs.isEmpty());
    }

    @Test
    public void closedCaseShouldHaveUnchangedContacs() throws Exception {
        String caseId = caseService.getCaseId(caseNodeRef);
        //add party
        partyService.addCaseParty(caseId, RECEIVER_ROLE, TEST_PERSON_CONTACT_EMAIL);
        //set property
        String originalCity = "London";
        String changedCity = "Leeds";
        nodeService.setProperty(testPersonContact, OpenESDHModel.PROP_CONTACT_CITY_NAME, originalCity);
        try {
            //lock case
            caseService.changeNodeStatus(caseNodeRef, CaseStatus.CLOSED);
            //change contact property
            nodeService.setProperty(testPersonContact, OpenESDHModel.PROP_CONTACT_CITY_NAME, changedCity);
            //check
            NodeRef versionNodeRef = partyService.getCaseParties(caseId).get(RECEIVER_ROLE).get(0);
            String verEmail = (String) nodeService.getProperty(versionNodeRef, OpenESDHModel.PROP_CONTACT_EMAIL);
            assertEquals("contact has same email", TEST_PERSON_CONTACT_EMAIL, verEmail);
            String verCity = (String) nodeService.getProperty(versionNodeRef, OpenESDHModel.PROP_CONTACT_CITY_NAME);
            assertEquals("contact has original city", originalCity, verCity);
        } finally {
            //unlock case, that it could be deleted
            caseService.changeNodeStatus(caseNodeRef, CaseStatus.ACTIVE);
        }
        //reopened case should have new contacs
        NodeRef nodeRef = partyService.getCaseParties(caseId).get(RECEIVER_ROLE).get(0);
        String email = (String) nodeService.getProperty(nodeRef, OpenESDHModel.PROP_CONTACT_EMAIL);
        assertEquals("contact has same email", TEST_PERSON_CONTACT_EMAIL, email);
        String city = (String) nodeService.getProperty(nodeRef, OpenESDHModel.PROP_CONTACT_CITY_NAME);
        assertEquals("contact has changed city", changedCity, city);
    }

    @Test
    public void caseOwnerCanAddAndRemoveParties() {
        NodeRef userCaseNodeRef = caseTestHelper.createCaseBehaviourOn(
                TEST_CASE_NAME + CaseHelper.ALICE_BEECHER,
                caseService.getCasesRootNodeRef(),
                CaseHelper.ALICE_BEECHER);
        casesToClean.add(userCaseNodeRef);
        AuthenticationUtil.setFullyAuthenticatedUser(CaseHelper.ALICE_BEECHER);
        shouldCreatePartyWithContactsAndGetContactsByCaseId(CaseHelper.ALICE_BEECHER, userCaseNodeRef);

        String userCaseId = caseService.getCaseId(userCaseNodeRef);
        partyService.removeCaseParty(userCaseId, testPersonContact.toString(), RECEIVER_ROLE);
        partyService.removeCaseParty(userCaseId, testOrgContact.toString(), RECEIVER_ROLE);
    }

    @Test(expected = AccessDeniedException.class)
    public void shouldNotRemovePartyFromClosedCase() throws Exception {
        String caseId = caseService.getCaseId(caseNodeRef);
        partyService.addCaseParty(caseId, RECEIVER_ROLE, TEST_PERSON_CONTACT_EMAIL);
        try {
            //lock case
            caseService.changeNodeStatus(caseNodeRef, CaseStatus.CLOSED);
            //try to remove party
            partyService.removeCaseParty(caseId, TEST_PERSON_CONTACT_EMAIL, RECEIVER_ROLE);
            //
            fail("Party removal must fail");
        } finally {
            //unlock case, that it could be deleted
            caseService.changeNodeStatus(caseNodeRef, CaseStatus.ACTIVE);
        }
    }

    @Test
    public void shouldPreventNamePropertyDuplicatesForContacts() {
        transactionRunner.runInTransactionAsAdmin(() -> {
            nodeService.setProperty(testPersonContact, OpenESDHModel.PROP_CONTACT_EMAIL,
                    TEST_PERSON2_CONTACT_EMAIL);
            testPersonContact2 = contactService.createContact(TEST_PERSON_CONTACT_EMAIL, ContactType.PERSON.name(),
                    createContactProperties(TEST_PERSON_CONTACT_EMAIL));

            return null;
        });
        String caseId = caseService.getCaseId(caseNodeRef);
        partyService.addCaseParty(caseId, RECEIVER_ROLE, TEST_PERSON2_CONTACT_EMAIL);
        partyService.addCaseParty(caseId, RECEIVER_ROLE, TEST_PERSON_CONTACT_EMAIL);
        Assert.assertTrue("Successfully accomplished test", true);
    }
}

