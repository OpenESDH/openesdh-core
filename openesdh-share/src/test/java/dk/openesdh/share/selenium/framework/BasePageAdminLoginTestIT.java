package dk.openesdh.share.selenium.framework;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import dk.magenta.share.selenium.framework.Browser;
import dk.openesdh.share.selenium.framework.enums.User;

public abstract class BasePageAdminLoginTestIT {

	@BeforeClass
	public static void setUpBeforeClass() {
		Browser.initialize();
		Pages.initialize();
		Pages.Login.loginWith(User.ADMIN);
	}

	public BasePageAdminLoginTestIT() {

	}

	@Before
	public void setup() {

	}

	@After
	public void tearDown() {

	}

	@AfterClass
	public static void tearDownAfterClass() {
		Pages.Login.logout();
		Browser.Driver.close();
	}

}
