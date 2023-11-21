package util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.AbstractDriverOptions;
import org.testng.Assert;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class BrowserProfile {

    private ChromeOptions chromeOptions;
    private FirefoxOptions firefoxOptions;
    private EdgeOptions edgeOptions;

    @Getter
    private String sessionName;
    @Getter
    private final Type browserType;
    private final String browserVersion;
    @Getter
    private String userAgent;
    private Boolean emulateMobile = false;
    @Getter
    private boolean emulateTablet = false;
    @Getter
    @Setter
    private UnexpectedAlertBehaviour unexpectedAlertBehaviour = UnexpectedAlertBehaviour.ACCEPT_AND_NOTIFY; //default value
    private Boolean allowVideoAccess = false;

    public void setEmulateMobile(boolean emulate) {
        Assert.assertEquals(getBrowserType(), Type.CHROME, "Do not support");
        this.emulateMobile = emulate;
    }

    public boolean getEmulateMobile() {
        return emulateMobile;
    }

    public void setEmulateTablet(boolean emulate) {
        Assert.assertEquals(getBrowserType(), Type.CHROME, "Do not support");
        this.emulateTablet = emulate;
    }

    public void allowVideoAccess() {
        Assert.assertEquals(getBrowserType(), Type.CHROME, "Do not support");
        allowVideoAccess = true;
    }

    @Getter
    @RequiredArgsConstructor
    public enum Type {
        CHROME("chrome", "114.0"),
        FIREFOX("firefox", "115.0"),
        EDGE("MicrosoftEdge", "102.0");

        private static final HashMap<String, Type> HUMAN_NAMES = new HashMap<>();

        private final String name;
        private final String defaultVersion;

        static {
            for (BrowserProfile.Type type : BrowserProfile.Type.values()) {
                HUMAN_NAMES.put(type.getName().toUpperCase(), type);
            }
        }

        public static Type getByName(String hName) {
            return HUMAN_NAMES.get(hName.toUpperCase());
        }
    }

    public BrowserProfile(Type type) {
        this(type, type.getDefaultVersion());
    }

    public BrowserProfile(Type type, String version) {
        browserType = type;
        browserVersion = version;
    }

    public BrowserProfile(String browserName) {
        this(Type.getByName(browserName));
    }

    public static BrowserProfile chrome() {
        return new BrowserProfile(Type.CHROME);
    }

    public MutableCapabilities buildCapabilities() {
        buildBrowserSpecificCaps();
        buildCommonCapabilities();
        return getCapabilities();
    }

    public ChromeOptions chromeOptions() {
        if (chromeOptions == null) {
            return buildChromeOptions();
        }
        return chromeOptions;
    }

    private ChromeOptions buildChromeOptions() {
        if (chromeOptions == null) {
            chromeOptions = new ChromeOptions();
        }

        LoggingPreferences logPrefs = new LoggingPreferences();
        logPrefs.enable(LogType.BROWSER, Level.ALL);
        chromeOptions.setCapability(ChromeOptions.LOGGING_PREFS, logPrefs);

        chromeOptions.addArguments("--enable-automation");
        chromeOptions.addArguments("disable-infobars");
        chromeOptions.addArguments("--disable-extensions");
        chromeOptions.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        chromeOptions.setExperimentalOption("useAutomationExtension", false);

        chromeOptions.addArguments("--no-sandbox"); //To prevent error error:    org.openqa.selenium.WebDriverException: unknown error: session deleted because of page crash
        chromeOptions.addArguments("--disable-dev-shm-usage"); //To prevent error error:    org.openqa.selenium.WebDriverException: unknown error: session deleted because of page crash

        if (customUserAgentProvided()) {
            chromeOptions.addArguments(userAgent);
        }

        if (getEmulateMobile()) {
            Map<String, String> mobileEmulation = new HashMap<>();
            mobileEmulation.put("deviceName", "iPhone 12 Pro");
            chromeOptions.setExperimentalOption("mobileEmulation", mobileEmulation);
        }
        if (emulateTablet) {
            Map<String, String> mobileEmulation = new HashMap<>();
            mobileEmulation.put("deviceName", "iPad");
            chromeOptions.setExperimentalOption("mobileEmulation", mobileEmulation);
        }
        if (allowVideoAccess) {
            chromeOptions.addArguments("use-fake-ui-for-media-stream");
            chromeOptions.addArguments("use-fake-device-for-media-stream");
        }
        return chromeOptions;
    }

    public FirefoxOptions firefoxOptions() {
        if (firefoxOptions == null) {
            return buildFirefoxOptions();
        }
        return firefoxOptions;
    }

    public FirefoxOptions buildFirefoxOptions() {
        if (firefoxOptions == null) {
            firefoxOptions = new FirefoxOptions();
        }
        if (customUserAgentProvided()) {
            firefoxOptions.addPreference("general.useragent.override", userAgent);
        }
        return firefoxOptions;
    }

    public EdgeOptions buildEdgeOptions() {
        if (edgeOptions == null) {
            edgeOptions = new EdgeOptions();
        }
        Assert.assertFalse(customUserAgentProvided(), "Custom UserAgent setup is not implemented for " + Type.EDGE);
        return edgeOptions;
    }

    private AbstractDriverOptions buildBrowserSpecificCaps() {
        Type browser = getBrowserType();
        switch (browser) {
            default:
                Assert.fail("Unknown browser type: " + browser);
            case CHROME:
                return buildChromeOptions();
            case FIREFOX:
                return buildFirefoxOptions();
            case EDGE:
                return buildEdgeOptions();
        }
    }

    private boolean customUserAgentProvided() {
        String ua = getUserAgent();
        return ua != null
                && !ua.equals("default")
                && !ua.isBlank()
                ;
    }

    private void buildCommonCapabilities() {
        capabilities().setBrowserVersion(browserVersion);
        capabilities().setUnhandledPromptBehaviour(unexpectedAlertBehaviour);
        Map<String, Object> selenoidOptions = new HashMap<>();
        selenoidOptions.put("enableVNC", true); //https://aerokube.com/selenoid/latest/#_live_browser_screen_enablevnc
        selenoidOptions.put("name", sessionName);
        selenoidOptions.put("sessionTimeout", "2m");
        setCustomOptions("selenoid:options", selenoidOptions);
//        caps.setCapability("enableVideo", true); //to enable video recording for tests on selenoid. For using we have to download a separate docker image
    }

    public void setCustomOptions(String name, Map<String, Object> caps) {
        capabilities().setCapability(name, caps);
    }

    /**
     * @returns - current Capabilities setup.
     * <b>Please note!!!</b> It doesn't add default capabilities<br/>
     * To get full capabilities including browser default values please use BrowserProfile.buildCapabilities();
     */
    public AbstractDriverOptions getCapabilities() {
        return capabilities();
    }

    private AbstractDriverOptions capabilities() {
        AbstractDriverOptions caps;
        switch (getBrowserType()) {
            default:
                Assert.fail("Unknown browser type " + getBrowserType());
            case CHROME:
                caps = chromeOptions;
                break;
            case FIREFOX:
                caps = firefoxOptions;
                break;
            case EDGE:
                caps = edgeOptions;
                break;
        }
        if (caps == null) {
            return buildBrowserSpecificCaps();
        }
        return caps;
    }

    public void setUserAgent(String userAgent) {
        if (userAgent.equals("default")) {
            return;
        }
        this.userAgent = userAgent;
    }

    public void setPageLoadStrategy(PageLoadStrategy pls) {
        capabilities().setPageLoadStrategy(pls);
    }

    public void setSessionName(String name) {
        this.sessionName = name;
    }

    @Deprecated
    public void addCapabilities(MutableCapabilities caps) {
        getCapabilities().merge(caps);
    }


}
