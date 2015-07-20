package dk.openesdh.share.selenium.tests;


import dk.openesdh.share.selenium.framework.enums.User;
import dk.openesdh.share.selenium.framework.pages.BaseCasePage;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CreateCaseTestIT  extends BaseCasePage {

    @Test
    public void createCaseAsAdmin() {
        assertNotNull(this.createCaseAsUser(User.ADMIN));
    }

    /**
     * Remember that the user abeecher must be added to the CaseSimpleCreator Group.
     */
    @Test
    public void createCaseAsNonAdminUser() {
        assertNotNull(this.createCaseAsUser(User.BRIGITTE));
    }

    //Attempt to create case a a non permitted user
    @Test
    public void createCaseAsNonPermittedUser() {
        try {
            this.createCaseAsUser(User.BOB);
        }
        catch(Exception toe) {
            //Any exception would indicate success in this case as we should not get far in the process.
            assertTrue(this.createCaseMenuItemNotVisible());
        }
    }

}
