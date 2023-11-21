package util;

import lombok.SneakyThrows;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.InvalidCookieDomainException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.Assert;
import ui.abstractObjects.containers.AbstractElementsContainer;
import ui.steps.Steps;
import util.log.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class Browser {

    @SneakyThrows
    public static <T extends AbstractElementsContainer> T switchToWindowWithPage(T page) {
        waitWindowContainsUrlAndSwitchToIt(page.getUrlPartForWindowHandleCalculation());
        return page;
    }

    public static void waitWindowContainsUrlAndSwitchToIt(String url) {
        String handles = new WaitFactory().waitTillWindowWithUrlPartOpened(url, WaitFactory.DEFAULT_EXPLICIT_TIMEOUT);
        Browser.switchToWindow(handles);
    }

    public static void switchToWindow(String windowHandle) {
        DriverConfig.getDriver().switchTo().window(windowHandle);
        Log.switchedToWindowMsg(windowHandle);
    }

    public static String getWindowHandleWhichContainsUrl(String urlPart) {
        ArrayList<String> handles = getWindowHandlesListContainsUrl(urlPart);
        if (handles.isEmpty()) {
            handles = waitWindowHandlesContainsUrl(urlPart);
        }
        Assert.assertNotEquals(handles.size(), 0, "Window with URL: '" + urlPart + "' was not found. List of available windows: \n" + handles);
        return handles.get(0);

    }

    public static String getWindowHandleWhichContainsUrl(String urlPart, long sleepSeconds) {
        ArrayList<String> handles = getWindowHandlesListContainsUrl(urlPart, sleepSeconds);
        if (handles.isEmpty()) {
            handles = waitWindowHandlesContainsUrl(urlPart);
        }
        Assert.assertFalse(handles.size() > 1, "More that 1 window handle found with URL: '" + urlPart + "'");
        Assert.assertNotEquals(handles.size(), 0, "Window with URL: '" + urlPart + "' was not found. List of available windows: \n" + handles);
        return handles.get(0);
    }

    public static ArrayList<String> getWindowHandlesListContainsUrl(String url) {
        return getWindowHandlesListContainsUrl(url, 0);
    }

    private static ArrayList<String> waitWindowHandlesContainsUrl(String url) {
        ArrayList<String> handles = getWindowHandlesListContainsUrl(url, 0);
        int attempts = 0;
        while (handles.isEmpty()) {
            Log.logInConsole("Widow handle with url '" + url + "' was not found.");
            WaitFactory.sleep(WaitFactory.TIMEOUT_2);
            handles = getWindowHandlesListContainsUrl(url, 0);
            attempts++;
            if (attempts > 5) {
                break;
            }
        }
        return handles;
    }

    private static HashMap<String, String> getWindowHandlesWithUrls(long sleepSeconds) {
        String initial = getWindowHandle();
        String initialUrl = Steps.getCurrentUrl();
        Log.debug("Initial Window: " + initial);
        HashMap<String, String> handelesVsUrls = new HashMap<>();
        //add url from initial handle
        handelesVsUrls.put(initial, initialUrl);

        //add matched URLs from other windows.
        for (String window : getWindowHandles()) {
            if (window.equals(initial)) {
                //skip initial window
                continue;
            }
            switchToWindow(window);
            //the block is added as workaround for  org.openqa.selenium.WebDriverException: unknown error: cannot determine loading status from no such execution context
            if (sleepSeconds > 0) {
                WaitFactory.sleep(sleepSeconds);
            }
            handelesVsUrls.put(window, Steps.getCurrentUrl());
        }
        switchToWindow(initial);
        return handelesVsUrls;
    }

    private static ArrayList<String> getWindowHandlesListContainsUrl(String urlPart, long sleepSeconds) {
        ArrayList<String> matchedUrls = new ArrayList<>();
        HashMap<String, String> windows = getWindowHandlesWithUrls(sleepSeconds);
        for (Map.Entry<String, String> entry : windows.entrySet()) {
            String handle = entry.getKey();
            String url = windows.get(handle);
            if (url != null && url.contains(urlPart)) {
                matchedUrls.add(handle);
            }
        }
        return matchedUrls;
    }

    public static String getWindowHandle() {
        return DriverConfig.getDriver().getWindowHandle();
    }

    public static Set<String> getWindowHandles() {
        return DriverConfig.getDriver().getWindowHandles();
    }

    public static String getBrowserInfo() {
        try {
            if (DriverConfig.driverCreated()) {
                Capabilities caps = ((RemoteWebDriver) DriverConfig.getDriver()).getCapabilities();
                return caps.getBrowserName() + " v." + caps.getBrowserVersion();
            } else {
                return "Browser info is not available since Driver is null.";
            }
        } catch (Throwable e) {
            return "Unable to get Browser info due to error: " + e.getMessage();
        }
    }

    public static LogEntries getConsoleLogs() {
        return DriverConfig.getDriver()
                .manage()
                .logs()
                .get(LogType.BROWSER);
    }

    public static void closeWindow() {
        DriverConfig.getDriver().close();
        Log.logInConsole("Browser window was closed.");
    }

    public static void closeWindow(String windowHandle) {
        switchToWindow(windowHandle);
        closeWindow();
        Log.logInConsole("Browser window(handle = " + windowHandle + ") was closed.");
    }

    public static void get(String url) {
        DriverConfig.getDriver().get(url);
        Log.logInReport("Navigated to " + url);
    }

    public static void openInNewTab(String url) {
        Steps.executeJsScript("window.open('" + url + "');");
        switchToWindowContainsUrl(url);
    }

    public static void closeAllWindowsExceptCurrent() {
        Set<String> windows = getWindowHandles();
        if (windows.size() <= 1) {
            Log.debug("Only one browser window is opened.");
            return;
        }
        String currentWindow = getWindowHandle();
        for (String window : windows) {
            if (!window.equals(currentWindow)) {
                Browser.switchToWindow(window);
                Browser.closeWindow();
            }
        }
        Browser.switchToWindow(currentWindow);
    }

    public static void setCookies(Set<Cookie> cookies) {
        for (Cookie cookie :
                cookies) {
            try {
                addCookie(cookie);
            } catch (InvalidCookieDomainException e) {
                Log.debug("Not able to set cookie: " +
                        "\nName/Domain: " + cookie.getName() + "/" + cookie.getDomain() +
                        "\nValue:" + cookie.getValue()
                );
                Log.debug("Cookie: " + cookie);
            }
        }
        Log.logInConsole("Cookies were added to the web driver.");
    }

    public static void addCookie(Cookie cookie) {
        DriverConfig.getDriver().manage().addCookie(cookie);
        Log.logInConsole("Cookies '" + cookie.getName() + "' = '" + cookie.getValue() + "' (domain = '" + cookie.getDomain() + "') were set to browser");
    }

    public static void deleteCookie(String name) {
        if (DriverConfig.getDriver().manage().getCookieNamed(name) == null) {
            return;
        }
        DriverConfig.getDriver().manage().deleteCookieNamed(name);
    }

    private static void printCookie(String title) {
        Log.debug("-------------- " + title + " -----------------");
        for (Cookie c :
                DriverConfig.getDriver().manage().getCookies()) {
            System.out.println("Name: " + c.getName() + "; Domain:" + c.getDomain() + "; Path: " + c.getPath() + "; Value: " + c.getValue());
        }
        Log.debug("-------------- End -----------------");
    }

    public static void closeWindowAndSwitchTo(String windowHandle) {
        Assert.assertTrue(DriverConfig.getDriver().getWindowHandles().size() > 1,
                "Browser has only 1 window opened. Most likely new window was not opened during the previous actions.");
        Browser.closeWindow();
        switchToWindow(windowHandle);
    }

    public static void closeWindowAndSwitchToWindowContainsURL(String url) {
        String handle = getWindowHandleWhichContainsUrl(url);
        closeWindowAndSwitchTo(handle);
    }

    public static void switchToWindowContainsUrl(String urlSubstring) {
        String handle = getWindowHandleWhichContainsUrl(urlSubstring);
        switchToWindow(handle);
    }

    public static void closeWindowAndSwitchToWindowContainsURL(String url, long sleepSeconds) {
        String handles = getWindowHandleWhichContainsUrl(url, sleepSeconds);
        closeWindowAndSwitchTo(handles);
    }

    @SneakyThrows
    public static <T extends AbstractElementsContainer> T closeWindowAndSwitchToWindowWithPage(T page) {
        closeWindowAndSwitchToWindowContainsURL(page.getUrlPartForWindowHandleCalculation());
        page.getClass().getDeclaredConstructor().newInstance();
        return page;
    }

    public static Set<Cookie> getCookies() {
        return DriverConfig.getDriver().manage().getCookies();
    }

    public static void switchToDefaultFrame() {
        DriverConfig.getDriver().switchTo().defaultContent();
        Log.logInConsole("Driver switched to the parent frame.");
    }

    public static void switchToIFrame(WebElement iframe) {
        DriverConfig.getDriver().switchTo().frame(iframe);
    }
}
