package util.execution;

import lombok.Getter;
import lombok.Setter;
import org.openqa.selenium.Cookie;
import org.testng.Assert;
import util.DriverWatcher;
import util.ExecutionVariables;
import util.log.Log;
import util.screenshot.ScreenshotOutput;

import java.io.File;
import java.util.*;

public class TestExecutionManager {

    private static boolean PREVIOUS_SCREENSHOTS_EXIST = true;
    private static boolean PREVIOUS_BROWSER_LOGS_EXIST = true;

    private static final HashMap<String, UserSession> USER_SESSIONS = new HashMap<>();
    private static final ArrayList<String> CRITICAL_EXECUTION_ERRORS = new ArrayList<>();

    private static final HashMap<String, DriverWatcher> DRIVER_WATCHERS = new HashMap<>();

    @Getter
    @Setter
    public static class UserSession {
        private String email;
        private Set<Cookie> cookies;
        private String csrf;

        public Set<Cookie> getSessionCookies() {
            Set<Cookie> cookies = new HashSet<>();
            for (Cookie c :
                    getCookies()) {
                if (c.getName().contains("session")) {
                    cookies.add(c);
                }
            }
            return cookies;
        }

    }

    public static void deleteFilesFromPreviousExecution() {
        //delete screenshot
        deletePreviousFiles(PREVIOUS_SCREENSHOTS_EXIST, ScreenshotOutput.PATH_TO_SAVE);
        PREVIOUS_SCREENSHOTS_EXIST = false;
        //delete Browser Logs
        deletePreviousFiles(PREVIOUS_BROWSER_LOGS_EXIST, LogsRecorder.PATH_TO_SAVE);
        PREVIOUS_BROWSER_LOGS_EXIST = false;

        Log.logInConsole("Output Files from previous test execution have been deleted.");
    }

    public static void addDriverWatcher(String threadId, DriverWatcher dw) {
        DRIVER_WATCHERS.put(threadId, dw);
    }

    public static HashMap<String, DriverWatcher> getDriverWatchers() {
        return DRIVER_WATCHERS;
    }

    public static void removeDriverWatchers(String id) {
        DRIVER_WATCHERS.remove(id);
    }

    private static void deletePreviousFiles(boolean indicator, String pathName) {
        if (indicator) {
            deleteFilesFormDir(pathName);
        }
        indicator = false;
    }

    private static void deleteFilesFormDir(String pathName) {
        File directory = new File(pathName);
        if (directory.exists()) {
            for (File file : Objects.requireNonNull(directory.listFiles())) {
                file.delete();
            }
        }
    }

    static void addCriticalError(String err) {
        CRITICAL_EXECUTION_ERRORS.add(err);
    }

    public static synchronized ArrayList<String> getCriticalExecutionErrors() {
        return CRITICAL_EXECUTION_ERRORS;
    }


    public static String getTestSuitePath() {
        String testSuite = ExecutionVariables.getMavenTestSuite();
        if (testSuite == null || testSuite.isEmpty()) {
            testSuite = ExecutionVariables.getMavenTestSuiteFullPath();
        }
        Assert.assertNotNull(testSuite, "Test Suite was not provided in maven command.");
        return testSuite;
    }

    public static boolean executingFullRegression() {
        return getTestSuitePath().contains("FullRegression");
    }

    public static boolean userAlreadyLogged(String email) {
        return USER_SESSIONS.containsKey(email);
    }

    public static synchronized void addUserSessionData(String email, Set<Cookie> cookies, String csrf) {
        UserSession us = new UserSession();
        us.setEmail(email);
        us.setCookies(cookies);
        us.setCsrf(csrf);

        USER_SESSIONS.put(email, us);
    }

    public static synchronized UserSession getUserSession(String email) {
        return USER_SESSIONS.get(email);
    }

    public static String getStackTraceForTestFolders() {
        StringBuilder prettifiedStacktrace = new StringBuilder();
        for (StackTraceElement e :
                getStackMatchedTrace()) {
            prettifiedStacktrace.append(e.toString()).append("\n");
        }
        return prettifiedStacktrace.toString();
    }

    private static ArrayList<StackTraceElement> getStackMatchedTrace() {
        ArrayList<StackTraceElement> elements = new ArrayList<>();
        for (StackTraceElement e :
                Thread.currentThread().getStackTrace()) {
            String className = e.getClassName();
            if (className.contains("testDataCreation.")
                    || className.contains("execution.regression.")
                    || className.contains("execution.ui.")
                    || className.contains("execution.api.")

            ) {
                elements.add(e);
            }
        }
        return elements;
    }


}
