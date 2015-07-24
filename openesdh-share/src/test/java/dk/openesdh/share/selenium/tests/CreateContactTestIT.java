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

    @FindBy(name="searchTerm")
    WebElement contactSearchFieldInput;

    @FindBy(css=".contact-search-ok-button .dijitButtonNode")
    WebElement contactSearchOkBtn;

    @FindBy(css="#CREATE_ORGANIZATION_DIALOG .dijitDialogPaneContent .footer .confirmationButton .dijitButtonNode")
    WebElement createContactDialogConfirmButton;

    @FindBy(css="#CREATE_CASE_DIALOG .dijitDialogPaneContent .footer .cancellation .dijitButtonNode")
    WebElement createContactDialogCancelButton;

    HashMap<String, Serializable> contactDetails;
    WebDriver driver = Browser.Driver;
    WebDriverWait wait = new WebDriverWait(Browser.Driver,7);

    @Test
    public void createContactAsAdmin() {
        this.loginAsUser(User.ADMIN);
        this.gotoContactsTypePage("organisation");
        this.clickCreateContactBtnType("organisation");
        assertNotNull(createOrgContactDialog);

        String contactEmail = this.createContact("organization");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[@class='value' and (text()='" + contactEmail + "'  ) ]")) );
    }

    @Test
    public void searchContact() {
        this.loginAsUser(User.ADMIN); WebDriverWait wait = new WebDriverWait(Browser.Driver,7);
        this.gotoContactsTypePage("organisation");
        this.clickCreateContactBtnType("organisation");
        assertNotNull(createOrgContactDialog);
        String contactEmail = this.createContact("organisation");

        contactSearchFieldInput.sendKeys(contactEmail);
        assertNotNull(contactSearchOkBtn);
        contactSearchOkBtn.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[@class='value' and (text()='"+contactEmail+"'  ) ]")) );

    }

    protected String createContact(String contactType){
        contactDetails = this.generateRandomContactDetails(contactType);
        for(String key : contactDetails.keySet()){
            driver.findElement(By.name(key)).sendKeys(contactDetails.get(key).toString());
        }
        createContactDialogConfirmButton.click();
        return contactDetails.get("email").toString();
    }


}
