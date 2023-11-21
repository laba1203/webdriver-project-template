package ui.abstractObjects.containers;

import org.openqa.selenium.By;
import ui.abstractObjects.elements.ElementImpl;
import ui.abstractObjects.elements.SelectImpl;

public interface ContainerWithElements {

    boolean isElementVisible(By locator);

    default ElementImpl element(By by, String name) {
        return new ElementImpl(by, name);
    }


    default SelectImpl select(By by, String name) {
        return new SelectImpl(by, name);
    }

}
