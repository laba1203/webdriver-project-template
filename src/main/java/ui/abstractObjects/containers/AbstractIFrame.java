package ui.abstractObjects.containers;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import ui.abstractObjects.elements.ElementImpl;
import util.Browser;
import util.DriverConfig;
import util.log.Log;

public class AbstractIFrame extends AbstractElementsContainer {
    private By frameLctr;

    protected AbstractIFrame() {
    }

    public AbstractIFrame(By frameLctr) {
        this.frameLctr = frameLctr;
    }

    protected void setWebElementAndFindItOnPage(By locator) {
        setFrameLctr(locator);
        getFrameWebElement();
    }

    protected void setFrameLctr(By locator) {
        frameLctr = locator;
    }

    private WebElement getFrameWebElement() {
        Assert.assertNotNull(frameLctr, "Frame locator is null.");
        return waitPresenceOf(frameLctr, "Iframe locator for " + this.getClass().getName(), EXTENDED_TIMEOUT).getWebElement();
    }

    public ElementImpl getFrameElement() {
        return new ElementImpl(frameLctr, "Campaign Iframe");
    }

    private void waitPreviewFrameVisibility() {
        waitFactory().waitUntilVisibilityOfElementLocated(getFrameWebElement(), EXTENDED_TIMEOUT,
                "iFrame was not visible. Locator: " + frameLctr
        );
    }

    public AbstractIFrame switchToThisFrame() {
        try {
            waitPreviewFrameVisibility();
        } catch (StaleElementReferenceException e) {
            waitPreviewFrameVisibility();
        }
        try {
            Browser.switchToIFrame(getFrameWebElement());
        } catch (StaleElementReferenceException e) {
            Log.debug("Caught StaleElementReferenceException in AbstractIFrame.getFrameWebElement class: " + this.getClass().getName());
            DriverConfig.getDriver()
                    .switchTo()
                    .frame(
                            getFrameWebElement());
        }
        Log.logInConsole("Switched to iFrame " + this.getClass().getName());
        return this;
    }


}
