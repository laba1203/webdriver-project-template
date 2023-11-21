package ui.abstractObjects.elements;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.testng.Assert;
import ui.abstractObjects.elements.factory.ImplementedBy;
import org.openqa.selenium.interactions.Locatable;
import util.DriverConfig;
import util.WaitFactory;

import java.lang.reflect.Method;

/**
 * wraps a web element interface with extra functionality. Anything added here will be added to all descendants.
 */
@ImplementedBy(ElementImpl.class)
public interface Element extends WebElement, Locatable, BasicElementWrapper {


    default ElementImpl parent(){
        return new ElementImpl(getWebElement().findElement(By.xpath("./..")), "Parent element of the '" + getName() + "'");
    }

    default Element moveMouseOver(){
        new Actions(DriverConfig.getDriver())
                .moveToElement(getWebElement())
                .build().perform();
        return this;
    }

    default String getAreaLabel() {
        return this.getAttribute("aria-label");
    }

    default Element init(){
        By lctr = getBy();
        Assert.assertNotNull(lctr, "init() is impossible since by is By is null.");
        setWebElement(findElement(lctr));
        return this;
    }

    default Element waitVisibility(){
        return waitVisibility(WaitFactory.DEFAULT_EXPLICIT_TIMEOUT);
    }

    /**
     * The method should not be re-named since it used as a string.
     *
     * @see ui.abstractObjects.elements.factory.ElementHandler#invoke(Object, Method, Object[])
     */
    default Element waitVisibility(Long timeout){
        WaitFactory wf = new WaitFactory();
        String error = getName() + " was not visible by locator: " + getBy();
        if (getBy() != null) {
            error = error + " by locator: " + getBy();
            return wf.waitUntilVisibilityOfElementLocated(getBy(), timeout, error);
        }
        setWebElement(
                wf.waitUntilVisibilityOfElementLocated(getWebElement(), timeout, error).getWebElement()
        );
        return this;
    }

    /**
     * The method should not be re-named since it used as a string.
     *
     * @see ui.abstractObjects.elements.factory.ElementHandler#invoke(Object, Method, Object[])
     *
     */
    default Element waitPresence(Long timeout){
        WaitFactory wf = new WaitFactory();
        By lctr = getBy();
        Assert.assertNotNull(lctr, "waitPresence() is not applicable when By is null.");
        String error = getName() + " was not present on the page by locator: " + lctr;
        wf.waitPrecenceOfElementLocated(lctr, timeout, error);
        return this;
    }

    default Element waitPresence(){
        return waitPresence(WaitFactory.DEFAULT_EXPLICIT_TIMEOUT);
    }

    default Element waitClickable(){
        return waitClickable(WaitFactory.DEFAULT_EXPLICIT_TIMEOUT);
    }

    Element waitClickable(long timeout);

    default boolean isDisabled(){
        return !this.isEnabled();
    }

    default Element clearAndSendKeys(CharSequence... text){
        clear();
        sendKeys(text);
        return this;
    }

    default Element moveToElementAndClick(){
        moveMouseOver();
        click();
        return this;
    }

    default Double getWebElementWidth(){
        String width = getWebElement().getCssValue("width");
        return Double.parseDouble(StringUtils.substringBefore(width, "px"));
    }

    @Override
    default ElementImpl findElement(By by) {
        return findElement(by, "Child of " + getName());
    }

    default Element findElement(Element element) {
        return findElement(element.getBy(), "Child of " + getName());
    }

}