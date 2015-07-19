package dk.openesdh.share.selenium.tests;


import dk.magenta.share.selenium.framework.Browser;
import dk.openesdh.share.selenium.framework.enums.User;
import dk.openesdh.share.selenium.framework.pages.AdminToolsPage;
import org.apache.commons.lang3.RandomStringUtils;
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

public class CreateContactTestIT extends AdminToolsPage {


    @FindBy(id="CREATE_ORGANIZATION_DIALOG")
    WebElement createOrgContactDialog;

    @FindBy(id="EDIT_CONTACT_DIALOG")
    WebElement editContactDialog;

    @FindBy(id="CREATE_PERSON_DIALOG")
    WebElement createPersonContactDialog;

    @FindBy(name="searchTerm")
    WebElement contactSearchFieldInput;

    @FindBy(css=".contact-search-ok-button .dijitButtonNode")
    WebElement contactSearchOkBtn;

    @FindBy(css="#CREATE_ORGANIZATION_DIALOG .dijitDialogPaneContent .footer .confirmationButton .dijitButtonNode")
    WebElement createContactDialogConfirmButton;

    @FindBy(css="#EDIT_CONTACT_DIALOG .dijitDialogPaneContent .footer .confirmation .dijitButtonNode")
    WebElement updateContactDialogConfirmButton;

    @FindBy(css="#CREATE_CASE_DIALOG .dijitDialogPaneContent .footer .cancellation .dijitButtonNode")
    WebElement createContactDialogCancelButton;

    HashMap<String, Serializable> contactDetails;
    WebDriver driver = Browser.Driver;
    WebDriverWait wait = new WebDriverWait(Browser.Driver,10);

    @Test
    public void createContactAsAdmin() {
        this.loginAsUser(User.ADMIN);
        this.gotoAdminConsoleAppPage("contactOrganisation");
        this.clickCreateContactBtnType("organisation");
        assertNotNull(createOrgContactDialog);

        String contactEmail = this.createContact("organization");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[@class='value' and (text()='" + contactEmail + "'  ) ]")) );
    }

    @Test
    public void searchContact() {
        this.loginAsUser(User.ADMIN);
        this.gotoAdminConsoleAppPage("contactOrganisation");
        this.clickCreateContactBtnType("organisation");
        assertNotNull(createOrgContactDialog);
        String contactEmail = this.createContact("organisation");

        contactSearchFieldInput.sendKeys(contactEmail);
        assertNotNull(contactSearchOkBtn);
        contactSearchOkBtn.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[@class='value' and (text()='"+contactEmail+"'  ) ]")) );
    }

    @Test
    public void editContactAsAdmin() {
        this.loginAsUser(User.ADMIN);
        this.gotoAdminConsoleAppPage("contactOrganisation");
        this.clickCreateContactBtnType("organisation");
        assertNotNull(createOrgContactDialog);
        String contactEmail = this.createContact("organisation");

        //Reduce the list down to the one contact so that we can be sure to target the edit icon button
        contactSearchFieldInput.sendKeys(contactEmail);
        assertNotNull(contactSearchOkBtn);
        contactSearchOkBtn.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[@class='value' and (text()='"+contactEmail+"'  ) ]")) );
        //The edit button
        WebElement editContactBtn = driver.findElement(By.xpath("//tr/td[last()]/span[@class='alfresco-renderers-PublishAction alfresco-debug-Info highlight'][1]"));
        editContactBtn.click();

        assertNotNull(editContactDialog);
        String email = RandomStringUtils.randomAlphabetic(9) +"@openESDH.org";
        WebElement emailFieldInput = driver.findElement(By.xpath("//div[@id='EDIT_CONTACT_DIALOG']//input[@name='email']"));
        assertNotNull(emailFieldInput);
        emailFieldInput.clear();
        emailFieldInput.sendKeys(email);
        updateContactDialogConfirmButton.click();
        contactSearchFieldInput.clear();
        contactSearchFieldInput.sendKeys(email);
        assertNotNull(contactSearchOkBtn);
        contactSearchOkBtn.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[@class='value' and (text()='" + email + "'  ) ]")));

    }

    @Test
    public void deleteContactAsAdmin() {
        this.loginAsUser(User.ADMIN);
        this.gotoAdminConsoleAppPage("contactOrganisation");
        this.clickCreateContactBtnType("organisation");
        assertNotNull(createOrgContactDialog);
        String contactEmail = this.createContact("organisation");

        //Reduce the list down to the one contact so that we can be sure to target the edit icon button
        contactSearchFieldInput.sendKeys(contactEmail);
        assertNotNull(contactSearchOkBtn);
        contactSearchOkBtn.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[@class='value' and (text()='" + contactEmail + "'  ) ]")));
        //The delete button
        WebElement deleteContactBtn = driver.findElement(By.xpath("//tr/td[last()]/span[@class='alfresco-renderers-PublishAction alfresco-debug-Info highlight'][2]"));
        deleteContactBtn.click();
        WebElement deleteConfirmationDialogBtn = driver.findElement(By.xpath("//div[contains(@class, 'dialogDisplayed')]//div[@class='footer']/span[1]//span[contains(@class,'dijitButtonNode')]"));
        wait.until(ExpectedConditions.elementToBeClickable(deleteConfirmationDialogBtn));
        deleteConfirmationDialogBtn.click();

        contactSearchFieldInput.clear();
        contactSearchFieldInput.sendKeys(contactEmail);
        assertNotNull(contactSearchOkBtn);
        contactSearchOkBtn.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='alfresco-lists-views-AlfListView bordered alfresco-debug-Info highlight']//div[text()='No results' or text()='Ingen resultater']")));
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
