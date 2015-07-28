package dk.openesdh.share.selenium.tests;

import dk.magenta.share.selenium.framework.Browser;
import dk.openesdh.share.selenium.framework.enums.User;
import dk.openesdh.share.selenium.framework.pages.AdminToolsPage;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.junit.Assert.assertNotNull;

public class CreateUserTestIT extends AdminToolsPage {

    @FindBy(xpath="//div[contains(@id, 'prompt_h')]")
    WebElement failureDialog;

    @FindBy(xpath="//div[@id='prompt_c']/div[@id='prompt']/div[@class='ft']/span/span/span/button")
    WebElement failureDialogOkBtn;

    @FindBy(xpath="//div[@id='deleteDialog']")
    WebElement deleteUserDialog;

    @FindBy(xpath="//div[@id='deleteDialog']/div[@class='ft']/span[@class='button-group']/span[1]/span[1]/button")
    WebElement deleteUserDialogDeleteBtn;

    @FindBy(xpath=("//span[@class='newuser-button']/span[1]/span[1]/button") )
    WebElement newUserBtn;

    @FindBy(name ="-" )
    WebElement userSearchBoxInput;

    @FindBy(xpath="//button[contains(text(),'Search')]")
    WebElement defUserSearchButton;

    WebDriver driver = Browser.Driver;
    WebDriverWait wait = new WebDriverWait(Browser.Driver,10);

    @Test
    public void createNewUserAsAdmin() {
        this.loginAsUser(User.ADMIN);
        String newUserName = this.createAlfrescoUser(User.CAROL);
    }

    @Test
    public void deleteUserAsAdmin() {
        this.loginAsUser(User.ADMIN);
        this.gotoAdminConsoleAppPage("users");
        WebElement userSearchButton = driver.findElement(By.cssSelector("[id$=_default-search-button-button]"));

        String newUserName = this.createAlfrescoUser(User.CAROL);
        userSearchBoxInput.clear();
        userSearchBoxInput.sendKeys(newUserName);
        defUserSearchButton.click();
        WebElement userProfileLink = driver.findElement(By.linkText(User.CAROL.firstName() + " " + User.CAROL.lastName()));
        wait.until(ExpectedConditions.elementToBeClickable(userProfileLink));
        userProfileLink.click();
        WebElement deleteUserBtn = driver.findElement(By.cssSelector("[id$=_default-deleteuser-button-button"));
        wait.until(ExpectedConditions.elementToBeClickable(deleteUserBtn));
        deleteUserBtn.click();
        assertNotNull(deleteUserDialog);
        deleteUserDialogDeleteBtn.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("-")));

    }

}
