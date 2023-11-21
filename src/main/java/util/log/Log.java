package util.log;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import util.BrowserProfile;
import util.Dates;
import util.DriverConfig;

public class Log {


    public static void logInReport(String msg) {
        logRecord(msg);
    }

    public static String threadId() {
        return "(#" + Thread.currentThread().getId() + ")";
    }

    private static String prefix(String substr) {
        return time() + " " + substr + threadId() + ": ";
    }

    private static String time() {
        return Dates.getCurrentDateTimeInFormat("HH:mm:ss"); //yyyy/MM/dd HH:mm:ss
    }

    private static String logPrefix() {
        return prefix("LOG");
    }

    private static String debugPrefix() {
        return prefix("DEBUG");
    }

    public static void logRecord(String msg) {
//        printAction(logPrefix() + msg);
        logRecord(msg, true);
    }

    public static void logInConsole(String msg) {
        logRecord(msg, false);
    }

    public static void logRecord(String msg, boolean logInReport) {
        printAction(logPrefix() + msg);
        if (logInReport) {
            ExtentSteps.passStep(msg);
        }
    }

    public static void testFailed(String msg) {
        printAction(prefix("FAILED") + msg);
    }

    public static void testPassed(String msg) {
        printAction(prefix("PASSED") + msg);
    }

    public static void debug(String msg) {
        printAction(debugPrefix() + msg);
    }

    private static void printAction(String msg) {
        System.out.println(msg);
    }

    public static void switchedToWindowMsg(String windowHandle) {
        logInConsole("Switched to Window: " + windowHandle);
    }

    public static void logDriverInfo(BrowserProfile bp) {
        Capabilities cap = ((RemoteWebDriver) DriverConfig.getDriver()).getCapabilities();
        String browserName = cap.getBrowserName().toLowerCase();
        String v = cap.getBrowserVersion();
        String os = cap.getPlatformName().toString();
        String message =
                "\nWeb Driver info:\n" +
                        infoLogRow("Browser name", browserName) +
                        infoLogRow("Browser version", v) +
                        infoLogRow("Platform", os) +
                        "Browser profile settings:\n" +
                        infoLogRow("User Agent", bp.getUserAgent()) +
                        infoLogRow("Emulate mobile", String.valueOf(bp.getEmulateMobile()));
        logRecord(message);
    }

    private static String infoLogRow(String name, String value) {
        if (value != null) {
            return " * " + name + ": " + value + "\n";
        }
        return "";
    }
}
