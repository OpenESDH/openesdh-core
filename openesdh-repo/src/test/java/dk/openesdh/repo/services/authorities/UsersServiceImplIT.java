package dk.openesdh.repo.services.authorities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
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
import dk.openesdh.repo.model.OpenESDHModel;
import dk.openesdh.repo.services.TransactionRunner;

@RunWith(RemoteTestRunner.class)
@Remote(runnerClass = SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:alfresco/application-context.xml",
    "classpath:alfresco/extension/openesdh-test-context.xml"})
public class UsersServiceImplIT {

    private static final String PERSON_USERNAME = "testuser";

    @Autowired
    @Qualifier("UsersService")
    private UsersService usersService;

    @Autowired
    @Qualifier("PersonService")
    private PersonService personService;

    @Autowired
    @Qualifier("AuthorityService")
    private AuthorityService authorityService;

    @Autowired
    @Qualifier("AuthenticationService")
    private MutableAuthenticationService authenticationService;

    @Autowired
    private TransactionRunner tr;

    @Before
    public void setUp() {
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
    }

    @After
    public void tearDown() {
        if (personService.personExists(PERSON_USERNAME)) {
            personService.deletePerson(PERSON_USERNAME);
        }
    }

    @Test
    public void shouldCreateAndUpdateUser() {
        String password = "pwd";
        String firstName = "Name";
        Map<QName, Serializable> props = new HashMap<>();
        props.put(ContentModel.PROP_USERNAME, PERSON_USERNAME);
        props.put(ContentModel.PROP_FIRSTNAME, firstName);
        props.put(ContentModel.PROP_EMAIL, "some_test_email@opene.org");
        props.put(ContentModel.PROP_PASSWORD, password);
        tr.runInNewTransaction(() -> usersService.createUser(props, true, null));
        tryToLogin(PERSON_USERNAME, password);

        firstName = "NameChanged";
        password = "pwd2";
        UserSavingContext.Assoc manager = getManager();
        props.put(ContentModel.PROP_FIRSTNAME, firstName);
        props.put(ContentModel.PROP_PASSWORD, password);

        NodeRef user = tr.runInNewTransaction(() -> usersService.updateUser(props, true, Arrays.asList(manager)));
        tryToLogin(PERSON_USERNAME, password);

        JSONObject userJson = usersService.getUserJson(user);
        JSONObject cm = (JSONObject) userJson.get(NamespaceService.CONTENT_MODEL_PREFIX);
        assertNotNull(cm);
        assertEquals(firstName, cm.get(ContentModel.PROP_FIRSTNAME.getLocalName()));
        JSONObject assoc = (JSONObject) userJson.get("assoc");
        assertEquals(manager.getTarget().toString(), assoc.get("manager"));
    }

    private void tryToLogin(String username, String password) {
        String currentUser = AuthenticationUtil.getRunAsUser();
        try {
            authenticationService.authenticate(username, password.toCharArray());
        } finally {
            AuthenticationUtil.setRunAsUser(currentUser);
        }
    }

    private UserSavingContext.Assoc getManager() {
        NodeRef manager = authorityService.getAuthorityNodeRef(CaseHelper.MIKE_JACKSON);
        return new UserSavingContext.Assoc(OpenESDHModel.ASPECT_OE_MANAGEABLE, OpenESDHModel.ASSOC_OE_MANAGER, manager);
    }

}
