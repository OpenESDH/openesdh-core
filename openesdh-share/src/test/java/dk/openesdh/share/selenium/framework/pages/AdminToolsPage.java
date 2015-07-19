package dk.openesdh.share.selenium.framework.pages;

import dk.magenta.share.selenium.framework.Browser;
import dk.openesdh.share.selenium.framework.enums.User;
import org.apache.commons.lang3.RandomStringUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.Serializable;
import java.util.HashMap;

import static org.junit.Assert.assertNotNull;

public class AdminToolsPage extends BasePage {

    public static final String URL = BASE_URL + "/page/console/admin-console";

    @FindBy(xpath="//div[contains(@id, 'prompt_h')]")
    WebElement failureDialog;

    @FindBy(xpath="//div[@id='prompt_c']/div[@id='prompt']/div[@class='ft']/span/span/span/button")
    WebElement failureDialogOkBtn;

    @FindBy(xpath = "//a[@href='contacts-organizations']")
    WebElement contactsOrgMenuItem;

    @FindBy(xpath = "//a[@href='contacts-people']")
    WebElement contactsPersonMenuItem;

    @FindBy(xpath = "//a[@href='users']")
    WebElement usersMenuItem;

    @FindBy(xpath = "//a[@href='groups']")
    WebElement groupsMenuItem;

    @FindBy(xpath = "//span[@class='newuser-button']/span/span/button")
    WebElement newUserBtn;

    @FindBy(xpath = "//span[@widgetid='CREATE_ORGANIZATION_BTN']//span[contains(@class,'dijitButtonNode')]")
    WebElement createOrgBtn;

    @FindBy(xpath = "//span[@widgetid='CREATE_PERSON_BTN']//span[contains(@class,'dijitButtonNode')]")
    WebElement createPersonBtn;

    @FindBy(xpath ="//input[@type='checkbox' and @name='-']")
    WebElement showAllGroupsCheckbox;

    @FindBy(xpath ="//input[@type='text' and @name='-']")
    WebElement defaultSearchInput;

    WebDriverWait wait = new WebDriverWait(Browser.Driver,7);
    WebDriver driver = Browser.Driver;

    public void gotoAdminToolsPage() {
        this.clickAdminToolsMenuItem();
    }

    public boolean isAt() {
        return Browser.Driver.getCurrentUrl().startsWith(URL);
    }

    public void gotoAdminConsoleAppPage(String page){
        if(!this.isAt()) {
            this.gotoAdminToolsPage();
            this.wait.until(ExpectedConditions.elementToBeClickable(usersMenuItem));
        }

        switch (page){
            //For the time being these are the only pages we may require within the testing surface area.
            case "users" : this.clickUsersMenuItem(); break;
            case "groups" : this.clickGroupsMenuItem(); break;
            case "contactPerson" : this.clickContactsPersonMenuItem(); break;
            case "contactOrganisation" : this.clickContactsOrganisationMenuItem(); break;
        }

    }

    public void clickContactsPersonMenuItem() {
        assertNotNull(contactsPersonMenuItem);
        contactsPersonMenuItem.click();
    }
    public void clickContactsOrganisationMenuItem() {
        assertNotNull(contactsOrgMenuItem);
        contactsOrgMenuItem.click();
    }
    public void clickGroupsMenuItem() {
        assertNotNull(groupsMenuItem);
        groupsMenuItem.click();
    }
    public void clickUsersMenuItem() {
        assertNotNull(usersMenuItem);
        usersMenuItem.click();
    }

    public void clickNewUserBtn() {
        assertNotNull(newUserBtn);
        newUserBtn.click();
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

    public String createAlfrescoUser(User user){
        this.gotoAdminConsoleAppPage("users");
        wait.until(ExpectedConditions.elementToBeClickable(newUserBtn));
        newUserBtn.click();
        WebElement firstNameInput = driver.findElement(By.cssSelector("[id$=_default-create-firstname]"));
        WebElement lastNameInput = driver.findElement(By.cssSelector("[id$=_default-create-lastname]"));
        WebElement emailInput = driver.findElement(By.cssSelector("[id$=_default-create-email]"));
        WebElement userNameInput = driver.findElement(By.cssSelector("[id$=_default-create-username]"));
        WebElement passwordInput = driver.findElement(By.cssSelector("[id$=_default-create-password]"));
        WebElement verifyPasswordInput = driver.findElement(By.cssSelector("[id$=_default-create-verifypassword]"));
        WebElement createUserButton = driver.findElement(By.cssSelector("[id$=_default-createuser-ok-button-button]"));
        WebElement createUserCancelButton = driver.findElement(By.cssSelector("[id$=_default-createuser-cancel-button-button]"));

        final HashMap<String, WebElement> propsInputMap = new HashMap<String, WebElement>(){
            {
                put("firstName", firstNameInput);
                put("lastName", lastNameInput);
                put("email", emailInput);
                put("userName", userNameInput);
                put("password", passwordInput);
                put("verifyPassword", verifyPasswordInput);
            }
        };

        clearAndEnter(propsInputMap.get("firstName"), user.firstName());
        clearAndEnter(propsInputMap.get("lastName"), user.lastName());
        clearAndEnter(propsInputMap.get("email"), user.firstName() + "@openESDH.org");
        clearAndEnter(propsInputMap.get("userName"), user.userName());
        clearAndEnter(propsInputMap.get("password"), user.password());
        clearAndEnter(propsInputMap.get("verifyPassword"), user.password());

        createUserButton.click();
        try{
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("-")));//The search box
        }
        catch (TimeoutException toe){

            WebElement failureDialog = driver.findElement(By.xpath("//div[contains(@id, 'prompt_h')]"));
            WebElement failureDialogOkBtn = driver.findElement(By.xpath("//div[@id='prompt_c']/div[@id='prompt']/div[@class='ft']/span/span/span/button"));

            if(elementExists(failureDialog)) {
                //In case the user already exists we assume the resulting failure popup dialog will be a result of this
                //and just click the ok button and detect the search field
                failureDialogOkBtn.click();
                createUserCancelButton.click();
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("-")));
                return user.userName();
            }
            else{
                throw new NoSuchElementException("Issue creating user: "+ toe.getMessage());
            }
        }

        return user.userName();
    }

    public boolean addUserToGroup(User user, String groupName){
        try {
            this.gotoAdminConsoleAppPage("groups");
            //the browse button
            WebElement browseBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[id$='_default-browse-button-button']")));
            browseBtn.click();
            //The group name link
            WebElement groupListLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//span[@class='yui-columnbrowser-item-label' and text()='" + groupName + "']")));
            groupListLink.click();
            //The button in the second column for adding a user
            WebElement addUserToGroupBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//span[@class='groups-adduser-button']")));
            addUserToGroupBtn.click();
            //The resulting popup dialog which is used to search for the person
            WebElement peoplePickerDialog = driver.findElement(By.cssSelector("[id$='_default-peoplepicker_h']"));
            wait.until(ExpectedConditions.visibilityOf(peoplePickerDialog));
            WebElement dialogSearchInput = driver.findElement(By.cssSelector("[id$='_default-search-peoplefinder-search-text']"));
            WebElement dialogSearchBtn = driver.findElement(By.cssSelector("[id$='_default-search-peoplefinder-search-button-button']"));

            clearAndEnter(dialogSearchInput, user.userName());
            dialogSearchBtn.click();
            //The add button for the person to be added to the group
            WebElement dialogAddBtn = driver.findElement(By.xpath("//span[contains(@id,'_default-search-peoplefinder-action-" + user.userName() + "')]/span/span/button"));

            wait.until(ExpectedConditions.elementToBeClickable(dialogAddBtn));
            dialogAddBtn.click();

            wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//span[@class='yui-columnbrowser-item-label' and text()='"+user.firstName()+" "+user.lastName()+" ("+ user.userName()+")']")));
            return true;
        }
        catch(TimeoutException toe){
            if(failureDialog.isDisplayed()) {
                //we assume for testing purposes that the user is already added and return true
                //Needs more rigorous checking
                failureDialogOkBtn.click();
                wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//div[contains(@id, 'prompt_h')]")));
                return true;
            }
            else
                throw new NoSuchElementException("Unable to add user to group because: "+ toe.getMessage());
        }
    }
}
