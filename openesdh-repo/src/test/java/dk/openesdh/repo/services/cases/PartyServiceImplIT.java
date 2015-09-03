package dk.openesdh.repo.services.cases;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import dk.openesdh.exceptions.contacts.NoSuchContactException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.namespace.DynamicNamespacePrefixResolver;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.tradeshift.test.remote.Remote;
import com.tradeshift.test.remote.RemoteTestRunner;

import dk.openesdh.repo.helper.CaseDocumentTestHelper;
import dk.openesdh.repo.helper.CaseHelper;
import dk.openesdh.repo.model.ContactInfo;
import dk.openesdh.repo.model.ContactType;
import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.contacts.ContactServiceImpl;

@RunWith(RemoteTestRunner.class)
@Remote(runnerClass = SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:alfresco/application-context.xml")
public class PartyServiceImplIT {

    //<editor-fold desc="injected required services">
    @Autowired
    @Qualifier("NodeService")
    private NodeService nodeService;

    @Autowired
    @Qualifier("AuthorityService")
    private AuthorityService authorityService;

    @Autowired
    @Qualifier("ContactService")
    private ContactServiceImpl contactService = null;

    @Autowired
    @Qualifier("CaseService")
    private CaseServiceImpl caseService = null;

    @Autowired
    @Qualifier("DictionaryService")
    private DictionaryService dictionaryService;

    @Autowired
    @Qualifier("transactionService")
    protected TransactionService transactionService;

    @Autowired
    @Qualifier("CaseDocumentTestHelper")
    private CaseDocumentTestHelper caseTestHelper;
    //</editor-fold>

    //<editor-fold desc="Global variables">
    private static Logger logger = Logger.getLogger(PartyServiceImplIT.class);
    private static final String ADMIN_USER_NAME = "admin";
    private static final String ABEECHER = "abeecher";
    private static final String MJACKSON = "mjackson";
    private static final String TEST_CASE_NAME = "Test_case";
    private static final String SENDER_ROLE = "Afsender";
    private static final String RECEIVER_ROLE = "Modtager";
    private static final String TEST_PERSON_CONTACT_EMAIL = "person@openesdh.org";
    private static final String TEST_ORG_CONTACT_EMAIL = "org@openesdh.org";

    private PartyServiceImpl partyService = null;
    private NodeRef caseNodeRef;
    private NodeRef partyGroupNodeRef;
    private NodeRef testPersonContact;
    private NodeRef testOrgContact;
    private DynamicNamespacePrefixResolver namespacePrefixResolver = new DynamicNamespacePrefixResolver(null);
//    private static final ApplicationContext APPLICATION_CONTEXT = ApplicationContextHelper.getApplicationContext(new String[]{"classpath:alfresco/application-context.xml"});
    //</editor-fold>

    @Before
    public void setUp() throws Exception {
        AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER_NAME);

        //<editor-fold desc="Services that are needed">
        partyService = new PartyServiceImpl();
        partyService.setNodeService(nodeService);
        partyService.setCaseService(caseService);
        partyService.setContactService(contactService);
        partyService.setAuthorityService(authorityService);
        partyService.setDictionaryService(dictionaryService);
        partyService.setNamespacePrefixResolver(namespacePrefixResolver);
        //</editor-fold>

        namespacePrefixResolver.registerNamespace(NamespaceService.APP_MODEL_PREFIX, NamespaceService.APP_MODEL_1_0_URI);
        namespacePrefixResolver.registerNamespace(OpenESDHModel.CONTACT_PREFIX, OpenESDHModel.CONTACT_URI);
        namespacePrefixResolver.registerNamespace(NamespaceService.CONTENT_MODEL_PREFIX, NamespaceService.CONTENT_MODEL_1_0_URI);

        caseNodeRef = caseTestHelper.createCaseBehaviourOn(TEST_CASE_NAME, caseService.getCasesRootNodeRef(), CaseHelper.ADMIN_USER_NAME);

        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>() {
            @Override
            public Void execute() throws Throwable {
                AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER_NAME);
                HashMap<QName, Serializable> personProps = new HashMap<QName, Serializable>();
                personProps.put(OpenESDHModel.PROP_CONTACT_EMAIL, TEST_PERSON_CONTACT_EMAIL);
                testPersonContact = contactService.createContact(TEST_PERSON_CONTACT_EMAIL, ContactType.PERSON.name(), personProps);

                HashMap<QName, Serializable> orgProps = new HashMap<QName, Serializable>();
                orgProps.put(OpenESDHModel.PROP_CONTACT_EMAIL, TEST_ORG_CONTACT_EMAIL);
                testOrgContact = contactService.createContact(TEST_ORG_CONTACT_EMAIL, ContactType.ORGANIZATION.name(), orgProps);
                return null;
            }
        });
        //we have to wait until the search will return the contact
        NodeRef contactNodeRef = null;
        int sleepCount = 0;
        int maxSleepCount = 120;
        do {
            try {
                contactNodeRef = contactService.getContactById(TEST_PERSON_CONTACT_EMAIL);
            } catch (NoSuchContactException e) {
                sleepCount++;
                if (sleepCount > maxSleepCount) {
                    throw e;
                } else {
                    Thread.sleep(1000);
                }
            }

        }
        while (contactNodeRef == null);


    }

    @After
    public void tearDown() throws Exception {
        AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER_NAME);
        ArrayList<NodeRef> nodes = new ArrayList<NodeRef>();
        if (partyGroupNodeRef != null) {
            nodes.add(partyGroupNodeRef);
        }

        if (testPersonContact != null) {
            nodes.add(testPersonContact);
        }

        if (testOrgContact != null) {
            nodes.add(testOrgContact);
        }

        List<NodeRef> cases = new ArrayList<NodeRef>();
        if (caseNodeRef != null) {
            cases.add(caseNodeRef);
        }
        caseTestHelper.removeNodesAndDeleteUsersInTransaction(nodes, cases, new ArrayList<String>());

        //we have to wait until the search NO LONGER return the contact
        NodeRef contactNodeRef = null;
        int sleepCount = 0;
        int maxSleepCount = 120;
        do {
            try {
                contactNodeRef = contactService.getContactById(TEST_PERSON_CONTACT_EMAIL);
                sleepCount++;
                if (sleepCount > maxSleepCount) {
                    throw new RuntimeException("Giving up wating on search to pick up on that contact was deleted.");
                }
                Thread.sleep(1000);
            } catch (NoSuchContactException e) {
                break;
            }
        }
        while (true);


    }

    @Test
    public void shouldCreateSenderPartyGroup() throws Exception {

        String caseId = caseService.getCaseId(caseNodeRef);
        createPartyAssertNotNUll(caseId, SENDER_ROLE);

        String dbid = this.nodeService.getProperty(caseNodeRef, ContentModel.PROP_NODE_DBID).toString();
        String expectedPartyGroupName = AuthorityType.GROUP.getPrefixString() + PartyService.PARTY_PREFIX
                + dbid + "_" + SENDER_ROLE;

        String resultPartyGroupName = (String) nodeService.getProperty(partyGroupNodeRef,
                ContentModel.PROP_AUTHORITY_NAME);

        Assert.assertEquals("Created party group name differs from expected", expectedPartyGroupName,
                resultPartyGroupName);
    }

    @Test
    public void shouldCreateReceiverPartyWithPersonAndOrgContacts() throws Exception {

        String caseId = caseService.getCaseId(caseNodeRef);
        partyGroupNodeRef = partyService.createParty(caseId, RECEIVER_ROLE,
                Arrays.asList(TEST_PERSON_CONTACT_EMAIL, TEST_ORG_CONTACT_EMAIL));
        Assert.assertNotNull("The nodeRef of the created party group cannot be null", partyGroupNodeRef);

        List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(partyGroupNodeRef, new HashSet<QName>(
                Arrays.asList(OpenESDHModel.TYPE_CONTACT_PERSON, OpenESDHModel.TYPE_CONTACT_ORGANIZATION)));

        Assert.assertFalse("Created party shouldn't be empty", childAssocs.isEmpty());

        List<NodeRef> resultContacts = childAssocs.stream().map(assoc -> assoc.getChildRef())
                .collect(Collectors.toList());

        Assert.assertTrue("Created party should contain test person contact",
                resultContacts.contains(testPersonContact));

        Assert.assertTrue("Created party should contain test organization contact",
                resultContacts.contains(testOrgContact));
    }

    @Test
    public void shouldCreatePartyAddPersonAndOrgContacts() throws Exception {
        String caseId = caseService.getCaseId(caseNodeRef);
        createPartyAssertNotNUll(caseId, SENDER_ROLE);

        boolean result = partyService.addContactsToParty(caseId, partyGroupNodeRef, SENDER_ROLE,
                Arrays.asList(TEST_PERSON_CONTACT_EMAIL, TEST_ORG_CONTACT_EMAIL));

        Assert.assertTrue("Should return true if successfully added contacts to the party", result);

        List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(partyGroupNodeRef, new HashSet<QName>(
                Arrays.asList(OpenESDHModel.TYPE_CONTACT_PERSON, OpenESDHModel.TYPE_CONTACT_ORGANIZATION)));

        Assert.assertFalse("Created party shouldn't be empty", childAssocs.isEmpty());

        List<NodeRef> resultContacts = childAssocs.stream().map(assoc -> assoc.getChildRef())
                .collect(Collectors.toList());

        Assert.assertTrue("Created party should contain added test person contact",
                resultContacts.contains(testPersonContact));

        Assert.assertTrue("Created party should contain added test organization contact",
                resultContacts.contains(testOrgContact));
    }

    @Test
    public void shouldCreatePartyAddPersonContact() throws Exception {
        String caseId = caseService.getCaseId(caseNodeRef);
        createPartyAssertNotNUll(caseId, SENDER_ROLE);

        boolean result = partyService.addContactToParty(caseId, partyGroupNodeRef, SENDER_ROLE,
                TEST_PERSON_CONTACT_EMAIL);
        Assert.assertTrue("Should return true if successfully added a contact to the party", result);

        List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(partyGroupNodeRef, new HashSet<QName>(
                Arrays.asList(OpenESDHModel.TYPE_CONTACT_PERSON)));

        Assert.assertFalse("Created party shouldn't be empty", childAssocs.isEmpty());

        List<NodeRef> resultContacts = childAssocs.stream().map(assoc -> assoc.getChildRef())
                .collect(Collectors.toList());

        Assert.assertTrue("Created party should contain added test person contact",
                resultContacts.contains(testPersonContact));
    }

    @Test
    public void shouldCreatePartyAndRetrieveVia_getCaseParty() throws Exception {
        String caseId = caseService.getCaseId(caseNodeRef);
        createPartyAssertNotNUll(caseId, RECEIVER_ROLE);

        NodeRef resultPartyGroupNodeRef = partyService.getCaseParty(caseNodeRef, caseId, RECEIVER_ROLE);
        Assert.assertEquals("The retrieved party is not equal to the created party", partyGroupNodeRef,
                resultPartyGroupNodeRef);
    }

    @Test
    public void shouldCreatePartyWithContactsAndGetContactsByRole() throws Exception {
        String caseId = caseService.getCaseId(caseNodeRef);
        partyGroupNodeRef = partyService.createParty(caseId, RECEIVER_ROLE,
                Arrays.asList(TEST_PERSON_CONTACT_EMAIL, TEST_ORG_CONTACT_EMAIL));
        Assert.assertNotNull("The nodeRef of the created party group cannot be null", partyGroupNodeRef);

        Map<String, Set<String>> contactsByRoleMap = partyService.getContactsByRole(caseId);
        Assert.assertNotNull("The retrieved contacts by role map cannot be null", contactsByRoleMap);

        Assert.assertTrue("The retrieved contacts by role map should contain RECEIVER_ROLE contacts",
                contactsByRoleMap.keySet().contains(RECEIVER_ROLE));

        Set<String> receivers = contactsByRoleMap.get(RECEIVER_ROLE);
        Assert.assertTrue("The retrieved map should contain receiver test person contact",
                receivers.contains(TEST_PERSON_CONTACT_EMAIL));
        Assert.assertTrue("The retrieved map should contain receiver test organization contact",
                receivers.contains(TEST_ORG_CONTACT_EMAIL));
    }

    @Test
    public void shouldCreatePartyWithContactsAndGetContactsByCaseId() throws Exception {
        String caseId = caseService.getCaseId(caseNodeRef);
        partyGroupNodeRef = partyService.createParty(caseId, RECEIVER_ROLE,
                Arrays.asList(TEST_PERSON_CONTACT_EMAIL, TEST_ORG_CONTACT_EMAIL));
        Assert.assertNotNull("The nodeRef of the created party group cannot be null", partyGroupNodeRef);

        List<ContactInfo> addedContacts = partyService.getPartiesInCase(caseId);
        Assert.assertNotNull("The retrieved parties contacts list should not be null", addedContacts);

        List<String> resultContactsEmails = addedContacts.stream().map(contact -> contact.getEmail())
                .collect(Collectors.toList());
        Assert.assertTrue("The retrieved parties contacts should contain person contact email",
                resultContactsEmails.contains(TEST_PERSON_CONTACT_EMAIL));
        Assert.assertTrue("The retrieved parties contacts should contain organization contact email",
                resultContactsEmails.contains(TEST_ORG_CONTACT_EMAIL));
    }

    @Test
    public void shouldCreatePartyThenRemoveParty() throws Exception {
        String caseId = caseService.getCaseId(caseNodeRef);
        partyGroupNodeRef = partyService.createParty(caseId, RECEIVER_ROLE,
                Arrays.asList(TEST_PERSON_CONTACT_EMAIL));
        Assert.assertNotNull("The nodeRef of the created party group cannot be null", partyGroupNodeRef);

        boolean result = partyService.removePartyRole(caseId, TEST_PERSON_CONTACT_EMAIL, RECEIVER_ROLE);
        Assert.assertTrue("The removePartyRole method should return true if successfully removed party", result);

        List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(partyGroupNodeRef, new HashSet<QName>(
                Arrays.asList(OpenESDHModel.TYPE_CONTACT_PERSON)));

        Assert.assertTrue("The party shouldn't contain removed person contact", childAssocs.isEmpty());

    }

    private void createPartyAssertNotNUll(String caseId, String role) {
        partyGroupNodeRef = partyService.createParty(caseId, role);
        Assert.assertNotNull("The nodeRef of the created party group cannot be null", partyGroupNodeRef);
    }

}