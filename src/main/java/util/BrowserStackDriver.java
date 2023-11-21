package util;

import groovy.json.JsonParser;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.FileReader;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

public class BrowserStackDriver {
    private static final String USERNAME = "curebitui1";
    private static final String AUTOMATE_KEY = "34ipy889ezDJZ5Xa25ze";
    private static final String URL = "https://" + USERNAME + ":" + AUTOMATE_KEY + "@hub-cloud.browserstack.com/wd/hub";


    @SuppressWarnings("unchecked")
    private MutableCapabilities getCapabilities(String configFile, String environment) throws Exception {
        JSONParser parser = new JSONParser();
        try (FileReader fr = new FileReader("src/main/resources/config/" + configFile)) {
            JSONObject config = (JSONObject) parser.parse(fr);

            JSONObject envs = (JSONObject) config.get("environments");

            MutableCapabilities capabilities = new MutableCapabilities();

            Map<String, String> envCapabilities = (Map<String, String>) envs.get(environment);
            Iterator<Map.Entry<String, String>> it = envCapabilities.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<? extends String, ? extends String> pair = it.next();
                capabilities.setCapability(pair.getKey(), pair.getValue());
            }

            Map<String, String> commonCapabilities = (Map<String, String>) config.get("capabilities");
            it = commonCapabilities.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<? extends String, ? extends String> pair = it.next();
                if (capabilities.getCapability(pair.getKey().toString()) == null) {
                    capabilities.setCapability(pair.getKey().toString(), pair.getValue().toString());
                }
            }
            return capabilities;
        }
    }

    WebDriver getDriver(String browserName, String sessionName) throws Exception {
        MutableCapabilities caps = getCapabilities("common.conf.json", browserName);
        caps.setCapability("name", sessionName);
        return new RemoteWebDriver(new URL(URL), caps);
    }


}
