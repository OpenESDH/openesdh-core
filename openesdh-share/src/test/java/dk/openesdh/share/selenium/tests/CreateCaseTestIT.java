package dk.openesdh.share.selenium.tests;


import dk.magenta.share.selenium.framework.Browser;
import dk.openesdh.share.selenium.framework.Pages;
import dk.openesdh.share.selenium.framework.enums.User;
import dk.openesdh.share.selenium.framework.pages.BaseCasePage;
import dk.openesdh.share.selenium.framework.pages.BasePage;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CreateCaseTestIT  extends BaseCasePage {
    @Test
    public void createCaseAsAdmin() {
        this.createCaseAsUser(User.ADMIN);
    }

    /**
     * Remember that the user abeecher must be added to the CaseSimpleCreator Group.
     */
//    @Test
    public void createCaseAsNonAdminUser() {
        this.createCaseAsUser(User.ALICE);
    }

    //Attempt to create case a a non permitted user
//    @Test
    public void createCaseAsNonPermittedUser() {
        this.loginAsUser(User.BOB);
        WebDriverWait wait = new WebDriverWait(Browser.Driver,10);
        this.clickCasesMenuItem();
        assertTrue(this.createCaseMenuItemNotVisible());
        this.clickHomeMenuButton();
    }

}
