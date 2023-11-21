package ui.abstractObjects.elements;

import org.openqa.selenium.*;
import org.testng.Assert;
import util.WaitFactory;
import util.log.Log;

public class ElementWrapperImpl implements BasicElementWrapper{
    private WebElement parent;
    private WebElement webElement;
    private String name;
    private By by;

    /**
     * Creates a Element for a given WebElement.
     *
     * @param element element to wrap up
     */
    protected ElementWrapperImpl(final WebElement element) {
        this.webElement = element;
    }

    public ElementWrapperImpl(final WebElement element, final String name) {
        this(element);
        setName(name);
    }

    public ElementWrapperImpl(final By by, final String name) {
        setBy(by);
        setName(name);
    }

    public ElementWrapperImpl(WebElement parent, By by, String name) {
        this(parent.findElement(by), name);
        this.parent = parent;
        this.by = by;
    }

    @Override
    public String getName() {
        if (this.name == null) {
            return "Undefined";
        }
        return this.name;
    }

    @Override
    public By getBy() {
        return this.by;
    }

    @Override
    public void setWebElement(WebElement element) {
        this.webElement = element;
    }

    @Override
    public WebElement getWebElement() {
        Assert.assertTrue(elementWired(), "WebElement nor By were not provided.");
        try {
            if (this.getWrappedElement() == null) {
                return initiateWebElement();
            } else {
                //the step to ping the element and make sure it still on the page
                this.getWrappedElement().getTagName();
            }
        } catch (StaleElementReferenceException e) {
            if (getBy() == null) {
                Log.debug("Unable to re-initiate element since locator is null for " + getName());
                throw e;
            }
            e.printStackTrace();
            initiateWebElement();
        }
        return getWrappedElement();
    }

    protected WebElement getParent(){
        return parent;
    }

    private WebElement initiateWebElement() {
        Assert.assertNotNull(getBy(), "By was not provided for " + getName());
        WebElement we;
        try {
            if (getParent() != null) {
                we = new WaitFactory().findElement(getParent(), getBy(), WaitFactory.DEFAULT_EXPLICIT_TIMEOUT, getName());
            } else {
                we = new WaitFactory().findElement(getBy(), WaitFactory.DEFAULT_EXPLICIT_TIMEOUT);
            }
        } catch (TimeoutException e) {
            throw new NoSuchElementException(
                    "Unable to locate element. Name = [" + getName() + "];  Locator: " + getBy(),
                    e
            );
        }
        setWebElement(we);
        return getWrappedElement();
    }

    @Override
    public void setName(String n) {
        this.name = n;
    }

    @Override
    public void setBy(By by) {
        this.by = by;
    }

    @Override
    public WebElement getWrappedElement() {
        return this.webElement;
    }

    @Override
    public boolean isEnabled(){
        return getWebElement().isEnabled();
    }

}
