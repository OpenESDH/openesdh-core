package dk.openesdh.repo.services.authorities;

import java.util.Set;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.apache.commons.io.IOUtils;
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

@RunWith(RemoteTestRunner.class)
@Remote(runnerClass = SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:alfresco/application-context.xml",
    "classpath:alfresco/extension/openesdh-test-context.xml"})
public class UsersCsvImportServiceImplIT {

    private static final String HEADER = "User Name,First Name,Last Name,E-mail Address,,Password,Company,"
            + "Job Title,Location,Telephone,Mobile,Skype,IM,Google User Name,Address,"
            + "Address Line 2,Address Line 3,Post Code,Telephone,Fax,Email,Member of groups\n";
    private static final String PERSON_USERNAME = "csvtestuser";
    private static final String HR_GROUP_SHORT_NAME = "HR_test";
    private static final String HR_GROUP_NAME = PermissionService.GROUP_PREFIX + "HR_test";
    private static final String IT_GROUP_SHORT_NAME = "IT_test";
    private static final String IT_GROUP_NAME = PermissionService.GROUP_PREFIX + "IT_test";

    @Autowired
    @Qualifier("UsersCsvImportService")
    private UsersCsvImportService usersCsvImportService;

    @Autowired
    @Qualifier("PersonService")
    private PersonService personService;

    @Autowired
    @Qualifier("AuthorityService")
    private AuthorityService authorityService;

    @Before
    public void setUp() {
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        authorityService.createAuthority(AuthorityType.GROUP, HR_GROUP_SHORT_NAME);
        authorityService.createAuthority(AuthorityType.GROUP, IT_GROUP_SHORT_NAME);
    }

    @After
    public void tearDown() {
        if (personService.personExists(PERSON_USERNAME)) {
            personService.deletePerson(PERSON_USERNAME);
        }
        authorityService.deleteAuthority(HR_GROUP_NAME);
        authorityService.deleteAuthority(IT_GROUP_NAME);
    }

    @Test
    public void shouldCreateUsersFromCSV() throws Exception {
        String csv = new StringBuilder(HEADER)
                .append(PERSON_USERNAME)
                .append(",Testuser,Testsurname,csvtestuser@opene.org,,passs,some company,some job,US,123456,,skypename,imname,googleuser,some address,,1234,,,,,HR_test;IT_test")
                .toString();

        usersCsvImportService.uploadUsersCsv(IOUtils.toInputStream(csv));
        Assert.assertTrue("Should create a user from csv", personService.personExists(PERSON_USERNAME));

        Set<String> hrUsers = authorityService.getContainedAuthorities(AuthorityType.USER, HR_GROUP_NAME, true);
        Assert.assertTrue("HR group should contain uploaded csv user", hrUsers.contains(PERSON_USERNAME));

        Set<String> itUsers = authorityService.getContainedAuthorities(AuthorityType.USER, IT_GROUP_NAME, true);
        Assert.assertTrue("IT group should contain uploaded csv user", itUsers.contains(PERSON_USERNAME));
    }

}
