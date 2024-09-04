package com.myorg.eks.config;

import java.lang.reflect.Field;

/**
 * ConfigInjector class is responsible for injecting configuration properties
 * into fields of an object that are annotated with @ConfigProperty.
 */
public class ConfigInjector {

    /**
     * Injects configuration properties into the fields of the target object.
     * Fields must be annotated with @ConfigProperty to be eligible for injection.
     *
     * @param target       The object whose fields are to be injected with configuration values.
     * @param configLoader The ConfigLoader instance used to retrieve configuration values.
     */
    public static void injectConfigProperties(Object target, ConfigLoader configLoader) {

        for (Field field : target.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(ConfigProperty.class)) {
                ConfigProperty annotation = field.getAnnotation(ConfigProperty.class);
                try {
                    field.setAccessible(true);

                    if (field.getType().equals(String.class)) {

                        String value = configLoader.getProperty(annotation.value());
                        field.set(target, value);

                    } else if (field.getType().equals(boolean.class) || field.getType().equals(Boolean.class)) {

                        boolean value = configLoader.getBooleanProperty(annotation.value());
                        field.set(target, value);

                    } else if (field.getType().equals(int.class) || field.getType().equals(Integer.class)) {

                        int value = configLoader.getIntegerProperty(annotation.value());
                        field.set(target, value);

                    } else {
                        throw new IllegalArgumentException("Unsupported field type: " + field.getType());
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Error injecting config property", e);
                }
            }
        }
    }
}
