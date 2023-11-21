package ui.abstractObjects.containers;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import ui.abstractObjects.elements.BasicElementWrapper;
import ui.abstractObjects.elements.ElementImpl;
import ui.abstractObjects.elements.factory.ElementFactory;
import util.WaitFactory;

import java.util.List;

import static util.WaitFactory.TIMEOUT_2;

public abstract class AbstractRow extends AbstractElementsContainer {
    private WebElement row;


    protected AbstractRow(WebElement row) {
        setWebElement(row);
        initElements();
    }

    @Override
    protected void initElements() {
        this.initElements(this);
    }

    protected <T extends AbstractRow> T initElements(T page) {
        return ElementFactory.initElements(getRowWebElement(), page);
    }

    void setWebElement(WebElement el) {
        row = el;
    }

    protected WebElement getRowWebElement() {
        return row;
    }

    protected ElementImpl getRowElement() {
        return new ElementImpl(row, "Row for " + getClass().getName());
    }

    @Override
    public ElementImpl element(By lctr, String name) {
        return new ElementImpl(getRowWebElement(), lctr, name);
    }

    protected abstract String getIdentifier();

    protected boolean identifierMatchedTo(String value) {
        return getIdentifier().equals(value);
    }

    public List<WebElement> findChildElements(By lctr, long waitTime) {
        return waitFactory().findElements(getRowWebElement(), lctr, waitTime);
    }

    @Override
    public boolean elementPresent(By locator, long timeout) {
        return isChildElementPresent(getRowWebElement(), locator, timeout);
    }

    @Override
    public boolean isElementVisible(By lctr, long timeout) {
        try {
            getRowElement().findElement(lctr, "E", WaitFactory.TIMEOUT_1);
            return true;
        } catch (AssertionError | NoSuchElementException e) {
            return false;
        }
    }

    @Override
    public List<WebElement> findElements(By lctr, long timeout) {
        return findElementsInTheRow(lctr, timeout);
    }

    @Override
    public void waitInvisibilityOf(By lctr, String name, long timeout) {
        String error = name + " was visible inside Parent element. Child locator: " + lctr;
        ElementImpl element;
        try {
            element = getRowElement().findElement(lctr, name, TIMEOUT_2);
        } catch (AssertionError | NoSuchElementException | StaleElementReferenceException e) {
            return;
        }
        waitFactory().waitInvisibilityOf((BasicElementWrapper) element, timeout, error);
    }

    public List<WebElement> findElementsInTheRow(By lctr, long timeout) {
        return waitFactory().findElements(getRowWebElement(), lctr, timeout);
    }


}
