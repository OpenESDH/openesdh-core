package dk.openesdh.repo.services.cases;

import com.tradeshift.test.remote.Remote;
import com.tradeshift.test.remote.RemoteTestRunner;
import dk.openesdh.SimpleCaseModel;
import dk.openesdh.exceptions.contacts.InvalidContactTypeException;
import dk.openesdh.repo.helper.CaseHelper;
import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.contacts.ContactServiceImpl;
import dk.openesdh.repo.services.documents.DocumentService;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.DynamicNamespacePrefixResolver;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(RemoteTestRunner.class)
@Remote(runnerClass = SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:alfresco/application-context.xml")
@Ignore
public class PartyServiceImplIT {
    private static Logger logger = Logger.getLogger(PartyServiceImplIT.class);

//    private static final ApplicationContext APPLICATION_CONTEXT = ApplicationContextHelper.getApplicationContext(new String[]{"classpath:alfresco/application-context.xml"});

    private static final String ADMIN_USER_NAME = "admin";
    private static final String ABEECHER = "abeecher";
    private static final String MJACKSON = "mjackson";
    private static final String fileName = "Test_case";
    private static final String fileContentTxt = "Mary havde et lille lam";

    @Autowired
    @Qualifier("NodeService")
    protected NodeService nodeService;

    @Autowired
    @Qualifier("SearchService")
    protected SearchService searchService;

    @Autowired
    @Qualifier("AuthorityService")
    protected AuthorityService authorityService;

    @Autowired
    @Qualifier("OwnableService")
    protected OwnableService ownableService;

    @Autowired
    @Qualifier("repositoryHelper")
    protected Repository repositoryHelper;

    @Autowired
    @Qualifier("ContactService")
    private ContactServiceImpl contactService = null;

    @Autowired
    @Qualifier("CaseService")
    private CaseServiceImpl caseService = null;

    @Autowired
    @Qualifier("ContentService")
    protected ContentService contentService;

    @Autowired
    @Qualifier("DocumentService")
    protected DocumentService documentService;

    @Autowired
    @Qualifier("PersonService")
    protected PersonService personService;

    @Autowired
    @Qualifier("TestCaseHelper")
    protected CaseHelper caseHelper;

    private DynamicNamespacePrefixResolver namespacePrefixResolver = new DynamicNamespacePrefixResolver(null);
    private NodeRef dummyUser;
    private NodeRef casesRootNodeRef;
    private PartyServiceImpl partyService = null;
    private NodeRef caseNodeRef;
    private NodeRef companyHomeNodeRef;

    @Before
    public void setUp() throws Exception {
        AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER_NAME);

        dummyUser = caseHelper.createDummyUser();

        getCompanyHomeNodeRef();

        //Services that are needed
        partyService = new PartyServiceImpl();
        partyService.setNodeService(nodeService);
        partyService.setCaseService(caseService);
        partyService.setContactService(contactService);
        partyService.setAuthorityService(authorityService);

        namespacePrefixResolver.registerNamespace(NamespaceService.APP_MODEL_PREFIX, NamespaceService.APP_MODEL_1_0_URI);
        namespacePrefixResolver.registerNamespace(OpenESDHModel.CONTACT_PREFIX, OpenESDHModel.CONTACT_URI);

        /**
         * Create a new temporary case
         */
        //1. Get the cases storage rootNode
        casesRootNodeRef = caseService.getCasesRootNodeRef();

        logger.warn("\n\n===> The case root storage nodeRef: "+ companyHomeNodeRef+ "<===\n\n");
        logger.warn("\n\n===> The case nodeRef: "+ caseNodeRef+ "<===\n\n");

        //2. Create a temp case NodeRef. Cases are created on a behavioural trigger from the creation of a content in the caseFolder storage root.
        NodeRef mainDocNode = createCaseContentNode(repositoryHelper.getCompanyHome(), fileName, fileContentTxt);
        caseNodeRef = documentService.getCaseNodeRef(mainDocNode);

        AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER_NAME);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void createParty() {
        String role = "Sender"; List<String> contacts;
        try {
            String dbid = (String) this.nodeService.getProperty(caseNodeRef, ContentModel.PROP_NODE_DBID);
            String partyName = PartyService.PARTY_PREFIX + "_" + dbid + "_" + role;
            String createdGroup = this.authorityService.createAuthority(AuthorityType.GROUP, partyName, role, partyService.PARTY_ZONES);
            /*if (!contacts.isEmpty() && StringUtils.isNotEmpty(contacts.get(0))) {
                for (String contact : contacts) {

                }
            }*/
            assert(this.authorityService.getAuthorityNodeRef(createdGroup) != null);
        } catch (Exception ge) {
            throw new AlfrescoRuntimeException("Unable to create party due to the following reason(s): " + ge.getMessage());
        }
    }

    /**
     * Creates a new content node setting the content provided.
     *
     * @param  parent   the parent node reference
     * @param  name     the name of the newly created content object
     * @param  text     the content text to be set on the newly created node
     * @return NodeRef  node reference to the newly created content node
     */
    private NodeRef createCaseContentNode(NodeRef parent, String name, String text) {

        // Create a map to contain the values of the properties of the node

        Map<QName, Serializable> props = new HashMap<QName, Serializable>(1);
        props.put(ContentModel.PROP_NAME, name);

        // use the node service to create a new node
        NodeRef node = this.nodeService.createNode(
                parent,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name),
                SimpleCaseModel.TYPE_CASE_SIMPLE,
                props).getChildRef();

        // Use the content service to set the content onto the newly created node
        ContentWriter writer = this.contentService.getWriter(node, ContentModel.PROP_CONTENT, true);
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.setEncoding("UTF-8");
        writer.putContent(text);

        // Return a node reference to the newly created node
        return node;
    }

    private NodeRef getUserNodeRef(String userName){
        return personService.getPerson(userName);
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    public void getCompanyHomeNodeRef(){
        StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
        ResultSet rs = searchService.query(storeRef, SearchService.LANGUAGE_XPATH, "/app:company_home");
        NodeRef companyHomeNodeRef = null;
        try
        {
            if (rs.length() == 0)
            {
                throw new AlfrescoRuntimeException("Didn't find Company Home");
            }
            companyHomeNodeRef = rs.getNodeRef(0);
        }
        finally
        {
            rs.close();
        }
    }
}