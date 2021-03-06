package dk.openesdh.repo.services.audit;

import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.namespace.QName;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.After;
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
import dk.openesdh.repo.services.TransactionRunner;
import dk.openesdh.repo.services.cases.CaseService;
import dk.openesdh.repo.services.cases.PartyService;
import dk.openesdh.repo.services.contacts.ContactServiceImpl;
import dk.openesdh.repo.services.contacts.PartyRoleService;
import dk.openesdh.repo.services.members.CaseMembersService;

/**
 * Created by flemming on 18/08/14.
 */
@RunWith(RemoteTestRunner.class)
@Remote(runnerClass = SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:alfresco/application-context.xml",
    "classpath:alfresco/extension/openesdh-test-context.xml"})
public class AuditSearchServiceImplIT {

    @Autowired
    @Qualifier("NodeService")
    private NodeService nodeService;

    @Autowired
    @Qualifier("TestCaseHelper")
    private CaseHelper caseHelper;

    @Autowired
    private TransactionRunner transactionRunner;

    @Autowired
    @Qualifier(CaseService.BEAN_ID)
    private CaseService caseService;

    @Autowired
    @Qualifier("CaseMembersService")
    private CaseMembersService caseMembersService;

    @Autowired
    @Qualifier(PartyService.BEAN_ID)
    private PartyService partyService;

    @Autowired
    @Qualifier("PartyRoleService")
    private PartyRoleService partyRoleService;

    @Autowired
    @Qualifier("ContactService")
    private ContactServiceImpl contactService;

    @Autowired
    @Qualifier("AuthorityService")
    private AuthorityService authorityService;

    @Autowired
    @Qualifier("AuditSearchService")
    private AuditSearchService auditSearchService;

    private static final String CASE_A_TITLE = "caseH" + new Date().getTime();
    private static final String TEST_PERSON_CONTACT_EMAIL = CASE_A_TITLE + "@opene.dk";
    private static final String NAME = "test1";
    private static final String DUMMY_USER = "dummyH" + new Date().getTime();
    private String caseAId;
    private NodeRef caseA = null;
    private NodeRef owner = null;
    private NodeRef contact = null;

    @Before
    public void setUp() throws Exception {
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        owner = caseHelper.createDummyUser(DUMMY_USER);
        caseA = caseHelper.createSimpleCase(CASE_A_TITLE, owner);

        transactionRunner.runInTransaction(() -> {
            caseAId = caseService.getCaseId(caseA);
            contact = createTestContact();
            return null;
        });

        // add party
        NodeRef partyRef = transactionRunner.runInTransaction(() -> {
            return partyService.addCaseParty(caseAId, this.getMemberPartyRole(), TEST_PERSON_CONTACT_EMAIL).get(0);
        });

        transactionRunner.runInTransaction(() -> {
            //remove party
            partyService.removeCaseParty(caseAId, partyRef);

            NodeRef mjacksonNodeRef = authorityService.getAuthorityNodeRef(CaseHelper.MIKE_JACKSON);
            // add member
            caseMembersService.addAuthorityToRole(mjacksonNodeRef, CaseHelper.CASE_SIMPLE_READER_ROLE, caseA);
            // remove member
            caseMembersService.removeAuthorityFromRole(CaseHelper.MIKE_JACKSON,
                    CaseHelper.CASE_SIMPLE_READER_ROLE, caseA);

            return null;
        });
    }

    private NodeRef createTestContact() {
        HashMap<QName, Serializable> personProps = new HashMap<>();
        personProps.put(OpenESDHModel.PROP_CONTACT_EMAIL, TEST_PERSON_CONTACT_EMAIL);
        personProps.put(OpenESDHModel.PROP_CONTACT_FIRST_NAME, NAME);
        personProps.put(OpenESDHModel.PROP_CONTACT_LAST_NAME, NAME);
        return contactService.createContact(TEST_PERSON_CONTACT_EMAIL, ContactType.PERSON.name(), personProps);
    }

    @Test
    public void testAuditLog() throws Exception {
        JSONArray result = auditSearchService.getAuditLogByCaseNodeRef(caseA, 1000);
        Set<String> recordedActions = new HashSet<>();
        result.forEach(item -> {
            String action = (String) ((JSONObject) item).get(AuditEntryHandler.ACTION);
            recordedActions.add(action);
        });
        assertTrue("History should have Case creation record", recordedActions.contains("auditlog.label.case.created"));
        assertTrue("History should have Member add record", recordedActions.contains("auditlog.label.member.added"));
        assertTrue("History should have Member remove record", recordedActions.contains("auditlog.label.member.removed"));
        assertTrue("History should have Party Add record", recordedActions.contains("auditlog.label.party.added"));
        assertTrue("History should have Party Remove record", recordedActions.contains("auditlog.label.party.removed"));
    }

    @After
    public void tearDown() throws Exception {
        transactionRunner.runInTransactionAsAdmin(() -> {
            if (caseA != null) {
                nodeService.deleteNode(caseA);
            }
            if (contact != null) {
                nodeService.deleteNode(contact);
            }
            caseHelper.deleteDummyUser(DUMMY_USER);
            return true;
        });
    }

    private NodeRef getMemberPartyRole() {
        return partyRoleService.getClassifValueByName(PartyRoleService.MEMBER_ROLE).get().getNodeRef();
    }
}
