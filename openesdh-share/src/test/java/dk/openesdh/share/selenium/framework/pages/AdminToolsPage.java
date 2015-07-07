package dk.openesdh.share.selenium.framework.pages;

import dk.magenta.share.selenium.framework.Browser;
import org.apache.commons.lang3.RandomStringUtils;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.Serializable;
import java.util.HashMap;

import static org.junit.Assert.assertNotNull;

public class AdminToolsPage extends BasePage {

    public static final String URL = BASE_URL + "/page/console/admin-console";

    @FindBy(xpath = "//a[@href='contacts-organizations']")
    WebElement contactsOrgMenuItem;

    @FindBy(xpath = "//a[@href='contacts-people']")
    WebElement contactsPersonMenuItem;

    @FindBy(xpath = "//a[@href='users']")
    WebElement usersMenuItem;

    @FindBy(xpath = "//span[@class='newuser-button']/span/span/button']")
    WebElement newUserBtn;

    @FindBy(xpath = "//span[@widgetid='CREATE_ORGANIZATION_BTN']//span[contains(@class,'dijitButtonNode')]")
    WebElement createOrgBtn;

    @FindBy(xpath = "//span[@widgetid='CREATE_PERSON_BTN']//span[contains(@class,'dijitButtonNode')]")
    WebElement createPersonBtn;

    WebDriverWait wait = new WebDriverWait(Browser.Driver,7);

    public void gotoPage() {
        this.clickAdminToolsMenuItem();
    }

    public boolean isAt() {
        return Browser.Driver.getCurrentUrl().startsWith(URL);
    }

    public void gotoContactsTypePage(String contactType) {
        WebElement contactTypeMenuItem = contactType.equalsIgnoreCase("person") ? contactsPersonMenuItem : contactsOrgMenuItem;
        WebElement contactTypeBtn = contactType.equalsIgnoreCase("person") ? createPersonBtn : createOrgBtn;

        if(!this.isAt())
            this.gotoPage();
        this.wait.until(ExpectedConditions.elementToBeClickable(contactTypeMenuItem));
        contactTypeMenuItem.click();
        wait.until(ExpectedConditions.elementToBeClickable(contactTypeBtn));
    }

    public void clickContactsOrganisationBtn() {
        assertNotNull(contactsOrgMenuItem);
        contactsOrgMenuItem.click();
    }

    public void clickContactsPeopleBtn() {
        assertNotNull(contactsPersonMenuItem);
        contactsPersonMenuItem.click();
    }

    public void clickCreateContactBtnType(String contactType){
        WebElement contactTypeBtn = contactType.equalsIgnoreCase("person") ? createPersonBtn : createOrgBtn;
        assertNotNull(contactTypeBtn);
        contactTypeBtn.click();
    }

    public HashMap<String,Serializable> generateRandomContactDetails(String contactType){
        HashMap <String,Serializable> details = new HashMap<>();
        //type Specific section
        if(contactType.equalsIgnoreCase("person")){
            details.put("firstName", RandomStringUtils.randomAlphabetic(7));
            details.put("middleName", RandomStringUtils.randomAlphabetic(6));
            details.put("lastName", RandomStringUtils.randomAlphabetic(7));
            details.put("cprNumber", RandomStringUtils.randomNumeric(10));
        }
        else{
            details.put("organizationName", RandomStringUtils.randomAlphabetic(7));
            details.put("cvrNumber", RandomStringUtils.randomNumeric(8));
        }

        String email = RandomStringUtils.randomAlphabetic(9) +"@openESDH.org";
        details.put("email", email);
        details.put("streetName", RandomStringUtils.randomAlphabetic(15));
        details.put("houseNumber", RandomStringUtils.randomNumeric(2));
        details.put("postCode", RandomStringUtils.randomNumeric(4));
        details.put("city", "Aarhus");
        details.put("countryCode", RandomStringUtils.randomAlphabetic(2).toUpperCase());

        return details;
    }

}
