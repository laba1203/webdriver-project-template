package util;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import org.testng.Assert;
import ui.abstractObjects.containers.AbstractElementsContainer;
import ui.abstractObjects.elements.BasicElementWrapper;
import ui.abstractObjects.elements.ElementImpl;
import ui.steps.Steps;
import util.exceptions.ObjectNotFound;
import util.log.Log;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.time.Duration.ofMillis;
import static java.time.Duration.ofSeconds;

public class WaitFactory {
    public static final long TIMEOUT_10 = 10;
    public static final long TIMEOUT_5 = 5;
    public static final long TIMEOUT_3 = 3;
    public static final long TIMEOUT_2 = 2;
    public static final long TIMEOUT_1 = 1;
    private static final long DEFAULT_SLEEP_MILLIS_500 = 500;

    static final long DEFAULT_IMPLICIT_TIME_OUT_30 = 30;
    public static final long DEFAULT_EXPLICIT_TIMEOUT = 30;
    public static final long EXTENDED_EXPLICIT_TIMEOUT = DEFAULT_EXPLICIT_TIMEOUT + 15;
    public static final long DEFAULT_PAGE_LOAD_TIMEOUT = 90;


    //custom timeouts:
    private static final long TEMP_IMPLICIT_BEFORE_EXPLICIT_WAIT = 0;
    public static final long EMAIL_RECEIVING_TIMEOUT = 120;


    public static void setDefaultImplicitlyWait() {
        setImplicitWait(ofSeconds(DEFAULT_IMPLICIT_TIME_OUT_30));
        setPageLoadTimeOut(DEFAULT_PAGE_LOAD_TIMEOUT);
    }

    private static void setDefaultImplicitlyWaitDuringError(Throwable e) {
        try {
            setDefaultImplicitlyWait();
        } catch (Throwable e1) {
            e1.printStackTrace();
        }
    }

    public static void setPageLoadTimeOut(long pageLoadTimeOut) {
        DriverConfig.getDriver().manage().timeouts().pageLoadTimeout(ofSeconds(pageLoadTimeOut));
    }

    private static void setImplicitWait(Duration duration) {
        DriverConfig.getDriver()
                .manage()
                .timeouts()
                .implicitlyWait(duration);
    }

    private void waitUntil(Wait<WebDriver> wait, ExpectedCondition<Boolean> isTrue) {
        Assert.assertNotNull(wait, "Wait can't be null");
        Assert.assertNotNull(isTrue, "Wait Condition can't be null.");
        setImplicitWait(ofSeconds(TEMP_IMPLICIT_BEFORE_EXPLICIT_WAIT));
        Throwable exc = null;
        try {
            wait.until(isTrue);
        } catch (Throwable e) {
            exc = e;
            if (!(e instanceof TimeoutException)) {
                Log.debug("Exception during wait: " + e.getMessage());
            }
            throw e;
        } finally {
            if (exc != null) {
                setDefaultImplicitlyWaitDuringError(exc);
            } else {
                setDefaultImplicitlyWait();
            }
        }

    }

    private void waitUntil(ExpectedCondition<Boolean> isTrue, Class<? extends Throwable> ignoredException, long timeout, String errorMsg) {
        FluentWait<WebDriver> wait = new WaitFactory().getWait(timeout).withMessage(errorMsg);
        if (ignoredException != null) {
            wait = wait.ignoring(ignoredException);
        }
        waitUntil(wait, isTrue);
    }

    private void waitUntil(ExpectedCondition<Boolean> isTrue, long timeout, String errorMsg) {
        waitUntil(isTrue, null, timeout, errorMsg);
    }

    private WebElement waitUntilElement(ExpectedCondition<WebElement> elementCondition, long timeout, String errorMsg) {
        Assert.assertNotNull(elementCondition, "Wait condition can't be null.");
        setImplicitWait(ofSeconds(TEMP_IMPLICIT_BEFORE_EXPLICIT_WAIT));
        Throwable exc = null;
        try {
            return getWait(timeout)
                    .withMessage(errorMsg)
                    .until(elementCondition);
        } catch (Throwable e) {
            if (!(e instanceof TimeoutException)) {
                Log.debug("Exception during wait: " + e.getMessage());
            }
            exc = e;
            throw e;
        } finally {
            if (exc != null) {
                setDefaultImplicitlyWaitDuringError(exc);
            } else {
                setDefaultImplicitlyWait();
            }
        }
    }

    private List<WebElement> waitUntilElements(ExpectedCondition<List<WebElement>> elements, long timeout, String errorMsg) {
        setImplicitWait(Duration.ofSeconds(TEMP_IMPLICIT_BEFORE_EXPLICIT_WAIT));
        Throwable exc = null;
        try {
            return getWait(timeout).withMessage(errorMsg).until(elements);
        } catch (Throwable e) {
            if (!(e instanceof TimeoutException)) {
                Log.debug("Exception during wait: " + e.getMessage());
            }
            exc = e;
            throw e;
        } finally {
            if (exc != null) {
                setDefaultImplicitlyWaitDuringError(exc);
            } else {
                setDefaultImplicitlyWait();
            }
        }
    }

    public FluentWait<WebDriver> getFluentWait(long timeout) {
        return getFluentWait(timeout, DEFAULT_SLEEP_MILLIS_500);
    }

    public FluentWait<WebDriver> getFluentWait(long timeout, long sleepMilliseconds) {
        return new FluentWait<>(Objects.requireNonNull(DriverConfig.getDriver()))
                .withTimeout(ofSeconds(timeout))
                .pollingEvery(ofMillis(sleepMilliseconds));
    }

    public WebDriverWait getWait(long timeOutInSeconds) {
        return getWait(timeOutInSeconds, DEFAULT_SLEEP_MILLIS_500);
    }

    public WebDriverWait getWait(long timeOutInSeconds, long sleepInMillis) {
        return new WebDriverWait(Objects.requireNonNull(DriverConfig.getDriver()), Duration.ofSeconds(timeOutInSeconds), Duration.ofMillis(sleepInMillis));
    }

    public WebDriverWait getWait() {
        return getWait(DEFAULT_EXPLICIT_TIMEOUT);
    }

    public void waitInvisibilityOfElementWithText(BasicElementWrapper element, String text) {
        waitInvisibilityOfElementWithText(element.getBy(), text);
    }


    public void waitInvisibilityOfElementWithText(By by, String text) {
        waitInvisibilityOfElementWithText(by, text,
                "Failed to wait till element with text <" + text + "> disappeared. Locator: " + by);
    }

    public void waitTillElementPopulated(By by) {
        waitInvisibilityOfElementWithText(by, "", "Element was not populated (current value is empty). Locator: " + by);
    }

    public void waitInvisibilityOfElementWithText(By by, String text, String errorMSg) {
        waitInvisibilityOfElementWithText(by, text, DEFAULT_EXPLICIT_TIMEOUT, errorMSg);
    }

    public void waitInvisibilityOfElementWithText(BasicElementWrapper by, String text, long timeout, String errorMSg) {
        waitInvisibilityOfElementWithText(by.getBy(), text, timeout, errorMSg);
    }

    public void waitInvisibilityOfElementWithText(By by, String text, long timeout, String errorMSg) {
        waitUntil(
                ExpectedConditions.invisibilityOfElementWithText(by, text),
                timeout,
                errorMSg
        );
    }

    public void waitTillAttributeContains(By by, String attribute, String value, long timeout, String errorMSg) {
        waitUntil(
                ExpectedConditions.attributeContains(by, attribute, value),
                timeout,
                errorMSg
        );
    }

    public void waitInvisibilityOf(By locator, long waitSec, String errorMsg) {
        try {
            waitUntil(
                    ExpectedConditions.invisibilityOfElementLocated(locator),
                    waitSec,
                    errorMsg
            );
        } catch (NullPointerException e) {
            e.printStackTrace();
            waitUntil(
                    ExpectedConditions.invisibilityOfElementLocated(locator),
                    waitSec,
                    errorMsg
            );
        }
    }

    public void waitInvisibilityOf(BasicElementWrapper el, long waitSec, String errorMsg) {
        waitInvisibilityOf(el.getBy(), waitSec, errorMsg);
    }

    public void waitInvisibilityOfWebElement(WebElement el, long waitSec, String errorMsg) {
        waitUntil(
                ExpectedConditions.invisibilityOf(el),
                waitSec,
                errorMsg
        );
    }

    public void waitUntilTextToBePresentInElement(By by, String text, long timeoutInSeconds) {
        String defaultError = "Failed to wait till text <" + text + "> will be present in element <" + by.toString() + ">.";
        waitUntilTextToBePresentInElement(by, text, timeoutInSeconds, defaultError);
    }

    public void waitUntilTextToBePresentInElement(WebElement element, String text, long timeout, String error) {
        waitUntil(
                ExpectedConditions.textToBePresentInElement(element, text),
                timeout,
                error
        );
    }

    public void waitUntilTextToBePresentInElement(By by, String text, long timeoutInSeconds, String errorMsg) {
        waitUntil(
                ExpectedConditions.textToBePresentInElementLocated(by, text),
                timeoutInSeconds,
                errorMsg
        );
    }

    public ElementImpl waitUntilVisibilityOfElementLocated(By by, long timeoutInSeconds) {
        return waitUntilVisibilityOfElementLocated(by, timeoutInSeconds,
                "Element '" + by.toString() + "' was not shown on the page.");
    }

    public ElementImpl waitUntilVisibilityOfElementLocated(By by, long timeoutInSeconds, String errorMsg) {
        waitUntilElement(
                ExpectedConditions.visibilityOfElementLocated(by),
                timeoutInSeconds,
                errorMsg
        );
        return new ElementImpl(by, by.toString());
    }

    public WebElement waitPrecenceOfElementLocated(By by, long timeoutInSeconds, String errorMsg) {
        return waitUntilElement(
                ExpectedConditions.presenceOfElementLocated(by),
                timeoutInSeconds,
                errorMsg
        );
    }

    public ElementImpl waitUntilVisibilityOfElementLocated(WebElement element, long timeoutInSeconds, String errorMsg) {
        waitUntilElement(
                ExpectedConditions.visibilityOf(element),
                timeoutInSeconds,
                errorMsg
        );
        return new ElementImpl(element, "Web Element");
    }

    public static List<WebElement> waitChildElementsForElement(ElementImpl parentElement, By childrenLocator) {
        setImplicitWait(ofSeconds(TIMEOUT_2));
        try {
            return parentElement.getWebElement().findElements(childrenLocator);
        } finally {
            setDefaultImplicitlyWait();
        }

    }

    public void waitAndRefreshTillElementDisappeared(By by, long timeout, String errorMsg) {
        Wait<WebDriver> wait = getFluentWait(timeout)
                .withMessage(errorMsg)
                .ignoring(TimeoutException.class);
        waitUntil(
                wait,
                driver -> {
                    try {
                        getWait(WaitFactory.TIMEOUT_5)
                                .until(ExpectedConditions.invisibilityOfElementLocated(by));
                        return true;
                    } catch (TimeoutException e) {
                        Log.debug("Element was not disappeared(Lctr = " + by + "). Refreshing a driver...");
                        Steps.refresh();
                        throw e;
                    }
                }
        );
    }

    public void waitAndRefreshPageUtilElementWithTextDisappeared(By locator, String text, long waitTime, String error) {
        Wait<WebDriver> wait = getFluentWait(waitTime)
                .withMessage(error)
                .ignoring(TimeoutException.class);
        waitUntil(
                wait,
                driver -> {
                    Log.debug("Refreshing a driver...");
                    Steps.refresh();
                    waitTillPageReadyState();
                    getWait(TIMEOUT_2, 2000).until(
                            ExpectedConditions.invisibilityOfElementWithText(locator, text)
                    );
                    Log.debug("Text <" + text + "> disappeared in the element " + locator.toString());
                    return true;
                }
        );

    }

    public void waitAndRefreshPageUtilAppearanceOfElementWithText(BasicElementWrapper element, String text, long waitTime) {
        String defaultError = "Element with text <" + text + "> was not found for " + waitTime + " seconds. Element locator: <" + element.getBy().toString() + ">.";
        waitAndRefreshPageUtilAppearanceOfElementWithText(element, text, waitTime, defaultError);
    }

    public void waitAndRefreshPageUtilAppearanceOfElementWithText(BasicElementWrapper element, String text, long waitTime, String errorMsg) {
        waitAndRefreshPageUtilAppearanceOfElementWithText(element.getBy(), text, waitTime, errorMsg);
    }

    public void waitAndRefreshPageUtilAppearanceOfElementWithText(By locator, String text, long waitTime, String errorMsg) {
        waitAndRefreshPageTillElementWithTextOnPage(null, locator, text, waitTime, errorMsg);
    }

    public void waitAndRefreshPageTillElementWithTextOnPage(AbstractElementsContainer page, By locator, String text, long waitTime, String errorMsg) {
        Wait<WebDriver> wait = getFluentWait(waitTime)
                .withMessage(errorMsg)
                .ignoring(TimeoutException.class)
                .ignoring(AssertionError.class)
                .ignoring(StaleElementReferenceException.class)
                .ignoring(NoSuchElementException.class);
        waitUntil(
                wait,
                driver -> {
                    if (page != null) {
                        page.waitAllLoadersDisappeared();
                    }
                    if (!new ElementImpl(locator, locator.toString()).getText().equals(text)) {
                        Log.debug("Element with text '" + text + "' was not shown. Refreshing a driver...");
                        Steps.refresh();
                    }
                    waitTillPageReadyState();
                    getWait(TIMEOUT_2).until(ExpectedConditions.textToBePresentInElementLocated(locator, text));
                    Log.debug("Element with Text <" + text + "> is appeared.");
                    return true;
                }
        );

    }

    public ElementImpl waitAndRefreshPageUtilElementAppearance(By locator, long waitTime) {
        String defaultError = "Element was not found for " + waitTime + " seconds. Element locator: <" + locator.toString() + ">.";
        return waitAndRefreshPageUtilElementAppearance(locator, waitTime, defaultError);
    }

    public ElementImpl waitAndRefreshPageUtilElementAppearanceOnThePage(AbstractElementsContainer page, By locator, long timeout, String errorMsg) {
        Wait<WebDriver> wait = getFluentWait(timeout)
                .withMessage(errorMsg)
                .ignoring(TimeoutException.class);
        return wait.until(driver -> {
            try {
                if (page != null) {
                    page.waitAllLoadersDisappeared();
                }
                waitUntilVisibilityOfElementLocated(locator, TIMEOUT_3);
                Log.debug("Element was found.");
            } catch (TimeoutException e) {
                Log.debug("Element was not found (" + locator + "). Refreshing...");
                Steps.refresh();
                waitTillPageReadyState(DEFAULT_PAGE_LOAD_TIMEOUT);
                if (page != null) {
                    page.waitAllLoadersDisappeared();
                }
                throw e;
            }
            return new ElementImpl(locator, locator.toString());
        });
    }

    public ElementImpl waitAndRefreshPageUtilElementAppearance(By locator, long timeout, String errorMsg) {
        return waitAndRefreshPageUtilElementAppearanceOnThePage(null, locator, timeout, errorMsg);
    }

    public void waitTillUrlChangedFrom(String url) {
        String defaultError = "URL was not changed ('" + url + "')";
        waitTillUrlChangedFrom(url, defaultError);
    }

    public void waitTillUrlChangedFrom(String url, String errorMsg) {
        getWait()
                .withMessage(errorMsg)
                .until(ExpectedConditions.not(ExpectedConditions.urlToBe(url)));
    }

    public void waitTillUrlNotContains(String fraction, long timeout, String errorMsg) {
        getWait(timeout)
                .withMessage(errorMsg)
                .until(ExpectedConditions.not(ExpectedConditions.urlContains(fraction)));
    }

    public void waitTillUrlContains(String fraction, long timeout, String errorMsg) {
        getWait(timeout)
                .withMessage(errorMsg)
                .until(ExpectedConditions.urlContains(fraction));
    }

    public void waitTillPageReadyState(long time) {
        try {
            waitPageReadyState(time);
        } catch (NullPointerException e) {
            waitPageReadyState(time);
        }
    }

    private void waitPageReadyState(long time) {
        getWait(time)
                .withMessage("Page was not loaded during " + time + " seconds.")
                .ignoring(ScriptTimeoutException.class)
                .until(
                        webDriver -> ((JavascriptExecutor) webDriver)
                                .executeScript("return document.readyState")
                                .equals("complete")
                )
        ;
    }

    public void waitTillPageReadyState() {
        waitTillPageReadyState(DEFAULT_PAGE_LOAD_TIMEOUT);
    }

    public void waitJQueryExecution(long time) {
        String js = "return window.jQuery != undefined && jQuery.active === 0";
        getWait(time)
                .withMessage("jQuery scripts execution was not completed.\n Script: '" + js + "'.\n")
                .until(
                        webDriver -> (Boolean) ((JavascriptExecutor) webDriver)
                                .executeScript(js)
                )
        ;
    }

    public List<WebElement> findElements(By by, long waitTime) {
        Objects.requireNonNull(by);
        List<WebElement> elements = new ArrayList<>();
        try {
            elements = waitUntilElements(
                    ExpectedConditions.presenceOfAllElementsLocatedBy(by),
                    waitTime,
                    "Elements were not found."
            );
        } catch (TimeoutException e) {
            Log.logInConsole("No elements were present by " + by + " (timeout = " + waitTime + ").");
        }
        return elements;
    }

    public List<WebElement> findElements(ElementImpl parent, By by, long waitTime) {
        return findElements(parent.getWebElement(), by, waitTime);
    }

    public List<WebElement> findElements(WebElement parent, By by, long waitTime) {
        setImplicitWait(ofSeconds(waitTime));
        try {
            return parent.findElements(by);
        } finally {
            setDefaultImplicitlyWait();
        }
    }

    public WebElement findElement(By by, long timeout) {
        return getWait(timeout).until(ExpectedConditions.presenceOfElementLocated(by));
    }

    public ElementImpl findElement(WebElement parent, By elementLctr, long timeout, String elementName) {
        setImplicitWait(Duration.ofSeconds(timeout));
        try {
            return new ElementImpl(parent.findElement(elementLctr), elementName);
        } finally {
            setDefaultImplicitlyWait();
        }
    }

    public String waitWindowHandlesContainsUrl(String url, long timeout) {
        String errorMsg = "Browser Window which contains URL '" + url + "' was not found.";
        return waitWindowHandlesContainsUrl(url, timeout, errorMsg);
    }

    public String waitWindowHandlesContainsUrl(String url, long timeout, String errorMsg) {
        return getFluentWait(timeout)
                .withMessage(errorMsg)
                .ignoring(AssertionError.class)
                .until(webDriver -> {
                    int count = Browser.getWindowHandlesListContainsUrl(url).size();
                    Assert.assertTrue(count > 0);
                    return Browser.getWindowHandleWhichContainsUrl(url);
                });

    }

    public void waitTillElementWithTextDisappeared(WebElement el, String text, long timeout, String errorMsg) {
        waitUntil(
                ExpectedConditions.not(ExpectedConditions.textToBePresentInElement(el, text)),
                timeout,
                errorMsg
        );
    }

    /**
     * Method for thread sleep and don't lose driver connection
     */
    public static void sleep(long timeoutInSec) {
        if (timeoutInSec == 0) return;

        Log.debug("Sleep started.");
        try {
            new FluentWait<>(Objects.requireNonNull(DriverConfig.getDriver()))
                    .withTimeout(ofSeconds(timeoutInSec))
                    .pollingEvery(ofMillis(DEFAULT_SLEEP_MILLIS_500))
                    .ignoring(ObjectNotFound.class)
                    .ignoring(NoSuchElementException.class)
                    .until(webDriver -> {
                        DriverConfig.getDriver().findElement(By.tagName("body"));
                        throw new ObjectNotFound();
                    });
        } catch (TimeoutException e) {
            Log.debug("Driver sleep is completed.");
        }
    }

    public static void waitAndRefresh(Retry.RetryCallbackWithoutErrors action, Class<? extends Throwable> ignoredException, long timeout) {
        waitAndRefresh(action, ignoredException, timeout, "Failed to perform Action (timeout = '" + timeout + "') due to exception " + ignoredException.getName());
    }

    public static void waitAndRefresh(Retry.RetryCallbackWithoutErrors action, Class<? extends Throwable> ignoredException, long timeout, String error) {
        new WaitFactory().waitUntil(webDriver -> {
            try {
                action.call();
            } catch (Throwable e) {
                Log.debug("Not able to perform action in waitAndRefresh(). Refreshing the page...");
                Steps.refresh();
                throw e;
            }
            return true;
        }, ignoredException, timeout, error);
    }

    public static void wait(Retry.RetryCallbackWithoutErrors action, Class<? extends Throwable> ignoredException, long timeout, String error) {
        new WaitFactory().waitUntil(
                webDriver -> {
                    try {
                        action.call();
                    } catch (Throwable e) {
                        Log.debug("Not able to wait till action performed due to error: " + e.getMessage());
                        throw e;
                    }
                    return true;
                },
                ignoredException,
                timeout,
                error
        );
    }

    public String waitTillWindowWithUrlPartOpened(String urlPart, long timeout) {
        return getFluentWait(timeout)
                .withMessage("Page which contains URL '" + urlPart + "' was not found.")
                .ignoring(AssertionError.class)
                .until(webDriver -> Browser.getWindowHandleWhichContainsUrl(urlPart));
    }

    public void waitWithDriverOrNot(Callback callback, String errorMessage, long timeout) {
        waitWithDriverOrNot(callback, AssertionError.class, errorMessage, timeout);
    }

    public void waitWithDriverOrNot(Callback callback, Class<? extends Throwable> ignoredException, String errorMessage, long timeout) {
        if (DriverConfig.driverCreated()) {
            new WaitFactory().getWait(timeout)
                    .withMessage(errorMessage)
                    .ignoring(ignoredException)
                    .until(webDriver -> callback.call()
                    );
        } else {
            Retry.perform(callback::call, ignoredException, errorMessage, (int) timeout, 1);
        }
    }

    public boolean isElementVisible(By lctr, long timeout) {
        try {
            waitUntilVisibilityOfElementLocated(lctr, timeout);
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    @FunctionalInterface
    public interface Callback {
        boolean call();
    }

}