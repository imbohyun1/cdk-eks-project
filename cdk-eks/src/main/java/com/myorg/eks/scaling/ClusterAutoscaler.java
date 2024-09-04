package com.myorg.eks.scaling;

import com.myorg.eks.config.ConfigInjector;
import com.myorg.eks.config.ConfigLoader;
import com.myorg.eks.config.ConfigProperty;
import com.myorg.eks.iam.OIDCTrustPolicyMapper;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.services.eks.Cluster;
import software.amazon.awscdk.services.eks.HelmChart;
import software.amazon.awscdk.services.iam.*;
import software.constructs.Construct;

import java.util.List;
import java.util.Map;

/**
 * ClusterAutoscaler class is responsible for deploying the Cluster Autoscaler
 * using Helm in a Kubernetes cluster.
 */
public class ClusterAutoscaler extends Construct {

    @ConfigProperty("scaling.clusterautoscaler.helm.version")
    private String clusterAutoscalerHelmVersion;

    @ConfigProperty("scaling.clusterautoscaler.helm.repo")
    private String clusterAutoscalerHelmRepo;

    @ConfigProperty("scaling.clusterautoscaler.helm.namespace")
    private String clusterAutoscalerNamespace;

    @ConfigProperty("scaling.clusterautoscaler.irsa")
    private boolean clusterAutoscalerIrsaEnabled;

    @ConfigProperty("scaling.clusterautoscaler.serviceaccount")
    private String clusterAutoscalerServiceAccountName;

    public ClusterAutoscaler(final Construct scope, final String id, final Cluster cluster, final ConfigLoader configLoader) {
        super(scope, id);

        System.out.println("-- Deploy Cluster Autoscaler using Helm --");

        // Inject configuration properties
        ConfigInjector.injectConfigProperties(this, configLoader);

        // Trust Relationship policy for IRSA
        IOpenIdConnectProvider openIdConnectProvider = cluster.getOpenIdConnectProvider();

        // Create a FederatedPrincipal for the service account
        FederatedPrincipal federatedPrincipal = new OIDCTrustPolicyMapper()
                .createFederatedPrincipalWithSubject(
                        openIdConnectProvider,
                        clusterAutoscalerNamespace,
                        clusterAutoscalerServiceAccountName);

        // Create an IAM role for the Cluster Autoscaler with the necessary managed policies
        IRole autoscalerRole = Role.Builder.create(this, "ClusterAutoscalerRole")
                .assumedBy(federatedPrincipal)
                .managedPolicies(List.of(
                        ManagedPolicy.fromAwsManagedPolicyName("AutoScalingFullAccess"),
                        ManagedPolicy.fromAwsManagedPolicyName("AmazonEKSClusterPolicy"),
                        ManagedPolicy.fromAwsManagedPolicyName("AmazonEKSWorkerNodePolicy")
                ))
                .build();

        // Deploy Cluster Autoscaler using Helm
        // Equivalent Helm CLI commands:
        // helm repo add autoscaler https://kubernetes.github.io/autoscaler
        // helm upgrade -i cluster-autoscaler cluster-autoscaler/cluster-autoscaler --namespace <namespace>

        HelmChart.Builder.create(this, "ClusterAutoscaler")
                .cluster(cluster)
                .chart("cluster-autoscaler")
                .repository(clusterAutoscalerHelmRepo)
                .namespace(clusterAutoscalerNamespace)
                .version(clusterAutoscalerHelmVersion)
                .release("cluster-autoscaler")
                .values(Map.of(
                        "autoDiscovery", Map.of(
                                "clusterName", cluster.getClusterName()
                        ),
                        "awsRegion", Stack.of(this).getRegion(),
                        "rbac", Map.of(
                                "serviceAccount", Map.of(
                                        "create", clusterAutoscalerIrsaEnabled,
                                        "name", clusterAutoscalerServiceAccountName,
                                        "annotations", Map.of(
                                                "eks.amazonaws.com/role-arn", autoscalerRole.getRoleArn()
                                        )
                                )
                        )
                ))
                .build();

    }
}
