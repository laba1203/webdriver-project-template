package ui.steps;

import org.openqa.selenium.JavascriptExecutor;
import util.DriverConfig;
import util.log.Log;

public class Steps {

    public static String getCurrentUrl() {
        return DriverConfig.getDriver().getCurrentUrl();
    }

    public static void refresh() {
        DriverConfig.getDriver().navigate().refresh();
    }

    public static Object executeJsScript(String script) {
        JavascriptExecutor js = (JavascriptExecutor) DriverConfig.getDriver();
        Log.logRecord("Script for execution: \r\n" + script);
        return js.executeScript(script);
    }

}
