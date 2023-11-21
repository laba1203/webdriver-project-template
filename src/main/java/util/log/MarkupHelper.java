package util.log;

import util.execution.ApiThreadResponse;
import util.execution.LogsRecorder;

public class MarkupHelper extends com.aventstack.extentreports.markuputils.MarkupHelper {

    public static String buildLink(String text, String link) {
        return "<a href='" + link + "' target='_blank'>" + text + "</a>";
    }

    public static String buildCommonFailedInfoMsg(String url, LogsRecorder.Info info, ApiThreadResponse.RequestData apiData) {
        String msg = "";
        if (!url.contains("http")) {
            url = "URL is not available. See details in stacktrace.";
        }
        msg += "<b>URL:</b> " + MarkupHelper.buildLink(url, url);
        String browserLogFile = MarkupHelper.buildLink("File", info.getRelativePath());
        if (info.errorCaptured()) {
            browserLogFile = "Browser logs are not available. See details in stacktrace.";
        }
        msg += "</br><b>Browser logs:</b> " + browserLogFile;
        if (apiData != null) {
            msg += "</br>" + buildFailedApiMsg(apiData);
        }

        return msg;
    }

    private static String buildFailedApiMsg(ApiThreadResponse.RequestData apiData) {
        if (apiData == null || apiData.url() == null) {
            return "";
        }
        String code = apiData.url() + "\n" +
                apiData.response().getStatusLine() + "\n" +
                apiData.body() + "\n" +
                "Response:\n" +
                apiData.response().then().extract().body().asString();

        return "<b>API Data:</b><br/>" + createCodeBlock(code).getMarkup();
    }
}
