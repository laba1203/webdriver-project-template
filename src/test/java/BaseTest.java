import org.openqa.selenium.PageLoadStrategy;
import org.testng.annotations.*;
import util.BrowserProfile;
import util.DriverConfig;
import util.ExecutionVariables;
import util.execution.TestExecutionManager;
import util.execution.TestProfile;
import util.log.ExtentManager;
import util.log.Log;
import util.mail.MailReader;

import java.util.Set;

public class BaseTest {
    private static final PageLoadStrategy DEFAULT_PAGE_LOAD_STRATEGY = PageLoadStrategy.NORMAL;
    TestProfile testProfile = new TestProfile();
    private BrowserProfile browserProfile;
    protected String userAgent = "default";
    protected BrowserProfile.Type browser = BrowserProfile.Type.CHROME;


    //setup driver
    @Parameters("browser")
    @BeforeClass(alwaysRun = true)
    public void commonSetup(@Optional String browser) throws Exception {
        TestProfile.setCurrentTestClass(this.getClass().getName());

        setBrowser(browser);
        testConfiguration();

        setupTestProfile();
        setupBrowserProfile();
        DriverConfig.createDriver(browserProfile());
    }

    @AfterClass(alwaysRun = true)
    public void quit() {
        DriverConfig.quitAndRemoveWebDriver();
        System.out.println("*** DEBUG: After Class executed in Base Test of class: " + getClass().getName() + " ***\r\n");

        ExtentManager.assignDeviceToClass(this.getClass(), getBrowser().getName());//It should be executed in AfterClass to have the device label on Class level
        for (String errorTag :
                TestProfile.getConsoleCapturedErrorTags()) {
            ExtentManager.assignCategoryToClass(this.getClass(), errorTag);
        }
        getTestProfile().executionCompleted();
    }

    @AfterSuite(alwaysRun = true)
    public void afterSuiteActions() {
        Log.debug("@AfterSuite has been started.");
        try {
            //close pending driver watchers:
            Set<String> driverWatchesIds = TestExecutionManager.getDriverWatchers().keySet();
            Log.debug("Driver Watchers set in @AfterSuite: " + driverWatchesIds);
            for (String id : driverWatchesIds) {
                TestExecutionManager.getDriverWatchers().get(id).close();
                TestExecutionManager.removeDriverWatchers(id);
            }
            if (!ExecutionVariables.testOnLocalMachine()) {
                new MailReader().deleteEmails();
            }
        } catch (Exception e) {
            Log.debug("Exception in @AfterSuite was captured.");
            e.printStackTrace();
        }
        Log.logRecord("@AfterSuite has been completed.");
    }

    /**
     * The method is designed to provide custom configuration for the test
     */
    protected void testConfiguration() {
        assignBrowserProfile();
    }

    protected TestProfile getTestProfile() {
        return testProfile;
    }

    protected void setDefaultBrowserProfile() {
        browserProfile = new BrowserProfile(getBrowser());
    }

    protected BrowserProfile browserProfile() {
        if (browserProfile == null) {
            setDefaultBrowserProfile();
        }
        return browserProfile;
    }

    void setupTestProfile() {
        testProfile = new TestProfile();
    }

    protected void setupBrowserProfile() {
        browserProfile().setPageLoadStrategy(getPageLoadStrategy());
        if (browserProfile().getUserAgent() == null || browserProfile().getUserAgent().equals("default")) {
            browserProfile().setUserAgent(userAgent);
        }
        browserProfile().setSessionName(testName());
    }

    protected String testName() {
        String classPath = this.getClass().getName();
        if (ExecutionVariables.testOnLocalMachine()) {
            String prefix = "";
            if (DriverConfig.isUsingSecondDriver()) {
                prefix = "#2";
            }
            String className = classPath.substring(classPath.lastIndexOf("."));
            return prefix + "[Local_Run]_" + className;
        }
        return classPath;
    }

    protected BrowserProfile.Type getBrowser() {
        return browser;
    }

    protected void setBrowser(String name) {
        if (name != null) {
            setBrowser(BrowserProfile.Type.getByName(name));
        }
    }

    protected void setBrowser(BrowserProfile.Type browser) {
        assert browser != null;
        this.browser = browser;
    }

    protected void setBrowserProfile(BrowserProfile bp) {
        browserProfile = bp;
    }

    protected void clearBrowserProfile() {
        setupBrowserProfile();
    }

    private void assignBrowserProfile() {
        if (browserProfile == null) {
            setDefaultBrowserProfile();
        }
    }

    protected PageLoadStrategy getPageLoadStrategy() {
        return DEFAULT_PAGE_LOAD_STRATEGY;
    }

}
