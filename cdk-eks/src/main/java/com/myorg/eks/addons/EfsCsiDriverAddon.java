package com.myorg.eks.addons;

import com.myorg.eks.config.ConfigInjector;
import com.myorg.eks.config.ConfigLoader;
import com.myorg.eks.config.ConfigProperty;
import com.myorg.eks.iam.OIDCTrustPolicyMapper;
import software.amazon.awscdk.services.eks.CfnAddon;
import software.amazon.awscdk.services.eks.CfnAddonProps;
import software.amazon.awscdk.services.eks.Cluster;
import software.amazon.awscdk.services.iam.*;
import software.constructs.Construct;

import java.util.List;

/**
 * EfsCsiDriverAddon class is responsible for deploying the EFS CSI Driver
 * as an EKS add-on and setting up the necessary IAM role with IRSA.
 */
public class EfsCsiDriverAddon extends Construct {

    @ConfigProperty("efs.csi.driver.version")
    private String efsCsiDriverVersion;

    public EfsCsiDriverAddon(final Construct scope, final String id, final Cluster cluster, final ConfigLoader configLoader) {
        super(scope, id);

        // Inject configuration properties into this instance
        ConfigInjector.injectConfigProperties(this, configLoader);

        // Trust Relationship policy for IRSA
        IOpenIdConnectProvider openIdConnectProvider = cluster.getOpenIdConnectProvider();
        String serviceAccountName = "efs-csi-controller-sa";

        FederatedPrincipal federatedPrincipal = new OIDCTrustPolicyMapper().createFederatedPrincipalWithSubject(
                openIdConnectProvider,
                "kube-system",
                serviceAccountName);

        // Create an IAM Role for the EFS CSI driver
        Role efsCsiDriverRole = Role.Builder.create(this, "EfsCsiDriverRole")
                .assumedBy(federatedPrincipal)
                .managedPolicies(List.of(
                        ManagedPolicy.fromAwsManagedPolicyName("service-role/AmazonEFSCSIDriverPolicy")
                ))
                .build();

        System.out.println("-- Install the EFS CSI Driver --");

        // Install the EFS CSI Driver as an EKS Add-on
        new CfnAddon(this, "efs-csi-driver-addon",CfnAddonProps.builder()
                .clusterName(cluster.getClusterName())
                .addonName("aws-efs-csi-driver")
                .addonVersion(efsCsiDriverVersion)
                .resolveConflicts("OVERWRITE")
                .serviceAccountRoleArn(efsCsiDriverRole.getRoleArn())
                .build());

    }
}
