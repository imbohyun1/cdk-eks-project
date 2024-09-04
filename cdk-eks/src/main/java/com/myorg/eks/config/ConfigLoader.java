package com.myorg.eks.config;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Properties;

/**
 * ConfigLoader class is responsible for loading configuration properties
 * from a properties file and providing methods to access these properties.
 */
public class ConfigLoader {

    private Properties properties;

    /**
     * Constructor that initializes the ConfigLoader by loading properties
     * from the "eks.properties" file located in the classpath.
     */
    public ConfigLoader() {

        properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("eks.properties")) {
            if (input == null) {
                throw new RuntimeException("Unable to find eks.properties");
            }
            properties.load(input);
            System.out.println("--- PROPERTIES ---");
            System.out.println(properties.toString()) ;

        } catch (IOException ex) {
            throw new RuntimeException("Error loading properties file", ex);
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public boolean getBooleanProperty(String key) {

        String value = properties.getProperty(key);
        if (value != null) {
            return "true".equalsIgnoreCase(value.trim());
        }
        throw new IllegalArgumentException("Property not found or not a boolean: " + key);
    }

    public int getIntegerProperty(String key) {

        String value = properties.getProperty(key);
        if (value != null) {
            try {
                return Integer.parseInt(value.trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Property value is not a valid integer: " + key, e);
            }
        }
        throw new IllegalArgumentException("Property not found or not an integer: " + key);
    }

}
