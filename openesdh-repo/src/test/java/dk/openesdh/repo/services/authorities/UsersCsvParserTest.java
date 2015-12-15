package dk.openesdh.repo.services.authorities;

import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.namespace.QName;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import dk.openesdh.repo.services.authorities.UsersCsvParser.User;

public class UsersCsvParserTest {

    private static final String HEADER = "User Name,First Name,Last Name,E-mail Address,,Password,Company,"
            + "Job Title,Location,Telephone,Mobile,Skype,IM,Google User Name,Address,"
            + "Address Line 2,Address Line 3,Post Code,Telephone,Fax,Email,Member of groups\n";

    @Test
    public void shouldParseUsers() throws Exception {
        
        String csv = new StringBuilder(HEADER)
                .append("csvtestuser,Testuser,Testsurname,csvtestuser@opene.org,,passs,some company,some job,US,123456,,skypename,imname,googleuser,some address,,1234,,,,,HR;IT")
                    .toString();

        UsersCsvParser parser = new UsersCsvParser();
        List<User> users = parser.parse(IOUtils.toInputStream(csv));
        Assert.assertFalse("Parsed users list shouldn't be empty", users.isEmpty());
        User user = users.get(0);
        Map<QName, String> userProps = user.getProperties();
        Assert.assertEquals("Wrong username parsed", "csvtestuser", userProps.get(ContentModel.PROP_USERNAME));
        Assert.assertEquals("Wrong user first name parsed", "Testuser", userProps.get(ContentModel.PROP_FIRSTNAME));
        Assert.assertEquals("Wrong user last name parsed", "Testsurname", userProps.get(ContentModel.PROP_LASTNAME));
        Assert.assertTrue("Parsed user should be a member of HR group", user.getGroups().contains("HR"));
        Assert.assertTrue("Parsed user should be a member of IT group", user.getGroups().contains("IT"));
    }
}
