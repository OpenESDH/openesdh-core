package dk.openesdh.share.selenium.framework.pages;


import dk.magenta.share.selenium.framework.Browser;
import dk.openesdh.share.selenium.framework.enums.User;

public class DashboardCasePage extends BaseCasePage {


    /**
     * Is the browser at the users dashboard?
     * @param user
     * @return
     */
    public boolean isAt(User user) {
        String userURL = BASE_URL + "/page/user/" + user.userName() + "/dashboard";

        return userURL.equals(Browser.Driver.getCurrentUrl());
    }

    /**
     * Goto the dashpanel of the user
     * @param user
     */
    public void gotoPage(User user) {
        String userURL = BASE_URL + "/page/user/" + user.userName() + "/dashboard";
        Browser.open(userURL);
    }

}
