package ui.abstractObjects.elements;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.Coordinates;
import org.openqa.selenium.interactions.Locatable;
import org.openqa.selenium.interactions.MoveTargetOutOfBoundsException;
import org.openqa.selenium.json.JsonException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import ui.steps.Steps;
import util.DriverConfig;
import util.Retry;
import util.WaitFactory;
import util.log.ExtentSteps;
import util.log.Log;

import java.util.Arrays;
import java.util.List;

public class ElementImpl extends ElementWrapperImpl implements Element {


    public ElementImpl(final WebElement element, final String name) {
        super(element, name);
    }

    public ElementImpl(final By by, final String name) {
        super(by, name);
    }

    public ElementImpl(WebElement parent, By by, String name) {
        super(parent, by, name);
    }

    private void logSendKeys(String logValue) {
        Log.logRecord("Enter value: '" + logValue + "' to " + getName(), false);
    }

    private void logClick() {
        Log.logRecord("Click to " + getName(), false);
    }

    public void liteClick() {
        ExtentSteps.log("Click to " + getName(),
                () ->
        getWebElement().click()
        );
    }

    @Override
    public void click() {
        retry(this::liteClick);
        logClick();
    }

    @Override
    public void sendKeys(CharSequence... keysToSend) {
        ExtentSteps.log(
                "Populate '" + getName() + "' with text: " + Arrays.toString(keysToSend),
                ()->
        retry(() -> getWebElement().sendKeys(keysToSend))
        );

        logSendKeys(Arrays.toString(keysToSend));
    }

    private void retry(Retry.RetryCallback call) {
        List<Class<? extends Throwable>> ignoredExceptions = List.of(
                StaleElementReferenceException.class,
                ElementClickInterceptedException.class
        );
        Retry.perform(call, ignoredExceptions, "Unable to perform action with element " + getName(), 2, 1);
    }

    @Override
    public Point getLocation() {
        return getWebElement().getLocation();
    }

    @Override
    public void submit() {
        getWebElement().submit();
    }

    @Override
    public String getAttribute(String name) {
        return getWebElement().getAttribute(name);
    }

    @Override
    public String getCssValue(String propertyName) {
        return getWebElement().getCssValue(propertyName);
    }

    @Override
    public Dimension getSize() {
        return getWebElement().getSize();
    }

    @Override
    public Rectangle getRect() {
        return getWebElement().getRect();
    }

    public List<WebElement> findElements(By by, long timeout) {
        return new WaitFactory().findElements(getWebElement(), by, timeout);
    }

    @Override
    public List<WebElement> findElements(By by) {
        return findElements(by, WaitFactory.TIMEOUT_1);
    }

    @Override
    public String getText() {
        try {
            return getWebElement().getText();
        } catch (StaleElementReferenceException e) {
            if (getBy() == null) {
                throw e;
            }
            e.printStackTrace();
            return getWebElement().getText();
        }
    }

    @Override
    public String getTagName() {
        return getWebElement().getTagName();
    }

    @Override
    public boolean isSelected() {
        return getWebElement().isSelected();
    }

    @Override
    public ElementImpl findElement(By by) {
        return findElement(by, "Child of " + getName());
    }

    @Override
    public boolean isEnabled() {
        return getWebElement().isEnabled();
    }

    @Override
    public boolean isDisplayed() {
        try {
            return getWebElement().isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    @Override
    public void clear() {
        retry(() -> getWebElement().clear());
    }

    @Override
    public Coordinates getCoordinates() {
        return ((Locatable) getWebElement()).getCoordinates();
    }

    @Override
    public <X> X getScreenshotAs(OutputType<X> outputType) throws WebDriverException {
        throw new UnsupportedOperationException("getScreenshotAs() not yet implemented");
    }

    @Override
    public ElementImpl moveMouseOver() {
        Actions builder = new Actions(DriverConfig.getDriver());
        try {
            builder.moveToElement(getWebElement()).build().perform();
        } catch (MoveTargetOutOfBoundsException | JavascriptException | JsonException e) {
            Log.debug("Exception is captured during moveToElement(): " + e.getMessage());
            e.printStackTrace();
            moveMouseOverByJs();
        } catch (StaleElementReferenceException e) {
            Log.debug("StaleElementReferenceException exception was returned during moveMouseOver(). Details:\n" + e.getMessage());
            e.printStackTrace();
            builder.moveToElement(getClickableElement()).build().perform();
        }
        Log.logRecord("Mouse moved over the element: " + getName(), false);
        return this;
    }

    @Override
    public ElementImpl waitClickable(long timeout) {
        WebDriverWait wait = new WaitFactory().getWait(timeout);
        WebElement clickable;
        if (getBy() != null) {
            clickable = wait.until(ExpectedConditions.elementToBeClickable(getBy()));
        } else {
            clickable = wait.until(ExpectedConditions.elementToBeClickable(getWebElement()));
        }
        setWebElement(clickable);
        return this;
    }

    private WebElement getClickableElement() {
        try {
            return this.waitClickable(WaitFactory.TIMEOUT_5);
        } catch (TimeoutException e) {
            Log.debug("Failed to return clickable element for '" + getName() + "' due to Timeout exception. Returning non clickable element.");
            e.printStackTrace();
            return getWebElement();
        } catch (WebDriverException e) {
            Log.debug("WebDriverException returned during wait to clickable element." +
                    "\nDetails:" + e.getMessage()
            );
            e.printStackTrace();
            return getWebElement();
        }
    }

    public void moveMouseOverByJs() {
        int yCoord = getWebElement().getLocation().getY();
        Steps.executeJsScript("window.scrollTo(0, " + yCoord + ")");
    }

    public boolean isAttributeContains(String attributeName, String value) {
        return getWebElement().getAttribute(attributeName).contains(value);
    }

    @Override
    public boolean isDisabled() {
        return !this.isEnabled();
    }

    public String getWebElementOutline() {
        return getWebElement().getCssValue("outline");
    }

    public ElementImpl getChildByText(String text) {
        By lctr = By.xpath(".//*[text()='" + text + "']");
        return this.findElement(lctr, text);
    }

    public void clickByActions() {
        new Actions(DriverConfig.getDriver())
                .moveToElement(getWebElement())
                .click(getWebElement())
                .build().perform();
        logClick();
    }

    public int getPosition() {
        String tagName = this.getTagName();
        return this.findElements(By.xpath(".//preceding-sibling::" + tagName)).size() + 1;
    }


    /**
     * @returns - tooltip text (from data balloon)
     */
    @Override
    public String getAreaLabel() {
        return this.getAttribute("aria-label");
    }

    public void clickByJs() {
        JavascriptExecutor js = (JavascriptExecutor) DriverConfig.getDriver();
        js.executeScript("arguments[0].click()", getWebElement());
        Log.logRecord("Clicked by JS to <" + getName() + ">.");
    }

    public String getInnerHTML() {
        return this.getAttribute("innerHTML");
    }

}
