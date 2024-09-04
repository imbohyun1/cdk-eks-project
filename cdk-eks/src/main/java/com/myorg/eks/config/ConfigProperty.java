package com.myorg.eks.config;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * ConfigProperty is a custom annotation used to indicate that a field
 * should be injected with a configuration property value.
 *
 * This annotation is retained at runtime, allowing reflection-based
 * tools like ConfigInjector to access it and perform the injection.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigProperty {

    String value();
}
