package api;

import com.google.gson.JsonObject;
import io.restassured.path.json.exception.JsonPathException;
import io.restassured.response.Response;
import util.log.Log;

import java.util.Arrays;
import java.util.Map;

public class UtilAPI {

    public static void logResponse(Response resp) {
        int symbolsLimit = 30000;
        String out = getHeadersAndBody(resp);
        int charsCount = out.length();
        if (charsCount > symbolsLimit) {
            Log.debug("Response truncated due to symbols limit (" + symbolsLimit + "). Actual symbols count: " + charsCount);
            out = out.substring(0, symbolsLimit) + "\n...";
        }
        Log.logRecord("API Response:\n"
                + out
        );
    }

    public static String getHeadersAndBody(Response resp) {
        String body;
        try {
            body = resp.then().extract().body().jsonPath().prettify();
        } catch (JsonPathException e) {
            Log.logRecord("Was not able to prettify response due to the error: " + e.getMessage());
            body = resp.then().extract().body().asString();
        }
        return resp.getStatusLine() + "\n"
                + "Headers: " + Arrays.toString(resp.getHeaders().asList().toArray()) + "\n"
                + "Response Body:\n"
                + body + "\n"
                + "  * * * * End response  * * *  "
                ;
    }

    public static String convertToUrlParameters(JsonObject object) {
        StringBuilder parameters = new StringBuilder();
        for (String key : object.keySet()) {
            parameters.append(key).append("=").append(object.get(key).getAsString()).append("&");

        }
        String out = parameters.toString();

        return out.substring(0, out.toCharArray().length - 1);
    }

    public static String convertHashToString(Map<String, String> map) {
        StringBuilder out = new StringBuilder();
        for (String name : map.keySet()) {
            String value = map.get(name);
            out.append(name).append("=").append(value).append("; ");
        }
        return out.toString();
    }

}
