package dk.openesdh.share.selenium;

import dk.magenta.share.selenium.framework.Browser;
import dk.openesdh.share.selenium.framework.BasePageAdminLoginTestIT;
import dk.openesdh.share.selenium.framework.Pages;
import dk.openesdh.share.selenium.framework.enums.User;
import dk.openesdh.share.selenium.framework.pages.CreateCasePage;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


/**
 * This testcase requires:
 * -user admin with password admin
 * -user alice with password alice
 * -user invalid with password invalid NOT TO EXIST!
 * @author Søren Kirkegård
 *
 */
public class CaseMenuTestIT extends BasePageAdminLoginTestIT {

    
    @Test
    public void testCaseMenuIsVisibleAndClickable() {
        Pages.Login.loginWith(User.ADMIN);
        assertTrue(Pages.Dashboard.isAt(User.ADMIN));

        Pages.Dashboard.clickCasesMenuItem();
        Pages.Dashboard.clickCasesMenuSearchItem();
        assertTrue(Pages.Search.isAt());
        Pages.Search.clickCasesMenuItem();
        Pages.Search.clickCreateSimpleCaseItem();
        // assertTrue(Pages.CreateCase.isAt()); //The dialog is now displayed in a pop-up
        WebElement createCaseDialogElem = Browser.Driver
                .findElement(By
                        .xpath("//div[div[span[@role='heading' and (text()='Opret sag' or text()='Create case' )]]]"));
        assertTrue(createCaseDialogElem != null);
    }

   

}
