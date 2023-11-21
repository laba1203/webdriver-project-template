package util;

import org.testng.Assert;
import util.execution.TestExecutionManager;
import util.log.Log;

import static util.EnvFactory.EnvType.STAGE_1;
import static util.EnvFactory.EnvType.STAGE_2;

public class EnvFactory {

    private static EnvType envType;

    public enum EnvType {STAGE_1, STAGE_2}

    public static EnvType getEnvType() {
        setupTestEnvironment();
        return envType;
    }

    public static boolean envEquals(EnvFactory.EnvType env) {
        return getEnvType() == env;
    }

    public static boolean envNotEquals(EnvFactory.EnvType env) {
        return !envEquals(env);
    }

    public static void setupTestEnvironment() {
        if (envType == null) {
            setEnvType();
            Log.logInConsole(getExecutionInfo());
            TestExecutionManager.deleteFilesFromPreviousExecution();
        }
    }

    private static void setEnvType() {
        if (envType == null) {
            String envName = ExecutionVariables.getMavenEnvName().toUpperCase();
            Assert.assertFalse(envName.isEmpty(), "ERROR: envType is null from maven variable env.NAME");
            envType = calculateEnvType(envName);
        }
    }

    private static EnvType calculateEnvType(String envName) {
        switch (envName) {
            default:
                String customDomain = ExecutionVariables.getCustomMavenEnvDomain();
                if (customDomain.equals("NONE") || customDomain.isEmpty()) {
                    Assert.fail("ERROR: Unknown env type: <" + envName + ">.");
                }
            case "STAGE_1":
                return STAGE_1;
            case "STAGE_2":
                return STAGE_2;
        }
    }

    private static String getExecutionInfo() {
        String info = "Test execution info:";
        String env = "\n  * Test environment: " + getEnvType();
        String testSuite = "";
        String testSuiteFullPath = "";
        String threadsCount = ExecutionVariables.getMavenThreads();
        String suiteFullPath = ExecutionVariables.getMavenTestSuiteFullPath();
        String suiteName = ExecutionVariables.getMavenTestSuite();
        if (suiteName != null) {
            testSuite = "\n  * Test Suite file: " + suiteName;
        }
        if (suiteFullPath != null) {
            testSuiteFullPath = "\n  * Test Suite file(full path): " + suiteFullPath + "";
        }
        String rServer = "";
        if (ExecutionVariables.remoteExecution()) {
            rServer = "\n  * Remote server: " + DriverConfig.REMOTE_MACHINE_URL;
        }
        String threads = "";
        if (threadsCount != null) {
            threads = "\n  * Threads count: " + threadsCount;
        }
        return "\n" + info +
                env +
                threads +
                testSuite +
                rServer +
                testSuiteFullPath +
                "\n";
    }


    public static boolean browserStackExecution() {
        String val = ExecutionVariables.getBrowserStackParam();
        if (val == null || val.isEmpty()) {
            return false;
        }
        return Boolean.parseBoolean(ExecutionVariables.getBrowserStackParam());

    }

}
