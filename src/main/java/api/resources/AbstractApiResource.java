package api.resources;

import api.objects.JsonData;
import com.google.gson.JsonObject;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.SneakyThrows;
import util.DriverConfig;
import util.DriverWatcher;
import util.execution.ApiThreadResponse;
import util.log.Log;

import java.util.HashMap;
import java.util.concurrent.Callable;

public class AbstractApiResource {

    private static final String CONTENT_TYPE = "application/json";
    protected boolean debugRequests = true;
    private boolean enableRequestLogging = true;

    private boolean enableRequestLogging() {
        return enableRequestLogging;
    }

    protected void disableRequestLoggingInThread() {
        enableRequestLogging = false;
    }

    private RequestSpecification addBodyWithDefaultRequestParams(RequestSpecification requestSpec, String body) {
        return addBodyWithDefaultRequestParams(requestSpec, CONTENT_TYPE, body);
    }

    private RequestSpecification addBodyWithDefaultRequestParams(RequestSpecification requestSpec, String contentType, String body) {
        requestSpec = requestSpec
                .contentType(contentType)
                .filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
        return addBody(requestSpec, body);
    }

    private RequestSpecification addBody(RequestSpecification requestSpec, String body) {
        if (body == null || body.isEmpty()) {
            return requestSpec;
        }
        return requestSpec.body(body);
    }

    public Response get(String url) {
        return get(RestAssured.given(), url);
    }

    public Response get(RequestSpecification requestSpec, String url) {
        return get(requestSpec, url, "");
    }

    public Response get(String url, JsonObject body) {
        return get(RestAssured.given(), url, body.toString());
    }

    public Response get(String url, String body) {
        return get(RestAssured.given(), url, body);
    }

    protected Response get(RequestSpecification requestSpec, String url, String body) {
        Response response = requestWithDriverWatcher(
                () -> this.addBodyWithDefaultRequestParams(requestSpec, body).get(url)
        );
        ApiThreadResponse.set(url, body, response);
        return response;
    }

    protected Response post(String url, JsonObject body) {
        return this.post(url, body.toString());
    }

    protected Response post(String url, String body) {
        return this.post(RestAssured.given(), url, body);
    }

    protected Response post(String url, JsonData body) {
        return post(url, body.getAsJsonString());
    }

    protected Response post(String url) {
        return this.post(RestAssured.given(), url, "");
    }

    protected Response post(RequestSpecification requestSpec, String url) {
        return this.post(requestSpec, url, "");
    }

    protected Response post(RequestSpecification requestSpec, String url, String body) {
        return this.post(requestSpec, url, CONTENT_TYPE, body);
    }

    protected Response post(RequestSpecification requestSpec, String url, String contentType, String body) {
        RequestSpecification request = addBodyWithDefaultRequestParams(requestSpec, contentType, body);
        return postCustomRequest(request, url, body);
    }

    protected Response postCustomRequest(RequestSpecification requestSpec, String url, String body) {
        Response resp = requestWithDriverWatcher(
                () -> addBody(requestSpec, body)
                        .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 11_0_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36")
                        .when()
                        .post(url)
        );
        if (enableRequestLogging()) {
            ApiThreadResponse.set(url, body, resp);
        }
        return resp;
    }

    protected Response patch(RequestSpecification requestSpec, String url, String body) {
        Response resp = requestWithDriverWatcher(
                () -> addBodyWithDefaultRequestParams(requestSpec, body)
                        .when()
                        .patch(url)
        );
        ApiThreadResponse.set(url, body, resp);
        return resp;
    }

    protected Response delete(String url, JsonObject body) {
        return this.delete(RestAssured.given(), url, body.toString());
    }

    protected Response delete(String url) {
        return this.delete(RestAssured.given(), url, "");
    }

    protected Response delete(RequestSpecification requestSpec, String url, String body) {
        Response resp = requestWithDriverWatcher(
                () -> addBodyWithDefaultRequestParams(requestSpec, body)
                        .when()
                        .delete(url)
        );
        ApiThreadResponse.set(url, body, resp);
        return resp;
    }

    protected Response put(String url, String body) {
        return put(RestAssured.given(), url, body);
    }

    protected Response put(String url, JsonObject body) {
        return put(url, body.toString());
    }

    protected Response put(RequestSpecification requestSpec, String url) {
        return this.put(requestSpec, url, "");
    }

    protected Response put(RequestSpecification requestSpec, String url, String body) {
        Response response = requestWithDriverWatcher(
                () -> addBodyWithDefaultRequestParams(requestSpec, body)
                        .when()
                        .put(url)
        );

        ApiThreadResponse.set(url, body, response);
        return response;
    }

    @Deprecated
    protected JsonObject convertToJson(HashMap<String, String> data) {
        JsonObject obj = new JsonObject();
        for (String key : data.keySet()) {
            obj.addProperty(key, data.get(key));
        }
        return obj;
    }

    protected boolean enableDriverWatcher() {
        return false;
    }

    @SneakyThrows
    private Response requestWithDriverWatcher(Callable<Response> req) {
        long start = System.currentTimeMillis();
        Response response;
        try (DriverWatcher watcher = new DriverWatcher()) {
            if (DriverConfig.driverCreated() && enableDriverWatcher()) {
                watcher.setDriver(DriverConfig.getDriver());
                watcher.setWatchedThreadId(Log.threadId());
                watcher.start();
            }
            Log.debug("Start request.");
            response = req.call();
        }
        long end = System.currentTimeMillis();
        float diff = (end - start) / 1000F;
        logRequestTime(diff);
        return response;

    }

    private void logRequestTime(float time) {
        Log.logInConsole("Request time: " + time + " sec");
    }

}
