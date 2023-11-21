package util;


import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import util.execution.TestExecutionManager;
import util.log.Log;

public class DriverWatcher extends Thread implements AutoCloseable {

    @Setter
    @Getter
    @NonNull
    private WebDriver driver;
    @Setter
    @Getter
    private String watchedThreadId = "<unknown>";

    private boolean watch = false;
    private static final int ATTEMPTS_LIMIT = 60;


    @SuppressWarnings("BusyWait")
    @SneakyThrows
    @Override
    public void run() {
        TestExecutionManager.addDriverWatcher(getWatchedThreadId(), this);
        watch = true;
        Log.debug("Driver watching has been started for driver from thread #" + getWatchedThreadId() + ".");
        int attempt = 0;
        while (watch) {
            if (attempt > ATTEMPTS_LIMIT) {
                watch = false;
                Log.debug("Driver Watching limit is achieved for driver from thread #" + getWatchedThreadId() + ".");
                return;
            }
            Log.debug("Watching... Attempt #" + attempt);
            try {
                this.getDriver().findElement(By.tagName("body"));
            }
            finally {
                this.close();
                this.interrupt();
            }
            attempt++;
            sleep(500);
        }
        Log.debug("Driver watching has been finished for thread #" + getWatchedThreadId() + ".");
    }

    @Override
    public void close() {
        watch = false;
        TestExecutionManager.removeDriverWatchers(getWatchedThreadId());
        Log.debug("Driver watching has been closed.");
    }
}
