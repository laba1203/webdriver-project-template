package util;

import org.testng.Assert;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class PropertyLoader {

    private static final String PROP_FILE = "/application.properties";
    private static final String CREDENTIAL = "/credential.properties";
    private static final String LOCAL_PROP_FILE = "/local.properties";

    private static final String EXTENT_PROP = "/extent.properties";


    private static String loadProperty(String propFile, String name) {
        Properties props = getPropertiesFile(propFile);
        Assert.assertNotNull(name, "Property key can't be null");
        if (props != null) {
            return props.getProperty(name);
        }
        return null;
    }

    public static String loadProperty(String name) {
        return loadProperty(PROP_FILE, name);
    }

    public static String loadExtentRepoetProp(String name){
        return loadProperty(EXTENT_PROP, name);
    }


    /**
     * Create /local.properties file to use this method
    */
    public static String loadLocalProperty(String name) {
        return loadProperty(LOCAL_PROP_FILE, name);
    }

    public static String loadCredentialProperty(String name) {
        return loadProperty(CREDENTIAL, name);
    }


    private static Properties getPropertiesFile(String fileName) {
        Properties props = new Properties();
        try (InputStream is = PropertyLoader.class.getResourceAsStream(fileName)) {
            if (is == null) {
                return null;
            }
            props.load(is);
            return props;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
