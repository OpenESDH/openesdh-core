package dk.openesdh.share.selenium.tests;

import dk.magenta.share.selenium.framework.Browser;
import dk.openesdh.share.selenium.framework.enums.User;
import dk.openesdh.share.selenium.framework.pages.BaseCasePage;
import dk.openesdh.share.selenium.framework.pages.BasePage;
import org.junit.Test;
import org.openqa.selenium.support.ui.WebDriverWait;


/**
 * This testcase requires:
 * -user admin with password admin
 * -user alice with password alice
 * -user invalid with password invalid NOT TO EXIST!
 * @author Søren Kirkegård
 *
 */
public class CaseMenuTestIT extends BasePage {

    //Admin can see menu
    @Test
    public void testCaseMenuIsVisibleAndClickableByAdmin() {
        this.loginAsUser(User.ADMIN);
        WebDriverWait wait = new WebDriverWait(Browser.Driver,10);
        this.clickCasesMenuItem(); //The assertion that it's not null is good enough
    }

    //Normal user can see menu
    @Test
    public void testCaseMenuIsVisibleAndClickableByNonAdmin() {
        this.loginAsUser(User.BOB);
        WebDriverWait wait = new WebDriverWait(Browser.Driver,10);
        this.clickCasesMenuItem();
    }

   

}
