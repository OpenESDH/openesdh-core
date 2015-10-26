package dk.openesdh.repo.services.xsearch;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.junit.After;
import org.junit.Assert;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.tradeshift.test.remote.Remote;
import com.tradeshift.test.remote.RemoteTestRunner;

import dk.openesdh.repo.helper.CaseHelper;
import dk.openesdh.repo.model.ContactType;
import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.contacts.ContactService;

@RunWith(RemoteTestRunner.class)
@Remote(runnerClass = SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:alfresco/application-context.xml")
public class ContactSearchServiceImplIT {

    private static final String SEARCH_TERM = "testOrg";
    private List<NodeRef> organizationContacts;

    @Autowired
    @Qualifier("ContactService")
    protected ContactService contactService;
    @Autowired
    @Qualifier("NodeService")
    private NodeService nodeService;
    @Autowired
    @Qualifier("TransactionService")
    private TransactionService transactionService;
    @Autowired
    @Qualifier("ContactSearchService")
    protected ContactSearchService contactSearchService;

    @Before
    public void setUp() throws Exception {
        AuthenticationUtil.setFullyAuthenticatedUser(CaseHelper.ADMIN_USER_NAME);
        organizationContacts = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            organizationContacts.add(createContact(i));
        }
    }

    private NodeRef createContact(int index) {
        String email = SEARCH_TERM + index + "@test.openesdh.org";
        HashMap<QName, Serializable> typeProps = new HashMap<>();
        typeProps.put(OpenESDHModel.PROP_CONTACT_ORGANIZATION_NAME, SEARCH_TERM + index);
        typeProps.put(OpenESDHModel.PROP_CONTACT_EMAIL, email);
        typeProps.put(OpenESDHModel.PROP_CONTACT_CVR_NUMBER, "1000000" + index);
        return transactionService.getRetryingTransactionHelper().doInTransaction(
                () -> contactService.createContact(email, ContactType.ORGANIZATION.name(), typeProps));
    }

    @After
    public void tearDown() throws Exception {
        for (NodeRef organizationContact : organizationContacts) {
            nodeService.deleteNode(organizationContact);
        }
    }

    @Test
    public void testGetNodes() throws InterruptedException {
        Map<String, String> params = new HashMap<>();
        params.put("baseType", "contact:organization");
        params.put("term", SEARCH_TERM);
        XResultSet nodes = null;

        //we have to wait until the search will return the contacts
        int sleepCount = 0;
        int maxSleepCount = 120;
        do {
            sleepCount++;
            if (sleepCount > maxSleepCount) {
                fail("Could not find created contacs in reasonable time");
            } else {
                Thread.sleep(1000);
            }
            nodes = contactSearchService.getNodes(params, 0, 50, null, true);
        } while (nodes.getLength() != organizationContacts.size());

        testPagingWith(params, 0, 2);
        testPagingWith(params, 1, 4);
        testPagingWith(params, 2, 3);
        testPagingWith(params, 0, organizationContacts.size());
    }

    private void testPagingWith(Map<String, String> params, int firstIndex, int pageSize) {
        XResultSet nodes;
        nodes = contactSearchService.getNodes(params, firstIndex, pageSize, null, true);
        Assert.assertEquals("Page size should be equal to items count", pageSize, nodes.getNodeRefs().size());
        Assert.assertEquals("Total count should stay the same", organizationContacts.size(), nodes.getNumberFound());
        Assert.assertEquals("First item should match created",
                getContactIdAndEmail(organizationContacts.get(firstIndex)),
                getContactIdAndEmail(nodes.getNodeRefs().get(0)));
    }

    private String getContactIdAndEmail(NodeRef nodeRef) throws InvalidNodeRefException {
        return nodeRef.getId() + "|" + nodeService.getProperty(nodeRef, OpenESDHModel.PROP_CONTACT_EMAIL);
    }

}
