package dk.openesdh.share.selenium.framework.pages;

import dk.openesdh.share.selenium.framework.Browser;
import dk.openesdh.share.selenium.framework.Pages;
import dk.openesdh.share.selenium.framework.enums.User;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * The login page on share
 * @author Søren Kirkegård
 *
 */
public class LoginPage extends BasePage {

    private static final String URL = BASE_URL + "/page/type/login";
    private static final String URL_ERROR = URL + "?error=true";

    @FindBy(how = How.NAME, using = "username")
    private WebElement userNameInput;

    @FindBy(how = How.NAME, using = "password")
    private WebElement passwordInput;

    @FindBy(how = How.ID, using = "page_x002e_components_x002e_slingshot-login_x0023_default-submit-button")
    private WebElement submitButton;


    public void gotoPage() {
        Browser.open(URL);
    }

    public boolean isAt() {
        //The id of the submit button is very special, so if that is displayed
        //we must be on the login page
        return submitButton.isDisplayed();
    }

    public void loginWith(User user) {
        Pages.Login.gotoPage();
        userNameInput.clear();
        userNameInput.sendKeys(user.username());
        passwordInput.clear();
        passwordInput.sendKeys(user.password());
        submitButton.click();
    }

    public boolean hasLoginError() {
        WebElement error = (new WebDriverWait(Browser.Driver, 10))
                .until(ExpectedConditions.presenceOfElementLocated(By.className("error")));
        String currentUrl = Browser.Driver.getCurrentUrl();

        return (error.isDisplayed() && URL_ERROR.equals(currentUrl));
    }

    public void logout() {
        WebElement userMenuPopup = Browser.Driver.findElement(By.id("HEADER_USER_MENU_POPUP"));
        userMenuPopup.click();

        WebElement userMenuPopup1 = Browser.Driver.findElement(By.id("HEADER_USER_MENU_POPUP_text"));
        userMenuPopup1.click();


        WebElement userMenuLogout = (new WebDriverWait(Browser.Driver, 10))
                .until(ExpectedConditions.presenceOfElementLocated(By.id("HEADER_USER_MENU_LOGOUT_text")));
        userMenuLogout.click();


        //WebElement userMenuLogout = Browser.Driver.findElement();

        //Browser.open(URL_LOGOUT);
    }

}
