package com.myorg;

import com.myorg.eks.EksStack;
import com.myorg.eks.config.ConfigLoader;
import com.myorg.eks.config.ConfigProperty;
import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;

/**
 * CdkEksApp class is the entry point for deploying the EKS stack using AWS CDK.
 */
public class CdkEksApp {

    @ConfigProperty("aws.account.id")
    private static String accountId;

    @ConfigProperty("aws.region.id")
    private static String region;

    public static void main(final String[] args) {

        // Create a new CDK app
        App app = new App();

        // Create a new EKS stack with specified environment settings
        new EksStack(app, "CdkEksLabStack", StackProps.builder()
                .env(Environment.builder()
                        .account(System.getenv("CDK_DEFAULT_ACCOUNT"))
                        .region(System.getenv("CDK_DEFAULT_REGION"))
                        .build())
                .build());

        // Synthesize the app
        app.synth();
    }
}

