package dk.openesdh.share.selenium.framework;

import java.util.concurrent.TimeUnit;

import dk.magenta.share.selenium.framework.Browser;
import dk.openesdh.share.selenium.framework.pages.BaseCasePage;
import dk.openesdh.share.selenium.framework.pages.*;

import org.openqa.selenium.support.PageFactory;

/**
 * Class for handling Pages on share
 * CAVEAT! Use this to access a page in testcase!
 * @author Søren Kirkegård
 *
 */
public class Pages {

    public static LoginCasePage Login;
    public static DashboardCasePage Dashboard;
    public static AdminToolsPage AdminToolsPage;
    public static SearchCasePage Search;
    public static EditCaseCasePage EditCase;
    public static CaseDashboardCasePage CaseDashboard;
    public static CaseDocumentsCasePage CaseDocumentsPage;
    public static DocumentDetailsCasePage DocumentDetails;

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
        Login = (LoginCasePage) initializePage(new LoginCasePage());
        AdminToolsPage = (AdminToolsPage) initializePage(new AdminToolsPage());
        Dashboard = (DashboardCasePage) initializePage(new DashboardCasePage());
        Search = (SearchCasePage) initializePage(new SearchCasePage());
        EditCase = (EditCaseCasePage) initializePage(new EditCaseCasePage());
        CaseDashboard = (CaseDashboardCasePage) initializePage(new CaseDashboardCasePage());
        DocumentDetails = (DocumentDetailsCasePage) initializePage(new DocumentDetailsCasePage());
        CaseDocumentsPage = (CaseDocumentsCasePage) initializePage(new CaseDocumentsCasePage());
    }
}
