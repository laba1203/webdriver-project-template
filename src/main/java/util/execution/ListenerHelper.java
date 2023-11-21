package util.execution;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import util.Browser;
import util.DriverConfig;
import util.log.ApiLogger;
import util.log.Log;
import util.screenshot.ScreenshotOutput;

import java.util.Objects;

class ListenerHelper {

    static String getURL() {
        try {
            if (DriverConfig.driverCreated()) {
                return Objects.requireNonNull(DriverConfig.getDriver()).getCurrentUrl();
            } else {
                Log.debug("URL is not available since Driver is null.");
                return "URL is not available since Driver is null.";
            }
        } catch (Throwable e) {
            return "Unable to get URL due to error: " + e.getMessage();
        }
    }

    static String buildTestArtifacts(String url, ScreenshotOutput screenshotOutput, LogsRecorder.Info logsInfo, ApiThreadResponse.RequestData requestData) {
        String apiData = "";
        if (requestData != null) {
            apiData = ApiLogger.buildApiResponse(requestData);
        }
        return "\r\n   ***  *** TEST ARTIFACTS  ***  *** \r\n" +
                "Test failed on URL: " + url + "\r\n\n" +
                "Screenshot: " + screenshotOutput.getPath() + "\r\n" +
                "Browser info: " + Browser.getBrowserInfo() + "\r\n" +
                buildMsgForAnalysedLogs(logsInfo) + "\r\n" +
                apiData
                ;
    }

    static String buildMsgForAnalysedLogs(LogsRecorder.Info info) {
        if (!DriverConfig.driverCreated()) {
            return "Browser logs are not available since browser was not created.";
        }
        try {
            return "File with browser logs: " + info.getFullPath() + "\r\n" +
                    new TestResultAnalyzer().analiseLogsResult(info.getEntries());
        } catch (Throwable e) {
            Log.debug("Error during getting of the browsers logs:" + e.getMessage());
            e.printStackTrace();
            return "File with browser logs and initial analise is not available because of error:\n" + e.getMessage();
        }
    }

    static void updateOriginalMessage(ITestResult result, String newMessage) {
        try {
            FieldUtils.writeField(result.getThrowable(), "detailMessage", newMessage, true);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    static boolean shouldBeLoggedInReport(ITestResult testResult) {
        ITestNGMethod m = testResult.getMethod();
        return m.isTest()
                || m.isBeforeClassConfiguration()
                || m.isBeforeGroupsConfiguration()
                || m.isBeforeSuiteConfiguration()
                || m.isAfterSuiteConfiguration()
                || m.isAfterTestConfiguration()
                || m.isAfterGroupsConfiguration()
                || m.isAfterClassConfiguration()
                ;

    }
}
