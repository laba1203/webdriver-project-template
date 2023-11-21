package util.screenshot;

import com.aventstack.extentreports.MediaEntityBuilder;
import util.log.ExtentManager;

import java.io.File;

public class ExtentAttachments {

    public static void attachOutsideScreenshot(String details, String filePath, String additionalPath, String newFileName) {
        ScreenshotOutput s = copyToExtentDir(filePath, additionalPath, newFileName);
        ExtentManager.getTest().info(details, MediaEntityBuilder.createScreenCaptureFromPath(s.getRelativePath()).build());
    }

    private static ScreenshotOutput copyToExtentDir(String initFilePath, String additionalPath, String newFileName){
        return new ScreenshotOutput().saveToDir(new File(initFilePath), additionalPath, newFileName);
    }




}
