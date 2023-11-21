package util.execution;

import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.testng.Assert;
import util.Browser;
import util.log.ExtentManager;

import java.util.List;

public class TestResultAnalyzer {

    public static final String JS_TAG = "JS_error";

    private static final List<String> SERVER_ERRORS = List.of(
            " 500 ",
            " 502 ",
            " 503 ",
            " 504 ",
            " 404 ",
            " 400 ",
            " 422 ",
            " 429 "
    );

    private static final String[] JS_ERRORS = new String[]{
            "Uncaught",
            "TypeError",
            "Ajax failure",
            "Unable to fetch",
            "SyntaxError",
            "React error",
            "Error: Route parameter missing",
            "RangeError"
    };

    public String analiseLogsResult() {
        LogsRecorder recorder = new LogsRecorder();
        try {
            return analiseLogsResult(recorder.getBrowserLogs());
        } catch (WebDriverException e) {
            e.printStackTrace();
            return "Browser logs are not available due to Exception (Browser: " + Browser.getBrowserInfo() + "). " +
                    "\nException: " + e.getMessage();
        }
    }

    public String analiseLogsResult(LogEntries entries) {
        return "  <<< --- Initial analysis of the test execution --- >>>\r\n" +
                getErrorsListForLogs(entries) +
                "  <<< -------  End of the initial analysis  -------- >>>\r\n";


    }

    private String getErrorsListForLogs(LogEntries logEntries) {
        TestProfile.setLogs(logEntries);
        StringBuilder out = new StringBuilder();
        for (LogEntry entry : logEntries) {
            String msg = entry.getMessage();
            String err = getMatchedError(msg);
            if(!err.isEmpty()){
                if(err.contains("JS") || err.contains(" 500 ")){
                    TestExecutionManager.addCriticalError(
                            err + "error captured. Message: " + msg + ". " +
                            "\nTest: " + TestProfile.getCurrentTestClass()
                    );
                }
                String result = "       * Error " + err + " was detected in: " + msg + "\r\n";
                out.append(result);
            }
        }

        return out.toString();
    }

    private String getMatchedServerError(String msg) {
        String exceptionResource = "https://tracking.intentsify.io";
        if(msg.startsWith(exceptionResource) && msg.contains("502")){
            return "";
        }
        for (String err : SERVER_ERRORS) {
            if (isServerError(err, msg)) {
                logInExtentReport(err);
                TestProfile.addCapturedError(err);
                return err;
            }
        }
        return "";
    }

    private void logInExtentReport(String e){
        List<String> errorsForLabel = List.of(" 500 ", " 502 ", " 504 ");
        if(errorsForLabel.contains(e)){
            ExtentManager.getTest().assignCategory(e);
        }
    }

    private String getMatchedError(String msg){
        String error = getMatchedServerError(msg);
        if(error.isEmpty() && isJsError(msg)){
            error = "JS";
            ExtentManager.getTest().assignCategory(JS_TAG);
            TestProfile.addCapturedError(JS_TAG);
        }
        return error;
    }

    public static boolean isServerError(String errorCode, String msg) {
        return msg.contains(errorCode);
    }

    private boolean isServerError(String msg) {
        return !getMatchedServerError(msg).isEmpty();
    }

    public static boolean isJsError(String msg) {
        for (String err : JS_ERRORS) {
            if (msg.contains(err)) {
                return true;
            }
        }
        return false;
    }

    public void assertErrorWasNotCaptured() {
        String errors = getErrorsListForLogs(new LogsRecorder().getBrowserLogs());
        Assert.assertTrue(errors.isEmpty(), "The following errors were found in Browser logs:\n" + errors);
    }

    public void assertJsErrorWasNotCaptured() {
        StringBuilder errors = new StringBuilder();
        for (LogEntry entry : new LogsRecorder().getBrowserLogs()) {
            String msg = entry.getMessage();
            if (isJsError(msg)) {
                String result = "       * JS Error was detected in: " + msg + "\r\n";
                errors.append(result);
            }
        }
        Assert.assertTrue(errors.toString().isEmpty(), "The following errors were found in Browser logs:\n" + errors);
    }


}
