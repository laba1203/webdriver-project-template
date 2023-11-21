package util.execution;

import org.openqa.selenium.UnsupportedCommandException;
import org.openqa.selenium.json.JsonException;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import util.Browser;
import util.DriverConfig;
import util.log.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;

public class TestProfile {

    private static final ThreadLocal<LogEntries> LOGS = new ThreadLocal<>();
    private static final ThreadLocal<ArrayList<String>> CAPTURED_CONSOLE_ERRORS = new ThreadLocal<>();
    private static final ThreadLocal<TestExecutionManager.UserSession> CURRENT_USER_SESSION_PRIMARY_DRIVER = new ThreadLocal<>();
    private static final ThreadLocal<TestExecutionManager.UserSession> CURRENT_USER_SESSION_SECONDARY_DRIVER = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_TEST_CLASS = new ThreadLocal<>();
    private boolean executionInProgress = true;


    public void executionCompleted() {
        LOGS.remove();
        CAPTURED_CONSOLE_ERRORS.remove();
        executionInProgress = false;
        cleanupCurrentUserSession();
    }

    public static synchronized void setLogs(LogEntries entries) {
        if (entries != null && !entries.getAll().isEmpty()) {
            LOGS.set(entries);
        }
    }

    public static synchronized void addCapturedError(String exception) {
        getConsoleCapturedErrorTags().add(exception);
    }

    public static synchronized ArrayList<String> getConsoleCapturedErrorTags() {
        if (CAPTURED_CONSOLE_ERRORS.get() == null) {
            CAPTURED_CONSOLE_ERRORS.set(new ArrayList<>());
        }
        return CAPTURED_CONSOLE_ERRORS.get();
    }

    public static synchronized void setCurrentUserSession(TestExecutionManager.UserSession userSession) {
        if (DriverConfig.isUsingPrimaryDriver()) {
            CURRENT_USER_SESSION_PRIMARY_DRIVER.set(userSession);
        } else {
            CURRENT_USER_SESSION_SECONDARY_DRIVER.set(userSession);
        }
    }

    public static synchronized void setCurrentTestClass(String name) {
        CURRENT_TEST_CLASS.set(name);
    }

    public static synchronized String getCurrentTestClass() {
        return CURRENT_TEST_CLASS.get();
    }

    public static synchronized TestExecutionManager.UserSession getCurrentUserSession() {
        if (DriverConfig.isUsingPrimaryDriver()) {
            return CURRENT_USER_SESSION_PRIMARY_DRIVER.get();
        } else {
            return CURRENT_USER_SESSION_SECONDARY_DRIVER.get();
        }
    }

    public static synchronized void cleanupCurrentUserSession() {
        CURRENT_USER_SESSION_PRIMARY_DRIVER.remove();
        CURRENT_USER_SESSION_SECONDARY_DRIVER.remove();
    }

    public static synchronized LogEntries getBrowserLogs() {
        LogEntries logEntries;
        try {
            logEntries = DriverConfig.getDriver().manage().logs().get(LogType.BROWSER);
        } catch (JsonException | UnsupportedCommandException e) {
            Log.debug("Browser log were not provided due to error: " + e.getMessage() + "\nBrowser: " + Browser.getBrowserInfo());
            e.printStackTrace();
            logEntries = new LogEntries(Collections.singleton(new LogEntry(Level.ALL, 1, "Logs were not provided due to error.")));
        }
        if (logEntries != null && logEntries.getAll().size() != 0) {
            setLogs(logEntries);
            return logEntries;
        }
        if (LOGS.get() == null) {
            return logEntries;
        }
        return LOGS.get();
    }

    public boolean jsErrorCaptured() {
        for (LogEntry entry : getBrowserLogs()) {
            String msg = entry.getMessage();
            if (TestResultAnalyzer.isJsError(msg)) {
                return true;
            }
        }
        return false;
    }

    private boolean errorCaptured(String error) {
        for (LogEntry entry : getBrowserLogs()) {
            String msg = entry.getMessage();
            if (TestResultAnalyzer.isServerError(error, msg)) {
                return true;
            }
        }
        return false;
    }

    public boolean error500Captured() {
        return errorCaptured(" 500 ");
    }

    public boolean error502Captured() {
        return errorCaptured(" 502 ");
    }

    public boolean error504Captured() {
        return errorCaptured(" 504 ");
    }


}
