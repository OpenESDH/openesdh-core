package dk.openesdh.test.selenium.framework.pages;


import dk.openesdh.test.selenium.framework.Browser;
import dk.openesdh.test.selenium.framework.enums.User;

public class DashboardPage extends BasePage {


    /**
     * Is the browser at the users dashboard?
     * @param user
     * @return
     */
    public boolean isAt(User user) {
        String userURL = BASE_URL + "/page/user/" + user.username() + "/dashboard";

        return userURL.equals(Browser.Driver.getCurrentUrl());
    }

    /**
     * Goto the dashpanel of the user
     * @param user
     */
    public void gotoPage(User user) {
        String userURL = BASE_URL + "/page/user/" + user.username() + "/dashboard";
        Browser.open(userURL);

    }

}
