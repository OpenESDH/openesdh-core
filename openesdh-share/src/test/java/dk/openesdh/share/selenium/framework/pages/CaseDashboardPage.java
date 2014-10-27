package dk.openesdh.share.selenium.framework.pages;

import dk.openesdh.share.selenium.framework.Browser;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CaseDashboardPage extends BasePage {
    private static final String URL = BASE_URL + "/page/hdp/ws/dk-openesdh-pages-case-dashboard";

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
}
