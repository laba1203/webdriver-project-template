package util.log;

import com.aventstack.extentreports.ExtentTest;
import lombok.SneakyThrows;
import util.Retry;
import util.WaitFactory;
import util.execution.TestExecutionManager;

public class ExtentSteps {

    private static final boolean ENABLE_DETAILED_LOGS = !TestExecutionManager.executingFullRegression();

    @SneakyThrows
    public static void log(String stepName, Retry.RetryCallback c) {
        if (ENABLE_DETAILED_LOGS) {
            try {
                c.call();
                passStep(stepName);
            } catch (Throwable e) {
                failStep(stepName);
                throw e;
            }
        } else {
            c.call();
            Log.logInConsole(stepName);
        }
    }

    public static void passStep(String n) {
        ExtentTest test = ExtentManager.getTest();
        if (test != null) {
            test.pass(n);
        }
    }

    private static void failStep(String n) {
        ExtentTest test = ExtentManager.getTest();
        if (test != null) {
            test.fail(n);
        }
    }

    @SneakyThrows
    public static boolean log(String stepName, WaitFactory.Callback c) {
        try {
            boolean v = c.call();
            passStep(stepName);
            return v;
        } catch (Throwable e) {
            failStep(stepName);
            throw e;
        }
    }
}
