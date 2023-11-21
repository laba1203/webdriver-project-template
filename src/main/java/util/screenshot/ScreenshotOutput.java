package util.screenshot;

import com.aventstack.extentreports.ExtentTest;
import org.apache.commons.io.FileUtils;
import util.DriverConfig;
import util.PropertyLoader;
import util.log.ExtentManager;
import util.log.Log;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Arrays;

public class ScreenshotOutput {

    private static final String REPORT_DIR = PropertyLoader.loadExtentRepoetProp("extent.reporter.spark.path");
    private static final String SCREENSHOT_DIR = "/screenshots";
    public static final String PATH_TO_SAVE = REPORT_DIR + SCREENSHOT_DIR;
    private String absoluteFilePath = "<not-provided>";
    private String relativePath = "<not-provided>";

    public String getPath() {
        return absoluteFilePath;
    }

    String getRelativePath() {
        return relativePath;
    }

    public ScreenshotOutput makeScreenshot(String fileName) {
        return makeScreenshot(fileName, true);
    }

    public ScreenshotOutput makeScreenshot(String fileName, boolean attachToReport) {
        if (!DriverConfig.driverCreated()) {
            Log.debug("Screenshot is not available since Driver is null.");
            return this;
        }
        try {
            File srcFile = new ScreenshotRecorder().takeVisiblePage();
            saveToDir(srcFile, buildFileName(fileName));

//            AllureAttachments.attachScreen("Page screenshot", absoluteFilePath);

            if (attachToReport) {
                ExtentTest test = ExtentManager.getTest();
                if (test != null) {
                    test.addScreenCaptureFromPath(getRelativePath(), "Page with failure");
                } else {
                    Log.debug("Screenshot is not available.");
                }
                //workaround
//            String screenshotBase64 = ((TakesScreenshot) Objects.requireNonNull(DriverConfig.getDriver())).getScreenshotAs(OutputType.BASE64);
//            ExtentTestManager.getTest().addScreenCaptureFromBase64String(screenshotBase64, "Page screenshot BASE64");
            }

            return this;

        } catch (Throwable e) {
            e.printStackTrace();
            Log.debug("Not able to make Screenshot because of exception. Details: \r\n"
                    + e.getMessage() + "\r\n"
                    + Arrays.toString(e.getStackTrace()));
            absoluteFilePath = "Not able to make screenshot due to exception: " + e;
            return this;
        }
    }

    private static String buildFileName(String method) {
        return "Screen_" + method + "_" + getTimeStamp() + ".png";
    }

    ScreenshotOutput saveToDir(File file, String additionalPath, String fileName) {
        String p = "/" + additionalPath + fileName;
        String filePath = PATH_TO_SAVE + p;
        relativePath = "." + SCREENSHOT_DIR + p;

        try {
            File newFile = new File(filePath);
            FileUtils.copyFile(file, newFile);
            absoluteFilePath = newFile.getAbsolutePath();
            Log.debug("Spark screenshot: " + getRelativePath());

        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    void saveToDir(File file, String fileName) {
        saveToDir(file, "", fileName);
    }

    private static String getTimeStamp() {
        return String.valueOf(new Timestamp(System.currentTimeMillis()));
    }
}
