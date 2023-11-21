package api.steps;

import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import lombok.SneakyThrows;
import org.testng.Assert;
import util.Retry;
import util.execution.ApiThreadResponse;
import util.execution.TestProfile;
import util.log.ApiLogger;
import util.log.ExtentManager;

import static io.restassured.http.ContentType.JSON;

public abstract class AbstractApiSteps {

    public static ValidatableResponse assertValidResponse(Response resp) {
        ValidatableResponse vResp = assertValidResponseCode(resp);
        return assertValidResponseOkValue(vResp);
    }

    public static ValidatableResponse assertValidResponseCode(Response resp) {
        return assertResponseCodeEquals(resp, 200);
    }

    public static ValidatableResponse assertResponseCodeEquals(Response resp, int expectedCode) {
        int actual = resp.statusCode();
        assertDuringApiCall(() -> {
            int code = resp.statusCode();
            try {
                Assert.assertEquals(code, expectedCode, "Incorrect response status code. Expected: <" + expectedCode + ">; Actual: <" + actual + ">.\n"/* + respData*/);
            } catch (AssertionError e) {
                if (code == 502 || code == 500 || code == 504) {
                    ExtentManager.getTest().assignCategory(String.valueOf(code));
                    TestProfile.addCapturedError(String.valueOf(code));
                }
                throw e;
            }
        });
        return resp.then();
    }

    @SneakyThrows
    static void assertDuringApiCall(Retry.RetryCallback verification) {
        try {
            verification.call();
        } catch (AssertionError e) {
            ApiThreadResponse.setFailedDuringApiCall(true);
            throw e;
        }
    }

    static ValidatableResponse assertValidResponseOkValue(ValidatableResponse resp) {
        String respData = ApiLogger.getRequestResponseInfo(ApiThreadResponse.getRequestData());
        resp.contentType(JSON);
        assertDuringApiCall(() -> {
                    String error = "";
                    Boolean okValue = null;
                    try {
                        okValue = resp.extract().path("ok");
                    } catch (Exception e) {
                        e.printStackTrace();
                        error = "\nCaused by error:" + e.getMessage();
                    }
                    Assert.assertNotNull(okValue, "'ok' value was not provided in the response\n" + respData + error);
                    Assert.assertTrue(okValue, "Incorrect response 'ok' value. \n" + respData);
                }
        );
        return resp;
    }
}
