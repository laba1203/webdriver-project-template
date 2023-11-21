package ui.steps;

import lombok.SneakyThrows;
import org.openqa.selenium.Keys;
import org.openqa.selenium.interactions.Actions;
import org.testng.Assert;
import util.DriverConfig;

public class KeyboardActions {

    @SneakyThrows
    public static void pressTab() {
        Actions actions = new Actions(DriverConfig.getDriver());
        actions.sendKeys(Keys.TAB).build().perform();
        Thread.sleep(500);
    }

    @SneakyThrows
    public static void pressTab(int times) {
        Assert.assertTrue(times >= 1, "Variable 'times' should be more than 1");
        for (int i = 1; i <= times; i++) {
            pressTab();
        }
    }

    public static void pressShiftTab() {
        Actions actions = new Actions(DriverConfig.getDriver());
        actions.sendKeys(Keys.SHIFT, Keys.TAB).build().perform();
    }

    public static void pressShiftTab(int times) {
        Assert.assertTrue(times >= 1, "Variable 'times' should be more than 1");
        for (int i = 1; i <= times; i++) {
            pressShiftTab();
        }
    }

    public static void pressSpace() {
        Actions actions = new Actions(DriverConfig.getDriver());
        actions.sendKeys(Keys.SPACE).build().perform();
    }

    public static void pressEsc() {
        Actions actions = new Actions(DriverConfig.getDriver());
        actions.sendKeys(Keys.ESCAPE).build().perform();
    }

    public static void pressEnter() {
        Actions actions = new Actions(DriverConfig.getDriver());
        actions.sendKeys(Keys.ENTER).build().perform();
    }

    public static void sendKeys(String value) {
        Actions actions = new Actions(DriverConfig.getDriver());
        actions.sendKeys(value).build().perform();
    }

    public static void keyDown(Keys keys) {
        Actions actions = new Actions(DriverConfig.getDriver());
        actions.keyDown(keys).build().perform();
    }

    public static void keyUp(Keys keys) {
        Actions actions = new Actions(DriverConfig.getDriver());
        actions.keyUp(keys).build().perform();
    }

}