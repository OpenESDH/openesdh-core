package dk.openesdh.repo.services.authorities;

import java.io.IOException;
import java.util.Set;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
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
@ContextConfiguration({ "classpath:alfresco/application-context.xml",
        "classpath:alfresco/extension/openesdh-test-context.xml" })
public class GroupsServiceImplIT {
    
    private static String CSV_HEADER = "Group name,Display name,Member of groups,Simple case,Staff case\n";
    private static String[] groups = { "IT_test", "HR_test", "HR_CREATE_test", "HR_CHIEF_test" };

    @Autowired
    @Qualifier("GroupsService")
    private GroupsService groupsService;
    
    @Autowired
    @Qualifier("AuthorityService")
    private AuthorityService authorityService;

    @Before
    public void setUp() {

    }

    @After
    public void tearDown() {
        for (String group : groups) {
            String authority = PermissionService.GROUP_PREFIX + group;
            if (authorityService.authorityExists(authority)) {
                authorityService.deleteAuthority(authority);
            }
        }
    }

    @Test
    public void shouldCreateGroupsFromCSV() throws IOException{
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        String csv = new StringBuilder(CSV_HEADER)
            .append("IT_test,IT group,,Read\n")
            .append("HR_test,HR group,,,Read\n")
            .append("HR_CREATE_test,HR creators,HR_test,,CREATE;WRITE\n")
            .append("HR_CHIEF_test,HR chief,HR_test;HR_CREATE_test").toString();
        
        groupsService.uploadGroupsCSV(IOUtils.toInputStream(csv));
        
        for (String group : groups) {
            Assert.assertTrue("The group should be created: " + group,
                    authorityService.authorityExists(PermissionService.GROUP_PREFIX + group));
        }
        
        Set<String> hrContainedGroups = authorityService.getContainedAuthorities(AuthorityType.GROUP,
                PermissionService.GROUP_PREFIX + "HR_test", true);
        Assert.assertTrue("HR creators group should belong to HR_test",
                hrContainedGroups.contains(PermissionService.GROUP_PREFIX + "HR_CREATE_test"));
        Assert.assertTrue("HR chief group should belong to HR_test",
                hrContainedGroups.contains(PermissionService.GROUP_PREFIX + "HR_CHIEF_test"));

    }

    @Test
    public void shouldThrowExceptionNoGroupExists() throws IOException {
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        String csv = new StringBuilder(CSV_HEADER).append("HR_CREATE_test,HR creators,HR_test,,CREATE;WRITE\n")
                .toString();

        try {
            groupsService.uploadGroupsCSV(IOUtils.toInputStream(csv));
            Assert.fail("Should throw exception while adding a group to nonexisting parent group.");
        } catch (Exception ex) {
            Assert.assertTrue("Wrong exception message",
                    ex.getMessage().contains("An authority was not found for GROUP_HR_test"));
        }
    }
}
