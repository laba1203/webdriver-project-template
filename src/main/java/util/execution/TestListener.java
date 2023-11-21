package util.execution;

import com.aventstack.extentreports.AnalysisStrategy;
import com.aventstack.extentreports.Status;
import org.testng.*;
import org.testng.internal.ConstructorOrMethod;
import util.log.ExtentManager;
import util.log.ExtentService;
import util.log.Log;
import util.log.MarkupHelper;
import util.screenshot.ScreenshotOutput;

public class TestListener implements ITestListener, IInvokedMethodListener {
    private String methodName;
    private String className;

    @Override
    public synchronized void onStart(ITestContext context) {
        ITestListener.super.onStart(context);
        ExtentService.getInstance().setAnalysisStrategy(AnalysisStrategy.CLASS);
    }

    @Override
    public synchronized void onFinish(ITestContext context) {
        ExtentService.getInstance().flush();
    }

    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult result) {
        Log.debug("Before invocation for " + method.getTestMethod().getMethodName());
        IInvokedMethodListener.super.beforeInvocation(method, result);

        if (ListenerHelper.shouldBeLoggedInReport(result)) { //workaround to not shown the beforeMethod/afterMethod in report
            ExtentManager.createMethod(result, true);
        }
    }

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult result) {
        IInvokedMethodListener.super.afterInvocation(method, result);

        className = method.getTestMethod().getTestClass().getName();
        methodName = method.getTestMethod().getMethodName();

        if (apiListenerEnabled(method)) {
            afterInvocationForApiTest(method, result);
            return;
        }
        if (failed(method, result)) {
            try {
                String originalMessage = result.getThrowable().getMessage();
                ScreenshotOutput screenshot = new ScreenshotOutput().makeScreenshot(getClassName() + "." + getMethodName());
                String url = ListenerHelper.getURL();
                LogsRecorder.Info info = new LogsRecorder().recordBrowserLogsToFile(result);

                ApiThreadResponse.RequestData requestData = null;
                boolean apiCallFailed = ApiThreadResponse.failedDuringApiCall();
                if (apiCallFailed) {
                    requestData = ApiThreadResponse.getRequestData();
                    ApiThreadResponse.setFailedDuringApiCall(false);
                }

                String ta = ListenerHelper.buildTestArtifacts(
                        url,
                        screenshot,
                        info,
                        requestData);
                String newMessage = getFailedMsg(originalMessage) + ta;
                ListenerHelper.updateOriginalMessage(result, newMessage);

                String msg = MarkupHelper.buildCommonFailedInfoMsg(url, info, requestData);
                ExtentManager.getTest().log(Status.INFO, msg); //log info before the failing the test
            } finally {
                ExtentManager.fail(result, true);
            }
            return;
        }

        if (ListenerHelper.shouldBeLoggedInReport(result)) { //workaround to not shown the beforeMethod/afterMethod in report
            ExtentManager.log(result, true);
        }

    }

//    @Override
//    public synchronized void onTestStart(ITestResult result) {
//        ITestListener.super.onTestStart(result);
//    }

    @Override
    public void onTestSuccess(ITestResult iTestResult) {
        ITestListener.super.onTestSuccess(iTestResult);

        Log.testPassed("Test method: " + getMethodName());
        ApiThreadResponse.clear();
    }

    @Override
    public synchronized void onTestFailure(ITestResult result) {
        ITestListener.super.onTestFailure(result);
        Log.testFailed(getMethodName());
    }

//    @Override
//    public synchronized void onTestSkipped(ITestResult result) {
//        ITestListener.super.onTestSkipped(result);
//    }

//    @Override
//    public synchronized void onTestFailedButWithinSuccessPercentage(ITestResult result) {
//        ITestListener.super.onTestFailedButWithinSuccessPercentage(result);
//    }


    private String getFailedMsg(String originalMessage) {
        return "FAILED" + Log.threadId() + ": " + originalMessage;
    }

    private boolean apiListenerEnabled(IInvokedMethod iInvokedMethod) {
        ConstructorOrMethod consOrMethod = iInvokedMethod.getTestMethod().getConstructorOrMethod();
        EnableApiListener enable = consOrMethod.getMethod().getDeclaringClass().getAnnotation(EnableApiListener.class);
        return enable != null;
    }

    private void afterInvocationForApiTest(IInvokedMethod method, ITestResult result) {
        if (failed(method, result)) {
            String newMsg = getFailedMsg(result.getThrowable().getMessage())
                    + ApiThreadResponse.buildApiDataMsg();
            ListenerHelper.updateOriginalMessage(result, newMsg);
        }
        ApiThreadResponse.clear();
    }

    private boolean failed(IInvokedMethod method, ITestResult result) {
        return //(method.isTestMethod() || method.isConfigurationMethod())
                //&&
                result.getStatus() == ITestResult.FAILURE
                ;
    }

    private String getMethodName() {
        return methodName;
    }

    private String getClassName() {
        return className;
    }

}
