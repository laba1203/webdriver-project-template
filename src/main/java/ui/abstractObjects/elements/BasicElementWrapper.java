package ui.abstractObjects.elements;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WrapsElement;
import ui.abstractObjects.elements.factory.ImplementedBy;
import util.WaitFactory;

import java.lang.reflect.Method;

@ImplementedBy(ElementWrapperImpl.class)
public interface BasicElementWrapper extends WrapsElement {

    /**
     * The method should not be re-named since it used as a string.
     *
     * @see ui.abstractObjects.elements.factory.ElementHandler#invoke(Object, Method, Object[])
     */
    String getName();

    void setName(String n);

    /**
     * The method should not be re-named since it used as a string.
     *
     * @see ui.abstractObjects.elements.factory.ElementHandler#invoke(Object, Method, Object[])
     */
    By getBy();

    void setBy(By by);

    WebElement getWebElement();

    void setWebElement(WebElement e);

    default boolean isDisabled() {
        return !isEnabled();
    }

    boolean isEnabled();

    /**
     * Returns true when the inner element is ready to be used.
     *
     * @return boolean true for an initialized WebElement, or false if we were somehow passed a null WebElement.
     */
    default boolean elementWired() {
        return getBy() != null || this.getWrappedElement() != null;
    }

    default ElementImpl findElement(By by) {
        return findElement(by, "Child of " + getName());
    }

    default ElementImpl findElement(By by, String name) {
        return new ElementImpl(getWebElement().findElement(by), name);
    }

    default ElementImpl findElement(By by, String name, long timeout) {
        return new WaitFactory().findElement(getWebElement(), by, timeout, name);
    }

}
