package dk.openesdh.share.selenium.tests;

import dk.magenta.share.selenium.framework.Browser;
import dk.openesdh.share.selenium.framework.enums.User;
import dk.openesdh.share.selenium.framework.pages.BaseCasePage;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class AddMemberToCaseTestIT extends BaseCasePage {

    //<editor-fold desc="Buttons specific to the case members page">
    @FindBy(id = "CASE_MEMBERS_ADD_AUTHORITIES_text")
    WebElement addAuthoritiesButton;

    @FindBy(id = "CASE_MEMBERS_ADD_AUTHORITY_CASESIMPLEREADER_text")
    WebElement addSimpleCaseReadersButton;

    @FindBy(id = "CASE_MEMBERS_ADD_AUTHORITY_CASESIMPLEWRITER_text")
    WebElement addSimpleCaseWritersButton;

    @FindBy(id = "CASE_MEMBERS_ADD_AUTHORITY_CASEOWNERS_text")
    WebElement addSimpleCaseOwnersButton;
    //</editor-fold>

    @FindBy(id = "members_picker_dialog")
    WebElement membersPickerDialog;

    WebDriverWait wait = new WebDriverWait(Browser.Driver,10);
    WebDriver driver = Browser.Driver;

    @Test
    public void addUserToCaseAsReader() {
        this.createCaseAsUser(User.ADMIN);
        this.gotoCasePage("members");
        wait.until(ExpectedConditions.elementToBeClickable(addAuthoritiesButton));
        addAuthoritiesButton.click();
        addSimpleCaseReadersButton.click();
        wait.until(ExpectedConditions.visibilityOf(membersPickerDialog));
        WebElement dialogSearchInput = membersPickerDialog.findElement(By.name("searchTerm"));
        clearAndEnter(dialogSearchInput, User.BRIGITTE.userName());
        clickPickerDialogSearchBtn();
        //the left window where the search result should be displayed
        //The add button which adds the button to the right window where picked items are displayed
        WebElement resultSelector = membersPickerDialog.findElement(By.xpath("//div[@class='sub-pickers']//table//tbody//tr//td[last()]//span"));
        resultSelector.click();
        WebElement pickedItemCancelButton = membersPickerDialog.findElement(By.xpath("//div[@class='picked-items']//table//tbody//tr//td[last()]//span"));
        wait.until(ExpectedConditions.elementToBeClickable(pickedItemCancelButton));
        //had to find the dialog button using the driver as opposed to the dialog element.findeElement() method. For some
        //reason the button is found but is shown as not visible. (There's another hidden dialog me thinks)
        WebElement dialogPickerOKButton = driver.findElement(By.xpath("//div[@id='members_picker_dialog']//div[@class='footer']//span[1]/span[contains(@class,'dijitButtonNode')]"));
        dialogPickerOKButton.click();
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText(User.BRIGITTE.firstName()+" "+User.BRIGITTE.lastName())));

    }

    @Test
    public void addUserToCaseAsWriter() {
        this.createCaseAsUser(User.ADMIN);
        this.gotoCasePage("members");
        wait.until(ExpectedConditions.elementToBeClickable(addAuthoritiesButton));
        addAuthoritiesButton.click();
        addSimpleCaseWritersButton.click();
        wait.until(ExpectedConditions.visibilityOf(membersPickerDialog));
        WebElement dialogSearchInput = membersPickerDialog.findElement(By.name("searchTerm"));
        clearAndEnter(dialogSearchInput, User.BOB.userName());
        clickPickerDialogSearchBtn();
        //the left window where the search result should be displayed
        //The add button which adds the button to the right window where picked items are displayed
        WebElement resultSelector = membersPickerDialog.findElement(By.xpath("//div[@class='sub-pickers']//table//tbody//tr//td[last()]//span"));
        resultSelector.click();
        WebElement pickedItemCancelButton = membersPickerDialog.findElement(By.xpath("//div[@class='picked-items']//table//tbody//tr//td[last()]//span"));
        wait.until(ExpectedConditions.elementToBeClickable(pickedItemCancelButton));
        //had to find the dialog button using the driver as opposed to the dialog element.findeElement() method. For some
        //reason the button is found but is shown as not visible. (There's another hidden dialog me thinks)
        WebElement dialogPickerOKButton = driver.findElement(By.xpath("//div[@id='members_picker_dialog']//div[@class='footer']//span[1]/span[contains(@class,'dijitButtonNode')]"));
        dialogPickerOKButton.click();
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText(User.BOB.firstName()+" "+User.BOB.lastName())));

    }

    @Test
    public void addUserToCaseAsOwner() {
        this.createCaseAsUser(User.ADMIN);
        this.gotoCasePage("members");
        wait.until(ExpectedConditions.elementToBeClickable(addAuthoritiesButton));
        addAuthoritiesButton.click();
        addSimpleCaseOwnersButton.click();
        wait.until(ExpectedConditions.visibilityOf(membersPickerDialog));
        WebElement dialogSearchInput = membersPickerDialog.findElement(By.name("searchTerm"));
        clearAndEnter(dialogSearchInput, User.BRIGITTE.userName());
        clickPickerDialogSearchBtn();
        //the left window where the search result should be displayed
        //The add button which adds the button to the right window where picked items are displayed
        WebElement resultSelector = membersPickerDialog.findElement(By.xpath("//div[@class='sub-pickers']//table//tbody//tr//td[last()]//span"));
        resultSelector.click();
        WebElement pickedItemCancelButton = membersPickerDialog.findElement(By.xpath("//div[@class='picked-items']//table//tbody//tr//td[last()]//span"));
        wait.until(ExpectedConditions.elementToBeClickable(pickedItemCancelButton));
        //had to find the dialog button using the driver as opposed to the dialog element.findeElement() method. For some
        //reason the button is found but is shown as not visible. (There's another hidden dialog me thinks)
        WebElement dialogPickerOKButton = driver.findElement(By.xpath("//div[@id='members_picker_dialog']//div[@class='footer']//span[1]/span[contains(@class,'dijitButtonNode')]"));
        dialogPickerOKButton.click();
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText(User.BRIGITTE.firstName()+" "+User.BRIGITTE.lastName())));

    }

}
