package ui.abstractObjects.elements;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import util.log.ExtentSteps;
import util.log.Log;

import java.util.List;

//* Native selenium select **//
public class SelectImpl extends org.openqa.selenium.support.ui.Select implements Select {
    private final By options = By.cssSelector(":scope option");
    private String name;
    private WebElement element;
    private By by;

    public SelectImpl(By locator, String elementName) {
        this(new ElementImpl(locator, elementName).getWebElement(), elementName);
        setBy(by);
    }

    public SelectImpl(WebElement element, String name) {
        super(element);
        setName(name);
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setBy(By by) {
        this.by = by;
    }

    @Override
    public WebElement getWebElement() {
        return this.getWrappedElement();
    }

    @Override
    public void setWebElement(WebElement e) {
        this.element = e;
    }

    @Override
    public boolean isEnabled() {
        return getWebElement().isEnabled();
    }

    @Override
    public By getBy() {
        return this.by;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void selectByVisibleText(String text) {
        ExtentSteps.log("Select option '" + text + "' in " + getName() + " select.",
                () ->
                        super.selectByVisibleText(text)
        );
        Log.logRecord("Text <" + text + "> is selected in the dropdown <" + name + ">.", false);
    }

    @Override
    public void selectOptionContains(String substr) {
        ExtentSteps.log("Select option contains '" + substr + "' in " + getName() + " select.",
                () -> {
                    String item = null;
                    for (String option :
                            getAvailableValues()) {
                        if (option.contains(substr)) {
                            item = substr;
                            break;
                        }
                    }
                    Assert.assertNotNull(item, "Option contains '" + substr + "' was not found in select " + getName());
                    selectByVisibleText(item);
                });
    }

    @Override
    public String getSelectedValue() {
        return super.getFirstSelectedOption().getText();
    }

    @Override
    public List<WebElement> getOptionsElements() {
        return new ElementImpl(super.getWrappedElement(), "Select element").findElements(options);
    }

}
