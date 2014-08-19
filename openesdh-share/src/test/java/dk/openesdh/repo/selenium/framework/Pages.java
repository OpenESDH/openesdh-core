package dk.openesdh.repo.selenium.framework;

import java.util.concurrent.TimeUnit;

import dk.openesdh.repo.selenium.framework.pages.BasePage;
import dk.openesdh.repo.selenium.framework.pages.DashboardPage;
import dk.openesdh.repo.selenium.framework.pages.LoginPage;
import org.openqa.selenium.support.PageFactory;

/**
 * Class for handling Pages on share
 * CAVEAT! Use this to access a page in testcase!
 * @author Søren Kirkegård
 *
 */
public class Pages {

    public static LoginPage Login;
    public static DashboardPage Dashboard;

    /**
     * helper method to initElements on a page
     * @param page BasePage that has elements that needs initializing
     * @return an initialized version of the BasePage
     */
    private static BasePage initializePage(BasePage page) {

        // Wait for the page to have loaded
        Browser.Driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

        PageFactory.initElements(Browser.Driver, page);
        return page;
    }

    /**
     * Public static method to initialize the pages. Remember to run this
     * whenever you're creating a new Driver.
     */
    public static void initialize() {
        Login = (LoginPage) initializePage(new LoginPage());
        Dashboard = (DashboardPage) initializePage(new DashboardPage());

    }
}
