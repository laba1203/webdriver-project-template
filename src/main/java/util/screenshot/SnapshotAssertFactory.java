package util.screenshot;

import com.assertthat.selenium_shutterbug.core.PageSnapshot;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.Assert;
import ui.abstractObjects.elements.Element;
import ui.abstractObjects.elements.ElementImpl;
import util.DriverConfig;
import util.ExecutionVariables;
import util.log.Log;
import util.screenshot.gifWriter.GifWriter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class SnapshotAssertFactory {

    private static final String DEFAULT_SCREENSHOTS_FOLDER = "src/test/testScreenshots/";
    private static final boolean CREATE_EXPECTED_FROM_ACTUAL = false;

    private String expectedDir;
    private String actualDir;
    private String diffDir;
    private String resultGifsDir;

    private final String fileName;
    private final String fileNameWithExtension;


    public SnapshotAssertFactory(String testSuite, String elementName) {
        setRootScreenshotsDirs(testSuite);
        fileName = calculateElementName(elementName);
        fileNameWithExtension = fileName + "." + Screenshot.FILE_FORMAT;
    }

    private Screenshot takeScreenshot(Element element, String path) throws IOException {
        File f = new ScreenshotRecorder().shotElement(element);
        FileUtils.copyFile(f, new File(path));
        return new Screenshot(f);
    }

    public void assertElementScreenshot(ElementImpl element) {
        assertElementScreenshot(element, 0.0D);
    }

    public void assertElementScreenshot(Element element) {
        assertElementScreenshot(element, 0.0D);
    }

    @SneakyThrows
    public void assertElementScreenshot(Element element, Double deviation) {
        Screenshot actual = takeScreenshot(element, getActualFilePath());
        assertScreenshot(actual, deviation);
    }

    public void assertPageScreenshot() {
        assertPageScreenshot(0.0D);
    }

    @SneakyThrows
    public void assertPageScreenshot(double deviation) {
        PageSnapshot actual = new ScreenshotRecorder().takeFullPage();
        actual.withName(getFileName()).save(getActualDir());
        Screenshot aScreenshot = new Screenshot(actual.getImage());
        assertScreenshot(aScreenshot, deviation);
    }

    private void assertScreenshot(Screenshot actual, Double deviation) throws IOException {
        File expected = getExpectedScreenshot();
        Screenshot.Diff difference = actual.getDiffFrom(expected);
        Double diff = difference.getPercentage();
        String diffData = "Actual deviation : " + diff + "%(pixels = " + difference.getPixels() + "); Expected % = " + deviation.toString();

        if (diff > 0) Log.logInReport(diffData);

        if (difference.getPixels() > 10 && diff > deviation) {
            BufferedImage eImage = ImageIO.read(expected);
            actual.createDiffImageFrom(eImage, getDiffDir() + getFileNameWithExtension());
            buildDiffGiff();
            Assert.fail("Screenshots are not similar for file :" + getFileNameWithExtension() + ". " + diffData);
        }
    }

    private void buildDiffGiff() throws IOException {
        String expectedFile = getExpectedFilePath();
        String actualFile = getActualFilePath();
        String diffFile = getDiffDir() + getFileNameWithExtension();
        String differGif = getResultGifsDir() + getFileNameWithExtension();

        ExtentAttachments.attachOutsideScreenshot("Expected", expectedFile, "expected/", getFileNameWithExtension());
//        AllureAttachments.attachScreen("Expected", expectedFile);

        ExtentAttachments.attachOutsideScreenshot("Actual", actualFile, "actual/", getFileNameWithExtension());
//        AllureAttachments.attachScreen("Actual", actualFile);

        File[] filesArray = {
                new File(expectedFile),
                new File(actualFile),
                new File(diffFile)
        };
        GifWriter.createGIF(filesArray, differGif);

        ExtentAttachments.attachOutsideScreenshot("Difference gif", differGif, "diff/", getFileNameWithExtension());
//        AllureAttachments.attachScreen("Difference gif", differGif);
    }

    private File getExpectedScreenshot() throws IOException {
        File file = new File(getExpectedFilePath());
        if (!file.exists()) {
            Assert.assertTrue(ExecutionVariables.testOnLocalMachine() || CREATE_EXPECTED_FROM_ACTUAL, "Expected screenshot was not found for " + getFileNameWithExtension());
            createExpectedScreenshotFromActual();
        }
        return file;
    }

    private void createExpectedScreenshotFromActual() throws IOException {
        File expectedFile = new File(getExpectedFilePath());
        ImageIO.write(getActualScreenshotImage(), Screenshot.FILE_FORMAT, expectedFile);
        Log.logRecord("Expected screenshot <" + getFileNameWithExtension() + "> has been copied from actual screenshot.");
    }

    private void setRootScreenshotsDirs(String testSuiteName) {
        String path = DEFAULT_SCREENSHOTS_FOLDER + testSuiteName;
        File file = new File(DEFAULT_SCREENSHOTS_FOLDER + testSuiteName);
        boolean created = file.mkdirs();
        if (created) Log.logInConsole("Test screenshots folder was created for " + path);
        setScreenshotsDir(file.getAbsolutePath());
    }

    private void setScreenshotsDir(String absolutePath) {
        expectedDir = absolutePath + "/expected/";
        actualDir = absolutePath + "/actual/";
        diffDir = absolutePath + "/diff/";
        resultGifsDir = absolutePath + "/gifs/";
        createFolders(); //creates folders if they're not exist
    }

    private void createFolders() {
        createFolder(expectedDir);
        createFolder(actualDir);
        createFolder(diffDir);
        createFolder(resultGifsDir);
    }

    private void createFolder(String path) {
        boolean created = new File(path).mkdirs();
        if (created) Log.logInConsole("New folder was create for path: " + path);
    }

    private String calculateElementName(String elementName) {
        String size = DriverConfig.getDriver().manage().window().getSize().toString();
        if (!ExecutionVariables.remoteExecution()) {
            Assert.fail("ERROR: Browser name can be retrieved only during remote execution !!!");
        }
        String browser = ((RemoteWebDriver) DriverConfig.getDriver()).getCapabilities().getBrowserName();
        return elementName + "_" + browser + "_" + size;
    }

    private String getFileNameWithExtension() {
        return Objects.requireNonNull(fileNameWithExtension);
    }

    private String getFileName() {
        return Objects.requireNonNull(fileName);
    }

    private String getActualFilePath() {
        return getActualDir() + getFileNameWithExtension();
    }

    private String getActualDir() {
        return Objects.requireNonNull(actualDir);
    }

    private String getDiffDir() {
        return Objects.requireNonNull(diffDir);
    }

    private String getResultGifsDir() {
        return Objects.requireNonNull(resultGifsDir);
    }

    private String getExpectedFilePath() {
        return Objects.requireNonNull(expectedDir) + getFileNameWithExtension();
    }

    private BufferedImage getActualScreenshotImage() throws IOException {
        String path = getActualFilePath();
        Log.debug("Actual screenshot path: " + path);
        return ImageIO.read(new File(path));
    }


}
