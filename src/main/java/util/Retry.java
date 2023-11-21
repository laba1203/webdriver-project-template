package util;

import com.google.common.base.Stopwatch;
import lombok.NonNull;
import lombok.SneakyThrows;
import ui.abstractObjects.elements.ElementImpl;
import ui.steps.Steps;
import util.log.Log;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


public class Retry {
    private static final int DEFAULT_MAX_RETRY = 2;

    public static void perform(RetryCallback callable, Class<? extends Throwable> expectedException) {
        perform(callable, expectedException, null, DEFAULT_MAX_RETRY, 1);
    }

    public static void perform(RetryCallback callable, int maxRetries) {
        perform(callable, Throwable.class, null, maxRetries, 0);
    }

    public static void perform(RetryCallback callable, String errorMessage, int maxRetries, int pauseInSec) {
        perform(callable, Throwable.class, errorMessage, maxRetries, pauseInSec);
    }

    public static void perform(RetryCallback callable, Class<? extends Throwable> expectedException, int maxRetries, int pause) {
        perform(callable, expectedException, null, maxRetries, pause);
    }

    public static void perform(RetryCallback callable, String errorMessage, int maxRetries) {
        perform(callable, Throwable.class, errorMessage, maxRetries, 0);
    }

    public static void perform(RetryCallback callable, int maxRetries, int pauseInSec) {
        perform(callable, Throwable.class, null, maxRetries, pauseInSec);
    }

    public static void perform(RetryCallback callable, Class<? extends Throwable> expectedException, int maxRetries) {
        perform(callable, expectedException, null, maxRetries, 0);
    }

    @SneakyThrows
    public static void perform(RetryCallback callable, Class<? extends Throwable> expectedException,
                               String message, int maxRetries, int pauseInSec) {
        perform(callable, List.of(expectedException), message, maxRetries, pauseInSec);
    }

    @SneakyThrows
    public static void perform(RetryCallback callable, List<Class<? extends Throwable>> expectedException,
                               String message, int maxRetries, int pauseInSec) {

        Stopwatch stopwatch = Stopwatch.createStarted();
        int retriesLeft = maxRetries;
        while (retriesLeft >= 0) {
            --retriesLeft;
            try {
                callable.call();
                return;
            } catch (Throwable e) {
                analyzeError(e, expectedException, message, retriesLeft, maxRetries, pauseInSec, stopwatch);
            }
        }
    }

    public static ElementImpl findElement(@NonNull FindCallback<ElementImpl> callable,
                                          @NonNull Class<? extends Throwable> expectedException) {
        return findElement(callable, expectedException, null, DEFAULT_MAX_RETRY, 0);
    }

    public static String getText(@NonNull FindCallback<String> callable,
                                 @NonNull Class<? extends Throwable> expectedException, int maxRetry) {
        return getText(callable, expectedException, maxRetry, null);
    }

    public static String getText(@NonNull FindCallback<String> callable,
                                 @NonNull Class<? extends Throwable> expectedException) {
        return (String) get(callable, expectedException, null, DEFAULT_MAX_RETRY, 0);
    }

    public static String getText(@NonNull FindCallback<String> callable,
                                 @NonNull Class<? extends Throwable> expectedException, int maxRetry, String msg) {
        return (String) get(callable, expectedException, msg, maxRetry, 0);
    }

    public static Boolean getBoolean(@NonNull FindCallback<Boolean> callable,
                                     @NonNull Class<? extends Throwable> expectedException, int maxRetry, String msg) {
        return (Boolean) get(callable, expectedException, msg, maxRetry, 0);
    }

    public static String getText(@NonNull FindCallback<String> callable,
                                 @NonNull Class<? extends Throwable> expectedException, int maxRetry, int pause, String msg) {
        return (String) get(callable, expectedException, msg, maxRetry, pause);
    }

    public static ElementImpl findElement(@NonNull FindCallback<ElementImpl> callable,
                                          @NonNull Class<? extends Throwable> expectedException,
                                          String message, int maxRetries, int pauseInSec) {
        return (ElementImpl) get(callable, expectedException, message, maxRetries, pauseInSec);
    }

    private static Object get(@NonNull FindCallback<?> callable,
                              @NonNull Class<? extends Throwable> expectedException,
                              String message, int maxRetries, int pauseInSec) {

        Stopwatch stopwatch = Stopwatch.createStarted();
        int retriesLeft = maxRetries;
        while (retriesLeft >= 0) {
            --retriesLeft;
            try {
                return callable.call();
            } catch (Throwable e) {
                analyzeError(e, List.of(expectedException), message, retriesLeft, maxRetries, pauseInSec, stopwatch);
            }
        }
        return null;
    }

    public static void retryWithPageRefresh(@NonNull RetryCallback callable,
                                            @NonNull Class<? extends Throwable> expectedException, int maxRetries) {
        retryWithPageRefresh(callable, expectedException, "", maxRetries, 0);
    }

    public static void retryWithPageRefresh(@NonNull RetryCallback callable,
                                            @NonNull Class<? extends Throwable> expectedException, String message, int maxRetries, int pauseInSec) {

        Stopwatch stopwatch = Stopwatch.createStarted();
        int retriesLeft = maxRetries;
        while (retriesLeft >= 0) {
            try {
                if (retriesLeft != maxRetries) {
                    Steps.refresh();
                }
                --retriesLeft;
                callable.call();
                return;
            } catch (Throwable e) {
                analyzeError(e, List.of(expectedException), message, retriesLeft, maxRetries, pauseInSec, stopwatch);
            }
        }
    }

    private static boolean exceptionMatched(List<Class<? extends Throwable>> exceptionsList, Throwable e) {
        for (Class<? extends Throwable> eClass :
                exceptionsList) {
            if (eClass.isInstance(e)) return true;
        }
        return false;
    }

    @SneakyThrows
    private static void analyzeError(Throwable e, List<Class<? extends Throwable>> expectedExceptions,
                                     String message, int retriesLeft, int maxRetries, int pauseInSec, Stopwatch stopwatch) {

        if (!exceptionMatched(expectedExceptions, e)) {
            throw e;
        } else {
            Log.debug(Objects.toString(e));
            Log.debug(String.format("Action failed. Retries left: %d", retriesLeft));
            e.printStackTrace();
        }
        if (retriesLeft <= 0) throw rethrow(e, message, stopwatch, maxRetries);
        Thread.sleep(pauseInSec * 1000L);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> RuntimeException rethrow(Throwable throwable, String msg, Stopwatch stopwatch, int maxRetries) throws T {
        long elapsed = stopwatch.stop().elapsed(TimeUnit.MILLISECONDS);
        String fullMsg = ("Failed after retried " + maxRetries + "time(s) for " +
                String.format("%02d min, %02d sec", TimeUnit.MILLISECONDS.toMinutes(elapsed),
                        TimeUnit.MILLISECONDS.toSeconds(elapsed) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(elapsed))));
        if (msg != null) {
            fullMsg = msg + ". " + fullMsg;
        }

        if (throwable instanceof AssertionError) {
            fullMsg = throwable.getMessage() + ". " + fullMsg;
            Log.logInConsole(fullMsg);
            throw new AssertionError(fullMsg, throwable);
        } else {
            throwable.addSuppressed(new AssertionError(fullMsg));
            throw (T) throwable;
        }
    }

    @FunctionalInterface
    public interface RetryCallback {
        void call() throws Exception;
    }

    @FunctionalInterface
    public interface FindCallback<V> {
        V call() throws Exception;
    }

    @FunctionalInterface
    public interface RetryCallbackWithoutErrors {
        void call();
    }

}