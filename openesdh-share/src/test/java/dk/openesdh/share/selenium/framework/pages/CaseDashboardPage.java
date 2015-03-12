package dk.openesdh.share.selenium.framework.pages;

import dk.openesdh.share.selenium.framework.Browser;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CaseDashboardPage extends BasePage {

    public static final String URL = BASE_URL + "/page/oe/case/@@ID@@/dashboard";

    @FindBy(id = "HEADER_CASE_CONFIGURATION_DROPDOWN")
    WebElement headerCaseConfigDropdown;

    @FindBy(id = "HEADER_CASE_JOURNALIZE_text")
    WebElement headerJournalizeItem;

    @FindBy(id = "HEADER_CASE_EDIT_text")
    WebElement headerCaseEditButton;


    @FindBy(id = "HEADER_CASE_UNJOURNALIZE_text")
    WebElement headerUnJournalizeItem;


    public void gotoPage(String caseId) {
        Browser.open(URL.replace("@@ID@@", caseId) );
    }

    public boolean isAt() {
        String tmpUrl = URL.replace("@@ID@@", this.getCaseId());
        return Browser.Driver.getCurrentUrl().startsWith(tmpUrl);
    }

    public void edit() {
        headerCaseConfigDropdown.click();
        headerCaseEditButton.click();
    }


    public void journalize(String journalKey) {
        headerCaseConfigDropdown.click();
        headerJournalizeItem.click();


        // TODO: Use more robust method of handling the category picker than
        // keyboard shortcuts

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//        Browser.Driver.findElement(By.cssSelector("*[role='dialog'] input")).click();

        // Click the select button
        Browser.Driver.switchTo().activeElement().click();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Choose the first category
        Browser.Driver.switchTo().activeElement().click();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Tab over to the OK button
        Browser.Driver.switchTo().activeElement().sendKeys("\t");

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Click OK
        Browser.Driver.switchTo().activeElement().click();

        // Accept the confirmation dialog
        Alert alert = Browser.Driver.switchTo().alert();
        alert.accept();
    }

    public void unJournalize() {
        headerCaseConfigDropdown.click();
        headerUnJournalizeItem.click();

        // Accept the confirmation dialog
        Alert alert = Browser.Driver.switchTo().alert();
        alert.accept();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
