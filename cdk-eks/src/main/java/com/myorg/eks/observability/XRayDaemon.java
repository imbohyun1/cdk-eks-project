package com.myorg.eks.observability;

import com.myorg.eks.config.ConfigInjector;
import com.myorg.eks.config.ConfigLoader;
import com.myorg.eks.config.ConfigProperty;
import com.myorg.eks.iam.OIDCTrustPolicyMapper;
import software.amazon.awscdk.services.eks.Cluster;
import software.amazon.awscdk.services.eks.HelmChart;
import software.amazon.awscdk.services.iam.FederatedPrincipal;
import software.amazon.awscdk.services.iam.IOpenIdConnectProvider;
import software.amazon.awscdk.services.iam.ManagedPolicy;
import software.amazon.awscdk.services.iam.Role;
import software.constructs.Construct;

import java.util.List;
import java.util.Map;

public class XRayDaemon extends Construct {


    @ConfigProperty("kubernetes.aws.xray.helm.repo")
    private String xrayHelmRepo;

    @ConfigProperty("kubernetes.aws.xray.helm.namespace")
    private String xrayHelmNamespace;


    public XRayDaemon(final Construct scope, final String id, final Cluster cluster, final ConfigLoader configLoader) {
        super(scope, id);

        // Trust Relationship policy for IRSA
        IOpenIdConnectProvider openIdConnectProvider = cluster.getOpenIdConnectProvider();
        String serviceAccountName = "xray-daemon-sa";

        // Create a FederatedPrincipal for the service account
        FederatedPrincipal federatedPrincipal = new OIDCTrustPolicyMapper().createFederatedPrincipalWithSubject(
                openIdConnectProvider,
                xrayHelmNamespace,
                serviceAccountName);

        // Create an IAM Role for the EBS CSI driver with the necessary managed policy
        Role xRayDaemonRole = Role.Builder.create(this, "XRayDaemonRole")
                .assumedBy(federatedPrincipal)
                .managedPolicies(List.of(
                        ManagedPolicy.fromAwsManagedPolicyName("service-role/AWSXRayDaemonWriteAccess")
                ))
                .build();

        System.out.println("-- Deploy Xray daemon using Helm --");

        // Inject configuration properties
        ConfigInjector.injectConfigProperties(this, configLoader);

        // Deploy Xray daemon using Helm
        //
        // helm repo add okgolove https://okgolove.github.io/helm-charts/
        // helm repo update
        // helm install aws-xray okgolove/aws-xray
        //    --namespace xray
        //
        // https://kubeapps.dev/

        HelmChart.Builder.create(this, "XRayDaemon")
                .cluster(cluster)
                .chart("aws-xray")
                .repository(xrayHelmRepo)
                .namespace(xrayHelmNamespace)
                .release("xray-daemon")
                .values(Map.of(
                        "serviceAccount", Map.of(
                                "create", true,
                                "name", serviceAccountName
                        )
                ))
                .build();
    }
}
