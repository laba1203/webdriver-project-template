package ui.abstractObjects.containers;

import lombok.SneakyThrows;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import ui.abstractObjects.elements.BasicElementWrapper;
import ui.abstractObjects.elements.ElementImpl;
import util.WaitFactory;
import util.exceptions.ObjectNotFound;
import util.log.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;

import static util.WaitFactory.TIMEOUT_1;

public interface ContainerWithRows {

    HashMap<Class<? extends AbstractRow>, HashMap<String, AbstractRow>> getAbstractRowsHashMap();

    /*
     * AbstractRows operations
     **/

    private long abstractRowsTimeout() {
        return TIMEOUT_1;
    }

    /**
     * @throws ObjectNotFound if rows is not found.
     */
    default AbstractRow findRowByIdentifier(Class<? extends AbstractRow> clazz, By lctr, String identifier) {
        HashMap<String, ? extends AbstractRow> rows = getRowsHash(clazz, lctr);
        Assert.assertNotNull(rows, "Hash with Abstract rows can not be null for class: " + clazz.getName());
        return findRow(rows, identifier);
    }

    default AbstractRow findRowByIdentifier(Class<? extends AbstractRow> clazz, BasicElementWrapper elements, String identifier) {
        return findRowByIdentifier(clazz, elements.getBy(), identifier);
    }

    /**
     * @throws ObjectNotFound if rows is not found.
     */
    default AbstractRow findRowContainsIdentifier(Class<? extends AbstractRow> clazz, BasicElementWrapper el, String identifier) {
        return findRowContainsIdentifier(clazz, el.getBy(), identifier);
    }

    /**
     * @throws ObjectNotFound if rows is not found.
     */
    default AbstractRow findRowContainsIdentifier(Class<? extends AbstractRow> clazz, By lctr, String identifier) {
        HashMap<String, ? extends AbstractRow> rows = getRowsHash(clazz, lctr);
        return findRowContains(rows, identifier, lctr);
    }

    /**
     * @returns null if row is not found
     */
    default AbstractRow getRowContainsIdentifier(Class<? extends AbstractRow> clazz, By lctr, String identifier) {
        HashMap<String, ? extends AbstractRow> rows = getRowsHash(clazz, lctr);
        return getRowContains(rows, identifier);
    }

    default AbstractRow getRowByIdentifier(Class<? extends AbstractRow> clazz, BasicElementWrapper el, String identifier) {
        return getRowByIdentifier(clazz, el.getBy(), identifier);
    }

    /**
     * @returns null if row is not found
     */
    default AbstractRow getRowByIdentifier(Class<? extends AbstractRow> clazz, By lctr, String identifier) {
        HashMap<String, ? extends AbstractRow> rows = getRowsHash(clazz, null, lctr, abstractRowsTimeout());
        return getRow(rows, identifier);
    }

    /**
     * @throws ObjectNotFound if rows is not found.
     */
    default AbstractRow findRowByIdentifier(Class<? extends AbstractRow> clazz, WebElement parent, By lctr, String identifier) {
        HashMap<String, ? extends AbstractRow> rows = getRowsHash(clazz, parent, lctr, abstractRowsTimeout());
        return findRow(rows, identifier);
    }

    /**
     * @returns null if row is not found
     */
    default AbstractRow getRowByIdentifier(Class<? extends AbstractRow> clazz, WebElement parent, By lctr, String identifier) {
        HashMap<String, ? extends AbstractRow> rows = getRowsHash(clazz, parent, lctr, abstractRowsTimeout());
        return getRow(rows, identifier);
    }

    /**
     * @throws ObjectNotFound if rows is not found.
     */
    default AbstractRow findRowByIdentifier(Class<? extends AbstractRow> clazz, By parent, By lctr, String identifier) {
        WebElement p = new ElementImpl(parent, "Parent for row").getWebElement();
        HashMap<String, ? extends AbstractRow> rows = getRowsHash(clazz, p, lctr, abstractRowsTimeout());
        return findRow(rows, identifier);
    }

    /**
     * @returns null if row is not found
     */
    private AbstractRow getRow(HashMap<String, ? extends AbstractRow> rows, String identifier) {
        return rows.get(identifier);
    }

    /**
     * @returns null if row is not found
     */
    private AbstractRow getRowContains(HashMap<String, ? extends AbstractRow> rows, String substr) {
        for (String key :
                rows.keySet()) {
            if (key.contains(substr)) {
                return rows.get(key);
            }
        }
        return null;
    }

    /**
     * @throws ObjectNotFound if rows is not found.
     */
    private AbstractRow findRow(HashMap<String, ? extends AbstractRow> rows, String identifier) {
        AbstractRow row = getRow(rows, identifier);
        if (row == null) {
            throw new ObjectNotFound("Row was not found by identifier '" + identifier + "'." +
                    "\nList of available rows: " +
                    "\n" + rows.keySet()
            );
        }
        return row;
    }

    /**
     * @throws ObjectNotFound if rows is not found.
     */
    private AbstractRow findRowContains(HashMap<String, ? extends AbstractRow> rows, String substring, By lctr) {
        AbstractRow row = getRowContains(rows, substring);
        if (row == null) {
            throw new ObjectNotFound("Row was not found which contains identifier '" + substring + "'. (Lctr: " + lctr + ")" +
                    "\nList of available rows: " +
                    "\n" + rows.keySet()
            );
        }
        return row;
    }

    default HashMap<String, ? extends AbstractRow> createRowsHash(Class<? extends AbstractRow> clazz, By lctr, long timeout) {
        return createRowsHash(clazz, null, lctr, timeout);
    }

    default HashMap<String, ? extends AbstractRow> createRowsHash(Class<? extends AbstractRow> clazz, WebElement parent, By lctr, long timeout) {
        /*HashMap<String, AbstractRow>*/
        HashMap<String, AbstractRow> rows = getCreatedHashRows(clazz);
        if (rows == null || rows.size() == 0) {
            //create new hash
            return initNewRowsHash(clazz, parent, lctr, timeout);
        }
        //reuse existing rows if they were initiated before
        Log.debug("All rows:" + rows);
        return rows;
    }

    default HashMap<String, ? extends AbstractRow> initNewRowsHash(Class<? extends AbstractRow> clazz, By lctr) {
        return initNewRowsHash(clazz, null, lctr, TIMEOUT_1);
    }

    default HashMap<String, ? extends AbstractRow> initNewRowsHash(Class<? extends AbstractRow> clazz, By lctr, long timeout) {
        return initNewRowsHash(clazz, null, lctr, timeout);
    }

    default HashMap<String, ? extends AbstractRow> initNewRowsHash(Class<? extends AbstractRow> clazz, WebElement parent, By lctr, long timeout) {
        HashMap<String, AbstractRow> rows = new HashMap<>();
        List<WebElement> elements;
        if (parent != null) {
            elements = new WaitFactory().findElements(parent, lctr, timeout);
        } else {
            elements = new WaitFactory().findElements(lctr, timeout);
        }
        try {
            for (WebElement el : elements) {
                Constructor<?> constructor = clazz.getDeclaredConstructor(WebElement.class);
                constructor.setAccessible(true);
                AbstractRow row = (AbstractRow) constructor.newInstance(el);
                String identifier = row.getIdentifier();
                Assert.assertNotNull(identifier, "AbstractRow identifier can't be null. Probably getIdentifier() should be fixed for the class: " + row.getClass().getName());
                rows.put(identifier, row);
            }
        } catch (NoSuchMethodException
                 | SecurityException
                 | InvocationTargetException
                 | IllegalAccessException
                 | InstantiationException e) {
            e.printStackTrace();
            Assert.fail("Exception captured in createRowsHash(). Details:\n" + e.getMessage());
        }
        addRowHash(clazz, rows);
        return rows;
    }

    private void addRowHash(Class<? extends AbstractRow> clazz, HashMap<String, AbstractRow> hash) {
        getAbstractRowsHashMap().put(clazz, hash);
    }

    default HashMap<String, AbstractRow> getCreatedHashRows(Class<? extends AbstractRow> clazz) {
        return getAbstractRowsHashMap().get(clazz);
    }

    default void clearRowsHash() {
        getAbstractRowsHashMap().clear();
    }

    default HashMap<String, ? extends AbstractRow> getRowsHash(Class<? extends AbstractRow> clazz, BasicElementWrapper rows) {
        return getRowsHash(clazz, rows.getBy());
    }

    default HashMap<String, ? extends AbstractRow> getRowsHash(Class<? extends AbstractRow> clazz, By lctr) {
        return getRowsHash(clazz, null, lctr, abstractRowsTimeout());
    }

    default HashMap<String, ? extends AbstractRow> getRowsHash(Class<? extends AbstractRow> clazz, WebElement parent, By lctr) {
        return getRowsHash(clazz, parent, lctr, abstractRowsTimeout());
    }

    @SneakyThrows
    default HashMap<String, ? extends AbstractRow> getRowsHash(Class<? extends AbstractRow> clazz, WebElement parent, By lctr, long timeout) {
        int rowsInitiationLimit = 10;

        try {
            return createRowsHash(clazz, parent, lctr, timeout);
        } catch (StaleElementReferenceException e) {
            Log.logRecord("Error during getRowsHash() ");
            e.printStackTrace();

            return createRowsHash(clazz, parent, lctr, timeout);
        } catch (AssertionError e) {
            e.printStackTrace();
            Log.debug("Error in getRowsHash(): " + e.getMessage());
            //repeat main actions
            return createRowsHash(clazz, parent, lctr, timeout);
        }
    }

}
