package util.execution;

import lombok.Getter;
import lombok.Setter;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.testng.ITestResult;
import util.Browser;
import util.PropertyLoader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;

public class LogsRecorder {
    private static final String REPORT_DIR = PropertyLoader.loadExtentRepoetProp("extent.reporter.spark.path");
    private static final String LOGS_DIR = "/browser-logs/";
    static final String PATH_TO_SAVE = REPORT_DIR + LOGS_DIR;

    @Getter
    @Setter
    public static class Info {
        private String fullPath;
        private String relativePath;
        private LogEntries entries;
        private String errorMsg;

        void setErrorMsg(String error) {
            this.errorMsg = error;
        }

        public boolean errorCaptured() {
            return errorMsg != null;
        }

    }

    Info recordBrowserLogsToFile(ITestResult result) {
        String cName = result.getMethod().getTestClass().getName();
        String mName = result.getMethod().getMethodName();
        String fileName = cName + "." + mName;
        Info info = new Info();
        try {
            LogEntries entries = Browser.getConsoleLogs();
            info.setEntries(entries);
            String logs = readLogs(entries);
            return writeFile(fileName, logs, info);
        } catch (Throwable e) {
            String error = "Unable to catch browser log due to error: " + e.getMessage();
            info.setErrorMsg(error);
            info.setRelativePath(error);
            info.setFullPath(error);
            return info;
        }
    }

    private String readLogs(LogEntries logEntries) {
        StringBuilder out = new StringBuilder();
        for (LogEntry entry : logEntries) {
            String logRow = new Date(entry.getTimestamp()) + " " + entry.getLevel() + " " + entry.getMessage() + "\r\n";
            out.append(logRow);
        }
        return out.toString();
    }

    /**
     * @returns path to the saved file.
     */
    private Info writeFile(String fileName, String text, Info info) {

        String fileNameWithExtention = fileName + ".txt";
        String filePath = PATH_TO_SAVE + fileNameWithExtention;
        Path dirPath = Paths.get(PATH_TO_SAVE);
        File dir = new File(PATH_TO_SAVE);
        try {
            if (!dir.exists()) {
                Files.createDirectory(dirPath);
            }
            try (
                    FileOutputStream fos = new FileOutputStream(filePath);
                    OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
                    Writer writer = new BufferedWriter(osw)
            ) {
                writer.write(text);
            }
            info.setFullPath(filePath);
            info.setRelativePath("." + LOGS_DIR + fileNameWithExtention);
            return info;
        } catch (IOException e) {
            String error = "Not able to create Browser Logs file due to exception: " + e + "\r\n" +
                    Arrays.toString(e.getStackTrace());
            info.setErrorMsg(error);
            info.setFullPath(error);
            info.setRelativePath(error);
            return info;
        }
    }

    public LogEntries getBrowserLogs() {
        return TestProfile.getBrowserLogs();
    }
}
