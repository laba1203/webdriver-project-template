package util;

import lombok.SneakyThrows;
import org.apache.commons.exec.OS;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.Assert;
import util.log.Log;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;


public class DriverConfig {
    //URL for selenium
    public static final String SELENOID_URL = "http://selenium.hub:4444/";

    public static final String REMOTE_MACHINE_URL = SELENOID_URL + "wd/hub"; //new server
    private static final ThreadLocal<WebDriver> PRIMARY_TL_DRIVER = new ThreadLocal<>();
    private static final ThreadLocal<WebDriver> SECOND_TL_DRIVER = new ThreadLocal<>();
    private static final ThreadLocal<Type> ACTIVE_TYPE = new ThreadLocal<>();

    public enum Type {PRIMARY, SECONDARY}


    private static void setThreadLocalDriver(WebDriver driver) {
        switch (getActiveDriverType()) {
            default:
            case PRIMARY:
                setPrimaryThreadLocalDriver(driver);
                break;
            case SECONDARY:
                setSecondThreadLocalDriver(driver);
                break;
        }
    }

    private static void setPrimaryThreadLocalDriver(WebDriver driver) {
        PRIMARY_TL_DRIVER.set(driver);
    }

    private static void setSecondThreadLocalDriver(WebDriver driver) {
        SECOND_TL_DRIVER.set(driver);
    }

    private void setNewLocalDriver(BrowserProfile bp) {
        Log.logInConsole("Util: Start creation of new Local WebDriver");
        String driverPathProperty = "path.mac.webDriver";
        if (OS.isFamilyWindows()) {
            driverPathProperty = "path.win.webDriver";
            Log.logInConsole("Test is running on Windows OS.");
        }
        final File file = new File(PropertyLoader.loadProperty(driverPathProperty));
        System.setProperty(PropertyLoader.loadProperty("webDriver"), file.getAbsolutePath());
        ChromeOptions options = new ChromeOptions();
        options.merge(bp.buildCapabilities());
        setThreadLocalDriver(new ChromeDriver(options));

        Log.logInConsole("Util: New Local WebDriver is created");
    }

    private static void setNewRemoteDriver(BrowserProfile browserProfile) throws MalformedURLException {
        Log.logInConsole("Util: Start creation of new Remote WebDriver. Thread ID: <" + Thread.currentThread().getId() + ">");
        RemoteWebDriver rDriver;
        try {
            rDriver = new RemoteWebDriver(new URL(REMOTE_MACHINE_URL), browserProfile.buildCapabilities());
        } catch (WebDriverException e) {
            Log.debug("WebDriverException was captured during driver creation. \nDetails: " + e.getMessage());
            e.printStackTrace();
            rDriver = new RemoteWebDriver(new URL(REMOTE_MACHINE_URL), browserProfile.buildCapabilities());
        }
        rDriver.setFileDetector(new LocalFileDetector()); //for uploading file into remote machine
        setThreadLocalDriver(rDriver);
        Log.logInConsole("Util: New Remote WebDriver created. Thread ID: <" + Thread.currentThread().getId() + ">.");
        setDefaultBrowserSize();

    }

    private static void createBrowserStackDriver(String browserName, String sessionName) throws Exception {
        setThreadLocalDriver(new BrowserStackDriver().getDriver(browserName, sessionName));
        Log.logInConsole("Driver was create in BrowserStack.");
    }

    private static void createSelenoidDriver(BrowserProfile browserProfile) throws Exception {
        if (ExecutionVariables.remoteExecution()) {
            //for remote testing
            setNewRemoteDriver(browserProfile);
        } else {
            //for local testing
            new DriverConfig().setNewLocalDriver(browserProfile);
        }
    }

    public static boolean driverNotCreated() {
        return !driverCreated();
    }

    public static boolean driverCreated() {
        return primaryDriverCreated();
    }

    private static boolean driverCreated(Type type) {
        switch (type) {
            default:
            case PRIMARY:
                return primaryDriverCreated();
            case SECONDARY:
                return secondDriverCreated();
        }
    }

    private static boolean primaryDriverCreated() {
        return PRIMARY_TL_DRIVER.get() != null;
    }

    public static boolean secondDriverCreated() {
        return SECOND_TL_DRIVER.get() != null;
    }

    public static void clearAllCookies() {
        WebDriver driver = getDriver();
        driver.manage().deleteAllCookies();
        setThreadLocalDriver(driver);
        Log.logInConsole("All browser cookies have been cleared.");
    }

    public static WebDriver getDriver() {
        return getDriver(getActiveDriverType());
    }

    private static WebDriver getDriver(Type type) {
        Assert.assertTrue(driverCreated(type), "FAILED in DriverConfig. Getting " + type + " driver when it was not created. Thread <" + Thread.currentThread().getId() + ">");
        return switch (type) {
            default -> PRIMARY_TL_DRIVER.get();
            case SECONDARY -> SECOND_TL_DRIVER.get();
        };
    }

    public static boolean isUsingPrimaryDriver() {
        return getActiveDriverType() == Type.PRIMARY;
    }

    public static boolean isUsingSecondDriver() {
        return getActiveDriverType() == Type.SECONDARY;
    }

    private static Type getActiveDriverType() {
        if (ACTIVE_TYPE.get() == null) {
            ACTIVE_TYPE.set(Type.PRIMARY);
            return Type.PRIMARY;
        }
        return ACTIVE_TYPE.get();
    }

    public static void useSecondDriver() {
        useSecondDriver(true);
    }

    public static void useSecondDriver(boolean b) {
        if (b) {
            ACTIVE_TYPE.set(Type.SECONDARY);
        } else {
            ACTIVE_TYPE.set(Type.PRIMARY);
        }
    }

    @SneakyThrows
    public static void actionsInSecondDriver(Retry.RetryCallback action) {
        useSecondDriver(true);
        action.call();
        quitSecondDriver();
    }

    public static void quitSecondDriver() {
        if (driverCreated(Type.SECONDARY)) {
            quitDriver(Type.SECONDARY);
        }
        useSecondDriver(false);
    }

    public static void createDriver(BrowserProfile.Type browser, String sessionName) throws Exception {
        BrowserProfile bp = new BrowserProfile(browser);
        bp.setSessionName(sessionName);
        createDriver(new BrowserProfile(browser));
    }

    public static void createDriver(BrowserProfile browserProfile) throws Exception {
        if (driverCreated(getActiveDriverType())) {
            quitAndRemoveActiveDriver();
        }
        if (EnvFactory.browserStackExecution()) {
            createBrowserStackDriver(browserProfile.getBrowserType().getName(), browserProfile.getSessionName());
        } else {
            createSelenoidDriver(browserProfile);
        }
        Log.logInConsole("WebDriver successfully created.");
        Log.logDriverInfo(browserProfile);
        WaitFactory.setDefaultImplicitlyWait();
        Log.logInConsole("Default waits were set up in the WaitFactory.class" +
                "\n * Implicit wait: " + WaitFactory.DEFAULT_IMPLICIT_TIME_OUT_30 +
                "\n * Page load timeout: " + WaitFactory.DEFAULT_PAGE_LOAD_TIMEOUT
        );
    }

    private static void quitAndRemoveActiveDriver() {
        try {
            getDriver().quit();
        } catch (org.openqa.selenium.WebDriverException e) {
            Log.debug("Following error was returned during driver.quit() \n"
                    + e.getMessage() + "\n"
                    + Arrays.toString(e.getStackTrace()));
        }
        if (isUsingSecondDriver()) {
            SECOND_TL_DRIVER.remove();
        } else {
            PRIMARY_TL_DRIVER.remove();
        }

    }

    public static void quitDriver(Type type) {
        getDriver(type).quit();
    }

    public static void quitAndRemoveWebDriver() {
        try {
            if (primaryDriverCreated()) {
                getDriver(Type.PRIMARY).quit();
            }
            if (secondDriverCreated()) {
                getDriver(Type.SECONDARY).quit();
            }
        } catch (org.openqa.selenium.WebDriverException e) {
            Log.debug("Following error was returned during driver.quit() \n"
                    + e.getMessage() + "\n"
                    + Arrays.toString(e.getStackTrace()));
        }
        PRIMARY_TL_DRIVER.remove();
        SECOND_TL_DRIVER.remove();
    }

    public static void resizeBrowser(int width, int height) {
        Dimension d = new Dimension(width, height);
        getDriver().manage().window().setSize(d);
        Log.logInConsole("Browser size has been set to: " + width + "x" + height);
    }

    public static void setDefaultBrowserSize() {
        resizeBrowser(1300, 1600);
    }

}
