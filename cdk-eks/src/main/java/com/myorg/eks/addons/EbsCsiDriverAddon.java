package com.myorg.eks.addons;

import com.myorg.eks.config.ConfigInjector;
import com.myorg.eks.config.ConfigLoader;
import com.myorg.eks.config.ConfigProperty;
import com.myorg.eks.iam.OIDCTrustPolicyMapper;
import software.amazon.awscdk.services.eks.*;
import software.amazon.awscdk.services.iam.*;
import software.constructs.Construct;

import java.util.List;


/**
 * EbsCsiDriverAddon class is responsible for deploying the EBS CSI Driver
 * as an EKS add-on and setting up the necessary IAM role with IRSA.
 */
public class EbsCsiDriverAddon extends Construct {

    @ConfigProperty("ebs.csi.driver.version")
    private String ebsCsiDriverVersion;

    public EbsCsiDriverAddon(final Construct scope, final String id, final Cluster cluster, final ConfigLoader configLoader) {
        super(scope, id);

        // Inject configuration properties into this instance
        ConfigInjector.injectConfigProperties(this, configLoader);

        // Trust Relationship policy for IRSA
        IOpenIdConnectProvider openIdConnectProvider = cluster.getOpenIdConnectProvider();
        String serviceAccountName = "ebs-csi-controller-sa";

        // Create a FederatedPrincipal for the service account
        FederatedPrincipal federatedPrincipal = new OIDCTrustPolicyMapper().createFederatedPrincipalWithSubject(
                openIdConnectProvider,
                "kube-system",
                serviceAccountName);

        // Create an IAM Role for the EBS CSI driver with the necessary managed policy
        Role ebsCsiDriverRole = Role.Builder.create(this, "EbsCsiDriverRole")
                .assumedBy(federatedPrincipal)
                .managedPolicies(List.of(
                        ManagedPolicy.fromAwsManagedPolicyName("service-role/AmazonEBSCSIDriverPolicy")
                ))
                .build();


        System.out.println("-- Install the EBS CsI Driver --");

        // Install the EBS CSI Driver as an EKS Add-on
        new CfnAddon(this, "ebs-csi-driver-addon", CfnAddonProps.builder()
                .clusterName(cluster.getClusterName())
                .addonName("aws-ebs-csi-driver")
                .addonVersion(ebsCsiDriverVersion)
                .resolveConflicts("OVERWRITE")
                .serviceAccountRoleArn(ebsCsiDriverRole.getRoleArn())
                .build());

    }
}
