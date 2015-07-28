package dk.openesdh.share.selenium.framework.pages;

import dk.magenta.share.selenium.framework.Browser;

public class SearchCasePage extends BaseCasePage {
    private static final String URL = BASE_URL + "/page/oe/case/search";

    public boolean isAt() {
        return Browser.Driver.getCurrentUrl().startsWith(URL);
    }

    public void gotoPage() {
        Browser.open(URL);
    }
}
