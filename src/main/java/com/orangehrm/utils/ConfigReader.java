package com.orangehrm.utils;

import com.orangehrm.exceptions.FrameworkException;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Loads config.properties once (eagerly) and exposes typed getters.
 *
 * Demonstrates: static init block, exception handling, encapsulation.
 *
 * CI/Jenkins support:
 *   System properties (-Dbrowser=firefox) take precedence over config.properties.
 *   This lets Jenkins override any value without modifying config files.
 *
 *   Example Maven invocations:
 *     mvn test                                    -> uses config.properties
 *     mvn test -Dbrowser=firefox                  -> overrides browser
 *     mvn test -Dheadless=true -Dbrowser=chrome   -> CI-style run
 */
public final class ConfigReader {

    private static final Properties PROPS = new Properties();

    static {
        try (FileInputStream fis = new FileInputStream("src/test/resources/config.properties")) {
            PROPS.load(fis);
        } catch (IOException e) {
            throw new FrameworkException("Failed to load config.properties", e);
        }
    }

    private ConfigReader() {}

    /**
     * Resolution order:
     *   1. System property (-Dkey=value from Maven CLI / Jenkins)
     *   2. config.properties
     *   3. throw FrameworkException
     */
    public static String get(String key) {
        String sysProp = System.getProperty(key);
        if (sysProp != null && !sysProp.isBlank()) {
            return sysProp;
        }
        String value = PROPS.getProperty(key);
        if (value == null) throw new FrameworkException("Missing key in config: " + key);
        return value;
    }

    public static int getInt(String key) {
        try {
            return Integer.parseInt(get(key));
        } catch (NumberFormatException e) {
            throw new FrameworkException("Property " + key + " is not an int", e);
        }
    }

    public static boolean getBoolean(String key) {
        return Boolean.parseBoolean(get(key));
    }
}
