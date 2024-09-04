package com.myorg.eks.observability;

import com.myorg.eks.iam.OIDCTrustPolicyMapper;
import software.amazon.awscdk.services.eks.CfnAddon;
import software.amazon.awscdk.services.eks.CfnAddonProps;
import software.amazon.awscdk.services.eks.Cluster;
import software.amazon.awscdk.services.iam.*;
import software.constructs.Construct;

import java.util.List;

/**
 * ContainerInsightsAddon class is responsible for deploying Container Insights
 * as an EKS add-on and setting up the necessary IAM role with IRSA.
 */
public class ContainerInsightsAddon extends Construct {

    public ContainerInsightsAddon(final Construct scope, final String id, final Cluster cluster) {
        super(scope, id);

        System.out.println("-- Install the Container Insights via Managed add-on --");

        // Trust Relationship policy for IRSA
        IOpenIdConnectProvider openIdConnectProvider = cluster.getOpenIdConnectProvider();

        // Create a FederatedPrincipal for multiple service accounts related to container insights
        FederatedPrincipal federatedPrincipal = new OIDCTrustPolicyMapper().createFederatedPrincipalForMultipleServiceAccounts(
                openIdConnectProvider,
                "container-insights");

        // Create an IAM Role for Container Insights with the necessary managed policy
        Role containerInsightsRole = Role.Builder.create(this, "ContainerInsightsRole")
                .assumedBy(federatedPrincipal)
                .managedPolicies(List.of(
                        ManagedPolicy.fromAwsManagedPolicyName("CloudWatchAgentServerPolicy")
                ))
                .build();

        // Install the Container Insights as an EKS Add-on
        new CfnAddon(this, "conatainer-insights-addon", CfnAddonProps.builder()
                .clusterName(cluster.getClusterName())
                .addonName("amazon-cloudwatch-observability")
                .addonVersion("v2.0.0-eksbuild.1")
                .resolveConflicts("OVERWRITE")
                .serviceAccountRoleArn(containerInsightsRole.getRoleArn())
                .build());

    }

}
