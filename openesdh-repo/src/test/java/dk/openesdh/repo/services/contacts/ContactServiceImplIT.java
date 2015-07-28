package dk.openesdh.repo.services.contacts;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ibm.icu.text.MessageFormat;
import com.tradeshift.test.remote.Remote;
import com.tradeshift.test.remote.RemoteTestRunner;

import dk.openesdh.repo.helper.CaseHelper;
import dk.openesdh.repo.model.ContactInfo;
import dk.openesdh.repo.model.ContactType;
import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.xsearch.ContactSearchService;

@RunWith(RemoteTestRunner.class)
@Remote(runnerClass = SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:alfresco/application-context.xml" })
public class ContactServiceImplIT {

    @Autowired
    @Qualifier("NodeService")
    private NodeService nodeService;

    @Autowired
    @Qualifier("TransactionService")
    private TransactionService transactionService;

    @Autowired
    @Qualifier("SearchService")
    private SearchService searchService;

    @Autowired
    @Qualifier("ContactSearchService")
    private ContactSearchService contactSearchService;

    @Autowired
    @Qualifier("ContactDAO")
    private ContactDAOImpl contactDao;

    private static final String TEST_PERSON_CONTACT_EMAIL = "person@openesdh.org";
    private static final String TEST_PERSON_CONTACT_FIRST_NAME = "Name";
    private static final String TEST_PERSON_CONTACT_LAST_NAME = "LastName";
    private static final String TEST_PERSON_CONTACT_MIDDLE_NAME = "MiddleName";
    private static final String TEST_PERSON_CONTACT_CPR_NUMBER = "1234567890";

    private static final String TEST_ORG_CONTACT_EMAIL = "org@openesdh.org";
    private static final String TEST_ORG_CONTACT_NAME = "OrgName";
    private static final String TEST_ORG_CONTACT_CVR_NUMBER = "12345678";

    private ContactServiceImpl contactService;
    private NodeRef testContactNodeRef;

    @Before
    public void setUp() {
        AuthenticationUtil.setFullyAuthenticatedUser(CaseHelper.ADMIN_USER_NAME);
        contactService = new ContactServiceImpl();
        contactService.setContactDAO(contactDao);
        contactService.setContactSearchService(contactSearchService);
        contactService.setNodeService(nodeService);
        contactService.setSearchService(searchService);

    }

    @After
    public void tearDown() {
        AuthenticationUtil.setFullyAuthenticatedUser(CaseHelper.ADMIN_USER_NAME);
        if (testContactNodeRef != null) {
            nodeService.deleteNode(testContactNodeRef);
        }
    }

    private void createContactAssertNotNullCheckEmail(String email, ContactType contactType) {
        HashMap<QName, Serializable> typeProps = createPersonContactProps();
        testContactNodeRef = transactionHelper().doInTransaction(
                () -> contactService.createContact(email, contactType.name(), typeProps));
        Assert.assertNotNull("A node ref of the created contact should not be null", testContactNodeRef);

        String createdContactEmail = nodeService.getProperty(testContactNodeRef, OpenESDHModel.PROP_CONTACT_EMAIL)
                .toString();
        Assert.assertEquals("The email of the created contact doesn't match the test contact email", email,
                createdContactEmail);
    }

    @Test
    public void shouldCreatePersonContactWithAllProps() {
        HashMap<QName, Serializable> typeProps = createPersonContactProps();
        
        testContactNodeRef = transactionHelper().doInTransaction(
                () -> contactService.createContact(TEST_PERSON_CONTACT_EMAIL, ContactType.PERSON.name(), typeProps));
        Assert.assertNotNull("A node ref of the created contact should not be null", testContactNodeRef);

        Map<QName, Serializable> resultProps = nodeService.getProperties(testContactNodeRef);

        typeProps.keySet().stream().forEach(key -> Assert.assertEquals(
                "The " + key.getLocalName() + " property value of the created contact doesn't match provided", typeProps.get(key), resultProps.get(key)));
    }

    @Test
    public void shouldCreateOrgContactWithAllProps() {
        HashMap<QName, Serializable> typeProps = createOrgContactProps();
        testContactNodeRef = transactionHelper().doInTransaction(
                () -> contactService.createContact(TEST_ORG_CONTACT_EMAIL, ContactType.ORGANIZATION.name(), typeProps));
        Assert.assertNotNull("A node ref of the created contact should not be null", testContactNodeRef);

        Map<QName, Serializable> resultProps = nodeService.getProperties(testContactNodeRef);
        
        typeProps.keySet().stream().forEach(key -> Assert.assertEquals(
                "The " + key.getLocalName() + " property value of the created contact doesn't match provided", typeProps.get(key), resultProps.get(key)));
    }

    @Test
    public void shouldCreatePersonContactThenGetContactInfo() {
        HashMap<QName, Serializable> typeProps = createPersonContactProps();
        testContactNodeRef = transactionHelper().doInTransaction(
                () -> contactService.createContact(TEST_PERSON_CONTACT_EMAIL, ContactType.PERSON.name(), typeProps));
        Assert.assertNotNull("A node ref of the created contact should not be null", testContactNodeRef);
        
        ContactInfo contactInfo = contactService.getContactInfo(testContactNodeRef);

        Assert.assertEquals(wrongPropValueMessage(OpenESDHModel.PROP_CONTACT_EMAIL),
                TEST_PERSON_CONTACT_EMAIL, contactInfo.getEmail());

        Assert.assertEquals(
                wrongPropValueMessage(OpenESDHModel.PROP_CONTACT_FIRST_NAME, OpenESDHModel.PROP_CONTACT_MIDDLE_NAME, OpenESDHModel.PROP_CONTACT_LAST_NAME),
                TEST_PERSON_CONTACT_FIRST_NAME + " " + TEST_PERSON_CONTACT_MIDDLE_NAME + " " + TEST_PERSON_CONTACT_LAST_NAME, contactInfo.getName()
        );

        Assert.assertEquals(wrongPropValueMessage(OpenESDHModel.PROP_CONTACT_CPR_NUMBER),
                TEST_PERSON_CONTACT_CPR_NUMBER, contactInfo.getIDNumebr());
    }

    @Test
    public void shouldCreateOrgContactThenGetContactInfo() {
        HashMap<QName, Serializable> typeProps = createOrgContactProps();
        testContactNodeRef = transactionHelper().doInTransaction(
                () -> contactService.createContact(TEST_ORG_CONTACT_EMAIL, ContactType.ORGANIZATION.name(),
                        typeProps));
        Assert.assertNotNull("A node ref of the created contact should not be null", testContactNodeRef);

        ContactInfo contactInfo = contactService.getContactInfo(testContactNodeRef);

        Assert.assertEquals(wrongPropValueMessage(OpenESDHModel.PROP_CONTACT_EMAIL), TEST_ORG_CONTACT_EMAIL,
                contactInfo.getEmail());

        Assert.assertEquals(wrongPropValueMessage(OpenESDHModel.PROP_CONTACT_ORGANIZATION_NAME),
                TEST_ORG_CONTACT_NAME, contactInfo.getName());

        Assert.assertEquals(wrongPropValueMessage(OpenESDHModel.PROP_CONTACT_CVR_NUMBER),
                TEST_ORG_CONTACT_CVR_NUMBER, contactInfo.getIDNumebr());
    }

    @Test
    public void shouldCreatePersonContactAndGetContactType(){
        HashMap<QName, Serializable> typeProps = createPersonContactProps();
        testContactNodeRef = transactionHelper().doInTransaction(
                () -> contactService.createContact(TEST_PERSON_CONTACT_EMAIL, ContactType.PERSON.name(), typeProps));
        Assert.assertNotNull("A node ref of the created contact should not be null", testContactNodeRef);
        
        ContactType resultContactType = contactService.getContactType(testContactNodeRef);
        Assert.assertEquals("Wrong contact type of the created person contact", ContactType.PERSON, resultContactType);
    }
    
    @Test
    public void shouldCreateOrgContactAndGetContactType() {
        HashMap<QName, Serializable> typeProps = createOrgContactProps();
        testContactNodeRef = transactionHelper().doInTransaction(
                () -> contactService.createContact(TEST_ORG_CONTACT_EMAIL, ContactType.ORGANIZATION.name(), typeProps));
        Assert.assertNotNull("A node ref of the created contact should not be null", testContactNodeRef);
        
        ContactType resultContactType = contactService.getContactType(testContactNodeRef);
        Assert.assertEquals("Wrong contact type of the created person contact", ContactType.ORGANIZATION, resultContactType);
    }

    @Test
    public void shouldCreateContactAndGetContactById() {
        createContactAssertNotNullCheckEmail(TEST_PERSON_CONTACT_EMAIL, ContactType.PERSON);
        NodeRef contactNodeRef = contactService.getContactById(TEST_PERSON_CONTACT_EMAIL);
        Assert.assertNotNull("A node ref of the created contact should not be null", contactNodeRef);
    }

    @Test
    public void shouldCreateContactAndGetByFilter() {
        createContactAssertNotNullCheckEmail(TEST_PERSON_CONTACT_EMAIL, ContactType.PERSON);
        List<ContactInfo> resultList = contactService.getContactByFilter(TEST_PERSON_CONTACT_EMAIL,
                ContactType.PERSON.name());
        Assert.assertFalse("The contact list got by filter shouldn't be empty", resultList.isEmpty());

        ContactInfo contactInfo = resultList.get(0);
        Assert.assertEquals(wrongPropValueMessage(OpenESDHModel.PROP_CONTACT_EMAIL), TEST_PERSON_CONTACT_EMAIL,
                contactInfo.getEmail());
    }

    private String wrongPropValueMessage(QName... prop) {
        StringJoiner sj = new StringJoiner(" ");
        Arrays.asList(prop).stream().forEach(propName -> sj.add(propName.getLocalName()));
        return MessageFormat.format(
                "Retrieved \"{0}\" property value of the created contact doesn't match provided",
                new Object[] { sj.toString() });
    }

    private HashMap<QName, Serializable> createPersonContactProps() {
        HashMap<QName, Serializable> typeProps = new HashMap<QName, Serializable>();
        typeProps.put(OpenESDHModel.PROP_CONTACT_EMAIL, TEST_PERSON_CONTACT_EMAIL);
        typeProps.put(OpenESDHModel.PROP_CONTACT_FIRST_NAME, TEST_PERSON_CONTACT_FIRST_NAME);
        typeProps.put(OpenESDHModel.PROP_CONTACT_LAST_NAME, TEST_PERSON_CONTACT_LAST_NAME);
        typeProps.put(OpenESDHModel.PROP_CONTACT_MIDDLE_NAME, TEST_PERSON_CONTACT_MIDDLE_NAME);
        typeProps.put(OpenESDHModel.PROP_CONTACT_CPR_NUMBER, TEST_PERSON_CONTACT_CPR_NUMBER);
        return typeProps;
    }

    private HashMap<QName, Serializable> createOrgContactProps() {
        HashMap<QName, Serializable> typeProps = new HashMap<QName, Serializable>();
        typeProps.put(OpenESDHModel.PROP_CONTACT_ORGANIZATION_NAME, TEST_ORG_CONTACT_NAME);
        typeProps.put(OpenESDHModel.PROP_CONTACT_EMAIL, TEST_ORG_CONTACT_EMAIL);
        typeProps.put(OpenESDHModel.PROP_CONTACT_CVR_NUMBER, TEST_ORG_CONTACT_CVR_NUMBER);
        return typeProps;
    }

    private RetryingTransactionHelper transactionHelper() {
        return transactionService.getRetryingTransactionHelper();
    }
}
