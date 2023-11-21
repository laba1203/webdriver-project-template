package ui.abstractObjects.elements.factory;

import ui.abstractObjects.elements.BasicElementWrapper;
import ui.abstractObjects.elements.Element;
import ui.abstractObjects.elements.ElementImpl;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WrapsElement;
import org.openqa.selenium.interactions.Locatable;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;
import org.openqa.selenium.support.FindAll;
import org.openqa.selenium.support.pagefactory.*;
import org.openqa.selenium.support.pagefactory.internal.LocatingElementListHandler;
import util.log.Log;

import java.lang.reflect.*;
import java.util.List;

/**
 * WrappedElementDecorator recognizes a few things that DefaultFieldDecorator does not.
 * <p/>
 * It is designed to support and return concrete implementations of wrappers for a variety of common html elements.
 */
public class ElementDecorator implements FieldDecorator {
    /**
     * factory to use when generating ElementLocator.
     */
    private final ElementLocatorFactory factory;

    /**
     * Constructor for an ElementLocatorFactory. This class is designed to replace DefaultFieldDecorator.
     *
     * @param factory for locating elements.
     */
    public ElementDecorator(ElementLocatorFactory factory) {
        this.factory = factory;
    }

    private boolean fieldMatched(Field field){
        return (WebElement.class.isAssignableFrom(field.getType())
                || isDecoratableList(field)
                || BasicElementWrapper.class.isAssignableFrom(field.getType())
        );
    }

    @Override
    public Object decorate(ClassLoader loader, Field field) {
        if (!fieldMatched(field)) {
            return null;
        }

        if (field.getDeclaringClass() == ElementImpl.class) {
            return null;
        }
        if(field.getDeclaredAnnotations().length == 0){
            Log.debug("Annotations are absent for field: " + field.getName());
            return null;
        }

        ElementLocator locator = factory.createLocator(field);
        if (locator == null) {
            return null;
        }

        Class<?> fieldType = field.getType();
        if (WebElement.class.equals(fieldType)) {
            fieldType = Element.class;
        }

        if (BasicElementWrapper.class.isAssignableFrom(fieldType)) {
            return proxyForLocator(loader, fieldType, locator, field);
        } else if (List.class.isAssignableFrom(fieldType)) {
            Class<?> erasureClass = getErasureClass(field);
            return proxyForListLocator(loader, erasureClass, locator);
        } else {
            return null;
        }
    }

    private Class<?> getErasureClass(Field field) {
        // Type erasure in Java isn't complete. Attempt to discover the generic
        // interfaceType of the list.
        Type genericType = field.getGenericType();
        if (!(genericType instanceof ParameterizedType)) {
            return null;
        }
        return (Class<?>) ((ParameterizedType) genericType).getActualTypeArguments()[0];
    }

    private boolean isDecoratableList(Field field) {
        if (!List.class.isAssignableFrom(field.getType())) {
            return false;
        }

        Class<?> erasureClass = getErasureClass(field);
        if (erasureClass == null) {
            return false;
        }

        if (!(WebElement.class.isAssignableFrom(erasureClass) || BasicElementWrapper.class.isAssignableFrom(erasureClass))) {
            return false;
        }

        return field.getAnnotation(FindBy.class) != null ||
                field.getAnnotation(FindBys.class) != null ||
                field.getAnnotation(FindAll.class) != null;
    }

    /**
     * Generate a type-parameterized locator proxy for the element in question. We use our customized InvocationHandler
     * here to wrap classes.
     *
     * @param loader        ClassLoader of the wrapping class
     * @param interfaceType Interface wrapping the underlying WebElement
     * @param locator       ElementLocator pointing at a proxy of the object on the page
     * @param <T>           The interface of the proxy.
     * @return a proxy representing the class we need to wrap.
     */
    protected <T> T proxyForLocator(ClassLoader loader, Class<T> interfaceType, ElementLocator locator, Field field) {
        InvocationHandler handler = new ElementHandler(interfaceType, locator, field);

        T proxy;
        proxy = interfaceType.cast(Proxy.newProxyInstance(
                loader, new Class[]{interfaceType, WebElement.class, WrapsElement.class, Locatable.class}, handler));
        return proxy;
    }

    /**
     * generates a proxy for a list of elements to be wrapped.
     *
     * @param loader        classloader for the class we're presently wrapping with proxies
     * @param interfaceType type of the element to be wrapped
     * @param locator       locator for items on the page being wrapped
     * @param <T>           class of the interface.
     * @return proxy with the same type as we started with.
     */
    @SuppressWarnings("unchecked")
    protected <T> List<T> proxyForListLocator(ClassLoader loader, Class<T> interfaceType, ElementLocator locator) {
        InvocationHandler handler;
        if (interfaceType.getAnnotation(ImplementedBy.class) != null) {
            handler = new ElementListHandler(interfaceType, locator);
        } else {
            handler = new LocatingElementListHandler(locator);
        }
        List<T> proxy;
        proxy = (List<T>) Proxy.newProxyInstance(
                loader, new Class[]{List.class}, handler);
        return proxy;
    }
}
