package ui.abstractObjects.elements;

import org.openqa.selenium.WebElement;
import ui.abstractObjects.elements.factory.ImplementedBy;

import java.util.ArrayList;
import java.util.List;

@ImplementedBy(SelectImpl.class)
public interface Select extends BasicElementWrapper {

    void selectByValue(String v);

    void selectByVisibleText(String v);

    void selectOptionContains(String substr);

    default ArrayList<String> getAvailableValues() {
        List<WebElement> items = getOptionsElements();

        ArrayList<String> itemsNames = new ArrayList<>();
        for (WebElement li : items) {
            String value = li.getText();
            itemsNames.add(value);
        }
        return itemsNames;
    }

    List<WebElement> getOptionsElements();

    default ArrayList<String> getOptionValues() {
        ArrayList<String> names = new ArrayList<>();
        List<WebElement> options = this.getOptionsElements();
        for (WebElement action : options) {
            names.add(action.getText());
        }
        return names;
    }

    String getSelectedValue();

}