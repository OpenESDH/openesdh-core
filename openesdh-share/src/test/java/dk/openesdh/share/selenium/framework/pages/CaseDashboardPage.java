package dk.openesdh.share.selenium.framework.pages;

import dk.openesdh.share.selenium.framework.Browser;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CaseDashboardPage extends BasePage {
    private static final String URL = BASE_URL + "/page/hdp/ws/dk-openesdh-pages-case-dashboard";

    @FindBy(id = "HEADER_CASE_CONFIGURATION_DROPDOWN")
    WebElement headerCaseConfigDropdown;

    @FindBy(id = "HEADER_CASE_JOURNALIZE_text")
    WebElement headerJournalizeItem;

    @FindBy(id = "HEADER_CASE_UNJOURNALIZE_text")
    WebElement headerUnJournalizeItem;

    public boolean isAt() {
        return Browser.Driver.getCurrentUrl().startsWith(URL);
    }

    public String getNodeRef() {
        Pattern p = Pattern.compile("nodeRef=(.+)");
        Matcher matcher = p.matcher(Browser.Driver.getCurrentUrl());
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return null;
        }
    }

    public void gotoPage(String nodeRef) {
        Browser.open(URL + "?nodeRef=" + nodeRef);
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
    }
}
