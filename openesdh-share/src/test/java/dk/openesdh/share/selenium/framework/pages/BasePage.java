package dk.openesdh.share.selenium.framework.pages;

import dk.magenta.share.selenium.framework.Browser;
import dk.openesdh.share.selenium.framework.Pages;
import dk.openesdh.share.selenium.framework.enums.User;
import org.junit.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import static org.junit.Assert.assertNotNull;

/**
 * All pages must extend this base page to share type and have basic shared
 * configurations.
 * Taking this out of its project simply because the base page object may be markedly different for both OpenE and
 * NukiDoc. - Lanre
 *
 * @author Lanre Abiwon
 */
public abstract class BasePage {

    public static final String BASE_URL = "http://localhost:8081/share";

    //<editor-fold desc="Anotated Selenium pre and post condition actions">
    @BeforeClass
    public static void setUpBeforeClass() {
        Browser.initialize();
        Pages.initialize();
    }

    @Before
    public void setup() {
        PageFactory.initElements(Browser.Driver, this);
        /*Pages.Login.loginWith(User.ADMIN);
        Pages.AdminToolsPage.createAlfrescoUser(User.BOB);
        Pages.AdminToolsPage.createAlfrescoUser(User.BRIGITTE);
        Pages.AdminToolsPage.createAlfrescoUser(User.CAROL);
        Pages.AdminToolsPage.createAlfrescoUser(User.HELENA);
        Pages.Login.logout();*/
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

    //<editor-fold desc="WebElements Global to all pages">
    @FindBy(id = "HEADER_HOME")
    WebElement headerMenuHomeButton;

    @FindBy(id = "HEADER_ADMIN_CONSOLE")
    WebElement headerMenuAdminToolsButton;

    @FindBy(id = "HEADER_CASES_DROPDOWN_text")
    WebElement headerMenuCasesButton;

    @FindBy(id = "CASE_MENU_CREATE_SIMPLE_CASE_text")
    WebElement casesMenuSimpleCaseButton;

    @FindBy(id = "CREATE_CASE_DIALOG")
    WebElement createCaseDialog;

    @FindBy(id = "CASE_MENU_SEARCH_LINK_text")
    WebElement searchLinkItem;
    //</editor-fold>

    public void loginAsUser(User user){
        Pages.Login.loginWith(user);
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
    public void clickHomeMenuButton() {
        Assert.assertNotNull(this.headerMenuHomeButton);
        this.headerMenuHomeButton.click();
    }
    public void clickAdminToolsMenuItem() {
        assertNotNull(headerMenuAdminToolsButton);
        headerMenuAdminToolsButton.click();
    }
    /**
     * clears the web element before filling it with inputText
     * @param input
     * @param inputText
     */
    public static void clearAndEnter(WebElement input, String inputText){
        input.clear();
        input.sendKeys(inputText);
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

}
