package dk.openesdh.share.selenium;

import dk.magenta.share.selenium.framework.Browser;
import dk.openesdh.share.selenium.framework.Pages;
import dk.openesdh.share.selenium.framework.enums.User;
import dk.openesdh.share.selenium.framework.pages.AdminToolsPage;
import dk.openesdh.share.selenium.framework.pages.BaseCasePage;
import dk.openesdh.share.selenium.framework.pages.CaseMembersCasePage;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.jdbc.support.nativejdbc.WebLogicNativeJdbcExtractor;

import static org.junit.Assert.assertNotNull;

public class AddContactToCaseTestIT extends BaseCasePage {
    String orgContact, personContact;

    @Before
    public void createContacts(){
        Pages.Login.loginWith(User.ADMIN);
        orgContact = Pages.AdminToolsPage.createContact("organisation");
        personContact = Pages.AdminToolsPage.createContact("person");
        Pages.Login.logout();
    }

    //<editor-fold desc="Buttons specific to the case members page">
    @FindBy(id = "HEADER_CASE_PARTIES_text")
    WebElement addPartyButton;

    @FindBy(id="CASE_PARTIES_ADD_PARTY_text")
    WebElement addPartyDropDownButton;

    @FindBy(id = "CASE_PARTY_ADD_CONTACT_AFSENDER_text")
    WebElement addSenderPartyButton;

    @FindBy(id = "CASE_PARTY_ADD_CONTACT_MODTAGER_text")
    WebElement addReceiverPartyButton;
    //</editor-fold>

    @FindBy(id = "contact_picker_dialog")
    WebElement partyPickerDialog;

    @FindBy(xpath = "//div[@id='contact_picker_dialog' and contains(@class, 'dialogDisplayed')]//div[@class='footer']//span[1]//span[contains(@class,'dijitButtonNode')]")
    WebElement partyPickerDialogOKButton;

    WebDriverWait wait = new WebDriverWait(Browser.Driver,10);
    WebDriver driver = Browser.Driver;

    @Test
    public void addContactToCaseAsSender() {
        this.createCaseAsUser(User.ADMIN);
        this.gotoCasePage("parties");
        wait.until(ExpectedConditions.elementToBeClickable(addPartyButton));
        addPartyButton.click();
        addPartyDropDownButton.click();
        addSenderPartyButton.click();
        wait.until(ExpectedConditions.visibilityOf(partyPickerDialog));
        WebElement dialogSearchInput = partyPickerDialog.findElement(By.name("searchTerm"));
        clearAndEnter(dialogSearchInput, personContact);
        WebElement dialogSearchButton = partyPickerDialog.findElement(By.xpath("//div[@class='buttons']//span[1]//span[contains(@class,'dijitButtonNode')]"));
        dialogSearchButton.click();
        //The add button which adds the button to the right window where picked items are displayed
        WebElement resultSelector = partyPickerDialog.findElement(By.xpath("//div[@class='sub-pickers']//table//tbody//tr//td[last()]//span"));
        resultSelector.click();
        WebElement pickedItemCancelButton = partyPickerDialog.findElement(By.xpath("//div[@class='picked-items']//table//tbody//tr//td[last()]//span"));
        wait.until(ExpectedConditions.elementToBeClickable(pickedItemCancelButton));
        partyPickerDialogOKButton.click();
        //A kind of weak check, checking for the sender button to be displayed as it should be the only one.
        //Should probably look into creating a structure like USER for contact such that we cycle through a set list of
        //contacts instead. Easier to get other contact details.
        WebElement contactNameLink = driver.findElement(By.xpath("//span[contains(@class,'authority-username') and text()= '("+personContact+")' ]"));
        wait.until(ExpectedConditions.visibilityOf(contactNameLink));

    }

    @Test
    public void addContactToCaseAsReceiver() {
        this.createCaseAsUser(User.ADMIN);
        this.gotoCasePage("parties");
        wait.until(ExpectedConditions.elementToBeClickable(addPartyButton));
        addPartyButton.click();
        addPartyDropDownButton.click();
        addReceiverPartyButton.click();
        wait.until(ExpectedConditions.visibilityOf(partyPickerDialog));
        WebElement dialogSearchInput = partyPickerDialog.findElement(By.name("searchTerm"));
        clearAndEnter(dialogSearchInput, orgContact);
        WebElement dialogSearchButton = partyPickerDialog.findElement(By.xpath(".//div[@class='buttons']//span[1]//span[contains(@class,'dijitButtonNode')]"));
        dialogSearchButton.click();
        //the left window where the search result should be displayed
        //The add button which adds the button to the right window where picked items are displayed
        WebElement resultSelector = partyPickerDialog.findElement(By.xpath(".//div[@class='sub-pickers']//table//tbody//tr//td[last()]//span"));
        resultSelector.click();
        WebElement pickedItemCancelButton = partyPickerDialog.findElement(By.xpath("//div[@class='picked-items']//table//tbody//tr//td[last()]//span"));
        wait.until(ExpectedConditions.elementToBeClickable(pickedItemCancelButton));
        partyPickerDialogOKButton.click();
        //A kind of weak check, checking for the sender button to be displayed as it should be the only one.
        //Should probably look into creating a structure like USER for contact such that we cycle through a set list of
        //contacts instead. Easier to get other contact details.
        WebElement contactNameLink = driver.findElement(By.xpath("//span[contains(@class,'authority-username') and text()= '("+orgContact+")' ]"));
        wait.until(ExpectedConditions.visibilityOf(contactNameLink));
    }


}
