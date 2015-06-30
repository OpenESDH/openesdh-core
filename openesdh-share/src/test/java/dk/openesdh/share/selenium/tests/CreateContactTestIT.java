package dk.openesdh.share.selenium.tests;


import dk.magenta.share.selenium.framework.Browser;
import dk.openesdh.share.selenium.framework.Pages;
import dk.openesdh.share.selenium.framework.enums.User;
import dk.openesdh.share.selenium.framework.pages.AdminToolsPage;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.Serializable;
import java.util.HashMap;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CreateContactTestIT extends AdminToolsPage {


    @FindBy(id="CREATE_ORGANIZATION_DIALOG")
    WebElement createOrgContactDialog;

    @FindBy(id="CREATE_PERSON_DIALOG")
    WebElement createPersonContactDialog;

    @FindBy(css="#CREATE_ORGANIZATION_DIALOG .dijitDialogPaneContent .footer .confirmationButton .dijitButtonNode")
    WebElement createContactDialogConfirmButton;

    @FindBy(css="#CREATE_CASE_DIALOG .dijitDialogPaneContent .footer .cancellation .dijitButtonNode")
    WebElement createContactDialogCancelButton;

    HashMap<String, Serializable> contactDetails;
    WebDriver driver = Browser.Driver;
    WebDriverWait wait = new WebDriverWait(Browser.Driver,7);

    //
    @Test
    public void createContactAsAdmin() {
        this.loginAsUser(User.ADMIN);
        WebDriverWait wait = new WebDriverWait(Browser.Driver,7);
        this.gotoContactsTypePage("organisation");
        this.clickCreateContactBtnType("organisation");
        assertNotNull(createOrgContactDialog);

        contactDetails = this.generateRandomContactDetails("organisation");
        driver.findElement(By.name("organizationName")).sendKeys(contactDetails.get("organizationName").toString());
        driver.findElement(By.name("cvrNumber")).sendKeys(contactDetails.get("cvrNumber").toString());
        driver.findElement(By.name("email")).sendKeys(contactDetails.get("email").toString());
        driver.findElement(By.name("streetName")).sendKeys(contactDetails.get("streetName").toString());
        driver.findElement(By.name("houseNumber")).sendKeys(contactDetails.get("houseNumber").toString());
        driver.findElement(By.name("postCode")).sendKeys(contactDetails.get("postCode").toString());
        driver.findElement(By.name("city")).sendKeys(contactDetails.get("city").toString());
        driver.findElement(By.name("countryCode")).sendKeys(contactDetails.get("countryCode").toString());

        createContactDialogConfirmButton.click();
//        wait.until();
    }


}
