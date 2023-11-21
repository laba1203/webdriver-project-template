package util.log;

import api.UtilAPI;
import io.restassured.response.Response;
import util.execution.ApiThreadResponse;

public class ApiLogger {

    public static String getRequestResponseInfo(ApiThreadResponse.RequestData requestData) {
        return "Response status line:" + requestData.response().statusLine() +
                "\nRequest URL: " + requestData.url() +
                "\nRequest body: " +
                "\n" + requestData.body() +
                "\nResponse Header: " + requestData.response().getHeaders() +
                "\nResponse Body: " +
                "\n" + requestData.response().then().extract().body().asString()
                ;
    }

    public static String buildApiDataMsg(ApiThreadResponse.RequestData requestData) {
        String request = requestData.body();
        String url = requestData.url();
        if (url == null) {
            url = "Request URL is not available.";
        }
        if (request == null) {
            request = "";
        }
        String apiResponse = buildApiResponse(requestData);
        return "****  API Data: **** "
                + "\nURL: " + url
                + "\nRequest body:\n" + request
                + "\n ** Response:"
                + "\n" + apiResponse
                ;
    }

    public static String buildApiResponse(ApiThreadResponse.RequestData requestData) {
        String apiResponse = "API Response is not available in the current Thread.";
        Response resp = requestData.response();
        if (resp != null) {
            apiResponse = UtilAPI.getHeadersAndBody(resp);
        }
        return apiResponse;
    }
}
