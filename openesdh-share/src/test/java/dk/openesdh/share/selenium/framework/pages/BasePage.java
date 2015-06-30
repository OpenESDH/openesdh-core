package dk.openesdh.share.selenium.framework.pages;

import dk.magenta.share.selenium.framework.Browser;
import dk.openesdh.share.selenium.framework.Pages;
import dk.openesdh.share.selenium.framework.enums.User;
import org.junit.*;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertNotNull;

/**
 * All pages must extend this base page to share type and have basic shared
 * configurations.
 * Taking this out of its project simply because the base page object may be markedly different for both OpenE and
 * NukiDoc. - Lanre
 *
 * @author Søren Kirkegård
 * @modifiedBy Lanre Abiwon
 */
public abstract class BasePage {

    public static final String BASE_URL = "http://localhost:8081/share";

    //<editor-fold desc="WebElements Global to all pages">
    @FindBy(id = "HEADER_HOME")
    WebElement headerMenuHomeButton;

    @FindBy(id = "HEADER_CASES_DROPDOWN_text")
    WebElement headerMenuCasesButton;

    @FindBy(id = "CASE_MENU_CREATE_SIMPLE_CASE_text")
    WebElement casesMenuSimpleCaseButton;

    @FindBy(id = "CREATE_CASE_DIALOG")
    WebElement createCaseDialog;

    @FindBy(id = "CASE_MENU_SEARCH_LINK_text")
    WebElement searchLinkItem;
    //</editor-fold>

    //<editor-fold desc="Anotated Selenium pre and post condition actions">
    @BeforeClass
    public static void setUpBeforeClass() {
        Browser.initialize();
        Pages.initialize();
    }

    @Before
    public void setup() {
        PageFactory.initElements(Browser.Driver, this);
    }

    @After
    public void tearDown() {

    }

    @AfterClass
    public static void tearDownAfterClass() {
        Pages.Login.logout();
        Browser.Driver.close();
    }
    //</editor-fold>


    public void loginAsUser(User user){
        Pages.Login.loginWith(user);
    }

    //Find the case ID from the URL with the new /page/oe/case/<id>/page scheme
    public String getCaseId() {
        Pattern p = Pattern.compile("\\/oe\\/case\\/(.+)/");
        Matcher matcher = p.matcher(Browser.Driver.getCurrentUrl());
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return null;
        }
    }

    public void clickCasesMenuItem() {
        assertNotNull(headerMenuCasesButton);
        headerMenuCasesButton.click();
    }

    public void clickCasesMenuSearchItem() {
        assertNotNull(searchLinkItem);
        searchLinkItem.click();
    }

    public void clickCreateCaseMenuItem() {
        Assert.assertNotNull(this.casesMenuSimpleCaseButton);
        this.casesMenuSimpleCaseButton.click();
    }

    public boolean createCaseMenuItemNotVisible() {
        return !elementExists(this.casesMenuSimpleCaseButton);
    }

    /**
     * To check if an element exists. According to the docs (see link below), the use of findeElements(By by) is the
     * recommended way to check for the existence of an element, however this always throws the exception with which
     * we're catching and using to return the false condition. So for now it is recommended to use this way to test for
     * existence of an element.
     * https://selenium.googlecode.com/git/docs/api/java/org/openqa/selenium/WebDriver.html#findElement-org.openqa.selenium.By-
     *
     * IMPORTANT => This test results in a wait time of around 10 seconds due to the wait before the exception is thrown
     *
     * @param element
     * @return true/false
     */
    public boolean elementExists(WebElement element){
        try{
            element.getLocation();
            return true;
        }
        catch (NoSuchElementException nse){
            return false;
        }
    }

    public void clickHomeMenuButton() {
        Assert.assertNotNull(this.headerMenuHomeButton);
        this.headerMenuHomeButton.click();
    }

    public static void selectAuthoritiesInPicker(String id, List<String> authorities) {

        WebElement searchInput = Browser.Driver
                .findElement(By
                        .xpath("//div[div[span[@role='heading' and (text()='Select...'  ) ]] and contains(@class,'alfresco-dialog-AlfDialog') ]//input[@name='searchTerm']"));
        WebElement searchButton = Browser.Driver
                .findElement(By
                        .xpath("//span[contains(@class,'confirmationButton')]/span[contains(@class,'dijitButtonNode')]"));
        for (String authority : authorities) {
            searchInput.clear();
            searchInput.sendKeys(authority);
            searchButton.click();
            // Wonderfully complicated XPath way to get the Add button for
            // adding the particular authority we want to add.
            WebElement addAuthority = Browser.Driver
                    .findElement(By
                            .xpath("//td[contains(@class, 'yui-dt-col-name') and "
                                    + "contains(., "
                                    + "'"
                                    + authority
                                    + "')]/following-sibling::td[contains(@class, 'yui-dt-col-add')]/descendant::a"));
            addAuthority.click();
        }

        WebElement authorityPickerOkButton = Browser.Driver.findElement(By
                .cssSelector("button[id$='" + id + "-cntrl-ok-button']"));
        authorityPickerOkButton.click();

    }

}