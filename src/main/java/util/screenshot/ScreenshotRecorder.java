package util.screenshot;

import com.assertthat.selenium_shutterbug.core.Capture;
import com.assertthat.selenium_shutterbug.core.PageSnapshot;
import com.assertthat.selenium_shutterbug.core.Shutterbug;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import ui.abstractObjects.elements.Element;
import util.DriverConfig;

import java.io.File;
import java.util.Objects;

public class ScreenshotRecorder {


    public File shotElement(Element element){
        return element.getWebElement().getScreenshotAs(OutputType.FILE);
    }

    /**
     * Takes page screenshot for the visible part.<br/>
     * <b>Note:</b> It doesn't take full page screenshot only visible part as per browser size!!
     */
    public File takeVisiblePage(){
        return ((TakesScreenshot) Objects.requireNonNull(DriverConfig.getDriver())).getScreenshotAs(OutputType.FILE);
    }

    public PageSnapshot takeFullPage(){
        return Shutterbug.shootPage(DriverConfig.getDriver(), Capture.FULL_SCROLL);
    }

}
