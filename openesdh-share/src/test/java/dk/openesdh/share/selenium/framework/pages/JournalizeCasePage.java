package dk.openesdh.share.selenium.framework.pages;

import dk.openesdh.share.selenium.framework.Browser;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class JournalizeCasePage extends BasePage {
    private static final String URL = BASE_URL + "/proxy/alfresco/api/openesdh/journalize";



    public boolean isAt() {
        return Browser.Driver.getCurrentUrl().startsWith(URL);
    }

    public void gotoPage(String nodeRef, String journalizeKey) {
        // TODO: Navigate to the page from the case dashboard
        Browser.Driver.get(URL + "?nodeRef=" + nodeRef + "&journalizeKey" + journalizeKey);
    }



}
