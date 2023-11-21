package ui.steps;

import ui.abstractObjects.elements.Element;
import ui.abstractObjects.elements.ElementImpl;
import util.log.Log;
import util.screenshot.SnapshotAssertFactory;


/**
 * Class to allocate common scenarios related to comparing of screenshots
 */
public class VisualSteps {

    public static void assertScreenshot(ElementImpl element, String testSuiteName, String elementName) {
        new SnapshotAssertFactory(testSuiteName, elementName).assertElementScreenshot(element);
    }

    public static void assertScreenshot(Element element, String testSuiteName, String elementName) {
        new SnapshotAssertFactory(testSuiteName, elementName).assertElementScreenshot(element);
    }

    public static void assertScreenshot(Element element, String testSuiteName, String elementName, double deviation) {
        new SnapshotAssertFactory(testSuiteName, elementName).assertElementScreenshot(element, deviation);
    }

    public static void assertFullPageScreenshot(String testSuiteName, String pageName) {
        Log.debug("Test suite path for the screenshot: " + testSuiteName);
        new SnapshotAssertFactory(testSuiteName, pageName).assertPageScreenshot();
    }


}
