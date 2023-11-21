package util.log;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ExtentManager {

    private static final Map<String, ClassTest> classTestMap = new HashMap<>();
    private static final ThreadLocal<ExtentTest> methodTest = new ThreadLocal<>();
    private static final ThreadLocal<ExtentTest> dataProviderTest = new ThreadLocal<>();

    public static synchronized ExtentTest getTest() {
        ExtentTest t = dataProviderTest.get() == null
                ? methodTest.get()
                : dataProviderTest.get();
        return t;
    }

    public static synchronized void assignDeviceToClass(Class<?> clazz, String device) {
        ClassTest ct = classTestMap.get(clazz.getName());
        if (ct != null) {
            ct.getClassLevel().assignDevice(device);
        } else {
            Log.debug("Extent class test was not found for " + clazz.getName());
        }
    }

    public static synchronized void assignCategoryToClass(Class<?> clazz, String category) {
        ClassTest ct = classTestMap.get(clazz.getName());
        if (ct != null) {
            ct.getClassLevel().assignCategory(category);
        } else {
            Log.debug("Extent class test was not found for " + clazz.getName());
        }
    }

    public static synchronized ExtentTest getTest(ITestResult result) {
        ExtentTest t = result.getParameters() != null && result.getParameters().length > 0
                ? dataProviderTest.get()
                : methodTest.get();
        return t;
    }

    public static synchronized ExtentTest createMethod(ITestResult result, Boolean createAsChild) {
        if (!createAsChild)
            return createMethod(result);
//        String className = result.getInstance().getClass().getSimpleName();
        String className = result.getInstance().getClass().getName();
        String methodName = result.getMethod().getMethodName();
//        result.getMethod().isBeforeClassConfiguration()
        String desc = result.getMethod().getDescription();
        ExtentTest classTest;
        if (classTestMap.containsKey(className)) {
            classTest = classTestMap.get(className).getGroup(result);
        } else {
            ClassTest cTest = new ClassTest(
                    ExtentService.getInstance().createTest(className, desc)
            );
            classTestMap.put(className, cTest);
            classTest = cTest.getGroup(result);
        }
        if (result.getParameters().length > 0) {
            boolean anyMatch = classTest.getModel().getChildren()
                    .stream()
                    .anyMatch(x -> x.getName().equals(methodName));
            if (!anyMatch)
                createTest(result, classTest);
            String paramName = Arrays.asList(result.getParameters()).toString();
            ExtentTest paramTest;
            if (result.getMethod().isDataDriven()) {
                paramTest = methodTest.get().createNode(paramName);
            } else {
                paramTest = methodTest.get().log(Status.INFO, paramName);
            }
            dataProviderTest.set(paramTest);
        } else {
            dataProviderTest.set(null);
            createTest(result, classTest);
        }
        return methodTest.get();
    }

    public static synchronized ExtentTest createMethod(ITestResult result) {
        String methodName = result.getMethod().getMethodName();
        if (result.getParameters().length > 0) {
            if (methodTest.get() != null && methodTest.get().getModel().getName().equals(methodName))
                ;
            else
                createTest(result, null);
            String paramName = Arrays.asList(result.getParameters()).toString();
            ExtentTest paramTest = methodTest.get().createNode(paramName);
            dataProviderTest.set(paramTest);
        } else {
            dataProviderTest.set(null);
            createTest(result, null);
        }
        return methodTest.get();
    }

    private static synchronized ExtentTest createTest(ITestResult result, ExtentTest classTest) {
        String methodName = result.getMethod().getMethodName();
        String desc = result.getMethod().getDescription();
        ExtentTest test;
        if (classTest != null)
            test = classTest.createNode(methodName, desc);
        else
            test = ExtentService.getInstance().createTest(methodName, desc);
        methodTest.set(test);
        String[] groups = result.getMethod().getGroups();
        ExtentTestCommons.assignGroups(test, groups);
        return test;
    }

    private static synchronized Status getStatus(ITestResult result) {
        return switch (result.getStatus()) {
            case ITestResult.SKIP -> Status.SKIP;
            case ITestResult.FAILURE -> Status.FAIL;
            default -> Status.PASS;
        };
    }

    public static synchronized void log(ITestResult result, Boolean createTestAsChild) {

        Status status = getStatus(result);
        String msg = "Test " + status.toLower().replace("skip", "skipp") + "ed"; //passed, skipped, failed
//        switch (result.getStatus()) {
//            case ITestResult.SKIP:
//                status = Status.SKIP;
//                msg += "skipped";
//                break;
//            case ITestResult.FAILURE:
//                status = Status.FAIL;
//                msg += "failed";
//                break;
//            default:
//                msg += "passed";
//                break;
//        }
        if (ExtentManager.getTest(result) == null)
            ExtentManager.createMethod(result, createTestAsChild);
        if (result.getThrowable() != null) {
            ExtentManager.getTest(result).log(status, result.getThrowable());
            return;
        }
        ExtentManager.getTest(result).log(status, msg);
    }

    public static synchronized void log(ITestResult result) {
        log(result, false);
    }

    public static synchronized void fail(ITestResult result, boolean createTestAsChild) {
        Status s = getStatus(result);

        if (ExtentManager.getTest(result) == null)
            ExtentManager.createMethod(result, createTestAsChild);

        ExtentManager.getTest(result).log(s, result.getThrowable());

    }

    private static class ClassTest {
        private ExtentTest classTest;
        private ExtentTest testsGroup;
        private ExtentTest beforeConfigGroup;
        private ExtentTest afterConfigGroup;

        private ClassTest(ExtentTest classTest) {
            this.classTest = classTest;
        }

        private ExtentTest getClassLevel() {
            return classTest;
        }

        private ExtentTest getGroup(ITestResult result) {
            ITestNGMethod m = result.getMethod();

            if (m.isTest()) {
                return getTestsNode(result);
            } else if (m.isAfterClassConfiguration() ||
                    m.isAfterTestConfiguration() ||
                    m.isAfterSuiteConfiguration()) {
                return getAfterNode(result);
            } else {
                //Before Class, Suite and all other
                return getSetupNode(result);
            }
        }

        private ExtentTest getSetupNode(ITestResult result) {
            if (beforeConfigGroup == null) {
                beforeConfigGroup = getClassTest(result).createNode("Setup methods");
            }
            return beforeConfigGroup;
        }

        private ExtentTest getAfterNode(ITestResult result) {
            if (afterConfigGroup == null) {
                afterConfigGroup = getClassTest(result).createNode("Cleanup methods");
            }
            return afterConfigGroup;
        }

        private ExtentTest getTestsNode(ITestResult result) {
            if (testsGroup == null) {
                testsGroup = getClassTest(result).createNode("Tests");
            }
            return testsGroup;
        }

        private ExtentTest getClassTest(ITestResult result) {
            String className = result.getInstance().getClass().getSimpleName();
            String desc = result.getMethod().getDescription();

            if (classTest == null) {
                classTest = ExtentService.getInstance().createTest(className, desc);
            }
            return classTest;
        }
    }

}
