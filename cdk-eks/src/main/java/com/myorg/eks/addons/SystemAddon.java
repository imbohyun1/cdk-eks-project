package com.myorg.eks.addons;

import com.myorg.eks.config.ConfigInjector;
import com.myorg.eks.config.ConfigLoader;
import com.myorg.eks.config.ConfigProperty;
import software.amazon.awscdk.services.eks.CfnAddon;
import software.amazon.awscdk.services.eks.CfnAddonProps;
import software.amazon.awscdk.services.eks.Cluster;
import software.constructs.Construct;

/**
 * SystemAddon class is responsible for deploying managed add-ons
 * to an EKS cluster based on configuration properties.
 */
public class SystemAddon extends Construct {

    @ConfigProperty("eks.managed.addon.vpccni.enabled")
    private boolean vpccniAddonEnabled;

    @ConfigProperty("eks.managed.addon.vpccni.version")
    private String vpccniAddonVersion;

    @ConfigProperty("eks.managed.addon.kubeproxy.enabled")
    private boolean kubeProxyAddonEnabled;

    @ConfigProperty("eks.managed.addon.kubeproxy.version")
    private String kubeProxyAddonVersion;

    @ConfigProperty("eks.managed.addon.coredns.enabled")
    private boolean coreDnsAddonEnabled;

    @ConfigProperty("eks.managed.addon.coredns.version")
    private String coreDnsAddonVersion;

    public SystemAddon(final Construct scope, final String id, final Cluster cluster, final ConfigLoader configLoader) {
        super(scope, id);

        // Inject configuration properties into this instance
        ConfigInjector.injectConfigProperties(this, configLoader);

        System.out.println("-- Deploy Managed add-ons --");

        // Deploy VPC CNI add-on if enabled
        if (vpccniAddonEnabled) {
            deployAddon(cluster, "vpc-cni", vpccniAddonVersion);
        }

        // Deploy Kube Proxy add-on if enabled
        if (kubeProxyAddonEnabled) {
            deployAddon(cluster, "kube-proxy", kubeProxyAddonVersion);
        }

        // Deploy CoreDNS add-on if enabled
        if (coreDnsAddonEnabled) {
            deployAddon(cluster, "coredns", coreDnsAddonVersion);
        }

    }

    /**
     * Deploys an EKS managed add-on to the specified cluster.
     *
     * @param cluster       The EKS cluster.
     * @param addonName     The name of the add-on.
     * @param addonVersion  The version of the add-on.
     */
    private void deployAddon(Cluster cluster, String addonName, String addonVersion) {

        new CfnAddon(this, "eks-" + addonName + "-addon", CfnAddonProps.builder()
                .clusterName(cluster.getClusterName())
                .addonName(addonName)
                .addonVersion(addonVersion)
                .resolveConflicts("OVERWRITE") // If the addon already exists, overwrite it
                .build());
    }
}

