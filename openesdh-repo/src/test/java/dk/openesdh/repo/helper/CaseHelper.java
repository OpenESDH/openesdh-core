package dk.openesdh.repo.helper;

import dk.openesdh.repo.services.TransactionRunner;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import dk.openesdh.simplecase.model.SimpleCaseModel;
import dk.openesdh.repo.model.CaseStatus;
import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.cases.CasePermission;
import dk.openesdh.repo.services.cases.CaseService;

/**
 * Created by ole on 18/08/14.
 */
@Service("TestCaseHelper")
public class CaseHelper {

    public static final String ADMIN_USER_NAME = "admin";
    public final static String DEFAULT_USERNAME = "username12";

    @Autowired
    @Qualifier("NodeService")
    private NodeService nodeService;
    @Autowired
    private TransactionRunner transactionRunner;
    @Autowired
    @Qualifier("personService")
    private PersonService personService;
    @Autowired
    @Qualifier("policyBehaviourFilter")
    private BehaviourFilter behaviourFilter;
    @Autowired
    @Qualifier("CaseService")
    private CaseService caseService;

    public static final String MIKE_JACKSON = "mjackson";
    public static final String ALICE_BEECHER = "abeecher";

    public static final String CASE_TYPE = "Simple";
    public static final String CASE_SIMPLE_WRITER_ROLE = CasePermission.WRITER.getFullName(CASE_TYPE);
    public static final String CASE_SIMPLE_READER_ROLE = CasePermission.READER.getFullName(CASE_TYPE);
    public static final String CASE_SIMPLE_CREATOR_ROLE = CasePermission.CREATOR.getFullName(CASE_TYPE);
    public static final String CASE_SIMPLE_OWNER_ROLE = CasePermission.OWNER.getFullName(CASE_TYPE);

    public static final String CASE_SIMPLE_CREATOR_GROUP = "GROUP_" + CASE_SIMPLE_CREATOR_ROLE;

    /**
     * Create a case. If disableBehaviour is true, transaction is run with behaviours disabled. when creating the case.
     *
     * @param parent
     * @param name
     * @param caseType
     * @param properties
     * @param owners
     * @param disableBehaviour
     * @return
     */
    public NodeRef createCase(final NodeRef parent, final String name, final QName caseType,
            final Map<QName, Serializable> properties, final List<NodeRef> owners, boolean disableBehaviour) {
        ChildAssociationRef assocRef = createCaseNode(parent, name, caseType, properties, owners, disableBehaviour);
        return assocRef.getChildRef();
    }

    private ChildAssociationRef createCaseNode(final NodeRef parent, final String name,
            final QName caseType, final Map<QName, Serializable> properties,
            final List<NodeRef> owners, final boolean disableBehaviour) {
        // We have to do in a transaction because we must set the case:owner
        // association before commit, to avoid an integrity error.
        return transactionRunner.runInTransactionAsAdmin(() -> {
            if (disableBehaviour) {
                // Disable behaviour for txn
                behaviourFilter.disableBehaviour();
            }
            properties.put(ContentModel.PROP_NAME, name);
            properties.put(OpenESDHModel.PROP_OE_STATUS, CaseStatus.ACTIVE);

            // Create test case
            ChildAssociationRef childAssoc = nodeService.createNode(
                    parent,
                    ContentModel.ASSOC_CONTAINS,
                    QName.createQName(OpenESDHModel.CASE_URI, name),
                    caseType,
                    properties
            );

            nodeService.setAssociations(childAssoc.getChildRef(), OpenESDHModel.ASSOC_CASE_OWNERS, owners);
            if (disableBehaviour) {
                // Re-enable behaviour
                behaviourFilter.enableBehaviour();
            }
            return childAssoc;
        });
    }

    /**
     * Create a case without disabling the behaviour.
     *
     * @param parent
     * @param cmName
     * @param caseType
     * @param properties
     * @param owners
     * @return
     */
    public NodeRef createCase(NodeRef parent, String cmName, QName caseType,
            Map<QName, Serializable> properties, List<NodeRef> owners) {
        return createCase(parent, cmName, caseType, properties, owners, false);
    }

    public NodeRef createSimpleCase(String title, NodeRef owner) {
        NodeRef casesRootNode = this.caseService.getCasesRootNodeRef();
        Map<QName, Serializable> properties = new HashMap<>();
        properties.put(ContentModel.PROP_TITLE, title);
        List<NodeRef> owners = new LinkedList<>();
        owners.add(owner);
        ChildAssociationRef assocRef = this.createCaseNode(casesRootNode, title, SimpleCaseModel.TYPE_CASE_SIMPLE, properties, owners, false);
        return assocRef.getChildRef();
    }

    public NodeRef createDummyUser(String userName, String firstName, String lastName, String email, String company) {
        if (personService.personExists(userName)) {
            personService.deletePerson(userName);
        }
        HashMap<QName, Serializable> properties = new HashMap<>();
        properties.put(ContentModel.PROP_USERNAME, userName);
        properties.put(ContentModel.PROP_FIRSTNAME, firstName);
        properties.put(ContentModel.PROP_LASTNAME, lastName);
        properties.put(ContentModel.PROP_EMAIL, email);
        properties.put(ContentModel.PROP_ORGANIZATION, company);
        properties.put(ContentModel.PROP_PASSWORD, "password");
        properties.put(ContentModel.PROP_ACCOUNT_LOCKED, false);
        properties.put(ContentModel.PROP_ENABLED, true);
        properties.put(ContentModel.PROP_SIZE_QUOTA, -1);
        return personService.createPerson(properties);
    }

    public NodeRef createDummyUser() {
        return createDummyUser(CaseHelper.DEFAULT_USERNAME);
    }

    public NodeRef createDummyUser(final String userName) {
        return transactionRunner.runInTransactionAsAdmin(() -> {
            return createDummyUser(userName,
                    "firstname",
                    "lastname",
                    RandomStringUtils.random(5) + "@example.dk",
                    "company");
        });
    }

    public void deleteDummyUser() {
        deleteDummyUser(DEFAULT_USERNAME);
    }

    public void deleteDummyUser(String userName) {
        personService.deletePerson(userName);
    }

    public NodeRef getDummyUser(String userName) {
        if (personService.personExists(userName)) {
            return personService.getPerson(userName);
        }
        return createDummyUser(userName);
    }
}
