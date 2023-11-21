package ui.abstractObjects.elements.factory;


import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.pagefactory.AbstractAnnotations;
import org.openqa.selenium.support.pagefactory.Annotations;
import org.openqa.selenium.support.pagefactory.ElementLocator;
import org.testng.Assert;
import ui.abstractObjects.elements.BasicElementWrapper;
import ui.abstractObjects.elements.Element;
import util.log.Log;

import java.lang.reflect.*;

import static ui.abstractObjects.elements.factory.ImplementedByProcessor.getWrapperClass;

/**
 * Replaces DefaultLocatingElementHandler. Simply opens it up to descendants of the WebElement interface, and other
 * mix-ins of WebElement and Locatable, etc. Saves the wrapping type for calling the constructor of the wrapped classes.
 */
public class ElementHandler implements InvocationHandler {
    private final String name;
    private final By by;
    private final ElementLocator locator;
    private final Class<?> wrappingType;
    private final Class<?> interfaceType;


    /**
     * Generates a handler to retrieve the WebElement from a locator for a given WebElement interface descendant.
     *
     * @param interfaceType Interface wrapping this class. It contains a reference the the implementation.
     * @param locator       Element locator that finds the element on a page.
     * @param <T>           type of the interface
     */
    public <T> ElementHandler(Class<T> interfaceType, ElementLocator locator, Field field) {
        this.name = field.getName();
        this.by = ((AbstractAnnotations) (new Annotations(field))).buildBy();
        this.locator = locator;
        this.interfaceType = interfaceType;
        if (!BasicElementWrapper.class.isAssignableFrom(interfaceType)) {
            throw new RuntimeException("interface not assignable to Element.");
        }

        this.wrappingType = getWrapperClass(interfaceType);
    }

    private Object initiateElement() throws Throwable {
        Constructor<?> cons;
        Object thing;

        WebElement element = locator.findElement();
        if (BasicElementWrapper.class.isAssignableFrom(interfaceType)) {
            cons = wrappingType.getConstructor(WebElement.class, String.class);
            thing = cons.newInstance(element, name);
        } else {
            Log.debug("BasicElementWrapper is not assignable for the object");
            cons = wrappingType.getConstructor(WebElement.class);
            thing = cons.newInstance(element);
        }
        ((BasicElementWrapper) thing).setBy(by);
        return thing;
    }

    @Override
    public Object invoke(Object object, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();

        switch (methodName) {
            default:
                break;
            case "getBy":
                return by;
            case "getName":
                return name;
            case "toString":
                return "Proxy element for: " + locator;
            case "getWrappedElement":
                Log.debug("returning from getWrappedElement");
                return locator.findElement();
            case "waitVisibility":
            case "waitPresence":
                return executeWaitMethod(method, args);
        }

        Object thing;
        thing = initiateElement();
        try {
            return method.invoke(wrappingType.cast(thing), args);
        } catch (InvocationTargetException e) {
            // Unwrap the underlying exception
            throw e.getCause();
        }
    }

    private Object executeWaitMethod(Method method, Object[] args) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Constructor<?> cons;
        cons = wrappingType.getConstructor(By.class, String.class);
        Long timeout = null;
        if(args!= null && args.length > 0){
            try {
                timeout = (Long) args[0];
            }catch (ClassCastException e){
                Log.debug("Unable to cast to Long object: " + args[0]);
                e.printStackTrace();
            }
        }

        Element el = ((Element) cons.newInstance(by, name));
        switch (method.getName()) {
            default:
                Assert.fail("Unknown method type: " + method.getName());

            case "waitPresence":
                if (timeout != null) {
                    return el.waitPresence(timeout);
                }
                return el.waitPresence();

            case "waitVisibility":
                if (timeout != null) {
                    return el.waitVisibility(timeout);
                }
                return el.waitVisibility();
        }
    }


    //Old variant
//    @Override
//    public Object invoke(Object object, Method method, Object[] objects) throws Throwable {
//        final WebElement element;
//        String methodName = method.getName();
////        if (methodName.equals("toString")) {
////            return "Proxy element for: " + locator;
////        }
////        if (methodName.equals("getBy") && by != null) {
////            return by;
////        }
////        if (methodName.equals("getName") && name != null) {
////            return name;
////        }
//        switch (methodName) {
//            default:
//                break;
//            case "getBy":
//                return by;
//            case "getName":
//                return name;
//            case "toString":
//                return "Proxy element for: " + locator;
//        }
//
//        element = locator.findElement();
//
//        if ("getWrappedElement".equals(method.getName())) {
//            Log.debug("returning from getWrappedElement");
//            return element;
//        }
//        Constructor<?> cons;
//        Object thing;
//        if (BasicElementWrapper.class.isAssignableFrom(interfaceType)) {
//            cons = wrappingType.getConstructor(WebElement.class, String.class);
//            thing = cons.newInstance(element, name);
//            ((BasicElementWrapper) thing).setBy(by);
//        } else {
//            Log.debug("BasicElementWrapper is not assignable for the object");
//            cons = wrappingType.getConstructor(WebElement.class);
//            thing = cons.newInstance(element);
//        }
//        try {
//            return method.invoke(wrappingType.cast(thing), objects);
//        } catch (InvocationTargetException e) {
//            // Unwrap the underlying exception
//            throw e.getCause();
//        }
//    }

}
