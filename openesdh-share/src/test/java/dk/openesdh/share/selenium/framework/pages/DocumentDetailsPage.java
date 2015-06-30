package dk.openesdh.share.selenium.framework.pages;

import dk.magenta.share.selenium.framework.Browser;
import dk.openesdh.share.selenium.framework.pages.BasePage;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class DocumentDetailsPage extends BasePage {
    private static final String URL = BASE_URL + "/page/document-details";

    public boolean isAt() {
        return Browser.Driver.getCurrentUrl().startsWith(URL);
    }

    public void gotoPage(String nodeRef) {
        Browser.open(URL + "?nodeRef=" + nodeRef);
    }

    /**
     * Deletes the node at the current page.
     */
    public void deleteNode() {
        WebElement deleteAction = Browser.Driver.findElement(By.cssSelector
                ("#onActionDelete a.action-link"));
        deleteAction.click();
        WebElement deleteButton = Browser.Driver.findElement(By.cssSelector
                ("#prompt button"));
        deleteButton.click();
    }
}
