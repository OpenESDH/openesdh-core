package dk.openesdh.repo.helper;

import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.cases.CaseService;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.nodelocator.CompanyHomeNodeLocator;
import org.alfresco.repo.nodelocator.NodeLocatorService;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by ole on 18/08/14.
 */
public class CaseHelper {

    public static final String ADMIN_USER_NAME = "admin";
    public final static String DEFAULT_USERNAME = "username12";

    @Autowired
    @Qualifier("NodeService")
    protected NodeService nodeService;

    @Autowired
    @Qualifier("searchService")
    protected SearchService searchService;
    @Autowired
    @Qualifier("authorityService")
    protected AuthorityService authorityService;
    @Autowired
    @Qualifier("permissionService")
    protected PermissionService permissionService;
    @Autowired
    @Qualifier("repositoryHelper")
    protected Repository repositoryHelper;
    @Autowired
    @Qualifier("retryingTransactionHelper")
    protected RetryingTransactionHelper retryingTransactionHelper;
    @Autowired
    @Qualifier("personService")
    protected PersonService personService;
    @Autowired
    @Qualifier("authenticationService")
    protected AuthenticationService authenticationService;
    @Autowired
    @Qualifier("nodeLocatorService")
    protected NodeLocatorService nodeLocatorService;

    @Autowired
    @Qualifier("transactionService")
    protected TransactionService transactionService;

    @Autowired
    @Qualifier("policyBehaviourFilter")
    private BehaviourFilter behaviourFilter;

    @Autowired
    private CaseService caseService;

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    public void setRepositoryHelper(Repository repositoryHelper) {
        this.repositoryHelper = repositoryHelper;
    }

    public void setRetryingTransactionHelper(RetryingTransactionHelper retryingTransactionHelper) {
        this.retryingTransactionHelper = retryingTransactionHelper;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    public void setNodeLocatorService(NodeLocatorService nodeLocatorService) {
        this.nodeLocatorService = nodeLocatorService;
    }

    /**
     * Create a case. If disableBehaviour is true, transaction is run with
     * behaviours disabled.
     * when creating the case.
     *
     *
     * @param parent
     * @param name
     * @param caseType
     * @param properties
     * @param owners
     * @param disableBehaviour
     * @return
     */
    public NodeRef createCase(String username,
                              final NodeRef parent,
                              final String name,
                              final QName caseType,
                              final Map<QName, Serializable> properties,
                              final List<NodeRef> owners,
                              boolean disableBehaviour) {
        ChildAssociationRef assocRef = createCaseNode(username, parent, name, caseType, properties, owners, disableBehaviour);
        caseService.createCase(assocRef);
        return assocRef.getChildRef();
    }

    private ChildAssociationRef createCaseNode(String username,
                                               final NodeRef parent,
                                               final String name,
                                               final QName caseType,
                                               final Map<QName, Serializable> properties,
                                               final List<NodeRef> owners,
                                               boolean disableBehaviour) {

//        AuthenticationUtil.setFullyAuthenticatedUser(username);
        return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<ChildAssociationRef>() {
            @Override
            public ChildAssociationRef doWork() throws Exception {
                return retryingTransactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<ChildAssociationRef>() {
                    @Override
                    public ChildAssociationRef execute() throws Throwable {
                        if (behaviourFilter != null) {
                            // Disable behaviour for txn
                            behaviourFilter.disableBehaviour();
                        }

                        properties.put(ContentModel.PROP_NAME, name);

                        // Create test case
                        ChildAssociationRef childAssoc = nodeService.createNode(
                                parent,
                                ContentModel.ASSOC_CONTAINS,
                                QName.createQName(OpenESDHModel.CASE_URI, name),
                                caseType,
                                properties
                        );
//                caseService.createCase(childAssoc);

                        nodeService.setAssociations(childAssoc.getChildRef(), OpenESDHModel.ASSOC_CASE_OWNERS, owners);

                        if (behaviourFilter != null) {
                            // Re-enable behaviour
                            behaviourFilter.enableBehaviour();
                        }

                        return childAssoc;
                    }
                });
            }
        }, username);
        // We have to do in a transaction because we must set the case:owner
        // association before commit, to avoid an integrity error.
    }

    /**
     * Create a case without disabling the behaviour.
     *
     *
     * @param username
     * @param parent
     * @param cmName
     * @param caseType
     * @param properties
     * @param owners
     * @return
     */
    public NodeRef createCase(String username, NodeRef parent, String cmName,
                              QName caseType, Map<QName, Serializable> properties,
                              List<NodeRef> owners) {
        return createCase(username, parent, cmName, caseType, properties, owners, false);
    }

    public NodeRef createSimpleCase(String title, String userName, NodeRef owner) {
        NodeRef caseNode;
        NodeRef companyHome = nodeLocatorService.getNode(CompanyHomeNodeLocator.NAME, null, null);
        Map<QName, Serializable> properties = new HashMap<>();
        properties.put(ContentModel.PROP_TITLE, title);
        List<NodeRef> owners = new LinkedList<>();
        owners.add(owner);
        ChildAssociationRef assocRef = this.createCaseNode(userName, companyHome, title, OpenESDHModel.TYPE_CASE_SIMPLE, properties, owners, false);

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

        NodeRef person = personService.createPerson(properties);

//        AuthenticationUtil.setFullyAuthenticatedUser(CaseHelper.DEFAULT_USERNAME);

        return person;
    }

    public NodeRef createDummyUser() {

        NodeRef result = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>(){

            public NodeRef execute() throws Throwable {
               return createDummyUser(CaseHelper.DEFAULT_USERNAME,
                        "firstname",
                        "lastname",
                        "email.email.dk",
                        "company");

            }
        });

        return result;
    }

    public void deleteDummyUser() {
        personService.deletePerson(DEFAULT_USERNAME);
    }

    public void setCaseService(CaseService caseService) {
        this.caseService = caseService;
    }
}
