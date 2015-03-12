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

public class SearchPage extends BasePage {
    private static final String URL = BASE_URL + "/page/oe/case/search";

    public boolean isAt() {
        return Browser.Driver.getCurrentUrl().startsWith(URL);
    }

    public void gotoPage() {
        Browser.open(URL);
    }
}
