package util.execution;

import io.restassured.response.Response;
import util.log.ApiLogger;

public class ApiThreadResponse {
    private static final ThreadLocal<Boolean> FAILED_DURING_API_CALL = new ThreadLocal<>();

    private static final ThreadLocal<RequestData> REQUEST_DATA = new ThreadLocal<>();

    public record RequestData(String url, String body, Response response) {
    }

    public static synchronized void set(String url, String reqBody, Response resp) {
        REQUEST_DATA.set(new RequestData(url, reqBody, resp));
    }

    public static synchronized RequestData getRequestData() {
        return REQUEST_DATA.get();
    }

    public static synchronized Response getResponse() {
        return getRequestData().response();
    }

    public static synchronized void setFailedDuringApiCall(boolean b) {
        FAILED_DURING_API_CALL.set(b);
    }

    public static synchronized boolean failedDuringApiCall() {
        return FAILED_DURING_API_CALL.get() != null && FAILED_DURING_API_CALL.get();
    }

    public static void clear() {
        REQUEST_DATA.remove();
    }

    public static String buildApiDataMsg() {
        return ApiLogger.buildApiDataMsg(getRequestData());
    }

}
