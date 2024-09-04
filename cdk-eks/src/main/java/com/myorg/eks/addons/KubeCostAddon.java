package com.myorg.eks.addons;

import com.myorg.eks.config.ConfigProperty;
import software.amazon.awscdk.services.eks.CfnAddon;
import software.amazon.awscdk.services.eks.CfnAddonProps;
import software.amazon.awscdk.services.eks.Cluster;
import software.constructs.Construct;

/**
 * KubeCostAddon class is responsible for deploying Kube Cost
 * as an EKS add-on and setting up the necessary IAM role with IRSA.
 */
public class KubeCostAddon extends Construct {

    @ConfigProperty("eks.kubecost.version")
    private String kubeCostVersion;

    public KubeCostAddon(final Construct scope, final String id, final Cluster cluster) {
        super(scope, id);

        System.out.println("-- Install the KubeCost --");

        // Install the EFS CSI Driver as an EKS Add-on
        new CfnAddon(this, "kubecost-addon", CfnAddonProps.builder()
                .clusterName(cluster.getClusterName())
                .addonName("kubecost_kubecost")
                .addonVersion(kubeCostVersion)
                .resolveConflicts("OVERWRITE")
                .build());

    }
}
