package ui.abstractObjects.containers;

import com.google.common.base.Stopwatch;
import lombok.SneakyThrows;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import ui.abstractObjects.elements.BasicElementWrapper;
import ui.abstractObjects.elements.Element;
import ui.abstractObjects.elements.ElementImpl;
import ui.abstractObjects.elements.SelectImpl;
import ui.abstractObjects.elements.factory.ElementFactory;
import util.DriverConfig;
import util.Util;
import util.WaitFactory;
import util.exceptions.ObjectNotFound;
import util.log.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static util.WaitFactory.TIMEOUT_1;
import static util.WaitFactory.TIMEOUT_3;

public abstract class AbstractElementsContainer implements ContainerWithElements, ContainerWithRows {
    protected static final long DEFAULT_TIMEOUT = WaitFactory.DEFAULT_EXPLICIT_TIMEOUT;
    protected static final long EXTENDED_TIMEOUT = WaitFactory.EXTENDED_EXPLICIT_TIMEOUT;
    private static final long DEFAULT_PAGE_LOAD_TIMEOUT = WaitFactory.DEFAULT_PAGE_LOAD_TIMEOUT;

    private final WaitFactory waitFactory = new WaitFactory();
    private final JavascriptExecutor js = (JavascriptExecutor) DriverConfig.getDriver();

    private long defaultElementWaitTimeout = TIMEOUT_3;
    protected String urlPartForWindowHandleCalculation;

    @FindBy(css = ".loadingMessage") //loader with message "Loading..."
    private Element loadingMsgLctr;
    @FindBy(css = "[class *= 'Loader']")
    private Element defaultLoader;
    @FindBy(css = "[class *= 'Loader']")
    private Element loader2;
    @FindBy(css = ".preloader")//loader circle
    private Element preloader;
    @FindBy(css = "[class = 'base-loader']")
    private Element baseLoaderLctr;
    @FindBy(css = "[class = 'base-loader'], .preloader, .loadingMessage, [class *= 'Loader']")
    private Element allLoaders;


    public AbstractElementsContainer() {
        initElements();
    }

    protected void initElements() {
        ElementFactory.initElements(this);
    }

    private final HashMap<Class<? extends AbstractRow>, HashMap<String, AbstractRow>> allRows = new HashMap<>();

    @Override
    public HashMap<Class<? extends AbstractRow>, HashMap<String, AbstractRow>> getAbstractRowsHashMap() {
        return allRows;
    }

    public String getUrlPartForWindowHandleCalculation() {
        return urlPartForWindowHandleCalculation;
    }

    public void setDefaultElementWaitTimeout(long timeout) {
        defaultElementWaitTimeout = timeout;
    }

    protected WaitFactory waitFactory() {
        return waitFactory;
    }

    protected WebDriverWait driverWait() {
        return waitFactory().getWait();
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows
    public <T extends AbstractElementsContainer> T refresh() {
        DriverConfig.getDriver().navigate().refresh();
        Log.logRecord("Page " + this.getClass().getName() + " was refreshed.");
        try {
            return (T) this.getClass().getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            Assert.fail(e.getMessage());
            return null;
        }
    }

    public boolean elementPresent(BasicElementWrapper element) {
        return elementPresent(element.getBy());
    }

    public boolean elementPresent(By locator) {
        return elementPresent(locator, defaultElementWaitTimeout);
    }

    public boolean elementPresent(By locator, long timeout) {
        try {
            waitPresenceOf(locator, "Element to be found", timeout);
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    public boolean isChildElementPresent(ElementImpl parent, By childLctr) {
        return isChildElementPresent(parent.getWebElement(), childLctr);
    }

    public boolean isChildElementPresent(ElementImpl parent, By childLctr, long timeout) {
        return isChildElementPresent(parent.getWebElement(), childLctr, timeout);
    }

    public boolean isChildElementPresent(BasicElementWrapper parent, BasicElementWrapper child, long timeout) {
        return isChildElementPresent(parent.getWebElement(), child.getBy(), timeout);
    }

    public boolean isChildElementPresent(BasicElementWrapper parent, BasicElementWrapper child) {
        return isChildElementPresent(parent.getWebElement(), child.getBy());
    }

    public boolean isChildElementPresent(WebElement parent, By childLctr) {
        return isChildElementPresent(parent, childLctr, defaultElementWaitTimeout);
    }

    public boolean isChildElementPresent(WebElement parent, By childLctr, long timeout) {
        int count = findChildElements(parent, childLctr, timeout).size();
        if (count > 1) {
            Log.debug("WARNING: More that 1 element is present.");
        }
        return count > 0;
    }

    protected boolean isElementVisible(BasicElementWrapper element) {
        return isElementVisible(element.getBy());
    }

    @Override
    public boolean isElementVisible(By locator) {
        return isElementVisible(locator, defaultElementWaitTimeout);
    }

    protected boolean isElementVisible(BasicElementWrapper element, long timeout) {
        return isElementVisible(element.getBy(), timeout);
    }

    protected boolean isElementVisible(By locator, long timeout) {
        return waitFactory().isElementVisible(locator, timeout);
    }

    protected boolean elementNotVisible(By lctr) {
        return elementNotVisible(lctr, defaultElementWaitTimeout);
    }

    protected boolean elementNotVisible(BasicElementWrapper e, long timeout) {
        return elementNotVisible(e.getBy(), timeout);
    }

    protected boolean elementNotVisible(By lctr, long timeout) {
        try {
            waitInvisibilityOf(lctr, "E", timeout);
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    protected void assertElementIsDisplayed(By lctr, String elementName) {
        Assert.assertTrue(new ElementImpl(lctr, elementName).isDisplayed(),
                "Element <" + elementName + "> is not displayed on the page " + getClass().getName() +
                        "\nLocator: " + lctr.toString());
    }

    protected void selectByText(String text, ArrayList<ElementImpl> items) {
        ElementImpl element = getElementByText(text, items);
        if (element == null) {
            throw new ObjectNotFound("Element with text <" + text + "> was not found in the above list.");
        }
        element.click();
    }

    protected ElementImpl getElementByText(String text, ArrayList<ElementImpl> items) {
        ArrayList<String> names = new ArrayList<>();
        for (ElementImpl li : items) {
            String name = li.getText();
            names.add(name);
            if (name.equalsIgnoreCase(text)) {
                li.setName(text);
                return li;
            }
        }
        throw new ObjectNotFound("Element with text <" + text + "> is not found. " +
                "\nList of available items: " + names);
    }

    protected ArrayList<ElementImpl> getElementsList(By by) {
        List<WebElement> elements = findElements(by);
        return convert(elements);
    }


    private ArrayList<ElementImpl> convert(List<WebElement> from) {
        ArrayList<ElementImpl> outList = new ArrayList<>();
        for (WebElement element :
                from) {
            outList.add(new ElementImpl(element, "Unknown Element in the list"));
        }
        return outList;
    }

    public Object executeScript(String script) {
        JavascriptExecutor js = (JavascriptExecutor) DriverConfig.getDriver();
        Log.logRecord("Script for execution: \r\n" + script);
        return js.executeScript(script);
    }


    public void scrollUp() {
        ((JavascriptExecutor) DriverConfig.getDriver())
                .executeScript("window.scrollTo(0, 0)");
    }

    public boolean contains(String text) {
        return isElementVisible(By.xpath("//*[contains(text(), '" + text + "')]"));
    }

    public boolean hasElementWithText(String text) {
        return elementPresent(By.xpath("//*[text()='" + text + "']"));
    }

    public boolean hasInputWithValue(String value) {
        return elementPresent(By.xpath("//input[@value='" + value + "']"));
    }

    protected String calculateWindowHandle() {
        return DriverConfig.getDriver().getWindowHandle();
    }

    public String getWindowHandle() {
        return calculateWindowHandle();
    }

    public List<WebElement> findElements(By lctr, long waitTime) {
        return waitFactory().findElements(lctr, waitTime);
    }

    public List<WebElement> findElements(BasicElementWrapper lctr, long waitTime) {
        return findElements(lctr.getBy(), waitTime);
    }

    public List<WebElement> findElements(By lctr) {
        return findElements(lctr, defaultElementWaitTimeout);
    }

    public List<WebElement> findElements(BasicElementWrapper lctr) {
        return findElements(lctr.getBy());
    }

    public List<WebElement> waitAndFindElements(By lctr, String name) {
        waitPresenceOf(lctr, name);
        return findElements(lctr, TIMEOUT_1);
    }

    public List<WebElement> findChildElements(ElementImpl parent, By lctr, long waitTime) {
        return findChildElements(parent.getWebElement(), lctr, waitTime);
    }

    public List<WebElement> findChildElements(WebElement parent, By lctr, long waitTime) {
        return waitFactory().findElements(parent, lctr, waitTime);
    }

    public List<WebElement> findChildElements(WebElement parent, By lctr) {
        return findChildElements(parent, lctr, defaultElementWaitTimeout);
    }

    public void waitPageLoading() {
        waitFactory().waitTillPageReadyState();
    }

    /**
     * for-loop was added because loaders could disappear and appear again
     */
    public void waitAllLoadersDisappeared() {
        Stopwatch stopwatch = Stopwatch.createStarted();
        for (int i = 0; i < 3; i++) {
            Stopwatch stopwatchInside = Stopwatch.createStarted();
            waitInvisibilityOf(allLoaders, EXTENDED_TIMEOUT);
            stopwatchInside.stop();
            Log.debug("Wait all loaders disappeared. Takes time: " + stopwatchInside);
        }
        stopwatch.stop();
        Log.debug("All loaders are disappeared. Takes time: " + stopwatch);
    }

    public void waitJQueryExecution() {
        waitFactory().waitJQueryExecution(DEFAULT_PAGE_LOAD_TIMEOUT);
    }

    public void tryToWaitJQueryExecution() {
        try {
            waitJQueryExecution();
        } catch (TimeoutException e) {
            Log.debug("JQuery execution was not completed on the page <" + this.getClass().getName() + ">." +
                    "\nDetails: " + e.getMessage()
            );
            e.printStackTrace();
        }
    }

    /**
     * Loader with dots
     */
    public void waitLoaderWithDotsDisappeared() {
        waitInvisibilityOf(defaultLoader, EXTENDED_TIMEOUT);
    }

    /**
     * Loader with dots v2
     */
    public void waitLoaderWithDotsDisappeared_v2() {
        waitInvisibilityOf(loader2, EXTENDED_TIMEOUT);
    }

    public void tryToWaitTillLoaderWithDotsDisappeared() {
        try {
            waitLoaderWithDotsDisappeared();
        } catch (TimeoutException e) {
            Log.debug("Loader with dots was not disappeared. Execution is continued.");
        }
    }

    public void tryToWaitTillLoadingMsgDisappeared() {
        try {
            waitTillLoadingMsgDisappeared(EXTENDED_TIMEOUT);
        } catch (TimeoutException e) {
            Log.debug("Loading message was not disappeared. Execution is continued.");
            e.printStackTrace();
        }
    }

    public void waitTillLoadingMsgDisappeared() { //common loader with message "Loading..."
        this.waitTillLoadingMsgDisappeared(EXTENDED_TIMEOUT);
    }

    /**
     * Common loader with message "Loading..."
     */
    public void waitTillLoadingMsgDisappeared(long timeout) {
        waitInvisibilityOf(loadingMsgLctr, timeout);
    }

    /**
     * Loader with message "Loading..."
     */
    protected boolean loadingMsgIsNotShown() {
        return elementNotVisible(loadingMsgLctr, TIMEOUT_1);
    }

    /**
     * Loader with circle
     */
    public void waitPreLoaderDisappeared(long timeout) {
        waitInvisibilityOf(preloader, timeout);
    }

    public void waitPreLoaderDisappeared() { //loader with circle
        waitPreLoaderDisappeared(EXTENDED_TIMEOUT);
    }

    public void waitTillBaseLoaderDisappeared() {
        waitInvisibilityOf(baseLoaderLctr, DEFAULT_TIMEOUT);
    }

    public boolean loaded() {
        return !(isElementVisible(loadingMsgLctr, 1) ||
                isElementVisible(defaultLoader, 1) ||
                isElementVisible(baseLoaderLctr)
        );
    }

    public ElementImpl getElement(By by) {
        return new ElementImpl(by, "Element by " + by.toString() + " on the page " + this.getClass().getName());
    }

    public ElementImpl elementContainedText(String substring) {
        By lctr = By.xpath("//*[contains(text(), '" + substring + "')]");
        return new ElementImpl(lctr, substring);
    }

    public ElementImpl elementByText(String text) {
        By lctr = By.xpath("//*[text()='" + text + "']");
        return new ElementImpl(lctr, text);
    }

    protected Integer removePrefixAndGetCountFormElement(String prefix) {
        String value = elementContainedText(prefix).getText().trim();
        return Integer.parseInt(value.substring(prefix.length()).trim());
    }

    public ElementImpl waitVisibilityOf(By lctr, String name) {
        return waitVisibilityOf(lctr, name, EXTENDED_TIMEOUT);
    }

    public ElementImpl waitVisibilityOf(By lctr, String name, long timeout) {
        String error = name + " was not visible by locator: " + lctr;
        ElementImpl element = waitFactory().waitUntilVisibilityOfElementLocated(lctr, timeout, error);
        element.setName(name);
        return element;
    }

    public ElementImpl waitPresenceOf(By lctr, String name) {
        return waitPresenceOf(lctr, name, DEFAULT_TIMEOUT);
    }

    public Element waitPresenceOf(BasicElementWrapper el, long timeout) {
        return waitPresenceOf(el.getBy(), el.getName(), timeout);
    }

    public ElementImpl waitPresenceOf(By lctr, String name, long timeout) {
        String error = name + " was not present on the page by locator: " + lctr;
        waitFactory().waitPrecenceOfElementLocated(lctr, timeout, error);
        return new ElementImpl(lctr, name);
    }

    public void waitInvisibilityOf(BasicElementWrapper e) {
        waitInvisibilityOf(e, DEFAULT_TIMEOUT);
    }

    public void waitInvisibilityOf(BasicElementWrapper e, long timeout) {
        waitInvisibilityOf(e.getBy(), e.getName(), timeout);
    }

    public void waitInvisibilityOf(By lctr, String name) {
        waitInvisibilityOf(lctr, name, DEFAULT_TIMEOUT);
    }

    public void waitInvisibilityOf(By lctr, String name, long timeout) {
        String error = name + " was not disappeared by locator: " + lctr;
        waitFactory().waitInvisibilityOf(lctr, timeout, error);
    }

    private WebElement getWebElement(By by, String elementName) {
        return new ElementImpl(by, elementName).getWebElement();
    }

    protected boolean isWebElementInFocus(By by, String elementName) {
        return (Boolean) js.executeScript("return $(arguments[0]).is(':focus');", getWebElement(by, elementName));
    }

    protected boolean isWebElementInFocus(BasicElementWrapper element) {
        return (Boolean) js.executeScript("return $(arguments[0]).is(':focus');", getWebElement(element.getBy(), element.getName()));
    }

    protected void dragAndDrop(WebElement source, WebElement target) {
        new Actions(DriverConfig.getDriver())
                .dragAndDrop(source, target)
                .perform();
    }

    public ElementImpl findByXpath(String xpathSelector, Object... arguments) {
        By by = By.xpath(xpathSelector.formatted(arguments));
        return element(by, "xPath with arguments: " + Arrays.toString(arguments));
    }

    public SelectImpl findSelectByXpath(String xpathSelector, Object... arguments) {
        By by = By.xpath(xpathSelector.formatted(arguments));
        return select(by, "xPath with arguments: " + Arrays.toString(arguments));
    }

    public ElementImpl findByCss(String cssSelector, Object... arguments) {
        By by = By.cssSelector(cssSelector.formatted(arguments));
        return element(by, "css with arguments: " + Arrays.toString(arguments));
    }

}
