package com.myorg.eks;

import java.util.ArrayList;
import java.util.List;

import com.myorg.eks.addons.EbsCsiDriverAddon;
import com.myorg.eks.addons.EfsCsiDriverAddon;
import com.myorg.eks.addons.KubeCostAddon;
import com.myorg.eks.addons.SystemAddon;
import com.myorg.eks.config.ConfigLoader;
import com.myorg.eks.config.ConfigProperty;
import com.myorg.eks.config.ConfigInjector;
import com.myorg.eks.observability.*;
import com.myorg.eks.observability.alarm.CloudWatchAlarm;
import com.myorg.eks.scaling.Karpenter;
import com.myorg.eks.scaling.ClusterAutoscaler;
import software.amazon.awscdk.services.eks.*;
import software.constructs.Construct;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.eks.Cluster;
import software.amazon.awscdk.services.eks.KubernetesVersion;
import software.amazon.awscdk.services.eks.ClusterProps;
import software.amazon.awscdk.lambdalayer.kubectl.KubectlLayer;

/**
 * EksStack class is responsible for creating an EKS cluster and managing its configuration,
 * including add-ons and node groups.
 */
public class EksStack extends Stack {

    @ConfigProperty("eks.cluster.name")
    private String clusterName;

    @ConfigProperty("eks.kubernetes.version")
    private String clusterVersion;

    @ConfigProperty("eks.managed.addon.enabled")
    private boolean managedAddonEnabled;

    @ConfigProperty("eks.controlplane.logging.enabled")
    private boolean eksControlPlaneLoggingEnabled;

    @ConfigProperty("eks.controlplane.logging.apiserver")
    private boolean eksControlPlaneLoggingApiserver;

    @ConfigProperty("eks.controlplane.logging.audit")
    private boolean eksControlPlaneLoggingAudit;

    @ConfigProperty("eks.controlplane.logging.authenticator")
    private boolean eksControlPlaneLoggingAuthenticator;

    @ConfigProperty("eks.controlplane.logging.kcm")
    private boolean eksControlPlaneLoggingKcm;

    @ConfigProperty("eks.controlplane.logging.scheduler")
    private boolean eksControlPlaneLoggingScheduler;

    @ConfigProperty("eks.container.insights.enabled")
    private boolean containerInsightsEnabled;

    @ConfigProperty("ebs.csi.driver.enabled")
    private boolean ebsCsiDriverEnabled;

    @ConfigProperty("efs.csi.driver.enabled")
    private boolean efsCsiDriverEnabled;

    @ConfigProperty("scaling.karpenter.enabled")
    private boolean karpenterEnabled;

    @ConfigProperty("scaling.clusterautoscaler.enabled")
    private boolean clusterAutoScalerEnabled;

    @ConfigProperty("kubernetes.kubeopsview.enabled")
    private boolean kubeOpsViewEnabled;

    @ConfigProperty("eks.kubecost.enabled")
    private boolean kubeCostEnabled;

    @ConfigProperty("kubernetes.prometheus.enabled")
    private boolean prometheusEnabled;

    @ConfigProperty("kubernetes.grafana.enabled")
    private boolean grafanaEnabled;

    @ConfigProperty("aws.loadbalancer.controller.enabled")
    private boolean awsLoadBalancerControllerEnabled;

    @ConfigProperty("kubernetes.aws.xray.enabled")
    private boolean awsXRayEnabled;


    public EksStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public EksStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        // Load properties
        ConfigLoader configLoader = new ConfigLoader();
        ConfigInjector.injectConfigProperties(this, configLoader);

        // Cluster setting
        ClusterProps.Builder builder = ClusterProps.builder();
        builder.version(KubernetesVersion.V1_29);
        builder.clusterName(clusterName);
        builder.kubectlLayer(new KubectlLayer(this, "KubectlLayer"));
        builder.authenticationMode(AuthenticationMode.API_AND_CONFIG_MAP);

        if (awsLoadBalancerControllerEnabled) {
            builder.albController(AlbControllerOptions.builder()
                    .version(AlbControllerVersion.V2_6_2)
                    .build());
        }

        builder.defaultCapacity(0);

        if (eksControlPlaneLoggingEnabled) {

            List<ClusterLoggingTypes> clusterLoggingTypes = this.configureClusterLogging();
            if(!clusterLoggingTypes.isEmpty())
                builder.clusterLogging(clusterLoggingTypes);
        }

        builder.outputClusterName(true);
        builder.outputConfigCommand(true);
        builder.outputMastersRoleArn(true);

        ClusterProps clusterProps = builder.build();

        // Create an EKS cluster
        Cluster cluster = new Cluster(this, "CdkEksCluster", clusterProps);

        // Install Add-ons for cluster
        this.installAddOns(cluster, configLoader);

        // AmazonEKSAdminPolicy with `cluster` scope
        AccessPolicy.fromAccessPolicyName("AmazonEKSAdminPolicy", AccessPolicyNameOptions.builder()
                .accessScopeType(AccessScopeType.CLUSTER)
                .build());


        // Create a managed nodegroup
        new EksManagedNodeGroupStack(this, "EKSManagedNodeGroup", cluster, configLoader);

    }

    /**
     * Reads control plane logging configuration and returns the list of ClusterLoggingTypes.
     *
     * @return List of ClusterLoggingTypes based on configuration.
     */
    private List<ClusterLoggingTypes> configureClusterLogging() {

        List<ClusterLoggingTypes> clusterLoggingTypes = new ArrayList<>();

        if (eksControlPlaneLoggingApiserver) {
            clusterLoggingTypes.add(ClusterLoggingTypes.API);
        }

        if (eksControlPlaneLoggingAudit) {
            clusterLoggingTypes.add(ClusterLoggingTypes.AUDIT);
        }

        if (eksControlPlaneLoggingKcm) {
            clusterLoggingTypes.add(ClusterLoggingTypes.CONTROLLER_MANAGER);
        }

        if (eksControlPlaneLoggingAuthenticator) {
            clusterLoggingTypes.add(ClusterLoggingTypes.AUTHENTICATOR);
        }

        if (eksControlPlaneLoggingScheduler) {
            clusterLoggingTypes.add(ClusterLoggingTypes.SCHEDULER);
        }

        return clusterLoggingTypes;
    }


    /**
     * Installs add-ons in the EKS cluster based on configuration.
     *
     * @param cluster       The EKS cluster.
     * @param configLoader  The configuration loader.
     */
    private void installAddOns(Cluster cluster, ConfigLoader configLoader) {

        new KubernetesMetricsServer(this, "KubernetesMetricsServer", cluster);

        if (containerInsightsEnabled) {
            new ContainerInsightsAddon(this, "ContainerInsights", cluster);
            new CloudWatchAlarm(this, "CloudWatchAlarm", cluster);
        }

        if (managedAddonEnabled) {
            new SystemAddon(this, "SystemAddon", cluster, configLoader);
        }

        if (ebsCsiDriverEnabled) {
            new EbsCsiDriverAddon(this, "EbsCsiDriver", cluster, configLoader);
        }

        if (efsCsiDriverEnabled) {
            new EfsCsiDriverAddon(this, "EfsCsiDriver", cluster, configLoader);
        }

        if (karpenterEnabled) {
            new Karpenter(this, "Karpenter", cluster);
        }

        if (clusterAutoScalerEnabled) {
            new ClusterAutoscaler(this, "ClusterAutoscaler", cluster, configLoader);
        }

        if (kubeOpsViewEnabled) {
            new KubeOpsView(this, "KubeOpsView", cluster, configLoader);
        }

        if (kubeCostEnabled) {
            new KubeCostAddon(this, "KubeCost", cluster);
        }

        if (prometheusEnabled) {
            new Prometheus(this, "Prometheus", cluster, configLoader);
        }

        if (grafanaEnabled) {
            new Grafana(this, "Grafana", cluster, configLoader);
        }

        if (awsXRayEnabled) {
            new XRayDaemon(this, "XRayDaemon", cluster, configLoader);
        }
        // Add more add-ons as needed
    }

}


